package com.jisj.tinyorm;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static com.jisj.tinyorm.EntityHelper.formatBy;
import static com.jisj.tinyorm.EntityHelper.getCreateTableStatement;

/**
 * Join DAO for by one field joining
 *
 * @param <ID> join table ID type
 * @param <J>  join field type
 * @param <I>  inverse join field type
 */
public class SimpleJoinDAO<ID, J, I> extends BaseDAO<SimpleJoinEntity<ID, J, I>, ID> {
    private static final String SELECT_SQL = "SELECT %s, %s, %s FROM %s WHERE %s=?";
    private static final String INSERT_SQL = "INSERT INTO %s (%s, %s, %s) VALUES(?,?,?)";
    private static final String UPDATE_SQL = "UPDATE %s SET %s=?, %s=? WHERE %s=?";
    private final String joinColumnName;
    private final String inverseColumnName;

    /**
     * Create the new SimpleJoinDAO instance
     *
     * @param dataSource DataSource
     * @param tableName join table name
     * @param idColumnName ID column name
     * @param joinColumnName column name of master table
     * @param inverseColumnName column name of joining table
     */
    @SuppressWarnings("unchecked")
    public SimpleJoinDAO(DataSource dataSource,
                         String tableName,
                         String idColumnName,
                         String joinColumnName,
                         String inverseColumnName) {
        super(dataSource, SimpleJoinEntity.class);

        this.tableName = tableName;
        this.idColumnName = idColumnName;
        this.joinColumnName = joinColumnName;
        this.inverseColumnName = inverseColumnName;
        this.insertStatement = INSERT_SQL.formatted(tableName, idColumnName, joinColumnName, inverseColumnName);
        this.updateStatement = UPDATE_SQL.formatted(this.tableName, joinColumnName, inverseColumnName, idColumnName);
        this.selectStatement = SELECT_SQL.formatted(
                idColumnName,
                joinColumnName,
                inverseColumnName,
                tableName,
                idColumnName);
        setDefaultDeleteStatement();
        this.createTableStatement = formatBy(getCreateTableStatement(this.getClass()), tableName);

        mapper = rs ->
        {
            try {
                return new SimpleJoinEntity<>((ID) rs.getObject(1),
                        (J) rs.getObject(2),
                        (I) rs.getObject(3));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

    }
    /**
     * Insert the record to join table
     *
     * @param id                 ID value
     * @param joinColumnValue    join column value
     * @param inverseColumnValue inverse join column value
     * @throws SQLException any DB exception
     */
    public void insert(ID id, J joinColumnValue, I inverseColumnValue) throws SQLException {
        insert(new SimpleJoinEntity<>(id, joinColumnValue, inverseColumnValue));
    }

    /**
     * Gives the inverse join IDs by specified master join table value
     * @param joinColumnValue master join table value
     * @return inverse join IDs
     * @throws SQLException any DB exceptions
     */
    public List<I> getInverseJoinIds(J joinColumnValue) throws SQLException {
        return find("%s=?".formatted(joinColumnName), joinColumnValue).stream()
                .map(SimpleJoinEntity::getInverseColumn)
                .toList();
    }

    /**
     * Gives the master join IDs by specified inverse join table value
     * @param inverseColColumnValue inverse join table value
     * @return master join IDs
     * @throws SQLException any DB exceptions
     */
    public List<J> getJoinIds(I inverseColColumnValue) throws SQLException {
        return find("%s=?".formatted(inverseColumnName), inverseColColumnValue).stream()
                .map(SimpleJoinEntity::getJoinColumn)
                .toList();
    }


}


