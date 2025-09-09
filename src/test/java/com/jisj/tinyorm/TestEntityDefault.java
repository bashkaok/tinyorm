package com.jisj.tinyorm;

import com.jisj.tinyorm.annotation.CrudDdl;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
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
@CrudDdl(createTableSql = "CREATE TABLE %s (recId BIGINT UNIQUE, name VARCHAR(255))",
        insertSql = "INSERT INTO %s (recId, name) VALUES (?,?)",
        updateSql = "UPDATE %s SET name = ?")
public class TestEntityDefault {
    @Id
    private Long recId;
    @Column(unique = true)
    private String name;
    @Transient
    private int transientJakarta;
    private final int finalField = 0;
    private transient int transientNative;

//    @ResultMapper
    private static TestEntityDefault map(ResultSet rs) throws SQLException {
        return TestEntityDefault.builder()
                .recId(rs.getLong(1))
                .name(rs.getString(2))
                .build();
    }
}
