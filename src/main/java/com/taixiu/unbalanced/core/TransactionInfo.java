package com.taixiu.unbalanced.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TransactionInfo {

    public static final String BET = "BET";
    public static final String RESERVE = "RESERVE";
    public static final String RELEASE = "RELEASE";
    public static final String WIN = "WIN";
    public static final String LOSE = "LOSE";
    public static final String COMMIT = "COMMIT";

    private String accessToken;
    private Long agencyId;
    private Long memberId;
    private String userId;

    private String transactionAction;
    private String roundId;
    private String transactionId;
    private String ticketId;
    private String referenceTransactionId;

    /*
game_winlost = Số tiền thắng thua
game_gain; // = (game_stake + game_winlost) Số tiền thu được là tổng số tiền phả trả cho user.
                             // Ví dụ user đặt cược game_stake = 10,000đ thắng game_winlost = 9,000đ. game_gain=19,000đ
=> endgame

game_ticket_status = Trạng thái của game, Open, Running, Cancel, Win, Lose, Draw
Start game = running

endgame dự vào winlost
winlost = 0 draw
> 0 win
< 0 lose

Ví dụ đặt tài 200k thì game_your_bet =  tài 200,000đ
Hoặc game_your_bet = Tài


    public String game_ticket_status;
    public String game_your_bet;
    public Double game_stake;
    public Double game_winlost;
    public Double game_gain; // = (game_stake + game_winlost) Số tiền thu được là tổng số tiền phả trả cho user.
                             // Ví dụ user đặt cược game_stake = 10,000đ thắng game_winlost = 9,000đ. game_gain=19,000đ
     */

    public static final String OPEN_STATUS = "Open";
    public static final String RUNNING_STATUS = "Running";
    public static final String WIN_STATUS = "Win";
    public static final String LOSE_STATUS = "Lose";
    public static final String DRAW_STATUS = "Draw";
    public static final String CANCEL_STATUS = "Cancel";
    public static final String REFUND_STATUS = "REFUND";
    public static final String TIP_STATUS = "TIP";

    private String gameTicketStatus;

    private String gameYourBet;
    private long gameStake;
    private long gameWinlost;
    private long gameGain; // = (game_stake + game_winlost) Số tiền thu được là tổng số tiền phả trả cho user.
    // Ví dụ user đặt cược game_stake = 10,000đ thắng game_winlost = 9,000đ. game_gain=19,000đ
    private long tax;
    private long refund;
    private long gameBetValue;
    private String gameTableId;

    public TransactionInfo() {

    }

    public TransactionInfo(String accessToken, Long agencyId, Long memmberId, String userId, String transactionAction,
                           String roundId, String transactionId, String ticketId, String refId) {
        this.accessToken = accessToken;
        this.agencyId = agencyId;
        this.memberId = memmberId;
        this.userId = userId;

        this.transactionAction = transactionAction;
        this.roundId = roundId;
        this.transactionId = transactionId;
        this.ticketId = ticketId;
        this.referenceTransactionId = refId;
    }

    public TransactionInfo(String accessToken, Long agencyId, Long memmberId, String userId, String transactionAction, String roundId, String transactionId,
                           String ticketId, String refId, String gameTicketStatus) {
        this.accessToken = accessToken;
        this.agencyId = agencyId;
        this.memberId = memmberId;
        this.userId = userId;

        this.transactionAction = transactionAction;
        this.roundId = roundId;
        this.transactionId = transactionId;
        this.ticketId = ticketId;
        this.referenceTransactionId = refId;
        this.gameTicketStatus = gameTicketStatus;
    }

    public TransactionInfo(String accessToken, Long agencyId, Long memmberId, String userId, String transactionAction, String roundId, String transactionId,
                           String ticketId, String refId, String gameTicketStatus, long gameStake, long tax) {
        this.accessToken = accessToken;
        this.agencyId = agencyId;
        this.memberId = memmberId;
        this.userId = userId;

        this.transactionAction = transactionAction;
        this.roundId = roundId;
        this.transactionId = transactionId;
        this.ticketId = ticketId;
        this.referenceTransactionId = refId;
        this.gameTicketStatus = gameTicketStatus;
        this.gameStake = gameStake;
        this.tax = tax;
    }

//    public void setGameInfo(String gameTicketStatus, String gameYourBet, long gameStake, long gameWinlost, long gameGain) {
//        this.gameTicketStatus = gameTicketStatus;
//        this.gameYourBet = gameYourBet;
//        this.gameStake = gameStake;
//        this.gameWinlost = gameWinlost;
//        this.gameGain = gameGain;
//    }

    public static final String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    public void setGameWinlostAndUpdateTicketStatus(long exchangeMoney) {
        this.setGameWinlost(exchangeMoney);
        if (exchangeMoney == 0) {
            this.setGameTicketStatus(TransactionInfo.DRAW_STATUS);
        }
        this.setGameTicketStatus(exchangeMoney < 0 ? TransactionInfo.LOSE_STATUS : TransactionInfo.WIN_STATUS);
    }
}