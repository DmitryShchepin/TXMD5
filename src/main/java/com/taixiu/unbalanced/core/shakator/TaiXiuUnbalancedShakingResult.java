package com.taixiu.unbalanced.core.shakator;

import com.mario.random.service.impl.RandomStrategy;
import com.taixiu.unbalanced.core.betting.BettingEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Slf4j
@Getter
@Setter
public class TaiXiuUnbalancedShakingResult implements ShakingResult {

    private static final Pattern RESULT_PARSER = Pattern.compile("\\{(\\d+-\\d+-\\d+)}");

    private Dice dice;
    private RandomStrategy randomStrategy;

    public String getIcon() {
        return this.dice.getIcon();
    }

    public BettingEntry getEntryResult() {
        int total = this.dice.getDice1() + this.dice.getDice2() + this.dice.getDice3();
        return total > 10 ? BettingEntry.TAI : BettingEntry.XIU;
    }

    public TaiXiuUnbalancedShakingResult(Dice dice) {
        this.dice = dice;
    }

    public String writeToString() {
        return getDice().getDice1() + "-" + getDice().getDice2() + "-" + getDice().getDice3();
    }

    public static TaiXiuUnbalancedShakingResult fromString(String resultString) {
        Matcher matcher = RESULT_PARSER.matcher(resultString);

        if (matcher.find()) {
            int[] numbers = Arrays.stream(matcher.group(1).split("-"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            return new TaiXiuUnbalancedShakingResult(new Dice(numbers[0], numbers[1], numbers[2]));
        }
        throw new RuntimeException("Result can not be parsed: " + resultString);
    }
}
