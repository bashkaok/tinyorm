package com.jisj.tinyorm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapperTest {
    @Mock
    ResultSet resultSet;

    @Test
    void build() throws SQLException {
        TestEntityMapper ex = new TestEntityMapper();

        Mapper<TestEntityMapper> mapper = new Mapper<>(TestEntityMapper.class);
        Function<ResultSet, TestEntityMapper> fn = mapper.build();

        when(resultSet.getLong("longF")).thenReturn(ex.getLongF());
        when(resultSet.getLong("longFL")).thenReturn(ex.getLongFL());
        when(resultSet.getBoolean("boolF")).thenReturn(ex.isBoolF());
        when(resultSet.getBoolean("boolFB")).thenReturn(ex.getBoolFB());
        when(resultSet.getDate("dateF")).thenReturn(ex.getDateF());
        when(resultSet.getTime("timeF")).thenReturn(ex.getTimeF());
        when(resultSet.getTimestamp("timestampF")).thenReturn(ex.getTimestampF());
        when(resultSet.getInt("intF")).thenReturn(ex.getIntF());
        when(resultSet.getInt("intFI")).thenReturn(ex.getIntFI());
        when(resultSet.getFloat("floatF")).thenReturn(ex.getFloatF());
        when(resultSet.getFloat("floatFF")).thenReturn(ex.getFloatFF());
        when(resultSet.getDouble("doubleF")).thenReturn(ex.getDoubleF());
        when(resultSet.getDouble("doubleFD")).thenReturn(ex.getDoubleFD());
        when(resultSet.getString("stringF")).thenReturn(ex.getStringF());

        TestEntityMapper ac = fn.apply(resultSet);
        System.out.println(ac);
        assertEquals(ex, ac);
    }
}