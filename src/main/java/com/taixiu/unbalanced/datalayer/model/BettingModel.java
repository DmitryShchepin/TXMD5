package com.taixiu.unbalanced.datalayer.model;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.nhb.common.data.PuObject;
import com.taixiu.unbalanced.datalayer.bean.BettingBean;
import org.bson.Document;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BettingModel extends AbstractBettingMongoModel {

   public void indexing() {
      this.createDatabaseIndexes(this.getCollection(), new Document("bettingId", 1), true);
      this.createDatabaseIndexes(this.getCollection(), new Document("sessionId", 1), false);
      this.createDatabaseIndexes(this.getCollection(), new Document("displayName", 1), false);
      this.createDatabaseIndexes(this.getCollection(), new Document("userId", 1), false);
      this.createDatabaseIndexes(this.getCollection(), new Document("createdAt", 1), 2592000L);
   }

   public MongoCollection<Document> getCollection() {
      return this.getMongoClient().getDatabase(getDbName()).getCollection("betting");
   }

   public void upsert(BettingBean bean) {
      this.getCollection().updateOne(new Document("bettingId", bean.getBettingId()), new Document("$set", bean.toDocument()), (new UpdateOptions()).upsert(true));
   }

   public void update(Collection<BettingBean> beans) {
      List<WriteModel<Document>> updates = new ArrayList();

       for (BettingBean bettingBean : beans) {
           Document where = new Document("bettingId", bettingBean.getBettingId());
           Document document = bettingBean.toDocument();
           Document set = new Document("$set", document);
           updates.add(new UpdateOneModel<>(where, set, (new UpdateOptions()).upsert(true)));
       }

      if (!updates.isEmpty()) {
         BulkWriteResult bulkWrite = this.getCollection().bulkWrite(updates);
         this.getLogger().info("update {} bettings, modified count: {}", beans.size(), bulkWrite.getModifiedCount());
      }

   }

   public Collection<BettingBean> findBySessionId(long sessionId) {
      FindIterable<Document> found = this.getCollection().find(new Document("sessionId", sessionId));
      List<BettingBean> bettings = new ArrayList();
      MongoCursor var5 = found.iterator();

      while(var5.hasNext()) {
         Document document = (Document)var5.next();
         bettings.add((new BettingBean()).readDocument(document));
      }

      return bettings;
   }

   public Collection<BettingBean> findByUserId(String userId, int limit, int skip) {
      FindIterable<Document> found = this.getCollection().find(new Document("userId", userId)).limit(limit).skip(skip).sort(new Document("_id", -1));
      List<BettingBean> bettings = new ArrayList();
      MongoCursor var6 = found.iterator();

      while(var6.hasNext()) {
         Document document = (Document)var6.next();
         bettings.add((new BettingBean()).readDocument(document));
      }

      return bettings;
   }

   public long countByUserId(String userId) {
      return this.getCollection().count(new Document("userId", userId));
   }

   public long getTotalBettingByUserId(String userId, int assetId, long fromTime) {
      return 0L;
   }

   public List<BettingBean> filter(Document where, int limit, int skip) {
      FindIterable<Document> found = this.getCollection().find(where).limit(limit).skip(skip);
      List<BettingBean> bettings = new ArrayList();
      MongoCursor var6 = found.iterator();

      while(var6.hasNext()) {
         Document document = (Document)var6.next();
         bettings.add((new BettingBean()).readDocument(document));
      }

      return bettings;
   }

   public long count(Document where) {
      return this.getCollection().count(where);
   }

   public Aggregate aggregate(Document where) {
      List<Document> pipeline = new ArrayList();
      pipeline.add(new Document("$match", where));
      pipeline.add(new Document("$group", (new Document("_id", "$userId")).append("totalBetting", new Document("$sum", "$betting")).append("count", new Document("$sum", 1))));
      AggregateIterable<Document> result = this.getCollection().aggregate(pipeline);
      Aggregate aggregate = new Aggregate();
      long totalBetting = 0L;
      int totalUser = 0;

      for(MongoCursor var8 = result.iterator(); var8.hasNext(); ++totalUser) {
         Document doc = (Document)var8.next();
         totalBetting += doc.getLong("totalBetting");
      }

      aggregate.setTotalBetting(totalBetting);
      aggregate.setTotalUser(totalUser);
      return aggregate;
   }

   public List<TopUser> fetchTopUser(long from, long to, int limit) {
      List<Document> pipeline = new ArrayList();
      pipeline.add(new Document("$match", new Document("createdTime", (new Document("$gte", from)).append("$lte", to))));
      pipeline.add(new Document("$group", (new Document("_id", "$displayName")).append("totalIncome", new Document("$sum", "$income"))));
      pipeline.add(new Document("$sort", new Document("totalIncome", -1)));
      pipeline.add(new Document("$limit", limit));
      AggregateIterable<Document> result = this.getCollection().aggregate(pipeline);
      List<TopUser> topUsers = new ArrayList();
      MongoCursor var9 = result.iterator();

      while(var9.hasNext()) {
         Document document = (Document)var9.next();
         topUsers.add(new TopUser(document.getString("_id"), document.getLong("totalIncome")));
      }

      return topUsers;
   }

   public static class TopUser {
      private String displayName;
      private long income;

      public Document toDocument() {
         Document doc = new Document();
         doc.put("displayName", this.displayName);
         doc.put("income", this.income);
         return doc;
      }

      public PuObject toPuObject() {
         PuObject puo = new PuObject();
         puo.setString("displayName", this.displayName);
         puo.setLong("income", this.income);
         return puo;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public long getIncome() {
         return this.income;
      }

      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      public void setIncome(long income) {
         this.income = income;
      }

      @ConstructorProperties({"displayName", "income"})
      public TopUser(String displayName, long income) {
         this.displayName = displayName;
         this.income = income;
      }
   }

   public static class Aggregate {
      private int totalUser;
      private long totalBetting;

      public int getTotalUser() {
         return this.totalUser;
      }

      public long getTotalBetting() {
         return this.totalBetting;
      }

      public void setTotalUser(int totalUser) {
         this.totalUser = totalUser;
      }

      public void setTotalBetting(long totalBetting) {
         this.totalBetting = totalBetting;
      }
   }
}
