package com.jisj.tinyorm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEntityMapper {
    private long longF = 100L;
    private Long longFL = 200L;
    private boolean boolF = true;
    private Boolean boolFB = true;
    private Date dateF = new Date(System.currentTimeMillis());
    private Timestamp timestampF = new Timestamp(System.currentTimeMillis());
    private Time timeF = new Time(System.currentTimeMillis());
    private int intF = 1000;
    private Integer intFI = 2000;
    private float floatF = 0.001F;
    private Float floatFF = 1.001F;
    private double doubleF = 200;
    private Double doubleFD = 300D;
    private String stringF = "string";

}

/*
        for (Object p : params) {
        switch (p) {
        case null -> st.setNull(pos, 0);
                case Long longP -> st.setLong(pos, longP);
                case Boolean boolP -> st.setBoolean(pos, boolP);
                case Date dateP -> st.setDate(pos, dateP);
                case Integer intP -> st.setInt(pos, intP);
                case Timestamp timeP -> st.setTimestamp(pos, timeP);
                case Float floatP -> st.setFloat(pos, floatP);
                case byte[] bytesP -> st.setBytes(pos, bytesP);
default -> st.setObject(pos, p);
  */
