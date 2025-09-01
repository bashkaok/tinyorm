package com.jisj.tinyorm;

import com.jisj.tinyorm.annotation.CrudDdl;
import com.jisj.tinyorm.annotation.ResultMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Annotated class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="tblEntityAnnotated")
@CrudDdl(createTableSql = "CREATE TABLE %s (RecID BIGINT UNIQUE, name VARCHAR(255))",
insertSql = "INSERT INTO %s (RecID, name) VALUES (?,?)")
public class TestEntityAnnotated {
    @Id
    @Column(name = "RecID")
    private long id;
    private String name;

    @ResultMapper
    static TestEntityAnnotated map(ResultSet rs) throws SQLException {
        return TestEntityAnnotated.builder()
                .id(rs.getLong(1))
                .name(rs.getString(2))
                .build();
    }
}
