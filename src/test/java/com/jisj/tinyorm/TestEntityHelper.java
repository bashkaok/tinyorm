package com.jisj.tinyorm;

import com.jisj.tinyorm.annotation.CrudDdl;
import jakarta.persistence.Table;

public class TestEntityHelper {
    @Table(name = "JakartaAnnotatedTable", options = "CREATE TABLE IF NOT EXISTS %s")
    public static class Jakarta {

    }

    @CrudDdl(createTableSql = "CREATE TABLE %s")
    public static class Native {

    }
}
