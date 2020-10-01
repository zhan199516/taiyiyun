package com.taiyiyun.passport.dao.group;

import com.taiyiyun.passport.po.group.GroupPromoteConfig;

import java.util.List;

/**
 * Created by zhangjun on 2018/1/23.
 */
public interface IGroupPromoteConfigDao {

    /**
     * 插入操作
     * @param groupPromoteConfig
     * @return
     */
    public int insert(GroupPromoteConfig groupPromoteConfig);

    /**
     * 修改操作
     * @param groupPromoteConfig
     * @return
     */
    public int update(GroupPromoteConfig groupPromoteConfig);

    /**
     * 根据主键Id修改
     * @param groupPromoteConfig
     * @return
     */
    public int updateById(GroupPromoteConfig groupPromoteConfig);

    /**
     * 根据主键Id删除
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * 分页列表
     * @param groupPromoteConfig
     * @return
     */
    public List<GroupPromoteConfig> listPage(GroupPromoteConfig groupPromoteConfig);

    /**
     * 普通列表
     * @param groupPromoteConfig
     * @return
     */
    public List<GroupPromoteConfig> list(GroupPromoteConfig groupPromoteConfig);

    /**
     * 告警通知列表
     * @param groupPromoteConfig
     * @return
     */
    public List<GroupPromoteConfig> listNotice(GroupPromoteConfig groupPromoteConfig);

    /**
     * 根据主键id获取数据
     * @param id
     * @return
     */
    public GroupPromoteConfig getOneById(Long id);

    /**
     * 根据群组和用户id，获取推广配置信息
     * @param groupPromoteConfig
     * @return
     */
    public GroupPromoteConfig getOneByGroupAndUserId(GroupPromoteConfig groupPromoteConfig);


}
