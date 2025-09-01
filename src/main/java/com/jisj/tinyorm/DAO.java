package com.jisj.tinyorm;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * CRUD DAO interface
 * <p>Entity type {@code <T>} supports next annotations:</p>
 * {@code jakarta.persistence.Table:}
 * <ul>
 *     <li>name - table name</li>
 *     <li>options - DDL create operator</li>
 * </ul>
 * {@code jakarta.persistence.Id} <br>
 * {@code jakarta.persistence.Column:}
 * <ul>
 *      <li>name - column name</li>
 * </ul>
 * {@link com.jisj.tinyorm.annotation.CaseSensitive} <br>
 * {@link com.jisj.tinyorm.annotation.CrudDdl} <br>
 * {@link com.jisj.tinyorm.annotation.ResultMapper} <br>
 * @param <T> entity type
 * @param <ID> identifier type
 */
public interface DAO<T, ID> {

    /**
     * Create new record
     * @param entity entity instance
     * @return created records count
     * @throws SQLException record constraints, any database exceptions
     */
    int insert(T entity) throws SQLException;

    /**
     * Read the entity by ID
     *
     * @param id entity ID
     * @return entity or {@code null}
     */
    Optional<T> getById(ID id);

    /**
     * Reads all records from the table
     *
     * @return stream of entities
     */
    Stream<T> getAll();

    /**
     * Updates the specified entity
     *
     * @param entity entity for update
     * @return updated count
     * @throws SQLException when record already exists, any database exception
     */
    int update(T entity) throws SQLException;

    /**
     * Delete the record with specified ID
     * @param id deleting entity ID
     * @return deleted count
     * @throws SQLException any database exception
     */
    int delete(ID id) throws SQLException;

    /**
     * Finds all records under conditions
     * <p>{@code = findAll("name=? AND status=?", "table", 34}</p>
     *
     * @param whereClause search conditions for WHERE clause
     * @param args        argument values
     * @return entity list or empty list
     * @throws SQLException any SQL exception
     */
    List<T> find(String whereClause, Object... args) throws SQLException;

    /**
     * Performs random select SQL query to DB. In SELECT clause should be pointed all fields of the queried entity
     *
     * @param sqlQuery plane SQL with params
     * @param args     params list
     * @return Entity list
     * @throws SQLException          SQL errors
     * @throws IllegalStateException when wrong SQL, wrong parameters number
     */
    List<T> query(String sqlQuery, Object... args) throws SQLException;

    /**
     * Creates the table from entity {@code @Table.options} annotation
     * @throws IllegalStateException when annotation @Table.options() is empty
     * @throws SQLException any SQL exception
     */
    void createTable() throws SQLException;

    /**
     * Creates the table from specified create operator
     * @param ddl SQL create operator
     * @throws SQLException any SQL exception
     */
    void createTable(String ddl) throws SQLException;

    /**
     * Drops the table if it exists
     * @throws SQLException any SQL exception
     */
    void dropTable() throws SQLException;

}
