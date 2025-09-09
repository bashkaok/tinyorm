package com.jisj.tinyorm;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.jisj.tinyorm.EntityHelper.*;

/**
 * Implementation of {@code DAO<T,ID>}
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public class BaseDAO<T, ID> extends AbstractDAO<T, ID> implements DAO<T, ID> {
    private static final Logger log = Logger.getLogger(BaseDAO.class.getName());
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
        createTableStatement = profile.createTableQuery.isEmpty() ?
                formatBy(getCreateTableStatement(this.getClass()), tableName) :
                profile.createTableQuery;
        insertStatement = profile.insertSQLQuery.isEmpty() ?
                formatBy(getInsertRecordStatement(this.getClass()), tableName) :
                profile.insertSQLQuery;
        updateStatement = profile.updateSQLQuery.isEmpty() ?
                formatBy(getUpdateRecordStatement(this.getClass()), tableName) :
                profile.updateSQLQuery;
        updateStatement = updateStatement.isEmpty() ? "" : updateStatement + " WHERE %s=?".formatted(idColumnName);
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

    @Override
    public ID create(T entity) throws SQLException {
        assertEntity(entity);
        if (insertStatement.isEmpty())
            throw new IllegalStateException("Insert record SQL statement not found in %s or DAO %s"
                    .formatted(profile.clazz, this.getClass()));
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(insertStatement)) {
            setParameters(st, profile.getInsertableFieldValues(entity));
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            //noinspection unchecked
            return rs.next() ? (ID) rs.getObject(1) : null;
        }
    }

    private void assertEntity(T entity) {
        if (entity == null)
            throw new IllegalArgumentException("Unexpected entity value <null>");
    }

    @Override
    public int update(T entity) throws SQLException {
        if (EntityHelper.getFieldValue(profile.getIdField(), entity) == null)
            throw new IllegalStateException("ID field <%s> is null in %s".formatted(profile.getIdField().getName(), entity));

        assertEntity(entity);
        if (updateStatement.isEmpty())
            throw new IllegalStateException("Update SQL query not found in entity %s or DAO %s \n"
                    .formatted(profile.clazz, this.getClass()) +
                    "Use @CrudDdl.updateSql() for entity or DAO class, or AbstractDAO.updateStatement");
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
        if (createTableStatement.isEmpty())
            throw new IllegalStateException(("DDL create options are empty in table <%s> \n" +
                    "Use @CrudDdl.createTableSql() | @Table.options() annotations | AbstractDAO.createTableStatement")
                    .formatted(getTableName()));
        createTable(createTableStatement);
    }

    @Override
    public void createTable(String ddl) throws SQLException {
        try (var con = dataSource.getConnection()) {
            createTable(con, ddl);
            log.fine("Table <%s> is created".formatted(getTableName()));
        }
    }

    @Override
    public void dropTable() throws SQLException {
        final String SQL = "DROP TABLE IF EXISTS %s".formatted(getTableName());
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement(SQL)) {
            st.execute();
            log.fine("Table <%s> dropped".formatted(getTableName()));
        }
    }


}
