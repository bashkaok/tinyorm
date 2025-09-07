package com.jisj.tinyorm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.List;

import static com.jisj.tinyorm.TestsEnv.getSqliteDataSource;
import static org.junit.jupiter.api.Assertions.*;


class BaseDAOTest {
    private static DataSource ds;
    private static DAO<TestEntityAnnotated, Long> daoAnn;
    private static DAO<TestEntityDefault, Long> daoDef;
    private static DAO<TestEntityGenerateKey, Integer> daoKey;

    @BeforeAll
    static void setUp() throws SQLException {
//        ds = getH2DataSource();
        ds = getSqliteDataSource();
        assertNotNull(ds);

        daoAnn = new BaseDAO<>(ds, TestEntityAnnotated.class);
        daoAnn.dropTable();
        daoAnn.createTable();
        addRecordsIntoAnnotated();
        daoDef = new BaseDAO<>(ds, TestEntityDefault.class);
        daoDef.dropTable();
        daoDef.createTable();
        daoKey = new BaseDAO<>(ds, TestEntityGenerateKey.class);
        daoKey.dropTable();
        daoKey.createTable();
    }

    private static void addRecordsIntoAnnotated() throws SQLException {
        final long recCount = 20;
        for (long i = 1L; i <= recCount; i++) {
            assertEquals(1, daoAnn.insert(TestEntityAnnotated.builder()
                    .id(i)
                    .name("Row-" + i)
                    .build()));
        }
    }

    @Test
    void insert() throws SQLException {
        final long recCount = 20;
        for (long i = 1L; i <= recCount; i++) {
            assertEquals(1, daoDef.insert(TestEntityDefault.builder()
                    .recId(i)
                    .name("Row-" + i)
                    .build()));
        }
        List<TestEntityDefault> result = daoDef.getAll().toList();
        assertEquals(recCount, result.size());
        for (long i = 1L; i <= recCount; i++) {
            assertEquals(i, result.get((int) i - 1).getRecId());
            assertEquals("Row-" + i, result.get((int) i - 1).getName());
        }

    }

    @Test
    void create() throws SQLException {
        assertEquals(1, daoKey.create(TestEntityGenerateKey.builder().name("Record1").build()));
        assertEquals(2, daoKey.create(TestEntityGenerateKey.builder().name("Record2").build()));
        assertEquals(10, daoKey.create(TestEntityGenerateKey.builder().id(10).name("Record10").build()));

    }

    @Test
    void update() throws SQLException {
        TestEntityDefault e = TestEntityDefault.builder()
                .recId(100L)
                .name("Row=100")
                .build();
        assertEquals(1, daoDef.insert(e));
        assertEquals(e, daoDef.getById(100L).orElseThrow());
        e.setName("Row:100");
        assertEquals(1, daoDef.update(e));
        assertEquals(e, daoDef.getById(100L).orElseThrow());
        //unexist record
        e.setRecId(500L);
        assertEquals(0, daoDef.update(e));
    }

    @Test
    void delete() throws SQLException {
        TestEntityDefault e = TestEntityDefault.builder()
                .recId(1000L)
                .name("Row=1000")
                .build();
        assertEquals(1, daoDef.insert(e));
        assertTrue(daoDef.getById(1000L).isPresent());
        assertEquals(1, daoDef.delete(1000L));
        assertFalse(daoDef.getById(1000L).isPresent());
        assertEquals(0, daoDef.delete(1001L));

    }

    @Test
    void getById() {
        assertFalse(daoAnn.getById(123L).isPresent());
        assertFalse(daoDef.getById(123L).isPresent());
        assertTrue(daoAnn.getById(10L).isPresent());
        assertEquals("Row-10", daoAnn.getById(10L).orElseThrow().getName());
    }

    @Test
    void getAll() {
        assertEquals(20, daoAnn.getAll().count());
    }


    @Test
    void testCreateTable() {
        DAO<TestEntityAnnotatedPG, Long> dao = new BaseDAO<>(ds, TestEntityAnnotatedPG.class);
        assertTrue(assertThrowsExactly(IllegalStateException.class, dao::createTable)
                .getMessage().contains("DDL create options are empty"));
    }

    @Test
    void find() throws SQLException {
        assertEquals(2, daoAnn.find("RecId=? OR name=?", 10L, "Row-9").size());
        assertEquals(20, daoAnn.find("name LIKE ?", "Row%").size());
    }

    @Test
    void query() throws SQLException {
        assertEquals(2, daoAnn.query("""
                        SELECT * FROM tblEntityAnnotated
                        WHERE RecId=? OR name=?""",
                10L, "Row-9").size());
        assertEquals(20, daoAnn.query("""
                        SELECT * FROM tblEntityAnnotated
                        WHERE name LIKE ?""",
                "Row%").size());

    }
}