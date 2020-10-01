package com.taiyiyun.passport.service.group.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.commons.GroupPhotoDrawer;
import com.taiyiyun.passport.commons.PassPortUtil;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicUserBlockDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.group.*;
import com.taiyiyun.passport.exception.PassportException;
import com.taiyiyun.passport.exception.group.GroupException;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserFollower;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.po.asset.TransferAnswer;
import com.taiyiyun.passport.po.follower.UserFollower;
import com.taiyiyun.passport.po.group.*;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.service.ITransferService;
import com.taiyiyun.passport.service.group.IGroupService;
import com.taiyiyun.passport.service.transfer.AssetCache;
import com.taiyiyun.passport.sqlserver.dao.IUsersDao;
import com.taiyiyun.passport.transfer.Answer.CoinInfo;
import com.taiyiyun.passport.util.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by nina on 2017/11/14.
 */
@Service
public class GroupServiceImpl implements IGroupService {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String LOCKADDGROUPUSER = "lock.addGroupUser";
    private static final String LOCKADDGROUPUSERBYSCANCODE = "lock.addGroupUserByScanCode";
    private static final String LOCK_APPROVE_GROUP_USER = "lock.addGroupUserByScanCode";
    private static final String LOCK_INTO_GROUP_PRIZE = "lock.intoGroupPrize";
    @Resource
    private IGroupDao groupDao;
    @Resource
    private IUsersDao usersDao;
    @Resource
    private IPublicUserService userService;
    @Resource
    private IGroupMemberDao gmDao;
    @Resource
    private IPublicUserDao publicUserDao;
    @Resource
    private IGroupThirdDao groupThirdDao;
    @Resource
    private IGroupThirdGroupDao groupThirdGroupDao;
    @Resource
    private IGroupPromoteDetailDao groupPromoteDetailDao;
    @Resource
    private IGroupPromoteConfigDao groupPromoteConfigDao;
    @Resource
    private ITransferService transferService;
    @Resource
    private IRedisService redisService;
    @Resource
    private IPublicUserBlockDao blockDao;


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<Message<JSONObject>> addGroup(PackBundle packBundle,String ownerId,String groupName,Integer isPromote, List<String> userIdList, UserDetails userDetails) {
        if(StringUtils.isNotEmpty(ownerId) && userIdList != null && !userIdList.isEmpty()) {
            Date now = new Date();
            //1.创建群
            Group group = new Group();
            String groupId = StringUtil.getUUID();
            group.setGroupId(groupId);
            group.setGroupState(0);
            group.setGroupType(0);
            group.setInviteType(1);
            group.setModifyRight(0);
            group.setOwnerId(ownerId);
            group.setIsPromote(isPromote == null?0:isPromote);
            group.setNeedAuth(0);
            group.setGroupName(groupName == null?"群聊":groupName);
            group.setCreateTime(now);
            group.setUpdateTime(now.getTime());
            //2.创建群成员
            if(!userIdList.contains(ownerId)) {
                userIdList.add(ownerId);
            }
            //进群原因
            String invitedReason = "";
            if (isPromote == 1){
                //设置群成员不允许修改群名称
                group.setModifyRightDefault(0);
                //设置群组类型为私密群 值为：1
                group.setGroupType(1);
                //设置不允许群成员邀请，值为：0
                group.setInviteType(0);
                invitedReason = "注册自动入群";
            }
            groupDao.insertSelective(group);
            List<PublicUser> publicUserList = createGroupMember(group, invitedReason, userDetails.getUserId(), 0, userIdList, now);
            //3.创建群名称
            int index = 0;
            String groupNameDefault = null;
            for (PublicUser publicUser:publicUserList){
                if (index == 3){
                    break;
                }
                if (groupNameDefault == null){
                    groupNameDefault = publicUser.getUserName();
                }
                else{
                    groupNameDefault += "、" + publicUser.getUserName();
                }
                index++;
            }
            if (StringUtil.isEmpty(groupName)){
                groupName = groupNameDefault;
            }
            group.setGroupName(groupName);
            //如果群为推广群的时候，不做3个人的限制
            if (isPromote == 0) {
                if (publicUserList.size() < 3) {
                    throw new GroupException("group.creategroup.notmorethan3", "群成员数量不能小于3人！", null);
                }
                //4.创建群头像
                makeGroupHeader(group, publicUserList);
                //返回给前台的群头像加上服务器地址
                group.setGroupHeader(Misc.getServerUri(null, group.getGroupHeader()));
            }
            else{
                //推广群设置群默认群组的头像
                String dheader =  Config.get("group.default.header.img","files/groupheaders/default/header.png");
                group.setGroupHeader(dheader);
                groupDao.updateByPrimaryKeySelective(group);
            }
            //5.建群通知消息
            Message<JSONObject> message = GroupMessageProcesser.createGroupMessagePublish(group, publicUserList);
            // 推送更改群信息消息
            List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
            GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupUserIdList);
            //6.将群成员id放入缓存中
            String userIdJson = JSONObject.toJSONString(userIdList);
            redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
            //调用进群奖励
            try {
                //如果包含ownerId，清除掉
                if(userIdList.contains(ownerId)) {
                    userIdList.remove(ownerId);
                }
                //如果客户端id为空走进群奖励，默认原始护照流程
                //否则，其他类型的护照不走进群奖励业务
                String clientId = userDetails.getClientId();
                if (StringUtil.isEmpty(clientId)){
                    intoGroupPrize(packBundle, group, userIdList,userDetails);
                }
            }
            catch (Exception e){
                logger.info("进群奖励异常：" + e.getMessage());
            }
            return Arrays.asList(message);
        } else {
        	throw new RuntimeException("群主ID和初始群成员ID不能为空");
        }
    }

    /**
     * 进群奖励币
     * @param packBundle
     * @param group
     * @param userIdList
     * @param userDetails
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void intoGroupPrize(PackBundle packBundle,Group group,List<String> userIdList,UserDetails userDetails){
        String groupId = group.getGroupId();
        try {
            DistributedRedisLock.acquire(LOCK_INTO_GROUP_PRIZE + groupId.concat(userDetails.getUserId()));
            logger.info("begin into group prize,group info:" + JSON.toJSONString(group));
            //是否为推广群 0:否  1：是
            Integer isPromote =  group.getIsPromote();
            //只有推广群执行以下业务逻辑
            if (isPromote.intValue() == EnumStatus.ZORO.getIndex()) {
                logger.info("this group not prize，group info：" + JSON.toJSONString(group));
                return;
            }
            try {
                //查询群推广配置信息,目前只有一条通用的配置数据，以后可以指定用户配置
                GroupPromoteConfig groupPromoteConfig = new GroupPromoteConfig();
                List<GroupPromoteConfig> groupPromoteConfigs = groupPromoteConfigDao.list(groupPromoteConfig);
                if (groupPromoteConfigs == null || groupPromoteConfigs.size() <= 0) {
                    logger.info("get group promote config info is null");
                    return;
                }
                groupPromoteConfig = groupPromoteConfigs.get(0);
                logger.info("get group promote config info:" + JSON.toJSONString(groupPromoteConfig));
                for (String userId : userIdList) {
                    Date createTime = groupPromoteConfig.getCreateTime();
                    if (createTime == null) {
                        createTime = DateUtil.fromString("2018-02-06 16:49:33");
                    }
                    long createTimes = createTime.getTime();
                    PublicUser targetUser = userService.getByUserId(userId);
                    if (targetUser == null) {
                        logger.info("prize user is null，owner id is:" + userId);
                        continue;
                    }
                    long registerTimes = targetUser.getCreateTime().getTime();
                    if (createTimes > registerTimes) {
                        logger.info("用户注册时间早于活动开始时间，不执行注册奖励（" + userId + "）:" + targetUser.getCreateTime());
                        continue;
                    }
                    //判断该用户是否进入过该群，如果已经进入过就不在给奖励
                    GroupPromoteDetail groupPromoteDetai = new GroupPromoteDetail();
                    groupPromoteDetai.setOwnerId(group.getOwnerId());
                    groupPromoteDetai.setTargetUserId(userId);
                    List<GroupPromoteDetail> list = groupPromoteDetailDao.list(groupPromoteDetai);
                    if (list != null && list.size() > 0) {
                        logger.info("用户已经在奖励列表中，不能继续奖励:" + userId);
                        continue;
                    }
                    //记录进群奖励明细数据
                    GroupPromoteDetail groupPromoteDetail = new GroupPromoteDetail();
                    groupPromoteDetail.setAmount(groupPromoteConfig.getAmount());
                    groupPromoteDetail.setCoinId(groupPromoteConfig.getCoinId());
                    groupPromoteDetail.setCharge(BigDecimal.ZERO);
                    groupPromoteDetail.setCreateTime(new Date());
                    groupPromoteDetail.setOwnerId(group.getOwnerId());
                    groupPromoteDetail.setGroupId(group.getGroupId());
                    groupPromoteDetail.setTargetUserId(userId);
                    groupPromoteDetail.setTransferStatus(EnumStatus.ZORO.getIndex());
                    logger.info("insert groupPromoteDetail:" + JSON.toJSONString(groupPromoteDetail));
                    groupPromoteDetailDao.insert(groupPromoteDetail);
                    //发起冻结转账
                    String ownerId = group.getOwnerId();
                    PublicUser publicUser = userService.getByUserId(ownerId);
                    if (publicUser == null) {
                        logger.info("prize user is null，owner id is:" + ownerId);
                        continue;
                    }
                    //获取币种信息
                    String platformId = groupPromoteConfig.getPlatformId();
                    String coinId = groupPromoteConfig.getCoinId();
                    String coinName = null;
                    CoinInfo coin = AssetCache.getInstance().getCoin(platformId, coinId);
                    if (coin != null) {
                        coinName = coin.getName();
                    }
                    logger.info("get coin info:" + JSON.toJSONString(coin));
                    //设置转账冻结参数，并发起冻结
                    String text = packBundle.getString("group.into.prize.remark");
                    if (StringUtil.isEmpty(text)) {
                        text = "入群奖励";
                    }
                    TransferAnswer apply = new TransferAnswer();
                    apply.setPlatformId(groupPromoteConfig.getPlatformId());
                    apply.setCoinId(groupPromoteConfig.getCoinId());
                    apply.setCoinName(coinName == null ? "coinId" : coinName);
                    apply.setAmount(groupPromoteConfig.getAmount());
                    apply.setToUserId(userId);
                    apply.setToUuid(targetUser.getUuid());
                    apply.setFromUserId(publicUser.getId());
                    apply.setFromUuid(publicUser.getUuid());
                    apply.setText(text);
                    apply.setCreateTime((new Date()).getTime());
                    Long expireTime = new Date().getTime() + 24 * 60 * 60 * 1000;
                    if (apply.getExpireTime() == 0) {
                        apply.setExpireTime(expireTime);
                    }
                    logger.info("begin transfer freeze:" + JSON.toJSONString(apply));
                    //处理冻结返回结果
                    String result = transferService.transferFrozen(packBundle, apply);
                    if (result == null) {
                        logger.info("transfer freeze result is null");
                        continue;
                    }
                    logger.info("transfer freeze result is success:" + JSON.toJSONString(result));
                    BaseResult<List<JSONObject>> baseResult = JSON.parseObject(result, new TypeReference<BaseResult<List<JSONObject>>>() {
                    });
                    //返回结果成功后，设置奖励明细为冻结转账成功
                    if (baseResult != null && baseResult.getStatus().intValue() == EnumStatus.ZORO.getIndex()) {
                        List<JSONObject> listMsg = baseResult.getData();
                        Trade trade = null;
                        if (listMsg != null && listMsg.size() > 0) {
                            try {
                                JSONObject jsonbject = listMsg.get(0);
                                Message<JSONObject> message = JSON.toJavaObject(jsonbject, Message.class);
                                JSONObject content = message.getContent();
                                trade = JSON.toJavaObject(content, Trade.class);
                            } catch (Exception ee) {
                                logger.info("transfer freeze result parse error:" + ee.getMessage());
                            }
                        }
                        Long tradId = apply.getTradeId();
                        BigDecimal fee = BigDecimal.ZERO;
                        Long frozenId = 0L;
                        if (trade != null) {
                            fee = trade.getFee();
                            frozenId = trade.getFrozenId();
                        }
                        Long detailId = groupPromoteDetail.getId();
                        GroupPromoteDetail groupPromoteDetailParam = new GroupPromoteDetail();
                        groupPromoteDetailParam.setId(detailId);
                        groupPromoteDetailParam.setTransferStatus(EnumStatus.ONE.getIndex());
                        groupPromoteDetailParam.setTransferTime(new Date());
                        groupPromoteDetailParam.setTradeId(tradId);
                        groupPromoteDetailParam.setCharge(fee);
                        groupPromoteDetailParam.setFrozenId(frozenId);
                        groupPromoteDetailParam.setExpireTime(expireTime);
                        int rval = groupPromoteDetailDao.updateById(groupPromoteDetailParam);
                        if (rval > 0) {
                            logger.info("update groupPromoteDetail success:" + JSON.toJSONString(groupPromoteDetail));
                        } else {
                            logger.info("update groupPromoteDetail fail:" + JSON.toJSONString(groupPromoteDetail));
                        }
                    }
                }
            }
            catch (Exception e){
                logger.info("into group prize error:" + e.getMessage());
            }
        } finally {
            DistributedRedisLock.release(LOCK_INTO_GROUP_PRIZE + groupId.concat(userDetails.getUserId()));
        }
    }

    @Override
    public List<Message<JSONObject>> addGroupUser(PackBundle packBundle, AddGroupUserParam addGroupUserParam, UserDetails userDetails) {
        String groupId = addGroupUserParam.getGroupId();
        try {
            DistributedRedisLock.acquire(LOCKADDGROUPUSER + groupId.concat(userDetails.getUserId()));
            final Group group = groupDao.selectByPrimarykey(groupId);
            if(group == null) {//说明群已被解散
                throw new GroupException("group.info.not.exist", "该群不存在或已经解散", null);
            }
            //群邀请类型为：0-不允许群成员邀请1-允许群成员邀请
            //群成员，当群设置成不允许群成员邀请的时候，不允许群成员邀请但是允许扫码和搜索入群
            if(group.getInviteType() == 0 && !StringUtils.equalsIgnoreCase(group.getOwnerId(), addGroupUserParam.getInviterId())) {
                if(addGroupUserParam.getJoinType() == 1) {
                    throw new GroupException("group.addgroupuser.notallowmemberinvite", "群主已设置不允许群成员邀请好友入群!", null);
                }
            }
            String inviterId = addGroupUserParam.getInviterId();
            GroupMember gmInviter = null;
            if(addGroupUserParam.getJoinType() != 3) {//只有当不是搜索入群的时候，才做这个校验
                if(StringUtils.isNotEmpty(inviterId)) {
                    Map<String, String> params = new HashMap<>();
                    params.put("groupId", groupId);
                    params.put("userId", inviterId);
                    gmInviter = gmDao.selectGroupMemberByGroupIdAndUserId(params);
                    if(gmInviter == null) {
                        if(addGroupUserParam.getJoinType() == 2) {
                            throw new GroupException("group.addgroupuser.inviterleave", "该二维码分享者已离开群聊，无法加入", null);
                        } else {
                            throw new RuntimeException("邀请人必须是群成员！");
                        }
                    }
                }
            }
            List<String> userIdList = addGroupUserParam.getUserIdList();
            if(StringUtils.isNotEmpty(groupId) && userIdList != null && !userIdList.isEmpty()) {
                Date now = new Date();
                List<PublicUser> publicUserList = null;
                publicUserList = createGroupMember(group, addGroupUserParam.getInviteReason(), addGroupUserParam.getInviterId(), addGroupUserParam.getJoinType(), userIdList, now);
                if(publicUserList == null || publicUserList.isEmpty()) {
                    //throw new GroupException("group.addgroupuser.cannotaddagain", "这些用户已经入群，不能再次添加！", null);
                    return null;
                }
                //更新群变更时间
                group.setUpdateTime(now.getTime());
                groupDao.updateByPrimaryKeySelective(group);
                if (1 == group.getNeedAuth() && !group.getOwnerId().equals(inviterId)) { // 需要群主审批，且邀请者不是群主。推送入群申请审批消息
                    Message<JSONObject> message = GroupMessageProcesser.groupUserJoinRequestMessagePublish(gmInviter, addGroupUserParam, group, publicUserList);
                    return Arrays.asList(message);
                } else { // 推送入群通知消息
                    List<String> groupCurrentUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
                    Message<JSONObject> message = GroupMessageProcesser.groupUserJoinResponseMessagePublish(addGroupUserParam.getJoinType(), gmInviter != null ? gmInviter.getUserId() : null,
                            gmInviter != null ? gmInviter.getNikeName() : null, addGroupUserParam.getInviteReason(), group, publicUserList, groupCurrentUserIdList);
                    redisService.evict(Const.GROUP_USERIDS + groupId);
                    String userIdJson = JSONObject.toJSONString(groupCurrentUserIdList);
                    redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
                    // 推送更改群信息消息
                    GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupCurrentUserIdList);
                    //调用进群奖励
                    try {
                        intoGroupPrize(packBundle, group, userIdList,userDetails);
                    }
                    catch (Exception e){
                        logger.info("进群奖励异常：" + e.getMessage());
                    }
                    return Arrays.asList(message);
                }
            } else {
                throw new RuntimeException("入群用户不能为空");
            }
        } finally {
            DistributedRedisLock.release(LOCKADDGROUPUSER + groupId.concat(userDetails.getUserId()));
        }
    }

    @Override
    public List<Message<JSONObject>> addGroupUserByScanCode(PackBundle packBundle,String thirdKey, UserDetails userDetails) {
        String userId = userDetails.getUserId();
        try {
            DistributedRedisLock.acquire(LOCKADDGROUPUSERBYSCANCODE + userId.concat(thirdKey));
            GroupThird groupThird = groupThirdDao.selectGroupThirdByThirdKey(thirdKey);
            if(groupThird == null) {
                throw new RuntimeException("没有获取到扫码入群的第三方信息");
            }
            List<GroupThirdGroup> groupThirdGroups = groupThirdGroupDao.selectGroupThirdGroupsByGtId(groupThird.getGtId());
            if(groupThirdGroups == null || groupThirdGroups.isEmpty()) {
                throw new RuntimeException("扫码入群第三方，没有提前建好聊天群");
            }
            for(GroupThirdGroup gtg : groupThirdGroups) {
                String groupId = gtg.getGroupId();
                int counts = gmDao.selectGroupMemberCounts(groupId);
                int maxNum = Config.getInt("group.max.num", 2000);
                if(counts >= maxNum) {
                    continue;
                } else {
                    Group group = groupDao.selectByPrimarykey(groupId);
                    Map<String, String> params = new HashMap<>();
                    params.put("groupId", groupId);
                    params.put("userId", userId);
                    GroupMember gm = gmDao.selectGroupMemberByGroupIdAndUserId(params);
                    if(gm != null) {
                        return null;
                    }
                    List<String> userIdList = new ArrayList<>();
                    userIdList.add(userId);
                    List<PublicUser> publicUserList = createGroupMember(group, "扫码入群", group.getOwnerId(), 2, userIdList, new Date());
                    GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
                    List<String> currentUserIdList = gmDao.selectGroupMemberUserIdByGroupId(groupId);
                    Message<JSONObject> message = GroupMessageProcesser.groupUserJoinResponseMessagePublish(2, group.getOwnerId(),
                            groupMember.getNikeName(), "扫码入群", group, publicUserList, currentUserIdList);
                    redisService.evict(Const.GROUP_USERIDS + groupId);
                    String userIdJson = JSONObject.toJSONString(currentUserIdList);
                    redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
                    //调用进群奖励
                    try {
                        intoGroupPrize(packBundle, group, userIdList,userDetails);
                    }
                    catch (Exception e){
                        logger.info("进群奖励异常：" + e.getMessage());
                    }
                    return Arrays.asList(message);
                }
            }
        } finally {
            DistributedRedisLock.release(LOCKADDGROUPUSERBYSCANCODE + userId.concat(thirdKey));
        }
        return null;
    }



    @Override
    public List<String> queryAllGroupThirdKeys() {
        return groupThirdDao.selectAllThirdKey();
    }

    @Override
    public List<Message<JSONObject>> approveGroupUser(PackBundle packBundle,ApproveGroupUserParam approveGroupUserParam,UserDetails userDetails) {
        String groupId = approveGroupUserParam.getGroupId();
        try {
            DistributedRedisLock.acquire(LOCK_APPROVE_GROUP_USER + groupId.concat(userDetails.getUserId()));
            if(approveGroupUserParam != null) {
                final Group group = groupDao.selectByPrimarykey(groupId);
                if(group == null) {//说明群已被解散
                    throw new RuntimeException("群已解散！");
                }
                List<String> userIdList = approveGroupUserParam.getUserIdList();
                //入群奖励用户列表
                List<String> prizeUserIdList = new ArrayList<>();
                List<PublicUser> publicUserList = new ArrayList<>();
                GroupMember gmFirst = null;
                GroupMember gmInviter = null;
                if(StringUtils.isNotEmpty(groupId) && userIdList != null && !userIdList.isEmpty()) {
                    for(String userId : userIdList) {
                        Map<String, String> params = new HashMap<> ();
                        params.put("groupId", groupId);
                        params.put("userId", userId);
                        GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
                        if(groupMember == null || groupMember.getJoinState() == 1) {
                            continue;
                        }
                        gmDao.authGroupMemberByGroupIdAndUserId(params);
                        if (null == gmFirst || null == gmInviter) {
                            gmFirst = gmDao.selectGroupMemberByGroupIdAndUserId(params);
                            if (null != gmFirst.getInviter() && !gmFirst.getInviter().isEmpty()) {
                                params.put("userId", gmFirst.getInviter());
                                gmInviter = gmDao.selectGroupMemberByGroupIdAndUserId(params);
                            }
                        }
                        prizeUserIdList.add(userId);
                        publicUserList.add(publicUserDao.getByUserId(userId));
                    }
                    if(publicUserList != null && !publicUserList.isEmpty()) {
                        //修改群变更时间记录
                        group.setUpdateTime(System.currentTimeMillis());
                        groupDao.updateByPrimaryKeySelective(group);
                        // 推送入群通知消息（消息推送范围：所有群成员）
                        List<String> groupCurrentUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
                        Message<JSONObject> message = GroupMessageProcesser.groupUserJoinResponseMessagePublish(gmFirst.getJoinType(), gmInviter != null ? gmInviter.getUserId() : null,
                                gmInviter != null ? gmInviter.getNikeName() : null, gmFirst.getInvitedReason(), group, publicUserList, groupCurrentUserIdList);
                        // 推送更改群信息消息
                        GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupCurrentUserIdList);
                        //放入缓存
                        redisService.evict(Const.GROUP_USERIDS + groupId);
                        String userIdJson = JSONObject.toJSONString(groupCurrentUserIdList);
                        redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
                        //调用进群奖励
                        try {
                            intoGroupPrize(packBundle, group, prizeUserIdList,userDetails);
                        }
                        catch (Exception e){
                            logger.info("进群奖励异常：" + e.getMessage());
                        }
                    }
                    return null;
                } else {
                    throw new RuntimeException("群ID和请求审批用户不能为空");
                }
            } else {
                throw new RuntimeException("请求审批用户不能为空");
            }
        } finally {
            DistributedRedisLock.release(LOCK_APPROVE_GROUP_USER + groupId.concat(userDetails.getUserId()));
        }
    }

    @Override
    public Group queryGroupByGroupId(String groupId) {
        Group group = groupDao.selectByPrimarykey(groupId);
        if(group != null) {
            int count = gmDao.selectGroupMemberCounts(groupId);
            group.setMemberAmount(count);
        } else {
            throw new GroupException("group.info.not.exist", "该群不存在或已经解散", null);
        }
        return group;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<Message<JSONObject>> deleteGroupUser(KickGroupUserParam kickGroupUserParam) {
        if(kickGroupUserParam != null) {
            String groupId = kickGroupUserParam.getGroupId();
            List<String> userIdList = kickGroupUserParam.getUserIdList();
            if(StringUtils.isNotEmpty(groupId) && userIdList != null && !userIdList.isEmpty()) {
            	Group group = groupDao.selectByPrimarykey(groupId);
                List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
            	for(String userId : userIdList) {
                    Map<String, String> params = new HashMap<> ();
                    params.put("groupId", groupId);
                    params.put("userId", userId);
                    gmDao.deleteGroupMemberCanNotOwner(params);
                }
                //修改群变更时间记录
                group.setUpdateTime(System.currentTimeMillis());
                groupDao.updateByPrimaryKeySelective(group);
                // 退群通知消息（消息推送范围：所有人包括被踢出人员）
                Message<JSONObject> message = GroupMessageProcesser.leaveGroupMessagePublish(true, group, userIdList, groupUserIdList);
                //增加缓存处理
                List<String> groupCurrentUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
                // 推送更改群信息消息
                GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupCurrentUserIdList);
                redisService.evict(Const.GROUP_USERIDS + groupId);
                String userIdJson = JSONObject.toJSONString(groupCurrentUserIdList);
                redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
                return Arrays.asList(message);
            } else {
            	throw new RuntimeException("删除群用户群ID和群用户ID不能为空");
            }
        } else {
        	throw new RuntimeException("删除群用户群ID和群用户ID不能为空");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<Message<JSONObject>> exitGroup(PackBundle packBundle,ExitGroupParam exitGroupParam) {
        if(exitGroupParam != null && StringUtils.isNotEmpty(exitGroupParam.getGroupId()) && StringUtils.isNotEmpty(exitGroupParam.getUserId())) {
            Group group = groupDao.selectByPrimarykey(exitGroupParam.getGroupId());
            if(group != null && StringUtils.isNotEmpty(group.getOwnerId())) {
                //判断是否是群主
                if(StringUtils.equalsIgnoreCase(exitGroupParam.getUserId(), group.getOwnerId())) { //群主
                    //判断群里是否只有一个人(如果只有一个人直接删除群，否则群主退群)
                    int i = gmDao.selectGroupMemberCounts(exitGroupParam.getGroupId());
                    if(i == 1) {
                        //删除群成员
                        deleteGroupMember(exitGroupParam.getGroupId(), exitGroupParam.getUserId());
                        //删除群
                        groupDao.deleteByPrimaryKey(exitGroupParam.getGroupId());
                        //增加缓存处理
                        redisService.evict(Const.GROUP_USERIDS + exitGroupParam.getGroupId());
                        // 退群通知消息
                        //当群内只有群主一人的时候，无需再推送退群消息
                        //Message<JSONObject> message = GroupMessageProcesser.leaveGroupMessagePublish(false, group, Arrays.asList(exitGroupParam.getUserId()), groupUserIdList);
                        //return Arrays.asList(message);
                        return null;
                    } else if(i > 1) {
                        //群成员大于1的时候，推广群群主不准退出
                        Integer isPromote = group.getIsPromote();
                        if (isPromote != null && isPromote.intValue() == EnumStatus.ONE.getIndex()){
                            String msg = "推广群群主不能退出！";
                            throw new PassportException("group.owner.not.exit",msg,null);
                        }
                        //删除群成员
                        deleteGroupMember(exitGroupParam.getGroupId(), exitGroupParam.getUserId());
                        List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
                        // 推送更改群信息消息
                        GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupUserIdList);
                        // 退群通知消息（推送范围：退群动作之后的所有群成员）
                        Message<JSONObject> leaveGroupMessage = GroupMessageProcesser.leaveGroupMessagePublish(false, group, Arrays.asList(exitGroupParam.getUserId()), groupUserIdList);
                        //增加缓存处理
                        redisService.evict(Const.GROUP_USERIDS + exitGroupParam.getGroupId());
                        String userIdJson = JSONObject.toJSONString(groupUserIdList);
                        redisService.put(Const.GROUP_USERIDS + exitGroupParam.getGroupId(), userIdJson, 3600);
                        //搜索除要被删除的人之外最先进群的一个人
                        GroupMember groupMember = gmDao.selectTheEarliestMem(exitGroupParam.getGroupId());
                        //赋予群主身份(并且更新群变更时间)
                        /*Map<String, String> params = new HashMap<> ();
                        params.put("groupId", exitGroupParam.getGroupId());
                        params.put("ownerId", groupMember.getUserId());
                        groupDao.updateGroupOwner(params);*/
                        group.setOwnerId(groupMember.getUserId());
                        group.setUpdateTime(System.currentTimeMillis());
                        groupDao.updateByPrimaryKeySelective(group);
                        // 变更群主消息（推送范围：退群动作之后所有在群内的群成员）
                        //Message<JSONObject> changeGroupOwnerMessage = GroupMessageProcesser.changeGroupOwnerMessagePublish(group, groupMember.getUserId(), groupUserIdList);
                        Message<JSONObject> changeGroupOwnerMessage = GroupMessageProcesser.changeGroupOwnerMessagePublish(group.getGroupId(), exitGroupParam.getUserId(), groupMember.getUserId(), groupUserIdList);
                        return Arrays.asList(leaveGroupMessage, changeGroupOwnerMessage);
                    } else {
                        String msg = "此群不存在或者已经解散！";
                        throw new PassportException("group.info.not.exist",msg,null);
                    }
                } else { //非群主
                    deleteGroupMember(exitGroupParam.getGroupId(), exitGroupParam.getUserId());
                    //修改群变更时间记录
                    group.setUpdateTime(System.currentTimeMillis());
                    groupDao.updateByPrimaryKeySelective(group);
                    // 推送更改群信息消息
                    List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
                    GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupUserIdList);
                    // 退群通知消息（推送范围：退群动作之后所有在群内的群成员）
                    Message<JSONObject> message = GroupMessageProcesser.leaveGroupMessagePublish(false, group, Arrays.asList(exitGroupParam.getUserId()), groupUserIdList);
                    //增加缓存处理
                    redisService.evict(Const.GROUP_USERIDS + group.getGroupId());
                    String userIdJson = JSONObject.toJSONString(groupUserIdList);
                    redisService.put(Const.GROUP_USERIDS + group.getGroupId(), userIdJson, 3600);
                    return Arrays.asList(message);
                }
            } else {
                String msg = "此群已经解散或是此群没有群主！";
                throw new PassportException("group.owner.not.exist",msg,null);
            }
        } else {
            String msg = "退出群的参数不能为空！";
            throw new PassportException("group.exit.param.not.empty",msg,null);
        }
    }

	@Override
    public List<Group> queryBySearchKey(String searchKey) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("searchKey", searchKey);
        List<Group> groups = groupDao.selectBySearchKey(params);
        if(groups != null && !groups.isEmpty()) {
            for(Group group : groups) {
                List<String> userIds = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
                group.setUserIdList(userIds);
            }
        }
        return groups;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateGroup(Group group, String groupHeader) throws IOException {
        if(StringUtils.isNotEmpty(groupHeader)) {
            try {
                String filePath = PassPortUtil.saveGroupHeader(groupHeader, null);
                group.setGroupHeader(filePath);
            } catch (IOException e) {
                throw e;
            }
        }
        //修改群变更时间记录
        group.setUpdateTime(System.currentTimeMillis());
        groupDao.updateByPrimaryKeySelective(group);
        
        // 推送更改群信息消息
        List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(group.getGroupId());
        GroupMessageProcesser.updateGroupInfoMessagePublish(group, group.getOwnerId(), groupUserIdList);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateGroupMember(GroupMember groupMember) {
        Integer msgReceiveType = groupMember.getMsgReceiveType();
        gmDao.updateByPrimaryKeySelective(groupMember);
        if(msgReceiveType != null) {
            String groupId = groupMember.getGroupId();
            List<String> userIds = gmDao.selectSetDisturbUserIds(groupId);
            String userIdJson = JSONObject.toJSONString(userIds);
            redisService.evict(Const.GROUP_SETDISTURB_USERIDS + groupId);
            redisService.put(Const.GROUP_SETDISTURB_USERIDS + groupId, userIdJson, 3600);
        }
        if(groupMember.getNeedSendMsg()) {
            Group group = groupDao.selectByPrimarykey(groupMember.getGroupId());
            //修改群变更时间记录
            group.setUpdateTime(System.currentTimeMillis());
            groupDao.updateByPrimaryKeySelective(group);
            // 推送更改个人信息消息
            PublicUser publicUser = publicUserDao.getByUserId(groupMember.getUserId());
            List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(groupMember.getGroupId());
            GroupMessageProcesser.updateMemberInfoMessagePublish(groupMember, (null == publicUser) ? "" : publicUser.getAvatarUrl(), groupUserIdList);
        }
    }

    @Override
    public GroupMember queryGroupMemberByGroupIdAndUserId(String groupId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
        if(groupMember != null) {
            PublicUser user = publicUserDao.getByUserId(userId);
            groupMember.setAvatarUrl(Misc.getServerUri(null, user.getAvatarUrl()));
        }
        return groupMember;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void changeGroupOwner(PackBundle packBundle,String groupId, String oldOwnerId, String newOwnerId) {
        //1.验证新群主用户id是否在群众
        Map<String, String> params = new HashMap<> ();
        params.put("groupId", groupId);
        params.put("userId", newOwnerId);
        GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
        if(groupMember == null) {
            String msg = "新群主必须为此群中的群成员！";
            throw new PassportException("group.new.owner.not.exist",msg,null);
        }
        //2.更换群主
        Group group = groupDao.selectByPrimarykey(groupId);
        if (group != null){
            //如果群为推广群，不能转让群组
            Integer isPromote = group.getIsPromote();
            if (isPromote != null && isPromote.intValue() == EnumStatus.ONE.getIndex()){
                String msg = "推广群不能更换群主！";
                throw new PassportException("group.owner.not.change",msg,null);
            }
        }
        group.setOwnerId(newOwnerId);
        //修改群变更时间记录
        group.setUpdateTime(System.currentTimeMillis());
        groupDao.updateByPrimaryKeySelective(group);
        //3.推送更换群主消息（推送范围：所有群成员）
        List<String> groupUserIdList = gmDao.selectGroupMemberUserIdByGroupId(groupId);
        GroupMessageProcesser.changeGroupOwnerMessagePublish(groupId, oldOwnerId, newOwnerId, groupUserIdList);
    }

    @Override
    public GroupMember queryGroupUserInfoOnLogin(String groupId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        return gmDao.selectGroupUserInfoOnLogin(params);
    }

    @Override
    public List<String> queryGroupIds(String userId) {
        return gmDao.selectGroupIds(userId);
    }

    @Override
    public List<Group> queryGroupsByGroupIds(List<String> groupIds, String userId) {
        List<Group> groups = new ArrayList<>();
        for(String groupId : groupIds) {
            List<String> userIds = gmDao.selectGroupMemberUserIdByGroupId(groupId);
            if(!userIds.contains(userId)) {//当前登陆用户不在群里的直接跳过
                continue;
            }
            Group group = groupDao.selectByPrimarykey(groupId);
            if(group != null) {
                group.setUserIdList(userIds);
                group.setMemberAmount(userIds.size());
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public List<Group> queryGroups(String userId) {
        List<String> groupIds = gmDao.selectGroupIds(userId);
        return queryGroupsByGroupIds(groupIds, userId);
    }

    @Override
    public List<String> queryGroupMemUserIds(String groupId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
        if(groupMember == null) {
            throw new RuntimeException("不能获取自己不在的群的群成员用户id集合！");
        }
        List<String> userIds = gmDao.selectGroupMemberUserIdByGroupId(groupId);
        return userIds;
    }

    @Override
    public List<GroupMember> queryGroupMemsByUserIds(GetGroupMemsByUserIdsParam getGroupMemsByUserIdsParam, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("groupId", getGroupMemsByUserIdsParam.getGroupId());
        params.put("userId", userId);
        GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
        if(groupMember == null) {
            throw new RuntimeException("不能获取自己不在的群的群成员信息集合！");
        }
        List<String> userIds = getGroupMemsByUserIdsParam.getUserIds();
        return queryGroupMemsByGroupIdAndUserIds(getGroupMemsByUserIdsParam.getGroupId(), userIds);
    }

    @Override
    public List<GroupMember> queryGroupMems(String groupId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
        if(groupMember == null) {
            throw new RuntimeException("不能获取自己不在的群的群成员信息集合！");
        }
        List<String> userIds = gmDao.selectGroupMemberUserIdByGroupId(groupId);
        return queryGroupMemsByGroupIdAndUserIds(groupId, userIds);
    }

    @Override
    public List<Group> queryGroupMems(List<String> groupIds, String userId) {
        List<Group> groups = new ArrayList<>();
        if(groupIds != null && !groupIds.isEmpty()) {
            for(String groupId : groupIds) {
                String userIdJson = redisService.get(Const.GROUP_USERIDS + groupId);
                List<String> userIdList = JSONObject.parseArray(userIdJson, String.class);
                if(userIdList == null || userIdList.size() == 0) {
                    userIdList = gmDao.selectGroupMemberUserIdByGroupId(groupId);
                    userIdJson = JSONObject.toJSONString(userIdList);
                    redisService.evict(Const.GROUP_USERIDS + groupId);
                    redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
                }
                if(userIdList != null && userIdList.size() != 0) {
                    Group group = groupDao.selectByPrimarykey(groupId);
                    if(group != null) {
                        List<GroupMember> groupMembers = gmDao.selectGroupMemsByGroupId(groupId);
                        if(groupMembers != null && !groupMembers.isEmpty()) {
                            for(GroupMember groupMember : groupMembers) {
                                if(StringUtils.isNotEmpty(groupMember.getAvatarUrl())) {
                                    groupMember.setAvatarUrl(Misc.getServerUri(null, groupMember.getAvatarUrl()));
                                }
                            }
                        }
                        group.setGroupHeader(Misc.getServerUri(null, group.getGroupHeader()));
                        group.setGroupMembers(groupMembers);
                        group.setMemberAmount(groupMembers.size());
                        groups.add(group);
                    }
                }
            }
        }
        return groups;
    }

    @Override
    public int queryGroupMemberCounts(String groupId) {
        return gmDao.selectGroupMemberCounts(groupId);
    }

    /**
     * 查询群成员信息
     *
     * @param groupMemberSearchDto
     * @return
     */
    @Override
    public BaseResult<List<Map<String,Object>>> listSearchGroupMembers(PackBundle bundle,GroupMemberSearchDto groupMemberSearchDto) {
        //业务结果通用返回对象
        BaseResult<List<Map<String,Object>>> resultData = new BaseResult<>();
        try{
            String groupId = groupMemberSearchDto.getGroupId();
            Group group = groupDao.selectByPrimarykey(groupId);
            if (group == null){
                String errMsg = "target group not exist";
                if(bundle != null) {
                    errMsg = bundle.getString("config.fail.target.group.not.exist");
                }
                resultData.setStatus(EnumStatus.ONE.getIndex());
                resultData.setError(errMsg);
                return resultData;
            }
            //分页记录数，默认20条记录
            Integer rows = groupMemberSearchDto.getRows();
            rows = rows == null?20:rows;
            //分页时间戳
            Long timestamp = groupMemberSearchDto.getTimestamp();
            //不为空且大于0 ，右移位30
            if (timestamp != null && timestamp.intValue() > 0){
                timestamp = timestamp >> 20;
            }
            //为空或0，默认设置为空
            if (timestamp == null || timestamp.intValue() == 0){
                timestamp = null;
            }
            String content = groupMemberSearchDto.getContent();
            GroupMember groupMember = new GroupMember();
            groupMember.setOffset(rows);
            groupMember.setId(timestamp);
            groupMember.setNikeName(content);
            groupMember.setUserName(content);
            groupMember.setGroupId(groupId);
            List<GroupMember> listMembers = gmDao.listMembersByContent(groupMember);
            List<Map<String,Object>> listResults = new ArrayList<>();
            for (GroupMember groupMemberTemp:listMembers){
                Map<String,Object> memberMap = new HashMap<>();
                Long id = groupMemberTemp.getId().longValue();
                id = id << 20;
                memberMap.put("groupId",groupMemberTemp.getGroupId());
                memberMap.put("userId",groupMemberTemp.getUserId());
                memberMap.put("joinTime",groupMemberTemp.getJoinTime());
                memberMap.put("joinType",groupMemberTemp.getJoinType());
                memberMap.put("nickname",groupMemberTemp.getNikeName());
                memberMap.put("avatarUrl",Misc.getServerUri(null, groupMemberTemp.getAvatarUrl()));
                memberMap.put("nikeNameAltered",groupMemberTemp.getNikeNameAltered());
                memberMap.put("timestamp",id);
                listResults.add(memberMap);
            }
            resultData.setData(listResults);
            resultData.setStatus(EnumStatus.ZORO.getIndex());
            resultData.setError("success");
            return resultData;
        }
        catch (Exception e){
            logger.info("GroupServiceImpl.listSearchGroupMembers.error:" + e.getMessage());
            resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
            resultData.setError(e.getMessage());
            return resultData;
        }
    }

    private List<GroupMember> queryGroupMemsByGroupIdAndUserIds(String groupId, List<String> userIds) {
        List<GroupMember> groupMembers = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        for(String userId : userIds) {
            params.put("userId", userId);
            GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
            if(groupMember != null) {
                PublicUser publicUser = publicUserDao.getByUserId(userId);
                groupMember.setAvatarUrl(Misc.getServerUri(null, publicUser.getAvatarUrl()));
                groupMembers.add(groupMember);
            }
        }
        return groupMembers;
    }

    private void deleteGroupMember(String groupId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        gmDao.deleteGroupMemberCanBeAll(params);
    }

    private List<PublicUser> createGroupMember(Group group, String invitedReason, String inviter, int joinType, List<String> userIdList, Date now) {
    	List<PublicUser> publicUserList = new ArrayList<>();
    	
    	GroupMember gm = new GroupMember();
        gm.setGroupId(group.getGroupId());
        gm.setInvitedReason(invitedReason);
        gm.setInviter(inviter);
        gm.setJoinTime(now);
        gm.setJoinType(joinType);
        gm.setShowNikeName(1);
        gm.setMsgReceiveType(0);
        gm.setTopTalk(0);
        int needAuth = group.getNeedAuth();
        //群设置为入群需要群主审核，并且邀请人不是群主的时候，需要审批
        if(needAuth == 1 && !StringUtils.equalsIgnoreCase(inviter, group.getOwnerId())) {
            gm.setJoinState(0);//待审批
        } else {
            gm.setJoinState(1);//已入群
        }
        gm.setMsgReceiveType(0);
        for(String userId : userIdList) {
            //验证用户是否再黑名单里
            Map<String, String> params0 = new HashMap<>();
            params0.put("userId" , userId);
            params0.put("blockId", group.getOwnerId());
            Integer count = blockDao.selectBlockByUserIdAndBlockUserId(params0);
            if(count != null && count > 0) {
                continue;
            }
            //验证群成员是否已经在群里了
            Map<String, String> params = new HashMap<> ();
            params.put("groupId", group.getGroupId());
            params.put("userId", userId);
            GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
            if(groupMember != null && groupMember.getJoinState() == 1) {
                continue;
            } else {
                PublicUser publicUser = publicUserDao.getByUserId(userId);
                if(publicUser == null) continue;
                gm.setNikeName(publicUser.getUserName());
                gm.setShowNikeName(0);
                gm.setUserId(userId);
                if(groupMember != null && groupMember.getJoinState() != 1) {
                    gm.setId(groupMember.getId());
                    gmDao.updateByPrimaryKeySelective(gm);
                    logger.info("update group info:" + JSON.toJSONString(gm));
                } else {
                    gmDao.insertSelective(gm);
                    logger.info("insert group info:" + JSON.toJSONString(gm));
                }
                publicUserList.add(publicUser);
            }
        }
        return publicUserList;
    }

    private void makeGroupHeader(Group group, List<PublicUser> publicUserList) {
        String userphotoroot = Config.get("userphotoroot");
        String groupheaderroot = Config.get("groupheaderroot");
        String groupheaderdir = Config.get("groupheaderdir");
        String backFilePath = groupheaderroot + groupheaderdir + "back.jpg";
        Date now = new Date();
        String datePath = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator;
        String dir = groupheaderroot + groupheaderdir + datePath;
        File dirFile = new File(dir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }
        String groupHeaderName = StringUtil.getUUID() + ".jpg";
        String groupHeaderFile = dir + groupHeaderName;
        int size = publicUserList.size() >= 9 ? 9 : publicUserList.size();
        List<String> userPhotos = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            String userPhoto = userphotoroot + publicUserList.get(i).getAvatarUrl();
            if(StringUtils.contains(publicUserList.get(i).getAvatarUrl(), "/upload/")) {
                userPhoto = "/mnt/n/UserData/MessagePush" + publicUserList.get(i).getAvatarUrl();
            } else if(StringUtils.contains(publicUserList.get(i).getAvatarUrl(), "resources")) {
                userPhoto = "/mnt/n/UserData/shareApp/" + publicUserList.get(i).getAvatarUrl();
            }
            boolean exists = FileUtil.exists(userPhoto);
            if(!exists) {
                userPhoto = userphotoroot + "resources/images/user/user_log_b.png";
            }
            userPhotos.add(userPhoto);
        }
        GroupPhotoDrawer gpd = GroupPhotoDrawer.getInstance();
        gpd.draw(backFilePath, userPhotos, groupHeaderFile);
        group.setGroupHeader(groupheaderdir + datePath + groupHeaderName);
        groupDao.updateByPrimaryKeySelective(group);
    }
}
