package com.taiyiyun.passport.dao.group;

import com.taiyiyun.passport.po.group.GroupPromoteDetail;

import java.util.List;

/**
 * Created by zhangjun on 2018/1/23.
 */
public interface IGroupPromoteDetailDao {
    /**
     * 插入操作
     * @param groupPromoteDetail
     * @return
     */
    public int insert(GroupPromoteDetail groupPromoteDetail);

    /**
     * 修改操作
     * @param groupPromoteDetail
     * @return
     */
    public int update(GroupPromoteDetail groupPromoteDetail);

    /**
     * 根据主键Id修改
     * @param groupPromoteDetail
     * @return
     */
    public int updateById(GroupPromoteDetail groupPromoteDetail);

    /**
     * 根据主键Id删除
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * 分页列表
     * @param groupPromoteDetail
     * @return
     */
    public List<GroupPromoteDetail> listPage(GroupPromoteDetail groupPromoteDetail);

    /**
     * 普通列表
     * @param groupPromoteDetail
     * @return
     */
    public List<GroupPromoteDetail> list(GroupPromoteDetail groupPromoteDetail);

    /**
     * 动态指定条件查询
     * @param groupPromoteDetail
     * @return
     */
    public List<GroupPromoteDetail> listCriteria(GroupPromoteDetail groupPromoteDetail);


    /**
     * 根据主键id获取数据
     * @param id
     * @return
     */
    public GroupPromoteDetail getOneById(Long id);
}
