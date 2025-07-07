package com.taixiu.unbalanced.core.betting;

public enum BettingEntry {
   TAI(1, "Tài"),
   XIU(2, "Xỉu");

   private int id;
   private String name;

   BettingEntry(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public static BettingEntry fromId(int id) {
      BettingEntry[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         BettingEntry entry = var1[var3];
         if (entry.getId() == id) {
            return entry;
         }
      }

      return null;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public boolean isBig() {
      return TAI == this;
   }
}
