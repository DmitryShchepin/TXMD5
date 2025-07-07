package com.taixiu.unbalanced.datalayer.bean;

import org.bson.Document;

public class EntryBetting {
   private int entryId;
   private long value;
   private long createdTime;

   public EntryBetting() {
   }

   public EntryBetting(int entryId, long value) {
      this.entryId = entryId;
      this.value = value;
   }

   public EntryBetting(int entryId, long value, long createdTime) {
      this.entryId = entryId;
      this.value = value;
      this.createdTime = createdTime;
   }

   public Document toDocument() {
      Document doc = new Document();
      this.writeDocument(doc);
      return doc;
   }

   public void writeDocument(Document doc) {
      doc.put("entryId", this.entryId);
      doc.put("value", this.value);
      doc.put("createdTime", this.createdTime);
   }

   public EntryBetting readDocument(Document doc) {
      this.entryId = doc.getInteger("entryId");
      this.value = doc.getLong("value");
      if (doc.containsKey("createdTime") && doc.get("createdTime") != null) {
         this.createdTime = doc.getLong("createdTime");
      }
      return this;
   }

   public int getEntryId() {
      return this.entryId;
   }

   public long getValue() {
      return this.value;
   }

   public void setEntryId(int entryId) {
      this.entryId = entryId;
   }

   public void setValue(long value) {
      this.value = value;
   }

   public long getCreatedTime() {
      return createdTime;
   }

   public void setCreatedTime(long createdTime) {
      this.createdTime = createdTime;
   }
}
