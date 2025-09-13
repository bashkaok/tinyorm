package com.jisj.tinyorm.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {
    private static final Profile pa = new Profile(TestEntityAnnotated.class);
    private static final Profile pd = new Profile(TestEntityDefault.class);
    private static final Profile pg = new Profile(TestEntityAnnotatedPG.class);


    @Test
    void tableAnnotation() {
        //annotatedPG
        assertEquals("\"tblEntityAnnotatedPG\"", pg.getTableName());
        //annotated
        assertEquals("tblEntityAnnotated", pa.getTableName());
        //default
        assertEquals("TestEntityDefault", pd.getTableName());
    }

    @Test
    void crudAnnotation() {
        System.out.println(pd.createTableQuery);
    }

    @Test
    void idAnnotation() {
        //annotatedPG
        assertEquals("\"RecID\"", pg.getIdColumnName());
        //annotated
        assertEquals("RecID", pa.getIdColumnName());
        //default
        assertEquals("recId", pd.getIdColumnName());
    }
}