package com.jisj.tinyorm;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@code DAO<T,ID>}
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
public class BaseDAO<T, ID> implements DAO<T, ID> {
    /**
     * Current datasource object
     */
    protected final DataSource dataSource;
    final Profile profile;
    Function<ResultSet, T> mapper;

    /**
     * Create the new DAO instance
     *
     * @param dataSource DataSource
     * @param clazz      entity class
     */
    public BaseDAO(DataSource dataSource, Class<?> clazz) {
        this.dataSource = dataSource;
        this.profile = new Profile(clazz);
        setMapper(EntityHelper.getMapper(clazz));
    }

    /**
     * Gives the table name
     *
     * @return table name
     */
    protected String getTableName() {
        return profile.getTableName();
    }

    /**
     * Sets the mapper to DAO
     * @see com.jisj.tinyorm.annotation.ResultMapper ResultMapper
     */
    public void setMapper(Function<ResultSet, T> mapper) {
        this.mapper = mapper;
    }

    /**
     * Gives the current mapper
     * @return current mapper
     */
    public Function<ResultSet, T> getMapper() {
        return mapper;
    }

    @Override
    public int insert(T entity) throws SQLException {
        assertEntity(entity);
        if (profile.insertSQLQuery.isEmpty())
            throw new IllegalStateException("Insert SQL query not found in " + profile.clazz);
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(profile.insertSQLQuery)) {
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
        if (profile.updateSQLQuery.isEmpty())
            throw new IllegalStateException("Update SQL query not found in " + profile.clazz);
        final String SQL = profile.updateSQLQuery + " WHERE %s=?".formatted(profile.getIdColumnName());
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            setParameters(st, profile.getUpdatableFieldValues(entity));
            return st.executeUpdate();
        }
    }

    @Override
    public Optional<T> getById(ID id) {
        final String SQL = "SELECT * FROM %s WHERE %s=?".formatted(profile.getTableName(), profile.getIdColumnName());
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            setParameters(st, id);
            ResultSet rs = st.executeQuery();
            return rs.next() ? Optional.of(mapper.apply(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets specified parameters to {@code PreparedStatement} instance
     *
     * @param st     PreparedStatement instance
     * @param params query parameters
     * @throws SQLException if parameterIndex does not correspond to a parameter marker in the SQL statement; if a database access error occurs or this method is called on a closed {@code PreparedStatement}
     */
    protected static void setParameters(PreparedStatement st, Object... params) throws SQLException {
        int pos = 1;

        for (Object p : params) {
            switch (p) {
                case null -> st.setNull(pos, 0);
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

    @Override
    public Stream<T> getAll() {
        final String SQL = "SELECT * FROM %s".formatted(profile.getTableName());
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
        final String SQL = "DELETE FROM %s WHERE %s=?".formatted(profile.getTableName(), profile.getIdColumnName());
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            setParameters(st, id);
            return st.executeUpdate();
        }
    }

    @Override
    public List<T> find(String whereClause, Object... args) throws SQLException {
        if (whereClause == null || whereClause.isEmpty())
            throw new IllegalStateException("Where clause is empty or null");
        final String SQL = "SELECT * FROM %s WHERE %s".formatted(getTableName(), whereClause);
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            setParameters(st, args);
            ResultSet rs = st.executeQuery();
            List<T> result = new ArrayList<>();
            while (rs.next())
                result.add(mapper.apply(rs));
            return result;
        }
    }

    @Override
    public List<T> query(String sqlQuery, Object... args) {
        return List.of();
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
        final String SQL = "DROP TABLE IF EXISTS %s".formatted(profile.getTableName());
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            st.execute();
        }
    }


}
