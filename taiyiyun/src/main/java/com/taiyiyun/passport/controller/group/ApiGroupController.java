package com.taiyiyun.passport.controller.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.PageBodyInfo;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.group.*;
import com.taiyiyun.passport.service.group.IGroupService;
import com.taiyiyun.passport.util.CacheUtil;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.SessionUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by nina on 2017/9/15.
 */
@Controller
public class ApiGroupController extends BaseController{

    @Resource
    private IGroupService groupService;

    @RequestMapping(value="/api/group/createGroup", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public String createGroup(HttpServletRequest request, @RequestBody GroupParam groupParam){
        String apiName = "/api/group/createGroup";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(groupParam == null) {
//            throw new RuntimeException("创建群异常：传递的参数实体不能为空！");
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            return toJson(1, "PackBundle对象不能为空！", apiName, "");
        }
        
        UserDetails userDetails = SessionUtil.getUserDetails(request);
    	if(null == userDetails) {
        	return toJson(1, bundle.getString("user.not.login"), apiName, "");
    	} else {
    		groupParam.setOwnerId(userDetails.getUserId());
    	}
//        if(StringUtils.isEmpty(groupParam.getOwnerId())) {
//            throw new RuntimeException("创建群异常：群主用户ID不能为空！");
//        }
        if(groupParam.getUserIdList() == null || groupParam.getUserIdList().isEmpty()) {
            return toJson(1, bundle.getString("group.member.ids.notempty"), apiName, "");
//            return toJson(1, "群成员用户ID集合不能为空！", apiName, "");
//            throw new RuntimeException("创建群异常：");
        }
        if(groupParam.getUserIdList().size() < 2) {
            return toJson(1, bundle.getString("group.creategroup.notmorethan3"), apiName, "");
//            throw new GroupException("", "群成员数量不能小于3人！", null);
        }
        List<String> userIdList = groupParam.getUserIdList();
    	List<String> allUserIdList = new ArrayList<>();
    	/*将群主的id放入群成员id集合，并且放在第一个*/
        allUserIdList.add(userDetails.getUserId());
    	for(String userId : userIdList) {
    	    allUserIdList.add(userId);
        }
        List<Message<JSONObject>> messages = groupService.addGroup(bundle,userDetails.getUserId(),null,0, allUserIdList, userDetails);
        return toJson(0, "", apiName, messages);
    }

    @ResponseBody
    @RequestMapping(value="/api/group/addGroupUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String addGroupUser(HttpServletRequest request, @RequestBody AddGroupUserParam addGroupUserParam) {
        String apiName = "/api/group/addGroupUser";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(addGroupUserParam == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            return toJson(1, "参数实体为空！", apiName, "");
//            throw new RuntimeException("添加成员异常：参数实体为空！");
        }
        if(StringUtils.isEmpty(addGroupUserParam.getGroupId())) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            return toJson(1, "群ID不能为空！", apiName, "");
//            throw new RuntimeException("添加成员异常：群ID为空！");
        }
        if(addGroupUserParam.getUserIdList() == null || addGroupUserParam.getUserIdList().isEmpty()) {
            return toJson(1, bundle.getString("group.member.ids.notempty"), apiName, "");
//            return toJson(1, "群成员id列表不能为空！", apiName, "");
//            throw new RuntimeException("添加成员异常：群成员id列表为空！");
        }
        int maxNum = Config.getInt("group.max.num", 2000);
        int memCounts = groupService.queryGroupMemberCounts(addGroupUserParam.getGroupId());
        if(memCounts + addGroupUserParam.getUserIdList().size() > maxNum) {
            List<Object> argues = new ArrayList<>();
            argues.add(maxNum);
            return toJson(1, bundle.getString("group.addgroupuser.membertoomany",maxNum), apiName, "");
//            throw new GroupException("group.addgroupuser.membertoomany", "群最大上限人数为" + maxNum , argues);
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        groupService.addGroupUser(bundle,addGroupUserParam, userDetails);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/addGroupUserByScanCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String addGroupUserByScanCode(HttpServletRequest request, String thirdKey) {
        String apiName = "/api/group/addGroupUserByScanCode";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(thirdKey)) {
            return toJson(1, bundle.getString("group.into.keyword.notempty"), apiName, "");
//            return toJson(1, "被扫码的关键字不能为空！", apiName, "");
//            throw new RuntimeException("扫码入群异常：被扫码的关键字不能为空！");
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        groupService.addGroupUserByScanCode(bundle,thirdKey,userDetails);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/thirdkeys", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String queryAllGroupThirdKeys() {
        String apiName = "/api/group/thirdkeys";
        List<String> thirdKeys = groupService.queryAllGroupThirdKeys();
        return toJson(0, "", apiName, thirdKeys);
    }

    @ResponseBody
    @RequestMapping(value="/api/group/approveGroupUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String approveGroupUser(HttpServletRequest request, @RequestBody ApproveGroupUserParam approveGroupUserParam) {
        String apiName = "/api/group/approveGroupUser";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(approveGroupUserParam == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            return toJson(1, "参数实体不能为空！", apiName, "");
//            throw new RuntimeException("入群审批异常：参数实体为空！");
        }
        if(StringUtils.isEmpty(approveGroupUserParam.getGroupId())) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            return toJson(1, "群ID不能为空！", apiName, "");
//            throw new RuntimeException("入群审批异常：群ID为空！");
        }
        if(approveGroupUserParam.getUserIdList() == null || approveGroupUserParam.getUserIdList().isEmpty()) {
            return toJson(1, bundle.getString("group.member.ids.notempty"), apiName, "");
//            return toJson(1, "群成员id列表不能为空！", apiName, "");
//            throw new RuntimeException("入群审批异常：群成员id列表为空！");
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        Group group = groupService.queryGroupByGroupId(approveGroupUserParam.getGroupId());
        if(group == null) {
            return toJson(1, bundle.getString("group.info.not.exist"), apiName, "");
//            return toJson(1, "此群不存在或者已经解散！", apiName, "");
//            throw new RuntimeException("入群审批异常：此群不存在或者已经解散！");
        }
        if(!StringUtils.equalsIgnoreCase(group.getOwnerId(), userDetails.getUserId())) {
            return toJson(1, bundle.getString("group.current.user.not.owner"), apiName, "");
//            throw new RuntimeException("入群审批异常：当前用户不是群主，无权进行入群审批！");
        }
        groupService.approveGroupUser(bundle,approveGroupUserParam,userDetails);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/kickGroupUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String kickGroupUser(HttpServletRequest request, @RequestBody KickGroupUserParam kickGroupUserParam) {
        String apiName = "/api/group/kickGroupUser";
        PackBundle bundle =  LangResource.getResourceBundle(request);
        if(kickGroupUserParam == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            return toJson(1, "参数实体不能为空！", apiName, "");
//            throw new RuntimeException("删除群成员异常：参数实体为空！");
        }
        if(StringUtils.isEmpty(kickGroupUserParam.getGroupId())) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            return toJson(1, "群ID不能为空！", apiName, "");
//            throw new RuntimeException("删除群成员异常：群ID为空！");
        }
        if(kickGroupUserParam.getUserIdList() == null || kickGroupUserParam.getUserIdList().isEmpty()) {
            return toJson(1, bundle.getString("group.member.ids.notempty"), apiName, "");
//            return toJson(1, "群成员id列表不能为空！", apiName, "");
//            throw new RuntimeException("删除群成员异常：群成员id列表为空！");
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        Group group = groupService.queryGroupByGroupId(kickGroupUserParam.getGroupId());
        if(group == null) {
            return toJson(1, bundle.getString("group.info.not.exist"), apiName, "");
//            return toJson(1, "此群不存在或者已经解散！", apiName, "");
//            throw new RuntimeException("删除群成员异常：此群不存在或者已经解散！");
        }
        if(!StringUtils.equalsIgnoreCase(group.getOwnerId(), userDetails.getUserId())) {
            return toJson(1, bundle.getString("group.current.user.not.owner"), apiName, "");
//            return toJson(1, "当前用户不是群主，无权进行群成员删除操作！", apiName, "");
//            throw new RuntimeException("删除群成员异常：当前用户不是群主，无权进行群成员删除操作！");
        }
        groupService.deleteGroupUser(kickGroupUserParam);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/leaveGroup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String leaveGroup(HttpServletRequest request, @RequestBody ExitGroupParam exitGroupParam) {
        String apiName = "/api/group/leaveGroup";
        PackBundle bundle = LangResource.getResourceBundle(request);
        
        if(exitGroupParam == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            String msg = "群成员退出群异常：参数实体为空！";
//            throw new GroupException("group.param.not.empty",msg,null);
        }
        
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
        	return toJson(1, bundle.getString("user.not.login"), apiName, "");
    	} else {
    		exitGroupParam.setUserId(userDetails.getUserId());
    	}
        
        if(StringUtils.isEmpty(exitGroupParam.getGroupId())) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            String msg = "群成员退出群异常：群ID为空！";
//            throw new GroupException("group.owner.id.not.empty",msg,null);
        }
        
        Group group = groupService.queryGroupByGroupId(exitGroupParam.getGroupId());
        if(group == null) {
            return toJson(1, bundle.getString("group.info.not.exist"), apiName, "");
//            String msg = "群成员退出群异常：此群不存在或者已经解散！";
//            throw new GroupException("group.info.not.exist",msg,null);
        }
        
        groupService.exitGroup(bundle,exitGroupParam);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String searchGroup(HttpServletRequest request, String key, Integer start, String tag) {
        String apiName = "/api/group/search";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(key)) {
            return toJson(1, bundle.getString("group.search.cannotnull"), apiName, "");
//            throw new GroupException("group.search.cannotnull", "群搜索关键字不能为空！", null);
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(start == null) {
            start = 0;
        }
        PageBodyInfo<GroupContent> cache = null;
        if(StringUtils.isEmpty(tag)) {
            tag = Const.GROUP_SEARCH_TAG + new Date().getTime();
            CacheUtil.evict(Const.GROUP_SEARCH_KEY + userDetails.getUserId());
        } else {
            cache = CacheUtil.getHalfHour(Const.GROUP_SEARCH_KEY + userDetails.getUserId());
            if(null == cache || !tag.equals(cache.getTag())) {
                cache = null;
            }
        }
        try {
            if(null == cache) {
                cache = new PageBodyInfo<>();
                cache.setPageSize(20);
                List<GroupContent> contents = new ArrayList<>();
                List<Group> dataList = groupService.queryBySearchKey(key);
                if(null != dataList && dataList.size() > 0) {
                    for(int i = 0; i < dataList.size(); i++) {
                        Group group = dataList.get(i);
                        if(null != group) {
                            GroupContent groupContent = new GroupContent();
                            groupContent.setDescription(group.getDescription());
                            groupContent.setGroupHeader(Misc.getServerUri(request, group.getGroupHeader()));
                            groupContent.setGroupId(group.getGroupId());
                            groupContent.setGroupName(group.getGroupName());
                            groupContent.setGroupType(group.getGroupType());
                            groupContent.setInviteType(group.getInviteType());
                            groupContent.setMemIds(group.getUserIdList());
                            groupContent.setModifyRight(group.getModifyRight());
                            groupContent.setIndex(i + 1);
                            groupContent.setOwnerId(group.getOwnerId());
                            groupContent.setNeedAuth(group.getNeedAuth());
                            contents.add(groupContent);
                        }
                    }
                }
                cache.setStart(start);
                cache.setTag(tag);
                cache.setList(contents);
                CacheUtil.putHalfHour(Const.GROUP_SEARCH_KEY + userDetails.getUserId(), cache);
            } else {
                cache.setStart(start);
                cache.setTag(tag);
            }
            Map<String, Object> extData = new HashMap<>();
            extData.put("hasMore", cache.getHasMore());
            extData.put("tag", cache.getTag());
            return toJson(0, bundle.getString("successful.search"), "get" + apiName, cache.getDataList(), extData);
        } catch (Exception e) {
            return toJson(4, bundle.getString("failed.execute"), "get" + apiName, new ArrayList<>(), false);
        }
    }


    @ResponseBody
    @RequestMapping(value="/api/group/searchUsers", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public DeferredResult<String> searchUsers(@RequestBody GroupMemberSearchDto groupMemberSearchDto,HttpServletRequest request) {
        String apiName = "/api/group/searchUsers";
        DeferredResult<String> dr = new DeferredResult<>();
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(groupMemberSearchDto == null) {
            dr.setResult(toJson(1, bundle.getString("group.param.not.empty"), apiName, ""));
            return dr;
        }
        if(StringUtils.isEmpty(groupMemberSearchDto.getGroupId())) {
            dr.setResult(toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, ""));
            return dr;
        }
        if(StringUtils.isEmpty(groupMemberSearchDto.getContent())) {
            dr.setResult(toJson(1, bundle.getString("group.search.cannotnull"), apiName, ""));
            return dr;
        }
        BaseResult<List<Map<String,Object>>> resultData =  groupService.listSearchGroupMembers(bundle,groupMemberSearchDto);
        String rjson = JSON.toJSONString(resultData);
        dr.setResult(rjson);
        return dr;
    }

    @ResponseBody
    @RequestMapping(value="/api/group/getGroupInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupInfo(HttpServletRequest request, String groupId) {
        String apiName = "/api/group/getGroupInfo";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            return toJson(1, "群ID不能为空！", apiName, "");
//            throw new RuntimeException("群基本信息异常：群ID不能为空！");
        }
        Group group = groupService.queryGroupByGroupId(groupId);
        group.setGroupHeader(Misc.getServerUri(null, group.getGroupHeader()));
        return toJson(0, "", apiName, group);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/setGroupInfo", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String setGroupInfo(HttpServletRequest request, @RequestParam(value = "groupHeader", required = false) String groupHeader, @RequestParam("groupId") String groupId, @RequestParam(value = "groupName", required = false) String groupName,  @RequestParam(value = "description", required = false) String description
                                , @RequestParam(value = "inviteType", required = false) Integer inviteType, @RequestParam(value = "groupType", required = false) Integer groupType, @RequestParam(value = "modifyRight", required = false) Integer modifyRight, @RequestParam(value = "needAuth", required = false) Integer needAuth) throws IOException {
        String apiName = "/api/group/setGroupInfo";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            return toJson(1, "群ID不能为空！", apiName, "");
//            throw new RuntimeException("设置群信息异常：群ID不能为空！");
        }
        Group group = groupService.queryGroupByGroupId(groupId);
        if(group == null) {
            return toJson(1, bundle.getString("group.info.not.exist"), apiName, "");
//            return toJson(1, "群不存在！", apiName, "");
//            throw new RuntimeException("设置群信息异常：群不存在！");
        }
        if(StringUtils.isEmpty(groupName) && StringUtils.isEmpty(description) &&
                inviteType == null && groupType == null && modifyRight == null && StringUtils.isEmpty(groupHeader) && needAuth == null) {
            return toJson(1, bundle.getString("group.setting.notempty"), apiName, "");
//            return toJson(1, "所有设置项不能同时为空！", apiName, "");
//            throw new RuntimeException("设置群信息异常：所有设置项不能同时为空！");
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        String userId = userDetails.getUserId();
        /*针对只设置群名称需要验证是否群成员有权限设置*/
        if(StringUtils.isNotEmpty(groupName) && StringUtils.isEmpty(description) && inviteType == null && groupType == null && modifyRight == null &&
                 StringUtils.isEmpty(groupHeader) && needAuth == null) {//只设置群名称
            if(!StringUtils.equals(userId, group.getOwnerId()) && group.getModifyRight() == 0) {
                return toJson(1, bundle.getString("group.setgroupinfo.groupname"), apiName, "");
//                throw new GroupException("group.setgroupinfo.groupname", "群主已设置群成员不可以修改群名称！", null);
            }
        } else {//不是只设置群名称
            if(!StringUtils.equals(userId, group.getOwnerId())) {
                return toJson(1, bundle.getString("group.setgroupinfo.onlygrouownercan"), apiName, "");
//                throw new GroupException("group.setgroupinfo.onlygrouownercan", "只有群主才能设置！", null);
            }
        }
        if(StringUtils.isNotEmpty(groupName)) {
            group.setGroupName(groupName);
        }
        if(StringUtils.isNotEmpty(description)) {
            group.setDescription(description);
        }
        if(inviteType != null) {
            group.setInviteType(inviteType);
            group.setInviteTypeDefault(0);
        }
        if(groupType != null) {
            group.setGroupType(groupType);
            group.setGroupTypeDefault(0);
        }
        if(modifyRight != null) {
            group.setModifyRight(modifyRight);
            group.setModifyRightDefault(0);
        }
        if(needAuth != null) {
            group.setNeedAuth(needAuth);
            group.setNeedAuthDefault(0);
        }
        groupService.updateGroup(group, groupHeader);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/setGroupUserInfo", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String setGroupUserInfo(HttpServletRequest request, @RequestBody SetGroupUserInfoParam setGroupUserInfoParam) {
        String apiName = "/api/group/setGroupUserInfo";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(setGroupUserInfoParam == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            throw new RuntimeException("设置群成员信息异常：参数实体异常！");
        }
        String groupId = setGroupUserInfoParam.getGroupId();
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("设置群成员信息异常：群ID不能为空！");
        }
        Group group = groupService.queryGroupByGroupId(groupId);
        if(group == null) {
            return toJson(1, bundle.getString("group.info.not.exist"), apiName, "");
//            throw new RuntimeException("设置群成员信息异常：群不存在！");
        }
        String nikeName = setGroupUserInfoParam.getNikeName();
        Integer msgReceiveType = setGroupUserInfoParam.getMsgReceiveType();
        Integer showNikeName = setGroupUserInfoParam.getShowNikeName();
        Integer topTalk = setGroupUserInfoParam.getTopTalk();
        if(StringUtils.isEmpty(nikeName) && msgReceiveType == null && showNikeName == null && topTalk == null) {
            return toJson(1, bundle.getString("group.member.info.notempty"), apiName, "");
//            throw new RuntimeException("设置群成员信息异常：群成员信息不能全为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        GroupMember gm = groupService.queryGroupMemberByGroupIdAndUserId(groupId, userId);
        if(gm == null) {
            return toJson(1, bundle.getString("group.member.info.not.exist"), apiName, "");
//            throw new RuntimeException("设置群成员信息异常：群成员不在此群！");
        }
        if(StringUtils.isNotEmpty(nikeName)) {
            gm.setNikeName(nikeName);
            gm.setNeedSendMsg(true);
            gm.setNikeNameAltered(1);
        } else {
            gm.setNeedSendMsg(false);
        }
        if(msgReceiveType != null) {
            gm.setMsgReceiveType(msgReceiveType);
            gm.setMsgReceiveTypeDefault(0);
        }
        if(showNikeName != null) {
            gm.setShowNikeName(showNikeName);
            gm.setShowNikeNameDefault(0);
        }
        if(topTalk != null) {
            gm.setTopTalk(topTalk);
            gm.setTopTalkDefault(0);
        }
        groupService.updateGroupMember(gm);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/getGroupUserInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupUserInfo(HttpServletRequest request,  @RequestParam("groupId") String groupId, @RequestParam("userId") String userId) {
        String apiName = "/api/group/getGroupUserInfo";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("获取成员信息异常：群ID不能为空！");
        }
        if(StringUtils.isEmpty(userId)) {
            return toJson(1, bundle.getString("group.member.ids.notempty"), apiName, "");
//            throw new RuntimeException("获取成员信息异常：用户ID不能为空！");
        }
        GroupMember groupMember = groupService.queryGroupMemberByGroupIdAndUserId(groupId, userId);
        return toJson(0, "", apiName, groupMember);
    }


    @ResponseBody
    @RequestMapping(value = "/api/group/alterProperty", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String alterProperty(HttpServletRequest request, @RequestBody AlterPropertyParam alterPropertyParam) {
        String apiName = "/api/group/alterProperty";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(alterPropertyParam == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            String msg = "更换群主异常：参数实体不能为空！";
//            throw new GroupException("group.param.not.empty",msg,null);
        }
        String groupId = alterPropertyParam.getGroupId();
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            String msg = "更换群主异常：群ID不能为空！";
//            throw new GroupException("group.owner.id.not.empty",msg,null);
        }
        String userId = alterPropertyParam.getUserId();
        if(StringUtils.isEmpty(userId)) {
            return toJson(1, bundle.getString("group.newowner.id.notempty"), apiName, "");
//            String msg = "更换群主异常：新群主用户id不能为空！";
//            throw new GroupException("group.newowner.id.notempty",msg,null);
        }
        Group group = groupService.queryGroupByGroupId(groupId);
        if(group == null) {
            return toJson(1, bundle.getString("group.info.not.exist"), apiName, "");
//            String msg = "更换群主异常：群不存在或已经解散！";
//            throw new GroupException("group.info.not.exist",msg,null);
        }
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(!StringUtils.equalsIgnoreCase(userDetails.getUserId(), group.getOwnerId())) {
            return toJson(1, bundle.getString("group.current.user.not.owner"), apiName, "");
//            String msg = "更换群主异常：当前登录用户必须为群主！";
//            throw new GroupException("group.current.user.not.owner",msg,null);
        }
        groupService.changeGroupOwner(bundle,groupId, userDetails.getUserId(), userId);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value="/api/group/getGroupUserInfoOnLogin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupUserInfoOnLogin(HttpServletRequest request, @RequestParam(value="groupId", required = true) String groupId) {
        String apiName = "/api/group/getGroupUserInfoOnLogin";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户群成员信息异常:群ID不能为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        GroupMember groupMember = groupService.queryGroupUserInfoOnLogin(groupId, userId);
        List<GroupMember> groupMembers = new ArrayList<>();
        groupMembers.add(groupMember);
        return toJson(0, "", apiName, groupMembers);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroupIds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupIds(HttpServletRequest request) {
        String apiName = "/api/group/getGroupIds";
        PackBundle bundle = LangResource.getResourceBundle(request);
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<String> groupIds = groupService.queryGroupIds(userId);
        return toJson(0, "", apiName, groupIds);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroupsByGroupIds", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupsByGroupIds(HttpServletRequest request, @RequestBody GetGroupsByGroupIdsParam param) {
        String apiName = "/api/group/getGroupsByGroupIds";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(param == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户参与的部分群信息列表异常：参数实体不能为空！");
        }
        if(param.getGroupIds() == null || param.getGroupIds().isEmpty()) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户参与的部分群信息列表异常：群id数组不能为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<Group> groups = groupService.queryGroupsByGroupIds(param.getGroupIds(), userId);
        if(groups != null) {
            for(Group group : groups) {
                group.setGroupHeader(Misc.getServerUri(null, group.getGroupHeader()));
            }
        }
        return toJson(0, "", apiName, groups);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroups(HttpServletRequest request) {
        String apiName = "/api/group/getGroups";
        LangResource.getResourceBundle(request);
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<Group> groups = groupService.queryGroups(userId);
        if(groups != null) {
            for(Group group : groups) {
                group.setGroupHeader(Misc.getServerUri(null, group.getGroupHeader()));
            }
        }
        return toJson(0, "", apiName, groups);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroupMemUserIds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupMemUserIds(HttpServletRequest request, @RequestParam(value="groupId") String groupId) {
        String apiName = "/api/group/getGroupMemUserIds";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户群成员USERID列表异常：群id不能为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<String> userIds = groupService.queryGroupMemUserIds(groupId, userId);
        return toJson(0, "", apiName, userIds);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroupMemsByUserIds", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupMemsByUserIds(HttpServletRequest request, @RequestBody GetGroupMemsByUserIdsParam param) {
        String apiName = "/api/group/getGroupMemUserIds";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(param == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户所在的某个群的部分群成员信息列表异常：参数实体不能为空！");
        }
        if(StringUtils.isEmpty(param.getGroupId())) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户所在的某个群的部分群成员信息列表异常：群id不能为空！");
        }
        if(param.getUserIds() == null || param.getUserIds().isEmpty()) {
            return toJson(1, bundle.getString("group.member.ids.notempty"), apiName, "");
//            return toJson(1, "群成员用户id列表不能为空！", apiName, "");
//            throw new RuntimeException("获取登录用户所在的某个群的部分群成员信息列表异常：群成员用户id列表不能为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<GroupMember> groupMembers = groupService.queryGroupMemsByUserIds(param, userId);
        return toJson(0, "", apiName, groupMembers);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroupMems", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupMems(HttpServletRequest request, @RequestParam(value = "groupId") String groupId) {
        String apiName = "/api/group/getGroupMems";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(StringUtils.isEmpty(groupId)) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户所在的某个群的所有群成员信息列表异常：群id不能为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<GroupMember> groupMembers = groupService.queryGroupMems(groupId, userId);
        return toJson(0, "", apiName, groupMembers);
    }

    @ResponseBody
    @RequestMapping(value = "/api/group/getGroupMems", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getGroupMems(HttpServletRequest request, @RequestBody GetGroupMemsParam param) {
        String apiName = "/api/group/getGroupMems";
        PackBundle bundle = LangResource.getResourceBundle(request);
        if(param == null) {
            return toJson(1, bundle.getString("group.param.not.empty"), apiName, "");
//            throw new RuntimeException("获取登录用户所在的部分群的全部成员信息异常：参数实体不能为空！");
        }
        if(param.getGroupIds() == null || param.getGroupIds().isEmpty()) {
            return toJson(1, bundle.getString("group.owner.id.not.empty"), apiName, "");
//            return toJson(1, "群id列表不能为空！", apiName, "");
//            throw new RuntimeException("获取登录用户所在的部分群的全部成员信息异常：群id列表不能为空！");
        }
        String userId = SessionUtil.getUserDetails(request).getUserId();
        List<Group> groups = groupService.queryGroupMems(param.getGroupIds(), userId);
        return toJson(0, "", apiName, groups);
    }

}
