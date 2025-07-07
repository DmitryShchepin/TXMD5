package com.taixiu.unbalanced.core.security.impl;

import com.taixiu.unbalanced.core.Md5Utils;
import com.taixiu.unbalanced.core.security.ResultProcessor;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingContext;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiGroup;
import net.fellbaum.jemoji.EmojiManager;
import org.apache.commons.lang.RandomStringUtils;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Getter
@Setter
@Slf4j
public class ResultProcessorMD5 implements ResultProcessor {

    private static final int DEFAULT_MD5_PASS_LENGTH = 100;
    private static final char[] allCharsArr = ("1234567890" +
            "qwertyuiopasdfghjklzxcvbnm" +
            "@#$%" +
            "QWERTYUIOPASDFGHJKLZXCVBNM").toCharArray();

    private final Random random = new SecureRandom();

    private int md5PassEmojiLength;
    private String resultAsText;
    private String encryptedResult; //MD5 result
    private boolean md5On;
    private int md5PassLength;
    private float adjustMd5;
    private Emoji[] allEmoji;

    public ResultProcessorMD5(boolean isMd5On, int md5PassLength, int md5PassEmojiLength, float adjustMd5) {

        this.md5On = isMd5On;
        this.adjustMd5 = adjustMd5;
        this.md5PassLength = md5PassLength;
        if (this.md5On && this.md5PassLength == 0) {
            this.md5PassLength = DEFAULT_MD5_PASS_LENGTH;
        }

        this.md5PassEmojiLength = md5PassEmojiLength;
        initEmoji();
        log.debug("ResultProcessorMD5 constructor end");
    }

    @Override
    public void clear() {
        resultAsText = null;
        encryptedResult = null;
    }

    @Override
    public boolean enableEncryption() {
        return md5On;
    }

    @Override
    public void handle(TaiXiuUnbalancedShakingContext context, TaiXiuUnbalancedShakingResult result) {

        if (md5PassLength == 0) {
            md5PassLength = DEFAULT_MD5_PASS_LENGTH;
        }

        int md5PassLengthRandomLength = this.md5PassLength - random.nextInt((int) (this.md5PassLength * adjustMd5));

        final String randomNumberFixedLength = RandomStringUtils.random(md5PassLengthRandomLength, 0, allCharsArr.length,
                false, false, allCharsArr, random);
        final String shakingResultText = "{" + result.writeToString() + "}";

        try {
            int randomDiceResultIndex = random.nextInt(randomNumberFixedLength.length() + 1);
            final String randomBeforeSession = RandomStringUtils.random(random.nextInt(10), true, false);

            StringBuilder sb = new StringBuilder();
            sb.append("#").append(randomBeforeSession).append(context.getSessionId()).append("_")
                    .append(randomNumberFixedLength, 0, randomDiceResultIndex)
                    .append(shakingResultText)
                    .append(randomNumberFixedLength.substring(randomDiceResultIndex));

            for (int i = 0; i < md5PassEmojiLength; i++) {
                sb.append(allEmoji[random.nextInt(allEmoji.length)].getEmoji());
            }
            resultAsText = sb.toString();
        } catch (Exception ex) {
            resultAsText = "#" + context.getSessionId() + "_" + shakingResultText + randomNumberFixedLength;
            log.debug("EXCEPTION: resultAsText: {}, error: {}", resultAsText, ex);
        }
        encryptedResult = Md5Utils.getMd5(resultAsText);
        log.debug("RESULT: resultAsText: {}, resultMd5: {}", resultAsText, encryptedResult);
    }

    private void initEmoji() {
        List<Emoji> allEmoji = new LinkedList<>();

        allEmoji.addAll(EmojiManager.getAllEmojisByGroup(EmojiGroup.TRAVEL_AND_PLACES));
        allEmoji.addAll(EmojiManager.getAllEmojisByGroup(EmojiGroup.SMILEYS_AND_EMOTION));
        allEmoji.addAll(EmojiManager.getAllEmojisByGroup(EmojiGroup.FOOD_AND_DRINK));

        String removeEmoji = "\uD83E\uDEE1,\uD83E\uDEE2,\uD83D\uDEDD,\uD83D\uDEDE," +
                "\uD83E\uDEE8,\uD83E\uDEE4,\uD83E\uDE77,\uD83E\uDEE2,\uD83D\uDEDF," +
                "\uD83E\uDE75,\uD83E\uDEE5,\uD83E\uDE76,\uD83E\uDEE0,\uD83E\uDD79," +
                "\uD83E\uDEE3,\uD83E\uDED7,\uD83E\uDED9,\uD83E\uDEDA,\uD83E\uDED8,\uD83E\uDEDB";
        String[] removeEmojiSplitted = removeEmoji.split(",");

        Set<Emoji> removeEmojiSet = new HashSet<>();
        for (String emojiRemoveChars : removeEmojiSplitted) {
            Optional<Emoji> emojiRemove = EmojiManager.getEmoji(emojiRemoveChars);
            emojiRemove.ifPresent(removeEmojiSet::add);
        }

        allEmoji.removeAll(removeEmojiSet);

        this.allEmoji = allEmoji.toArray(new Emoji[0]);
    }

}