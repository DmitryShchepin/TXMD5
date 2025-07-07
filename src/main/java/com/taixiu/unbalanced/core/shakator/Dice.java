package com.taixiu.unbalanced.core.shakator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dice {

    private int dice1;
    private int dice2;
    private int dice3;
    private long sessionId;

    public Dice(int dice1, int dice2, int dice3) {
        this.dice1 = dice1;
        this.dice2 = dice2;
        this.dice3 = dice3;
    }

    public int getTotal() {
        return this.dice1 + this.dice2 + this.dice3;
    }

    public String getIcon() {
        return "[" + this.dice1 + "," + this.dice2 + "," + this.dice3 + "]";
    }

    public String toString() {
        return this.getIcon();
    }

    @Override
    public Dice clone() {
        Dice dice = new Dice(this.dice1, this.dice2, this.dice3, this.sessionId);
        return dice;
    }

}
