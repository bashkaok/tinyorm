package com.jisj.tinyorm.dao;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static com.jisj.tinyorm.dao.EntityHelper.*;
import static com.jisj.utils.StringMage.wrapInQuotes;

/**
 * Table profile class
 * <p>Supports next annotations:</p>
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
 */
final class Profile {
    final Class<?> clazz;
    EntityHelper.TableAnnotation annotationTable;
    private boolean isCaseSensitive = false;
    private String idColumnName;
    private Field idField;
    String insertSQLQuery = "";
    String updateSQLQuery = "";
    String createTableQuery = "";

    /**
     * Create new profile for the specified entity class
     *
     * @param clazz entity class
     */
    Profile(Class<?> clazz) {
        this.clazz = clazz;
        buildProfile();
    }

    Field getIdField() {
        return idField;
    }

    /**
     * Gives table name
     *
     * @return annotated table name or class name
     */
    String getTableName() {
        return isCaseSensitive ? wrapInQuotes(annotationTable.name()) : annotationTable.name();
    }

    String getIdColumnName() {
        return isCaseSensitive ? wrapInQuotes(idColumnName) : idColumnName;
    }

    private void buildProfile() {
        isCaseSensitive = isCaseSensitive(clazz);
        annotationTable = getTableAnnotation(clazz);
        createTableQuery = formatBy(getCreateTableStatement(clazz), getTableName());
        insertSQLQuery = formatBy(getInsertRecordStatement(clazz), getTableName());
        updateSQLQuery = formatBy(getUpdateRecordStatement(clazz), getTableName());

        idField = EntityHelper.getIdField(clazz);
        idColumnName = getColumnName(idField);

    }

    private String assertInsertSQL(String sql) {
        //TODO check the count of insertable fields in the entity and SQL
        return sql.contains("%s") ? sql.formatted(getTableName()) : sql;
    }

    private String assertUpdateSQL(String sql) {
        //TODO check the count updatable fields in the entity and SQL
        return sql.contains("%s") ? sql.formatted(getTableName()) : sql;
    }


    Object[] getInsertableFieldValues(Object entityInstance) {
        return getInsertableFields(clazz)
                .map(field -> getFieldValue(field, entityInstance))
                .toArray();
    }

    Object[] getUpdatableFieldValues(Object entityInstance) {
        return Stream.concat(
                        getUpdatableFields(clazz)
                                .map(field -> getFieldValue(field, entityInstance)),
                        Stream.of(getFieldValue(idField, entityInstance)))
                .toArray();
    }
}
