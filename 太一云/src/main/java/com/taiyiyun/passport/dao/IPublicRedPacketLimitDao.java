package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicRedPacketLimit;

import java.util.List;

/**
 * Created by zhangjun on 2018/2/3.
 */
public interface IPublicRedPacketLimitDao {
    /**
     * 保存红包信息
     * @param publicRedPacketLimit
     * @return
     */
    public int insert(PublicRedPacketLimit publicRedPacketLimit);

    /**
     * 修改数据
     * @param publicRedPacketLimit
     * @return
     */
    public int update(PublicRedPacketLimit publicRedPacketLimit);

    /**
     * 根据主键id修改记录
     * @param publicRedPacketLimit
     * @return
     */
    public int updateById(PublicRedPacketLimit publicRedPacketLimit);

    /**
     * 根据id删除数据
     * @param redPacketId
     * @return
     */
    public int deleteById(String redPacketId);

    /**
     * 分页查询数据
     * @param publicRedPacketLimit
     * @return
     */
    public List<PublicRedPacketLimit> listPage(PublicRedPacketLimit publicRedPacketLimit);

    /**
     * 列表数据
     * @param publicRedPacketLimit
     * @return
     */
    public List<PublicRedPacketLimit> list(PublicRedPacketLimit publicRedPacketLimit);

    /**
     * 根据主键Id，查询列表数据
     * @param id
     * @return
     */
    public PublicRedPacketLimit getOneById(Long id);
}
