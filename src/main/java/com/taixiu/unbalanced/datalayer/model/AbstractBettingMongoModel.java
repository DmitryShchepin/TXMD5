package com.taixiu.unbalanced.datalayer.model;

import com.nhb.common.db.models.AbstractMongoModel;

public abstract class AbstractBettingMongoModel extends AbstractMongoModel {

    private String dbName = "taixiu_unbalanced";

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return this.dbName;
    }
}
