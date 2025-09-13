package com.jisj.tinyorm;

import com.jisj.tinyorm.function.ThrowingFunction;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map function from {@code ResultSet} row to specified object
 * @param <T> result object type
 */
@FunctionalInterface
    public interface Mapper<T> extends ThrowingFunction<ResultSet, T, SQLException> {
    }