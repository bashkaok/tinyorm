package com.jisj.tinyorm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static com.jisj.tinyorm.TestsEnv.getSqliteDataSource;
import static org.junit.jupiter.api.Assertions.*;

class SimpleJoinDAOTest {
    private static SimpleJoinDAO<String, Integer, Integer> dao;

    @BeforeAll
    static void setUp() throws SQLException {
//        ds = getH2DataSource();
        DataSource ds = getSqliteDataSource();
        assertNotNull(ds);

        dao = new SimpleJoinDAO<>(ds, "join_table", "RecId", "master", "slave");
        dao.createTableStatement = "CREATE TABLE join_table (RecId INT UNIQUE, master BIGINT, slave BIGINT)";
        dao.dropTable();
        dao.createTable();
        dao.insert("a",1,1);
        dao.insert("b",1,2);
        dao.insert("c",9,1);
    }

    @Test
    void getById() {
        assertEquals(new SimpleJoinEntity<>("b", 1,2), dao.getById("b").orElseThrow());
    }

    @Test
    void update_and_delete() throws SQLException {
        dao.insert("e", 10, 90);
        assertEquals(new SimpleJoinEntity<>("e", 10,90), dao.getById("e").orElseThrow());
        dao.update(new SimpleJoinEntity<>("e", 10,100));
        assertEquals(new SimpleJoinEntity<>("e", 10,100), dao.getById("e").orElseThrow());
        dao.delete("e");
        assertEquals(3, dao.getAll().count());
    }
    @Test
    void getAll() {
        assertEquals(3, dao.getAll().count());
    }


    @Test
    void getInverseJoinIds() throws SQLException {
        assertEquals(List.of(1,2), dao.getInverseJoinIds(1));
    }

    @Test
    void getJoinIds() throws SQLException {
        assertEquals(List.of(1,9), dao.getJoinIds(1));
    }
}