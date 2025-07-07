package com.taixiu.unbalanced.core.security;

import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingContext;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingResult;

public interface ResultProcessor {

    boolean enableEncryption();

    void handle(TaiXiuUnbalancedShakingContext context, TaiXiuUnbalancedShakingResult result);

    String getResultAsText();

    String getEncryptedResult();

    void setResultAsText(String resultAsText);

    void setEncryptedResult(String encryptedResult);

    void clear();
}
