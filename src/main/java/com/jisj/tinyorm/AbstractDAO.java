package com.jisj.tinyorm;

import java.sql.*;
import java.util.function.Function;

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
    protected Function<ResultSet, T> mapper;
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
    public void setMapper(Function<ResultSet, T> mapper) {
        this.mapper = mapper;
    }

    /**
     * Gives the current mapper
     *
     * @return current mapper
     */
    public Function<ResultSet, T> getMapper() {
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
            setParameters(st, id);
            return st.executeUpdate();
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
}
