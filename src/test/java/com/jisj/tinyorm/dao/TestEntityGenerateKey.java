package com.jisj.tinyorm.dao;

import com.jisj.tinyorm.annotation.CrudDdl;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class with by DB generated key
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CrudDdl(createTableSql = "CREATE TABLE %s (id INTEGER UNIQUE, name VARCHAR(255), PRIMARY KEY(id AUTOINCREMENT))",
        insertSql = "INSERT INTO %s (id, name) VALUES (?,?)",
        updateSql = "UPDATE %s SET name = ?")
public class TestEntityGenerateKey {
    @Id
    private Integer id;
    @Column(unique = true)
    private String name;
}
