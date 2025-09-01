package com.jisj.tinyorm;

import com.jisj.tinyorm.annotation.CaseSensitive;
import com.jisj.tinyorm.annotation.ResultMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Annotated class for PG case-sensitive names
 */
@Data
@Builder
@Table(name="tblEntityAnnotatedPG")
@CaseSensitive
public class TestEntityAnnotatedPG {
    @Id
    @Column(name = "RecID")
    private long id;
    private String name;

    @ResultMapper
    static public TestEntityAnnotatedPG map(ResultSet rs) throws SQLException {
        return TestEntityAnnotatedPG.builder()
                .id(rs.getLong(1))
                .name(rs.getString(2))
                .build();
    }
}
