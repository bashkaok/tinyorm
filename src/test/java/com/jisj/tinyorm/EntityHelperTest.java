package com.jisj.tinyorm;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EntityHelperTest {

    @Test
    void getFieldValue() {
    }

    @Test
    void getGetter() throws NoSuchFieldException, NoSuchMethodException {
        Field idField = TestEntityDefault.class.getDeclaredField("id");
        System.out.println(idField.getDeclaringClass().getMethod("getId"));
        assertEquals("getId", EntityHelper.getGetter(TestEntityDefault.class.getDeclaredField("id")).getName());
    }

    @Test
    void getMapper() {

        assertThrowsExactly(IllegalStateException.class,
                () -> EntityHelper.getMapper(TestEntityMapperAnnotation.WrongReturn.class));

        assertThrowsExactly(IllegalStateException.class,
                () -> EntityHelper.getMapper(TestEntityMapperAnnotation.WrongParameters0.class));

        assertThrowsExactly(IllegalStateException.class,
                () -> EntityHelper.getMapper(TestEntityMapperAnnotation.WrongParameters2.class));

        assertThrowsExactly(IllegalStateException.class,
                () -> EntityHelper.getMapper(TestEntityMapperAnnotation.WrongParameterType.class));

        assertThrowsExactly(IllegalStateException.class,
                () -> EntityHelper.getMapper(TestEntityMapperAnnotation.NonStatic.class));

        assertDoesNotThrow(() -> EntityHelper.getMapper(TestEntityMapperAnnotation.CorrectMapper.class));
    }

}