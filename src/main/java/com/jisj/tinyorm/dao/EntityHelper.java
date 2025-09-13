package com.jisj.tinyorm.dao;

import com.jisj.tinyorm.Mapper;
import com.jisj.tinyorm.annotation.CaseSensitive;
import com.jisj.tinyorm.annotation.CrudDdl;
import com.jisj.tinyorm.annotation.ResultMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static com.jisj.utils.StringMage.capitalize;

/**
 * Helper class for an entity parsing
 */
class EntityHelper {

    /**
     * Gives the field value
     *
     * @param field          field
     * @param entityInstance object instance
     * @return instance field value
     */
    static Object getFieldValue(Field field, Object entityInstance) {
        try {
            return getGetter(field).invoke(entityInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static Method getGetter(Field field) {
        try {
            return field.getType() == boolean.class ?
                    field.getDeclaringClass().getDeclaredMethod("is" + capitalize(field.getName())) :
                    field.getDeclaringClass().getDeclaredMethod("get" + capitalize(field.getName()));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Getter for <" + field.getName() + "> not found");
        }
    }

    static Method getSetter(Field field) {
        try {
            return field.getDeclaringClass()
                    .getDeclaredMethod("set" + capitalize(field.getName()), getGetter(field).getReturnType());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Setter for <" + field.getName() + "> not found");
        }
    }


    static Optional<Annotation> getAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(clazz.getAnnotation(annotationClass));
    }

    /**
     * Checks the entity class on  {@code @CaseSensitive} annotation
     *
     * @param clazz entity class
     * @return {@code true} if the class is annotated and {@code @CaseSensitive.caseSensitive() == true}
     */
    static boolean isCaseSensitive(Class<?> clazz) {
        return getAnnotation(clazz, CaseSensitive.class)
                .map(CaseSensitive.class::cast)
                .map(CaseSensitive::caseSensitive)
                .orElse(false);
    }

    static TableAnnotation getTableAnnotation(Class<?> clazz) {
        return getAnnotation(clazz, Table.class)
                .map(Table.class::cast)
                .map(a -> TableAnnotation.getProxy(a, clazz))
                .orElseGet(() -> TableAnnotation.getDefault(clazz));
    }

    /**
     * Gives the ID field from specified entity class
     *
     * @param clazz entity class
     * @return ID field
     * @throws IllegalStateException if {@code @Id} annotation not found
     */
    static Field getIdField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Id.class) != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ID annotation not found in " + clazz));
    }

    /**
     * Gives the DB column name for the specified field
     *
     * @param field entity field
     * @return name of field or name from {@code @Column annotation}
     */
    static String getColumnName(Field field) {
        return field.getAnnotation(Column.class) == null ? field.getName() :
                field.getAnnotation(Column.class).name().isEmpty() ? field.getName() :
                        field.getAnnotation(Column.class).name();
    }

    /**
     * Finds the annotated by {@code @ResultMapper} method in the specified entity class or builds default mapper
     *
     * @param clazz entity class
     * @return mapper function
     */
    static <R> Mapper<R> getMapper(Class<?> clazz) {
        return findMapper(clazz)
                .map(method -> assertMapper(method, clazz))
                .map(method -> {
                    method.setAccessible(true);
                    return method;
                })
                .map(method -> (Mapper<R>) resultSet -> {
                    try {
//                        noinspection unchecked
                        return (R) method.invoke(null, resultSet);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(() -> EntityMapper.buildMapper(clazz));
    }

    /**
     * Finds the annotated by {@code @ResultMapper} method in the specified entity class
     *
     * @param clazz entity class
     * @return Optional mapper method
     */
    static Optional<Method> findMapper(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotations().length > 0)
                .filter(method -> Arrays.stream(method.getDeclaredAnnotations())
                        .anyMatch(an -> an.annotationType() == ResultMapper.class))
                .findFirst();
    }

    static Method assertMapper(Method mapper, Class<?> returnType) {
        if (mapper.getReturnType() != returnType)
            throw new IllegalStateException("Bad mapper return type: " + mapper.getReturnType() + " - expected: " + returnType);
        if (mapper.getParameterTypes().length != 1)
            throw new IllegalStateException("Bad mapper parameter count: " + mapper.getParameterTypes().length + " - expected: 1 ");
        if (mapper.getParameterTypes()[0] != ResultSet.class)
            throw new IllegalStateException("Bad mapper parameter type: " + mapper.getParameterTypes()[0] + " - expected: " + ResultSet.class);
        if (!Modifier.isStatic(mapper.getModifiers()))
            throw new IllegalStateException("Mapper method must be a static: <" + mapper.getName() + "> in " + returnType);
        return mapper;
    }

    /**
     * Creates new instance of the specified entity class
     *
     * @param clazz entity class
     * @return new instance
     * @throws IllegalStateException if default constructor not found
     */
    static Object newInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Default constructor not found in " + clazz);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives a creation table statement from annotations {@code @CrudDdl and @Table}
     *
     * @param clazz entity or BaseDAO subclass
     * @return SQL string or empty String
     */
    static String getCreateTableStatement(Class<?> clazz) {
        return getAnnotation(clazz, CrudDdl.class)
                .map(CrudDdl.class::cast)
                .map(CrudDdl::createTableSql)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> getAnnotation(clazz, Table.class)
                        .map(Table.class::cast)
                        .map(Table::options)
                        .filter(s -> !s.isEmpty())
                        .orElse("")
                );
    }

    /**
     * Gives the insert record statement from annotations {@code @CrudDdl}
     *
     * @param clazz entity or BaseDAO subclass
     * @return SQL string or empty String
     */
    static String getInsertRecordStatement(Class<?> clazz) {
        return getAnnotation(clazz, CrudDdl.class)
                .map(CrudDdl.class::cast)
                .map(CrudDdl::insertSql)
                .orElse("");
    }

    /**
     * Gives the update record statement from annotations {@code @CrudDdl}
     *
     * @param clazz entity or BaseDAO subclass
     * @return SQL string or empty String
     */
    static String getUpdateRecordStatement(Class<?> clazz) {
        return getAnnotation(clazz, CrudDdl.class)
                .map(CrudDdl.class::cast)
                .map(CrudDdl::updateSql)
                .orElse("");
    }


    /**
     * Formats the specified string by specified parameters if string contains format sign '%'
     *
     * @param str    string for format
     * @param params format parameters
     * @return formatted string or initial string
     */
    static String formatBy(String str, Object... params) {
        return str.contains("%") ? str.formatted(params) : str;
    }

    /**
     * Gives the legal entity fields - excluded FINAL, TRANSIENT and {@code @Transient} annotated
     *
     * @param clazz entity class
     * @return stream of fields
     */
    static Stream<Field> getFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(EntityHelper::isNotTransient);
    }

    private static boolean isNotTransient(Field field) {
        return !field.accessFlags().contains(AccessFlag.FINAL) &&
                !field.accessFlags().contains(AccessFlag.TRANSIENT) &&
                field.getAnnotation(Transient.class) == null;
    }

    static Stream<Field> getInsertableFields(Class<?> clazz) {
        //TODO add the options from @Column
        return getFields(clazz);
    }

    static Stream<Field> getUpdatableFields(Class<?> clazz) {
        //TODO add the options from @Column
        return getFields(clazz)
                .filter(field -> !field.equals(getIdField(clazz))); //TODO remove
    }


    /**
     * Proxy class for {@code jakarta.persistence.Table}
     *
     * @param name
     * @param options
     */
    record TableAnnotation(String name, String options) {
        static TableAnnotation getProxy(Table table, Class<?> clazz) {
            return new TableAnnotation(
                    table.name().isEmpty() ? clazz.getSimpleName() : table.name(),
                    table.options());
        }

        static TableAnnotation getDefault(Class<?> clazz) {
            return new TableAnnotation(clazz.getSimpleName(), "");
        }

    }
}
