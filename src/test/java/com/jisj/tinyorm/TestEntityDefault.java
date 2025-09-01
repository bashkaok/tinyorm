package com.jisj.tinyorm;

import com.jisj.tinyorm.annotation.CrudDdl;
import jakarta.persistence.Id;
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
@CrudDdl(createTableSql = "CREATE TABLE %s (id BIGINT UNIQUE, name VARCHAR(255))",
        insertSql = "INSERT INTO %s (id, name) VALUES (?,?)",
        updateSql = "UPDATE %s SET name = ?")
public class TestEntityDefault {
    @Id
    private long id;
    private String name;

//    @ResultMapper
    private static TestEntityDefault map(ResultSet rs) throws SQLException {
        return TestEntityDefault.builder()
                .id(rs.getLong(1))
                .name(rs.getString(2))
                .build();
    }
}
