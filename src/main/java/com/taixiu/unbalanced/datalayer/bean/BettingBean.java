package com.taixiu.unbalanced.datalayer.bean;

import com.nhb.common.db.beans.AbstractMongoBean;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BettingBean extends AbstractMongoBean {
   private static final long serialVersionUID = 7585507995139890810L;
   private String bettingId;
   private long sessionId;
   private String userId;
   private String displayName;
   private List<EntryBetting> bettings;
   private long betting;
   private long winMoney;
   private long income;
   private long createdTime;

   private Double resultAsDouble;
   private Double autoResultAsDouble;
   private Boolean isAutoBetting;
   public void writeDocument(Document doc) {
      doc.put("bettingId", this.bettingId);
      doc.put("sessionId", this.sessionId);
      doc.put("userId", this.userId);
      doc.put("displayName", this.displayName);
      if (this.bettings != null) {
         doc.put("bettings", this.bettings.stream().map(EntryBetting::toDocument).collect(Collectors.toList()));
      }

      doc.put("betting", this.betting);
      doc.put("winMoney", this.winMoney);
      doc.put("income", this.income);
      doc.put("createdTime", this.createdTime);
      doc.put("createdAt", new Date());
      if (resultAsDouble != null) {
         doc.put("resultAsDouble", resultAsDouble);
      }
      if (autoResultAsDouble != null) {
         doc.put("autoResultAsDouble", autoResultAsDouble);
      }
      if (isAutoBetting != null) {
         doc.put("isAutoBetting", isAutoBetting);
      }
   }

   public BettingBean readDocument(Document doc) {
      this.bettingId = doc.getString("bettingId");
      this.sessionId = doc.getLong("sessionId");
      this.userId = doc.getString("userId");
      this.displayName = doc.getString("displayName");
      if (doc.containsKey("bettings")) {
         this.bettings = (List) ((List) doc.get("bettings"))
                 .stream()
                 .map((x) -> (new EntryBetting()).readDocument((Document) x))
                 .collect(Collectors.toList());
      }

      if (doc.containsKey("betting")) {
         this.betting = doc.getLong("betting");
      }
      this.winMoney = doc.getLong("winMoney");
      this.income = doc.getLong("income");
      this.createdTime = doc.getLong("createdTime");
      if (doc.containsKey("resultAsDouble")) {
         this.resultAsDouble = doc.getDouble("resultAsDouble");
      }
      if (doc.containsKey("autoResultAsDouble")) {
         this.autoResultAsDouble = doc.getDouble("autoResultAsDouble");
      }
      if (doc.containsKey("isAutoBetting")) {
         this.isAutoBetting = doc.getBoolean("isAutoBetting");
      }
      return this;
   }

   @Override
   public Document toDocument() {
      Document document = new Document();
      this.writeDocument(document);
      return document;
   }

   public String getBettingId() {
      return this.bettingId;
   }

   public long getSessionId() {
      return this.sessionId;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public List<EntryBetting> getBettings() {
      return this.bettings;
   }

   public long getWinMoney() {
      return this.winMoney;
   }

   public long getBetting() {
      return betting;
   }
   public long getIncome() {
      return this.income;
   }

   public long getCreatedTime() {
      return this.createdTime;
   }

   public void setBettingId(String bettingId) {
      this.bettingId = bettingId;
   }

   public void setSessionId(long sessionId) {
      this.sessionId = sessionId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public void setBettings(List<EntryBetting> bettings) {
      this.bettings = bettings;
   }

   public void setWinMoney(long winMoney) {
      this.winMoney = winMoney;
   }

   public void setBetting(long betting) {
      this.betting = betting;
   }
   public void setIncome(long income) {
      this.income = income;
   }

   public void setCreatedTime(long createdTime) {
      this.createdTime = createdTime;
   }
   public Double getResultAsDouble() {
      return resultAsDouble;
   }

   public void setResultAsDouble(Double resultAsDouble) {
      this.resultAsDouble = resultAsDouble;
   }

   public Boolean getAutoBetting() {
      return isAutoBetting;
   }

   public void setAutoBetting(Boolean autoBetting) {
      isAutoBetting = autoBetting;
   }

   public Double getAutoResultAsDouble() {
      return autoResultAsDouble;
   }

   public void setAutoResultAsDouble(Double autoResultAsDouble) {
      this.autoResultAsDouble = autoResultAsDouble;
   }
}
