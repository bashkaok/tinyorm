package com.jisj.tinyorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base operations for an entity or BaseDAO subclasses
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudDdl {
    /**
     * Record create SQL query, e.g: "INSERT INTO %s (id, name) VALUES(?,?)" or "INSERT INTO tblName (id, name) VALUES(?,?)"
     * <p>Fields order in the SQL query and entity MUST BE same</p>
     * @return create SQL query
     */
    String insertSql() default "";
    /**
     * Record update SQL query, e.g: "UPDATE %s SET UUID=?, name = ?" or "UPDATE tblName SET UUID=?, name = ?"
     * <p>Fields order in the SQL query and entity MUST BE same</p>
     * @return update SQL query
     */
    String updateSql() default "";

    /**
     * Create table operator, e.g.:"CREATE TABLE %s (id BIGINT UNIQUE, name VARCHAR(255))" or "CREATE TABLE tblName (id BIGINT UNIQUE, name VARCHAR(255))"
     * <p>SQL query is saved to protected field {@code AbstractDAO.createTableStatement}</p>
     *
     * @return create table SQL query
     */
    String createTableSql() default "";
}
