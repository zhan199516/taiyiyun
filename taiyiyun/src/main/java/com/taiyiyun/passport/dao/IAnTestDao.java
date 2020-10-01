package com.taiyiyun.passport.dao;

import java.util.LinkedHashMap;
import java.util.List;

public interface IAnTestDao {
    List<LinkedHashMap> selectSql(String sql);

    int updateSql(String sql);
}
