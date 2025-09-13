package com.jisj.tinyorm.dao;

import jakarta.persistence.Id;

/**
 * Inner join table entity class
 *
 * @param <ID> join table unique key type
 * @param <J>  join field type
 * @param <I>  inverse join field type
 */
@SuppressWarnings({"LombokGetterMayBeUsed"})
public class SimpleJoinEntity<ID, J, I> {
    @Id
    private ID id;
    private J joinColumn;
    private I inverseColumn;

    SimpleJoinEntity() {
    }

    SimpleJoinEntity(ID id, J joinColumnValue, I inverseColumnValue) {
        this.id = id;
        this.joinColumn = joinColumnValue;
        this.inverseColumn = inverseColumnValue;
    }

    /**
     * Gives the record ID
     * @return record ID
     */
    public ID getId() {
        return id;
    }

    @SuppressWarnings({"unused"})
    void setId(ID id) {
        this.id = id;
    }

    /**
     * Gives the master table join column value
     * @return join column value
     */
    public J getJoinColumn() {
        return joinColumn;
    }

    @SuppressWarnings("unused")
    void setJoinColumn(J joinColumn) {
        this.joinColumn = joinColumn;
    }

    /**
     * Gives the joining table join column value
     * @return joining table join column value
     */
    public I getInverseColumn() {
        return inverseColumn;
    }

    @SuppressWarnings("unused")
    void setInverseColumn(I inverseColumn) {
        this.inverseColumn = inverseColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SimpleJoinEntity<?, ?, ?> that = (SimpleJoinEntity<?, ?, ?>) o;
        return getId().equals(that.getId()) && getJoinColumn().equals(that.getJoinColumn()) && getInverseColumn().equals(that.getInverseColumn());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getJoinColumn().hashCode();
        result = 31 * result + getInverseColumn().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleJoinEntity{" +
                "id=" + id +
                ", joinColumn=" + joinColumn +
                ", inverseColumn=" + inverseColumn +
                '}';
    }
}
