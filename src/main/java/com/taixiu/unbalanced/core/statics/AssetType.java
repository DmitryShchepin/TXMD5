package com.taixiu.unbalanced.core.statics;

public enum AssetType {
   GOLD(1, "gold"),
   CHIP(2, "chip"),
   GUARRANTEED_GOLD(3, "guarranteed_gold"),
   GUARRANTEED_CHIP(4, "guarranteed_chip");

   private int id;
   private String name;

   private AssetType(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public static AssetType fromId(int assetId) {
      AssetType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         AssetType type = var1[var3];
         if (type.getId() == assetId) {
            return type;
         }
      }

      return null;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }
}
