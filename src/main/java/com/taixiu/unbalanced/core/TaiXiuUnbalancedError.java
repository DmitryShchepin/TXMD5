package com.taixiu.unbalanced.core;

import lombok.Getter;

@Getter
public enum TaiXiuUnbalancedError {
   SESSION_INVALID(2000, "Phiên đã kết thúc hoặc không hợp lệ"),
   BETTING_TIME_OUT(2001, "Đã hết thời gian đặt cược"),
   BETTING_INVALID(2002, "Tiền cược không hợp lệ"),
   BALANCE_NOT_ENOUGH(2003, "Lỗi khi thực hiện số dư"),
   USER_WAS_LOCKED_IN_GAME(2004, "Bạn bị khóa chơi game Tài Xỉu"),
   RANDOM_RESULT_GENERATION_ERROR(2005, "Lỗi khi tạo kết quả ngẫu nhiên");

   private final int code;
   private final String message;

   TaiXiuUnbalancedError(int code, String message) {
      this.code = code;
      this.message = message;
   }
}
