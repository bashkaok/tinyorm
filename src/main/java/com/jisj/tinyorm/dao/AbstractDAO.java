package com.jisj.tinyorm.dao;

import com.jisj.tinyorm.Mapper;
import com.jisj.tinyorm.utils.Jdbc;

import java.sql.*;

/**
 * Abstract DAO class
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
abstract class AbstractDAO<T, ID> {
    /**
     * Default SELECT record query. Parameters: {@code tableName, idColumnName}
     */
    protected static final String SELECT_BY_ID_SQL = "SELECT * FROM %s WHERE %s=?";
    /**
     * Default DELETE record query. Parameters: {@code tableName, idColumnName}
     */
    protected static final String DELETE_BY_ID_SQL = "DELETE FROM %s WHERE %s=?";
    /**
     * Mapper {@code ResultSet → entity<T>}
     */
    protected Mapper<T> mapper;
    /**
     * DAO table name
     */
    protected String tableName;
    /**
     * Entity ID column name
     */
    protected String idColumnName;
    /**
     * Select SQL query
     */
    protected String selectStatement;
    /**
     * Insert SQL query
     */
    protected String insertStatement;
    /**
     * Update SQL query
     */
    protected String updateStatement;
    /**
     * Create table SQl query
     */
    protected String createTableStatement;
    /**
     * Delete SQL query
     */
    protected String deleteStatement;

    /**
     * Sets the default select statement {@link AbstractDAO#SELECT_BY_ID_SQL} from {@link AbstractDAO#tableName} and {@link AbstractDAO#idColumnName}
     */
    protected void setDefaultSelectStatement() {
        this.selectStatement = SELECT_BY_ID_SQL.formatted(tableName, idColumnName);
    }

    /**
     * Sets the delete statement {@link AbstractDAO#DELETE_BY_ID_SQL} from {@link AbstractDAO#tableName} and {@link AbstractDAO#idColumnName}
     */
    protected void setDefaultDeleteStatement() {
        this.deleteStatement = DELETE_BY_ID_SQL.formatted(tableName, idColumnName);
    }

    /**
     * Sets the mapper to DAO
     *
     * @param mapper function gets the {@code ResultSet} and returns the entity
     * @see com.jisj.tinyorm.annotation.ResultMapper ResultMapper
     */
    public void setMapper(Mapper<T> mapper) {
        this.mapper = mapper;
    }

    /**
     * Gives the current mapper
     *
     * @return current mapper
     */
    public Mapper<T> getMapper() {
        return mapper;
    }

    /**
     * Gives the table name
     *
     * @return table name
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Creates the table from specified create operator with connection
     * @param con connection
     * @param createStatement create table SQL query
     * @throws SQLException any database exception
     */
    protected static void createTable(Connection con, String createStatement) throws SQLException {
        try (var st = con.prepareStatement(createStatement)){
            st.execute();
        }
    }

    /**
     * Deletes the record with specified connection
     *
     * @param con connection
     * @param id  record ID
     * @return 1 if the record deleted
     * @throws SQLException any DB Exception
     */
    protected int delete(Connection con, ID id) throws SQLException {
        try (var st = con.prepareStatement(deleteStatement)) {
            Jdbc.setParameters(st, id);
            return st.executeUpdate();
        }
    }


}
