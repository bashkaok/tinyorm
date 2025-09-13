package com.jisj.tinyorm.utils;

import com.jisj.tinyorm.Mapper;

import java.sql.*;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Static methods for JDBC
 */
public class Jdbc {
    private Jdbc(){}

    /**
     * Sets specified parameters to {@code PreparedStatement} instance
     *
     * @param st     PreparedStatement instance
     * @param params query parameters
     * @throws SQLException if parameterIndex does not correspond to a parameter marker in the SQL statement; if a database access error occurs or this method is called on a closed {@code PreparedStatement}
     */
    public static void setParameters(PreparedStatement st, Object... params) throws SQLException {
        if (st.getParameterMetaData().getParameterCount() != params.length)
            throw new IllegalStateException("Unexpected parameters count: %d - expected %d"
                    .formatted(params.length, st.getParameterMetaData().getParameterCount()));
        int pos = 1;

        for (Object p : params) {
            switch (p) {
                case null -> st.setNull(pos, Types.NULL);
                case Long longP -> st.setLong(pos, longP);
                case Boolean boolP -> st.setBoolean(pos, boolP);
                case Date dateP -> st.setDate(pos, dateP);
                case Integer intP -> st.setInt(pos, intP);
                case Timestamp timeP -> st.setTimestamp(pos, timeP);
                case Float floatP -> st.setFloat(pos, floatP);
                case byte[] bytesP -> st.setBytes(pos, bytesP);
                default -> st.setObject(pos, p);
            }
            pos++;
        }
    }

    /**
     * Execs the query to immutable stream. After processing the stream {@code ResultSet, Statement, Connection} will be closed
     * @param statement PreparedStatement with SQL query and filled parameters
     * @param mapper result mapper
     * @return specified result type
     * @param <T> result type
     * @throws SQLException if a database access error occurs or this method is called on a closed {@code PreparedStatement}
     */
    public static <T> Stream<T> execToStream(PreparedStatement statement, Mapper<T> mapper) throws SQLException {
            ResultSet rs = statement.executeQuery();
            return StreamSupport.stream(new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.IMMUTABLE) {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    try {
                        if (rs.next()) {
                            action.accept(mapper.apply(rs));
                            return true;
                        }
                        statement.getConnection().close();
                        statement.close();
                        return false;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, false);
    }
}
