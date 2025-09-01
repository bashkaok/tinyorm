package com.jisj.tinyorm.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for case-sensitive database table
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CaseSensitive {
    /**
     * Case-sensitive table names and field names in DB, ex. PostgresSQL
     * <p>If {@code true} - double quotes will be added to table and field names </p>
     */
    boolean caseSensitive() default true;
}
