package com.jisj.tinyorm.dao;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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

    @Test
    void getCreateTableStatement() {
        assertEquals("CREATE TABLE IF NOT EXISTS %s", EntityHelper.getCreateTableStatement(TestEntityHelper.Jakarta.class));
        assertEquals("CREATE TABLE %s", EntityHelper.getCreateTableStatement(TestEntityHelper.Native.class));
    }

    @Test
    void getInsertRecordStatement() {
        assertEquals("INSERT INTO %s (name) VALUES(?)", EntityHelper.getInsertRecordStatement(TestEntityHelper.Native.class));
    }

    @Test
    void getUpdateRecordStatement() {
        assertEquals("UPDATE %s SET name=?", EntityHelper.getUpdateRecordStatement(TestEntityHelper.Native.class));
    }

    @Test
    void getFields() {
        List<String> nonTransient = EntityHelper.getFields(TestEntityDefault.class)
                .map(Field::getName)
                .toList();
        List.of("transientJakarta", "finalField", "transientNative")
                .forEach(f-> assertFalse(nonTransient.contains(f)));
    }


}