package com.jisj.tinyorm.dao;

import com.jisj.tinyorm.annotation.ResultMapper;
import jakarta.persistence.Id;

import java.sql.ResultSet;

@SuppressWarnings("ALL")
public class TestEntityMapperAnnotation {

    static class WrongReturn {
        @Id
        int id;

        @ResultMapper
        void map(ResultSet rs) {

        }
    }

    static class WrongParameters0 {
        @Id
        int id;

        @ResultMapper
        WrongParameters0 map() {
            return null;
        }
    }

    static class WrongParameters2 {
        @Id
        int id;

        @ResultMapper
        WrongParameters2 map(ResultSet rs, String s) {
            return null;
        }
    }

    static class WrongParameterType {
        @Id
        int id;

        @ResultMapper
        WrongParameterType map(String s) {
            return null;
        }
    }

    static class NonStatic {
        @Id
        int id;

        @ResultMapper
        TestEntityMapperAnnotation.NonStatic map(ResultSet rs) {
            return null;
        }
    }


    static class CorrectMapper {
        @Id
        int id;

        @ResultMapper
        static CorrectMapper map(ResultSet rs) {
            return null;
        }
    }


}
