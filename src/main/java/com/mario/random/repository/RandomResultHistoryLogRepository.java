package com.mario.random.repository;

import com.mario.random.entity.RandomResultHistoryLog;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.nhb.common.db.models.AbstractMongoModel;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RandomResultHistoryLogRepository extends AbstractMongoModel {

    private static final String DB_NAME = "random_service";

    private String collectionName;

    public RandomResultHistoryLogRepository() {
        super();
        log.debug("Creating random result history repo");
    }

    public RandomResultHistoryLogRepository(String collectionName) {
        super();
        log.debug("Creating random result history repo: {}", collectionName);
        this.collectionName = collectionName;
    }

    public MongoCollection<Document> getCollection() {
        return getMongoClient().getDatabase(DB_NAME).getCollection(collectionName);
    }

    public void create(RandomResultHistoryLog historyLog) {
        log.debug("Creating history log: {}", historyLog);
        try {
            Document document = new Document()
                    .append(Fields.STUDIO_ID, historyLog.getStudioId())
                    .append(Fields.GAME_ID, historyLog.getGameId())
                    .append(Fields.GAME_NAME, historyLog.getGameName())
                    .append(Fields.SESSION_ID, historyLog.getSessionId())
                    .append(Fields.REQUEST, historyLog.getRequest())
                    .append(Fields.RESULT, historyLog.getResult())
                    .append(Fields.RESULT_HASH, historyLog.getResultHash())
                    .append(Fields.RESULT_MD5, historyLog.getResultMD5())
                    .append(Fields.DETAIL, historyLog.getDetail())
                    .append(Fields.GAME_TYPE, historyLog.getGameType())
                    .append(Fields.STUDIO_GAME_RESULT, historyLog.getStudioGameResult())
                    .append(Fields.IS_MAINTENANCE_MODE, historyLog.isMaintenanceMode())
                    .append(Fields.MAINTAIN_AT, historyLog.getMaintainAt())
                    .append(Fields.MAINTAIN_DURATION, historyLog.getMaintainDuration())
                    .append(Fields.ERROR_MESSAGE, historyLog.getErrorMessage())
                    .append(Fields.CREATED_AT, historyLog.getCreatedAt())
                    .append(Fields.UPDATED_AT, historyLog.getUpdatedAt())
                    .append(Fields.STATUS, historyLog.getStatus().name());

            getCollection().insertOne(document);
            log.debug("Created history log: {}", document.getObjectId(Fields.ID));
        } catch (Exception e) {
            log.error("Error creating history log: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(String id, RandomResultHistoryLog historyLog) {
        try {
            getCollection().updateOne(
                    Filters.eq(Fields.ID, id),
                    Updates.combine(
                            Updates.set(Fields.RESULT, historyLog.getResult()),
                            Updates.set(Fields.RESULT_HASH, historyLog.getResultHash()),
                            Updates.set(Fields.RESULT_MD5, historyLog.getResultMD5()),
                            Updates.set(Fields.STUDIO_GAME_RESULT, historyLog.getStudioGameResult()),
                            Updates.set(Fields.IS_MAINTENANCE_MODE, historyLog.isMaintenanceMode()),
                            Updates.set(Fields.MAINTAIN_AT, historyLog.getMaintainAt()),
                            Updates.set(Fields.MAINTAIN_DURATION, historyLog.getMaintainDuration()),
                            Updates.set(Fields.DETAIL, historyLog.getDetail()),
                            Updates.set(Fields.ERROR_MESSAGE, historyLog.getErrorMessage()),
                            Updates.set(Fields.UPDATED_AT, historyLog.getUpdatedAt()),
                            Updates.set(Fields.STATUS, historyLog.getStatus() != null ? historyLog.getStatus().name() : null),

                            Updates.set(Fields.SESSION_STARTS_AT, historyLog.getSessionStartsAt()),
                            Updates.set(Fields.SESSION_ENDS_AT, historyLog.getSessionEndsAt()),
                            Updates.set(Fields.ENDED, historyLog.getEnded()),
                            Updates.set(Fields.ROUND_ID, historyLog.getRoundId())
                    )
            );
            log.debug("Updated history log: {}", id);
        } catch (Exception e) {
            log.error("Error updating history log: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(String sessionId, Map<String, Object> updates) {
        try {
            List<Bson> updatesList = new ArrayList<>();

            updates.forEach((field, value) -> {
                if (Fields.isValidField(field)) {
                    updatesList.add(Updates.set(field, value));
                } else {
                    log.warn("Skipping invalid field: {}", field);
                }
            });

            if (!updatesList.isEmpty()) {
                getCollection().updateMany(
                        Filters.eq(Fields.SESSION_ID, sessionId),
                        Updates.combine(updatesList)
                );
                log.debug("Updated history logs for sessionId: {}", sessionId);
            } else {
                log.warn("No valid fields to update for sessionId: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error updating history logs by sessionId: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<RandomResultHistoryLog> getHistoryLogs(Integer limit, Date startDate, Date endDate, boolean descending) {
        try {
            List<Bson> filters = new ArrayList<>();

            // Filter by date range if provided
            if (startDate != null && endDate != null) {
                filters.add(Filters.and(Filters.gte(Fields.CREATED_AT, startDate), Filters.lte(Fields.CREATED_AT, endDate)));
            } else if (startDate != null) {
                filters.add(Filters.gte(Fields.CREATED_AT, startDate));
            } else if (endDate != null) {
                filters.add(Filters.lte(Fields.CREATED_AT, endDate));
            }

            // Combine filters if necessary
            Bson finalFilter = filters.isEmpty() ? new Document() : Filters.and(filters);

            // Build the query
            FindIterable<Document> iterable = getCollection()
                    .find(finalFilter)
                    .sort(descending ? Sorts.descending(Fields.CREATED_AT) : Sorts.ascending(Fields.CREATED_AT));

            // Apply limit if provided
            if (limit != null && limit > 0) {
                iterable = iterable.limit(limit);
            }

            // Convert documents to objects
            List<RandomResultHistoryLog> logs = new ArrayList<>();
            for (Document doc : iterable) {
                logs.add(documentToObject(doc));
            }

            return logs;
        } catch (Exception e) {
            log.error("Error retrieving history: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    public RandomResultHistoryLog findById(String id) {
        try {
            Document doc = getCollection().find(Filters.eq(Fields.ID, id)).first();
            return documentToObject(doc);
        } catch (Exception e) {
            log.error("Error finding history log: {}", e.getMessage(), e);
            return null;
        }
    }

    public RandomResultHistoryLog findBySessionId(String sessionId) {
        try {
            return documentToObject(getCollection().find(Filters.eq(Fields.SESSION_ID, sessionId)).first());
        } catch (Exception e) {
            log.error("Error finding history logs by gameId: {}", e.getMessage(), e);
        }
        return null;
    }

    private RandomResultHistoryLog documentToObject(Document doc) {
        if (doc == null) {
            return null;
        }

        try {
            return RandomResultHistoryLog.builder()
                    .id(doc.getObjectId(Fields.ID).toString())
                    .studioId(doc.getString(Fields.STUDIO_ID))
                    .gameId(doc.getString(Fields.GAME_ID))
                    .gameName(doc.getString(Fields.GAME_NAME))
                    .sessionId(doc.getString(Fields.SESSION_ID))
                    .request(doc.getString(Fields.REQUEST))
                    .result(doc.getString(Fields.RESULT))
                    .resultHash(doc.getString(Fields.RESULT_HASH))
                    .resultMD5(doc.getString(Fields.RESULT_MD5))
                    .gameType(doc.getString(Fields.GAME_TYPE))
                    .detail(doc.getString(Fields.DETAIL))
                    .studioGameResult(doc.getLong(Fields.STUDIO_GAME_RESULT))
                    .isMaintenanceMode(doc.getBoolean(Fields.IS_MAINTENANCE_MODE, false))
                    .maintainAt(doc.getLong(Fields.MAINTAIN_AT))
                    .maintainDuration(doc.getLong(Fields.MAINTAIN_DURATION))
                    .errorMessage(doc.getString(Fields.ERROR_MESSAGE))
                    .createdAt(doc.getDate(Fields.CREATED_AT))
                    .updatedAt(doc.getDate(Fields.UPDATED_AT))
                    .status(RandomResultHistoryLog.Status.valueOf(doc.getString(Fields.STATUS)))

                    .sessionStartsAt(doc.getLong(Fields.SESSION_STARTS_AT))
                    .sessionEndsAt(doc.getLong(Fields.SESSION_ENDS_AT))
                    .ended(doc.getBoolean(Fields.ENDED))
                    .roundId(doc.getString(Fields.ROUND_ID))

                    .build();
        } catch (Exception e) {
            log.debug("Document to object mapping failed: {}", doc, e);
            return null;
        }
    }

    public void setCollectionFromGameName(String gameName) {
        if (gameName == null) {
            log.warn("Game name is null, using 'default' as fallback");
            collectionName = "default";
            return;
        }
        collectionName = gameName.trim()
                .toLowerCase()
                .replaceAll("\\s+", "");
    }

    public static class Fields {

        public static final String ID = "_id";
        public static final String STUDIO_ID = "studioId";
        public static final String GAME_ID = "gameId";
        public static final String GAME_NAME = "gameName";
        public static final String SESSION_ID = "sessionId";
        public static final String REQUEST = "request";
        public static final String DETAIL = "detail";
        public static final String RESULT = "result";
        public static final String RESULT_HASH = "resultHash";
        public static final String RESULT_MD5 = "resultMD5";
        public static final String GAME_TYPE = "gameType";
        public static final String STUDIO_GAME_RESULT = "studioGameResult";
        public static final String IS_MAINTENANCE_MODE = "isMaintenanceMode";
        public static final String MAINTAIN_AT = "maintainAt";
        public static final String MAINTAIN_DURATION = "maintainDuration";
        public static final String ERROR_MESSAGE = "errorMessage";
        public static final String CREATED_AT = "createdAt";
        public static final String UPDATED_AT = "updatedAt";
        public static final String STATUS = "status";

        public static final String SESSION_STARTS_AT = "sessionStartsAt";
        public static final String SESSION_ENDS_AT = "sessionEndsAt";
        public static final String ENDED = "ended";
        public static final String ROUND_ID = "roundId";

        static final Set<String> VALID_FIELDS = new HashSet<>();

        static {
            for (Field field : Fields.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    try {
                        field.setAccessible(true); // Access private fields if needed
                        VALID_FIELDS.add(String.valueOf(field.get(null)));
                    } catch (IllegalAccessException e) {
                        log.debug("Error adding field to valid fields set: {}", e.getMessage(), e);
                    }
                }
            }
        }

        static boolean isValidField(String fieldName) {
            return VALID_FIELDS.contains(fieldName);
        }
    }
}
