package com.jisj.tinyorm.dao;

import com.jisj.tinyorm.annotation.CrudDdl;
import jakarta.persistence.Table;

public class TestEntityHelper {
    @Table(name = "JakartaAnnotatedTable", options = "CREATE TABLE IF NOT EXISTS %s")
    public static class Jakarta {

    }

    @CrudDdl(createTableSql = "CREATE TABLE %s", insertSql = "INSERT INTO %s (name) VALUES(?)", updateSql = "UPDATE %s SET name=?")
    public static class Native {

    }
}
