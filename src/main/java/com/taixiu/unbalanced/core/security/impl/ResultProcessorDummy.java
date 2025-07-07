package com.taixiu.unbalanced.core.security.impl;

import com.taixiu.unbalanced.core.security.ResultProcessor;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingContext;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingResult;
import lombok.Data;

@Data
public class ResultProcessorDummy implements ResultProcessor {

    private String resultAsText;
    private String encryptedResult;

    @Override
    public boolean enableEncryption() {
        return false;
    }

    @Override
    public void handle(TaiXiuUnbalancedShakingContext context, TaiXiuUnbalancedShakingResult result) {
        // Do nothing
    }

    @Override
    public void clear() {
        resultAsText = null;
        encryptedResult = null;
    }
}