package com.jisj.tinyorm;

import com.jisj.tinyorm.utils.Jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Query object
 */
public class Query<T> {
    private final DataSource dataSource;
    private final Mapper<T> mapper;
    private final String query;
    private final Object[] args;

    /**
     * Create new query instance
     * @param dataSource database DataSource
     * @param mapper ResultSet → result mapper
     * @param query SQL query
     * @param args query parameters
     */
    public Query(DataSource dataSource,
                 Mapper<T> mapper,
                 String query,
                 Object... args) {
        this.dataSource = dataSource;
        this.mapper = mapper;
        this.query = query;
        this.args = args;
    }

    /**
     * Execs the query with result to list
     *
     * @return result list
     */
    public List<T> toList() {
        return execToStream().toList();
    }

    /**
     * Execs the query with result to stream
     *
     * @return result stream
     */
    public Stream<T> toStream() {
        return execToStream();
    }

    private Stream<T> execToStream() {
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement st = con.prepareStatement(query);
            Jdbc.setParameters(st, args);
            return Jdbc.execToStream(st, mapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
