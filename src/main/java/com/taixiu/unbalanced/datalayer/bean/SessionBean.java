package com.taixiu.unbalanced.datalayer.bean;

import com.nhb.common.db.beans.AbstractMongoBean;
import com.taixiu.unbalanced.datalayer.statics.F;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Getter
@Setter
public class SessionBean extends AbstractMongoBean {

    private static final long serialVersionUID = 6309562058791155628L;

    private long id;
    private int dice1;
    private int dice2;
    private int dice3;
    private boolean ended;
    private long endTime;
    private String result;
    private String md5;
    private double resultAsDouble;

    private String roundId;

    private long startTime;
    private boolean refunded;
    private boolean failed;

    private String resultGenerationStrategy;

    private String externalRandomResultStatus;
    private String externalRandomResultRequest;
    private String externalRandomResultError;
    private String externalRandomResultDetails;
    private Long externalRandomResultMaintenanceAt;
    private Long externalRandomResultMaintenanceDuration;

    private Object customData;

    public SessionBean readDocument(Document doc) {
        this.setId(doc.getLong("id"));
        this.setObjectId(doc.getObjectId(F._ID));
        this.setRoundId(doc.getString(F.ROUND_ID));
        readDice(doc);
        this.setStartTime(doc.getLong("startTime"));
        this.setEnded(doc.getBoolean("ended"));
        this.setRefunded(doc.getBoolean("refunded"));
        if (doc.containsKey("failed")) {
            this.setFailed(doc.getBoolean("failed"));
        }
        this.setEndTime(doc.getLong("endTime"));
        this.setResult(doc.getString("result"));
        this.setMd5(doc.getString("md5"));

        Double resultAsDouble = doc.getDouble("resultAsDouble");
        if (resultAsDouble != null) {
            this.setResultAsDouble(resultAsDouble);
        }
        if (doc.containsKey(F.CUSTOM_DATA)) {
            this.customData = doc.get(F.CUSTOM_DATA);
        }

        if (doc.containsKey(F.RESULT_GENERATION_STRATEGY)) {
            resultGenerationStrategy = doc.getString(F.RESULT_GENERATION_STRATEGY);
        }

        if (doc.containsKey(F.EXTERNAL_RANDOM_RESULT_STATUS)) {
            externalRandomResultStatus = doc.getString(F.EXTERNAL_RANDOM_RESULT_STATUS);
        }

        if (doc.containsKey(F.EXTERNAL_RANDOM_RESULT_REQUEST)) {
            externalRandomResultRequest = doc.getString(F.EXTERNAL_RANDOM_RESULT_REQUEST);
        }

        if (doc.containsKey(F.EXTERNAL_RANDOM_RESULT_ERROR)) {
            externalRandomResultError = doc.getString(F.EXTERNAL_RANDOM_RESULT_ERROR);
        }

        if (doc.containsKey(F.EXTERNAL_RANDOM_RESULT_DETAILS)) {
            externalRandomResultDetails = doc.getString(F.EXTERNAL_RANDOM_RESULT_DETAILS);
        }

        if (doc.containsKey(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_AT)) {
            externalRandomResultMaintenanceAt = doc.getLong(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_AT);
        }

        if (doc.containsKey(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_DURATION)) {
            externalRandomResultMaintenanceAt = doc.getLong(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_DURATION);
        }

        return this;
    }

    protected void readDice(Document doc) {
        this.setDice1(doc.getInteger("dice1"));
        this.setDice2(doc.getInteger("dice2"));
        this.setDice3(doc.getInteger("dice3"));
    }

    public Document toEndSessionDocument() {
        Document doc = new Document();
        doc.put("id", this.getId());
        toDices(doc);
        doc.put("ended", this.isEnded());
        doc.put("endTime", this.getEndTime());
        doc.put("result", this.getResult());
        doc.put("md5", this.getMd5());

        if (getResultAsDouble() > 0) {
            doc.put("resultAsDouble", this.getResultAsDouble());
        }
        doc.put(F.ROUND_ID, this.roundId);

        if (this.customData != null) {
            doc.put(F.CUSTOM_DATA, customData);
        }
        doc.put(F.RESULT_GENERATION_STRATEGY, resultGenerationStrategy);
        doc.put(F.EXTERNAL_RANDOM_RESULT_STATUS, externalRandomResultStatus);
        doc.put(F.EXTERNAL_RANDOM_RESULT_REQUEST, externalRandomResultRequest);
        doc.put(F.EXTERNAL_RANDOM_RESULT_ERROR, externalRandomResultError);
        doc.put(F.EXTERNAL_RANDOM_RESULT_DETAILS, externalRandomResultDetails);
        doc.put(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_AT, externalRandomResultMaintenanceAt);
        doc.put(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_DURATION, externalRandomResultMaintenanceDuration);
        return doc;
    }

    protected void toDices(Document doc) {
        doc.put("dice1", this.getDice1());
        doc.put("dice2", this.getDice2());
        doc.put("dice3", this.getDice3());
    }

    public void writeDocument(final Document doc) {
        doc.put("id", this.getId());
        toDices(doc);
        doc.put("startTime", this.startTime);
        doc.put("ended", this.isEnded());
        doc.put("failed", this.isFailed());
        doc.put("refunded", this.isRefunded());
        doc.put("endTime", this.getEndTime());
        doc.put("result", this.getResult());
        doc.put("md5", this.getMd5());
        doc.put("resultAsDouble", this.getResultAsDouble());

        doc.put(F.ROUND_ID, this.roundId);

        if (customData != null) {
            doc.put(F.CUSTOM_DATA, this.customData);
        }
        doc.put(F.RESULT_GENERATION_STRATEGY, resultGenerationStrategy);
        doc.put(F.EXTERNAL_RANDOM_RESULT_STATUS, externalRandomResultStatus);
        doc.put(F.EXTERNAL_RANDOM_RESULT_REQUEST, externalRandomResultRequest);
        doc.put(F.EXTERNAL_RANDOM_RESULT_ERROR, externalRandomResultError);
        doc.put(F.EXTERNAL_RANDOM_RESULT_DETAILS, externalRandomResultDetails);
        doc.put(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_AT, externalRandomResultMaintenanceAt);
        doc.put(F.EXTERNAL_RANDOM_RESULT_MAINTENANCE_DURATION, externalRandomResultMaintenanceDuration);
    }

    public Document toDocument() {
        Document doc = new Document();
        this.writeDocument(doc);
        return doc;
    }
}
