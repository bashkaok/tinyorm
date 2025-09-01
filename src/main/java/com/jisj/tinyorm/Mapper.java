package com.jisj.tinyorm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.jisj.tinyorm.EntityHelper.*;

/**
 * Mapper builder class
 * @param <R> return entity instance type
 */
public class Mapper<R> {
    private final Class<?> entityClass;
    /**
     * {@code Map<columnName, fieldSetter>}
     */
    private final Map<String, Method> setters = new HashMap<>();

    Mapper(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    Function<ResultSet, R> build() {
        fillSetters();
        return this::map;
    }

    private void fillSetters() {
        getFields(entityClass)
                .forEach(field -> setters.put(getColumnName(field), getSetter(field)));
    }

    private R map(ResultSet rs) {
        @SuppressWarnings("unchecked") R entity = (R) newInstance(entityClass);
        for (var f : setters.entrySet()) {
            try {
                f.getValue().invoke(entity, getRSValue(rs, f.getValue().getParameterTypes()[0], f.getKey()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return entity;
    }

    private static Object getRSValue(ResultSet rs, Class<?> type, String columnName) {
        try {
            if (type == Long.class || type == long.class)
                return rs.getLong(columnName);
            else if (type == Boolean.class || type == boolean.class)
                return rs.getBoolean(columnName);
            else if (type == Date.class)
                return rs.getDate(columnName);
            else if (type == Integer.class || type == int.class)
                return rs.getInt(columnName);
            else if (type == Time.class)
                return rs.getTime(columnName);
            else if (type == Timestamp.class)
                return rs.getTimestamp(columnName);
            else if (type == Float.class || type == float.class)
                return rs.getFloat(columnName);
            else if (type == Double.class || type == double.class)
                return rs.getDouble(columnName);
            else if (type == String.class)
                return rs.getString(columnName);
            else throw new IllegalStateException("Unknown data type: " + type);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> Function<ResultSet, T> buildMapper(Class<?> entityClass) {
        Mapper<T> mapper = new Mapper<>(entityClass);
        return mapper.build();
    }
}
