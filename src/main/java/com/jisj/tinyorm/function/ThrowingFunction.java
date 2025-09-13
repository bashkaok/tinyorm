package com.jisj.tinyorm.function;

/**
 * Function interface with throwing exception
 *
 * @param <T> accept parameter type
 * @param <R> return type
 * @param <E> exception type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    /**
     * Applies this function to the given argument
     *
     * @param t the function argument
     * @return the function result
     * @throws E specified exception
     */
    R apply(T t) throws E;
}
