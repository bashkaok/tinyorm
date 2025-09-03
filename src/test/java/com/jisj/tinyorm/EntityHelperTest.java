package com.jisj.tinyorm;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EntityHelperTest {

    @Test
    void getFieldValue() {
    }

    @Test
    void getGetter() throws NoSuchFieldException, NoSuchMethodException {
        Field idField = TestEntityDefault.class.getDeclaredField("recId");
        System.out.println(idField.getDeclaringClass().getMethod("getRecId"));
        assertEquals("getRecId", EntityHelper.getGetter(TestEntityDefault.class.getDeclaredField("recId")).getName());
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

    @Test
    void newInstance() {
        Arrays.stream((SimpleJoinEntity.class.getDeclaredConstructors()))
                .forEach(System.out::println);
    }

    @Test
    void genericFields() {
        SimpleJoinEntity<Integer, Integer, Integer> e = new SimpleJoinEntity<>();
        Arrays.stream(e.getClass().getDeclaredFields())
                .forEach(f-> System.out.println(f.toGenericString()));
    }


}