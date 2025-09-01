package com.jisj.tinyorm;

import org.h2.jdbcx.JdbcDataSource;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

public class TestsEnv {
    public static DataSource getH2DataSource() {
        JdbcDataSource ds = new JdbcDataSource();
//        ds.setURL("jdbc:h2:mem:testdb");
        ds.setURL("jdbc:h2:file:D:\\Tools\\Java\\tinyorm\\target\\test-data\\testdb");
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    public static DataSource getSqliteDataSource() {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:file:D:\\Tools\\Java\\tinyorm\\target\\test-data\\testdb.sqlite");
        return ds;
    }

}
