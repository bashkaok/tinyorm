package com.jisj.tinyorm;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@code DAO<T,ID>}
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public class BaseDAO<T, ID> extends AbstractDAO<T, ID> implements DAO<T, ID> {
    /**
     * Current datasource object
     */
    protected final DataSource dataSource;
    final Profile profile;

    /**
     * Create the new DAO instance
     *
     * @param dataSource DataSource
     * @param clazz      entity class
     */
    public BaseDAO(DataSource dataSource, Class<?> clazz) {
        this.dataSource = dataSource;
        this.profile = new Profile(clazz);
        tableName = profile.getTableName();
        idColumnName = profile.getIdColumnName();
        setMapper(EntityHelper.getMapper(clazz));
        setDefaultSelectStatement();
        setDefaultDeleteStatement();
        insertStatement = profile.insertSQLQuery;
        updateStatement = profile.updateSQLQuery + " WHERE %s=?".formatted(idColumnName);
    }

    @Override
    public int insert(T entity) throws SQLException {
        assertEntity(entity);
        if (insertStatement.isEmpty())
            throw new IllegalStateException("Insert SQL query not found in " + profile.clazz);
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(insertStatement)) {
            setParameters(st, profile.getInsertableFieldValues(entity));
            return st.executeUpdate();
        }
    }

    private void assertEntity(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Unexpected entity value <null>");
    }

    @Override
    public int update(T entity) throws SQLException {
        assertEntity(entity);
        if (updateStatement.isEmpty())
            throw new IllegalStateException("Update SQL query not found in " + profile.clazz);
        final String SQL = updateStatement;
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            setParameters(st, profile.getUpdatableFieldValues(entity));
            return st.executeUpdate();
        }
    }

    @Override
    public Optional<T> getById(ID id) {
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(selectStatement)) {
            setParameters(st, id);
            ResultSet rs = st.executeQuery();
            return rs.next() ? Optional.of(mapper.apply(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<T> getAll() {
        final String SQL = "SELECT * FROM %s".formatted(tableName);
        try {
            final Connection con = dataSource.getConnection();
            var st = con.createStatement();
            var rs = st.executeQuery(SQL);
            return StreamSupport.stream(new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.IMMUTABLE) {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    try {
                        if (rs.next()) {
                            action.accept(mapper.apply(rs));
                            return true;
                        }
                        st.close();
                        con.close();
                        return false;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int delete(ID id) throws SQLException {
        try (var con = dataSource.getConnection()) {
            return super.delete(con, id);
        }
    }

    @Override
    public List<T> find(String whereClause, Object... args) throws SQLException {
        if (whereClause == null || whereClause.isEmpty())
            throw new IllegalStateException("Where clause is empty or null");
        final String SQL = "SELECT * FROM %s WHERE %s".formatted(getTableName(), whereClause);
        return query(SQL, args);
    }

    @Override
    public List<T> query(String sqlQuery, Object... args) throws SQLException {
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(sqlQuery)) {
            setParameters(st, args);
            ResultSet rs = st.executeQuery();
            List<T> result = new ArrayList<>();
            while (rs.next())
                result.add(mapper.apply(rs));
            return result;
        }
    }

    @Override
    public void createTable() throws SQLException {
        final String ddlSql = profile.createTableQuery.isEmpty() ? profile.annotationTable.options() : profile.createTableQuery;
        if (ddlSql.isEmpty())
            throw new IllegalStateException("DDL create options are empty in " + profile.clazz +
                    "\nUse @CrudDdl.createTableSql() or @Table.options() annotations");
        createTable(ddlSql);
    }

    @Override
    public void createTable(String ddl) throws SQLException {
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(ddl)) {
            st.execute();
        }
    }

    @Override
    public void dropTable() throws SQLException {
        final String SQL = "DROP TABLE IF EXISTS %s".formatted(getTableName());
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            st.execute();
        }
    }


}
