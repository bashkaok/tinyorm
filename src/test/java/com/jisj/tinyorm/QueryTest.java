package com.jisj.tinyorm;

import com.jisj.tinyorm.dao.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static com.jisj.tinyorm.dao.TestsEnv.getSqliteDataSource;
import static org.junit.jupiter.api.Assertions.*;

class QueryTest {
    private static DataSource ds;

    @BeforeAll
    static void setUp() throws SQLException {
        ds = getSqliteDataSource();
        assertNotNull(ds);

        DAO<TestEntityGenerateKey, Integer> dao = new BaseDAO<>(ds, TestEntityGenerateKey.class);
        dao.dropTable();
        dao.createTable();
        dao.create(TestEntityGenerateKey.builder().name("Record-1").build());
        dao.create(TestEntityGenerateKey.builder().name("Record-2").build());
        dao.create(TestEntityGenerateKey.builder().name("Record-3").build());
        assertEquals(3, dao.getAll().count());
    }

    @Test
    void toList() {
        final String SQL = "SELECT id, name FROM TestEntityGenerateKey WHERE name <> ?";
        Query<Integer> query = new Query<>(ds, rs -> rs.getInt(1), SQL, "Record-3");
        assertEquals(List.of(1, 2), query.toList());

        Query<String> query1 = new Query<>(ds, rs -> rs.getString(2),
                "SELECT id, name FROM TestEntityGenerateKey");
        assertEquals(List.of("Record-1", "Record-2", "Record-3"), query1.toList());


    }
}