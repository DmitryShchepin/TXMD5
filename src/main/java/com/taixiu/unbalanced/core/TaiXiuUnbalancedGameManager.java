package com.taixiu.unbalanced.core;

import com.mario.random.entity.GeneratedResultMessageResponse;
import com.mario.random.observers.GameResultObserver;
import com.mario.random.service.impl.RandomStrategy;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObject;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventHandler;
import com.taixiu.unbalanced.core.betting.BettingEntry;
import com.taixiu.unbalanced.core.betting.BettingManager;
import com.taixiu.unbalanced.core.player.PlayerEvent;
import com.taixiu.unbalanced.core.player.TaiXiuUnbalancedPlayer;
import com.taixiu.unbalanced.core.randomizer.RandomServiceManager;
import com.taixiu.unbalanced.core.security.ResultProcessor;
import com.taixiu.unbalanced.core.shakator.RandomStrategyTaiXiuShakator;
import com.taixiu.unbalanced.core.shakator.Shakator;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingContext;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingResult;
import com.taixiu.unbalanced.core.statics.AssetType;
import com.taixiu.unbalanced.core.statics.CoreTXUF;
import com.taixiu.unbalanced.core.statics.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.taixiu.unbalanced.core.TransactionInfo.generateTransactionId;
import static java.util.Optional.ofNullable;

@Slf4j
public class TaiXiuUnbalancedGameManager extends BaseLoggable implements GameResultObserver {

    public static final int INTERVAL_UPDATE = 1000;

    private static final String BOOL_TRUE_TEXT = "1";
    private static final String BOOL_FALSE_TEXT = "0";

    private static final Integer START_GAME_PHASE = 0;
    private static final Integer FIRST_RESULT_GENERATED_PHASE = 1;
    private static final Integer SECOND_RESULT_GENERATED_PHASE = 2;

    @Getter
    private Session session;
    @Getter
    private final Map<String, TaiXiuUnbalancedPlayer> players = new ConcurrentHashMap<>();
    @Getter
    private final int timeForBetting;
    @Getter
    private final int timeForPayout;
    @Getter
    @Setter
    private TaiXiuGameState gameState;
    /**
     * Using as fallback strategy randomizer.
     */
    @Getter
    @Setter
    private Shakator shakator;
    @Getter
    @Setter
    private BettingManager bettingManager;
    @Setter
    @Getter
    private FixedSizeList<SessionDice> history = new FixedSizeList<>(100);
    @Getter
    private final AtomicBoolean timedOut;
    @Getter
    private final AtomicInteger countDown;
    @Getter
    private TaiXiuUnbalancedDataAdapter dataAdapter;
    @Getter
    @Setter
    private TaiXiuUnbalancedMessenger messenger;
    @Getter
    @Setter
    private DatalayerAdapter datalayerAdapter;
    @Getter
    private final EventHandler playerBettingHandler = new BaseEventHandler(this, "onPlayerBettingHandler");
    @Getter
    private final EventHandler playerBatchBettingHandler = new BaseEventHandler(this, "onPlayerBatchBettingHandler");
    @Getter
    private ScheduleFuture schedulePayout;
    @Getter
    private ScheduleFuture scheduleBetting;
    @Getter
    @Setter
    private Scheduler scheduler;

    @Getter
    private final AssetTransactionManager assetTransactionManager = new AssetTransactionManager();
    @Getter
    private final GameActionInfo gameInfo;

    private TaiXiuUnbalancedShakingResult result;

    private final Object synResult = new Object();

    @Getter
    private final ResultProcessor resultProcessor;
    @Setter
    private RandomServiceManager randomServiceManager;
    @Setter
    private boolean forcedInternalStrategy;

    /**
     * Represents the current phase of the game result lifecycle.
     * The possible states are as follows:
     *
     * <ul>
     *   <li>0: The result has not started generating.</li>
     *   <li>1: A hash result has been generated either internally or by an external service.</li>
     *   <li>2: The real result has been generated.</li>
     * </ul>
     * <p>
     * At the end of the game, this value is reset to 0.
     */
    private final AtomicInteger resultGamePhase = new AtomicInteger(START_GAME_PHASE);
    private CountDownLatch endGameLatch = new CountDownLatch(1);
    private ScheduleFuture timeoutExternalRandomizationSchedule;
    @Getter
    @Setter
    private volatile RandomStrategy randomStrategy;

    private final AtomicBoolean isStart = new AtomicBoolean(false);
    private final AtomicBoolean isEnd = new AtomicBoolean(false);

    @Getter
    private int betUserCount;
    private final GameInformation gameInformation;

    public TaiXiuUnbalancedGameManager(int timeForBetting,
                                       int timeForPayout,
                                       ResultProcessor resultProcessor,
                                       GameInformation gameInformation) {
        this.gameState = TaiXiuGameState.WAITING_FOR_START;
        this.timeForBetting = timeForBetting;
        this.timeForPayout = timeForPayout;
        this.countDown = new AtomicInteger();
        this.timedOut = new AtomicBoolean(true);
        this.shakator = new RandomStrategyTaiXiuShakator();
        this.resultProcessor = resultProcessor;
        this.gameInfo = new GameActionInfo(String.valueOf(gameInformation.getGameId()), gameInformation.getGameName());
        this.gameInformation = gameInformation;
    }

    public long getRemainingTime() {
        long remainningTime = 0;
        if (getGameState() == TaiXiuGameState.BETTING) {
            remainningTime = getTimeForBetting() - (System.currentTimeMillis() - getSession().getStartTime());
        } else if (getGameState() == TaiXiuGameState.PAYOUT && getSession().getEndTime() > 0) {
            remainningTime = getTimeForPayout() - (System.currentTimeMillis() - getSession().getEndTime());
        }
        log.debug("payout remaining time: {}", remainningTime);
        if (remainningTime < 0) {
            remainningTime = 0;
        }
        return remainningTime;
    }

    public synchronized void start() {
        log.info("StartSession oldId: {}", session);

        if (!isStart.compareAndSet(false, true)) {
            log.info("StartSession {} repeated", session);
            return;
        }
        reset();

        session = dataAdapter.createSession(assetTransactionManager.getRoundId());

        if (schedulePayout != null) {
            schedulePayout.cancel();
        }

        log.info("StartSession: {}", session.getId());
        setGameState(TaiXiuGameState.BETTING);
        generateResultAndStartGame(new TaiXiuUnbalancedShakingContext(session.getId()));
    }

    private void startCountDownForEndGame(long sessionId) {
        int times = timeForBetting / INTERVAL_UPDATE;
        countDown.set(times);
        scheduleBetting = scheduler.scheduleAtFixRate(INTERVAL_UPDATE, times + 3, () -> {
            log.debug("update betting, sessionId: {} - countDown {}", sessionId, countDown);

            try {
                sendUpdateBetting();
                int countdown = countDown.decrementAndGet();
                if (countdown < 0) {
                    return;
                }

                if (countdown <= 10) {
                    log.info("update betting, sessionId: {} - countDown {}", sessionId, countDown);
                }

                if (countdown == 5) {
                    timedOut.set(true);
                }
                if (countdown == 0) {
                    try {
                        endGame();
                    } catch (Exception e) {
                        log.error("End game get error", e);
                    }
                }
            } catch (Exception e) {
                log.error("Update interval get error", e);
            }
        });
    }

    private boolean canUseExternalRandomization() {
        return !forcedInternalStrategy && randomServiceManager != null && randomServiceManager.canGenerateResult();
    }

    private void generateResultAndStartGame(TaiXiuUnbalancedShakingContext context) {
        log.debug("Choosing randomization strategy");
        if (canUseExternalRandomization()) {
            log.info("External randomization strategy chosen");
            startWithExternalRandomization(context);
        } else {
            log.info("Internal randomization strategy chosen");
            startWithInternalRandomization(context);
        }

        log.info("Game end latch awaiting: {}", endGameLatch.getCount());
        try {
            endGameLatch.await();
            log.info("Latch released, continue end game phase: {}", endGameLatch.getCount());
            startCountDownForEndGame(session.getId());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("Wait interrupted: {}", e.getMessage());
        }
    }

    private void startWithExternalRandomization(TaiXiuUnbalancedShakingContext context) {
        setRandomStrategy(RandomStrategy.External);
        timeoutExternalRandomizationSchedule = scheduler.scheduleAtFixRate(5000, 1, () -> {
            log.info("External random service timeout. Switching to internal randomization.");
            timeoutExternalRandomizationSchedule.cancel();
            randomServiceManager.setForceReconnectNextSession(true);
            startWithInternalRandomization(new TaiXiuUnbalancedShakingContext(session.getId()));
        });
        log.info("Use 'External' generation strategy. Request result generation.");
        randomServiceManager.generateResult(String.valueOf(context.getSessionId()), assetTransactionManager.getRoundId());
    }

    private boolean isWrongRoundId(GeneratedResultMessageResponse generatedResultMessageResponse) {
        return Objects.isNull(generatedResultMessageResponse) ||
                Strings.isEmpty(generatedResultMessageResponse.getRoundID()) ||
                !Objects.equals(assetTransactionManager.getRoundId(), generatedResultMessageResponse.getRoundID());
    }

    @Override
    public void onResultGenerated(GeneratedResultMessageResponse generatedResultMessageResponse) {
        log.debug("On result generated: {}, response: {}", session.getId(), generatedResultMessageResponse);

        if (randomStrategy == RandomStrategy.Internal) {
            log.warn("Time out external randomization, game already started with internal randomization");
            return;
        }

        if (timeoutExternalRandomizationSchedule != null) {
            timeoutExternalRandomizationSchedule.cancel();
        }

        log.debug("onResultGenerated: {} \n generatedResultMessageResponse.isUnderMaintenance(): {} \n latch count: {}",
                generatedResultMessageResponse, generatedResultMessageResponse.isUnderMaintenance(), endGameLatch.getCount());

        if (!validateResultMessageResponse(generatedResultMessageResponse)) {
            if (resultGamePhase.get() != START_GAME_PHASE) {
                return;
            }
            startWithInternalRandomization(new TaiXiuUnbalancedShakingContext(session.getId()));
            return;
        }

        log.debug("resultGamePhase: {}", resultGamePhase.get());
        try {
            if (resultGamePhase.getAndIncrement() == START_GAME_PHASE) {
                endGameLatch.countDown();
                log.info("Receive first response with hash result: {}", generatedResultMessageResponse);
                resultProcessor.setEncryptedResult(generatedResultMessageResponse.getHashResult());
                updateSessionMD5();
                getMessenger().startBetting(session.getId(), resultProcessor.getEncryptedResult());
            } else {
                log.info("Receive second response with real result for session: {}", generatedResultMessageResponse.getSessionID());
                result = TaiXiuUnbalancedShakingResult.fromString(generatedResultMessageResponse.getResult());
                log.debug("Result generated from string: {}", result);
                resultProcessor.setResultAsText(generatedResultMessageResponse.getResult());
                result.setRandomStrategy(RandomStrategy.External);
            }
        } catch (Exception e) {
            log.error("Error while trying to release barrier: {}", result);
        }
    }

    private boolean validateResultMessageResponse(GeneratedResultMessageResponse generatedResultMessageResponse) {

        if (!Objects.equals(generatedResultMessageResponse.getSessionID(), String.valueOf(session.getId()))) {
            log.warn("Received session id not equals to current session id: {}, {}",
                    generatedResultMessageResponse.getSessionID(), session.getId());
            return false;
        }

        if (isWrongRoundId(generatedResultMessageResponse)) {
            log.warn("Received round Id not correct. Switching to internal randomization: {}, session round id {}",
                    generatedResultMessageResponse, assetTransactionManager.getRoundId());
            return false;
        }

        if (resultGamePhase.get() == START_GAME_PHASE && Strings.isEmpty(generatedResultMessageResponse.getHashResult())) {
            log.warn("Received empty hash result. Switching to internal randomization: {}", generatedResultMessageResponse);
            return false;
        }

        if (generatedResultMessageResponse.isUnderMaintenance() && resultGamePhase.get() == START_GAME_PHASE) {
            log.warn("External random service is under maintenance. Switching to internal randomization: {}", generatedResultMessageResponse);
            return false;
        }

        return true;
    }

    private void updateSessionMD5() {
        if (resultProcessor.enableEncryption()) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put("md5", resultProcessor.getEncryptedResult());
            dataAdapter.updateSession(session, updates);
        } else {
            log.debug("MD5 encryption is disabled. Skipping MD5 update.");
        }
    }

    @Override
    public void onError(Throwable throwable) {
        dataAdapter.updateSession(session, Collections.singletonMap("externalRandomResultError",
                throwable != null ? throwable.getMessage() :
                        String.format("Game state is no longer recoverable. Unable to continue the current game session: %s.", session.getId())));
        log.error("Game state is no longer recoverable. Unable to continue the current game session: {}. Error: {}.", session.getId(), throwable);
    }

    private void refundOnError() {
        log.debug("Initializing refund on error, players number: {}", bettingManager.getAllBettedPlayers().size());
        for (TaiXiuUnbalancedPlayer player : bettingManager.getAllBettedPlayers()) {

            String refId = UUID.randomUUID().toString();
            try {
                String transactionId = generateTransactionId();

                TransactionInfo transactionInfo = new TransactionInfo(
                        player.getAccessToken(),
                        player.getAgencyId(),
                        player.getMemberId(),
                        player.getUserId(),
                        TransactionAction.CANCEL.name(),
                        assetTransactionManager.getRoundId(),
                        transactionId,
                        assetTransactionManager.getOrCreateFirstTicketIdByUserId(player.getUserId()),
                        null,
                        TransactionInfo.REFUND_STATUS,
                        player.getTotalBet(),
                        0);

                UserAndExchangeValue userAndExchangeValue = new UserAndExchangeValue(
                        player.getUserId(), player.getUsername(), player.getTotalBet(), refId, transactionInfo);

                Account account = datalayerAdapter.changeMoney(userAndExchangeValue, gameInfo);
                log.debug("Refunded player: {}, account: {}", player, account);
            } catch (Exception e) {
                log.error("Error while refund for player: {}", player, e);
            }
            getMessenger().sendError(player.getSessionId(), TaiXiuUnbalancedError.RANDOM_RESULT_GENERATION_ERROR);
        }
    }

    private void startWithInternalRandomization(TaiXiuUnbalancedShakingContext context) {
        setRandomStrategy(RandomStrategy.Internal);

        long sessionId = context.getSessionId();
        synchronized (synResult) {
            try {
                result = (TaiXiuUnbalancedShakingResult) shakator.doShake(context);
                result.setRandomStrategy(RandomStrategy.Internal);
            } catch (Exception e) {
                log.error("Shaking error", e);
            }
        }
        resultProcessor.handle(context, result);
        updateSessionMD5();
        endGameLatch.countDown();
        log.info("Count down game end latch, left: {}", endGameLatch.getCount());
        resultGamePhase.getAndAdd(SECOND_RESULT_GENERATED_PHASE);

        getMessenger().startBetting(sessionId, resultProcessor.getEncryptedResult());
    }

    private void sendUpdateBetting() {
        messenger.updateBetting(this);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void onPlayerBettingHandler(Event rawEvent) {

        PlayerEvent event = (PlayerEvent) rawEvent;
        TaiXiuUnbalancedPlayer player = (TaiXiuUnbalancedPlayer) event.getPlayer();
        final long betting = Math.abs((Long) event.get("betting"));
        final int entryId = (Integer) event.get("entryId");
        final long sessionId = (Long) event.get("sessionId");

        if (sessionId != getSessionId()) {
            getMessenger().sendError(player.getSessionId(), TaiXiuUnbalancedError.SESSION_INVALID);
        } else {
            BettingEntry entry = BettingEntry.fromId(entryId);
            if (timedOut.get()) {
                getMessenger().sendError(player.getSessionId(), TaiXiuUnbalancedError.BETTING_TIME_OUT);
            } else if (betting <= 0L) {
                getMessenger().sendError(player.getSessionId(), TaiXiuUnbalancedError.BETTING_INVALID);
            } else {
                String referenceId = UUID.randomUUID().toString();
                long startTime = System.currentTimeMillis();

                TransactionInfo transactionInfo = new TransactionInfo(player.getAccessToken(),
                        player.getAgencyId(), player.getMemberId(), player.getUserId(),
                        TransactionAction.BET.name(),
                        assetTransactionManager.getRoundId(), generateTransactionId(),
                        assetTransactionManager.getOrCreateFirstTicketIdByUserId(player.getUserId()), null,
                        TransactionInfo.RUNNING_STATUS,
                        player.getTotalBet() + betting,
                        player.getTax());

//            transactionInfo.setGameYourBet("Đặt " + entry.getName() + " #" + sessionId);
                final String gameYourBet = buildGameYourBet(player, null, entry, betting, sessionId);
                transactionInfo.setGameYourBet(gameYourBet);
                transactionInfo.setGameTableId(
                        (player.isAllIn() ? BOOL_TRUE_TEXT : BOOL_FALSE_TEXT) + "/" +
                                (player.isSqe() ? BOOL_TRUE_TEXT : BOOL_FALSE_TEXT)
                );

                Account account = null;
                try {
                    account = datalayerAdapter.changeMoney(new UserAndExchangeValue(player.getUserId(), player.getUsername(),
                                    -betting, referenceId, transactionInfo),
                            gameInfo);
                } catch (Exception e) {
                    log.error("Betting error", e);
                }

                if (account == null) {
                    getMessenger().sendError(player.getSessionId(), TaiXiuUnbalancedError.BALANCE_NOT_ENOUGH);
                    return;
                }

                long changeMoneyTime = System.currentTimeMillis() - startTime;
                if (changeMoneyTime > 2000L) {
                    log.info("[taixiu-unbalanced-betting] long running transaction sessionId: {}, customerId: {}, time: {}", sessionId, player.getCustomerId(), changeMoneyTime);
                }

                if (changeMoneyTime > 3000L && (timedOut.get() || sessionId != getSessionId())) {
                    String refId = UUID.randomUUID().toString();
                    try {
                        datalayerAdapter.changeMoney(
                                new UserAndExchangeValue(player.getUserId(), player.getUsername(), betting, refId,
                                        new TransactionInfo(player.getAccessToken(),
                                                player.getAgencyId(), player.getMemberId(), player.getUserId(),
                                                TransactionAction.CANCEL.name(), assetTransactionManager.getRoundId(), generateTransactionId(),
                                                assetTransactionManager.getOrCreateFirstTicketIdByUserId(player.getUserId()), account.getTransactionId(),
                                                TransactionInfo.CANCEL_STATUS, player.getBetting(entry), 0)),
                                gameInfo);
                    } catch (Exception e) {
                        log.error("Cancel after 3s and timeout error", e);
                    }

                    getMessenger().sendError(player.getSessionId(), TaiXiuUnbalancedError.BETTING_TIME_OUT);
                } else {
                    bettingManager.addBetting(player, entry, betting);

                    if (player.getBettingId() == null) {
                        player.setBettingId(UUID.randomUUID().toString());
                    }
                    dataAdapter.updatePlayerBetting(player, getSessionId());
                    getMessenger().bettingSuccess(player, bettingManager);
                }
            }
        }
    }

    private static String buildGameYourBet(TaiXiuUnbalancedPlayer player, BettingEntry result, BettingEntry bettingEntry,
                                           long newBetting, long sessionId) {
        final BettingEntry[] entries = BettingEntry.values();
        String gameYourBet = result == null ? StringUtils.EMPTY : String.format("Kết quả %s. ", result.getName());
        boolean isFirst = true;
        for (BettingEntry entry : entries) {
            long betting = player.getBetting(entry);
            if (betting == 0) {
                continue;
            }
            if (bettingEntry == entry) {
                betting += newBetting;
                bettingEntry = null;
            }
            if (isFirst) {
                gameYourBet += "Đặt " + entry.getName() + ": " + DecimalFormatUtil.format(betting);
                isFirst = false;
            } else {
                gameYourBet += ", " + entry.getName() + ": " + DecimalFormatUtil.format(betting);
            }
        }
        if (bettingEntry != null) {
            if (isFirst) {
                gameYourBet += "Đặt " + bettingEntry.getName() + ": " + DecimalFormatUtil.format(newBetting);
            } else {
                gameYourBet += ", " + bettingEntry.getName() + ": " + DecimalFormatUtil.format(newBetting);
            }
        }

        if (result != null) {
            gameYourBet += String.format(". Nhận %s #%s", DecimalFormatUtil.format(player.getWinMoney()), sessionId);
        } else {
            gameYourBet += String.format(" #%s", sessionId);
        }

        return gameYourBet;
    }

    private synchronized void endGame() {
        final long sessionId = getSessionId();
        log.info("EndSession {}", sessionId);

        if (!isEnd.compareAndSet(false, true)) {
            log.info("EndSession {} repeated", sessionId);
            return;
        }

        if (scheduleBetting != null) {
            scheduleBetting.cancel();
        }
        if (schedulePayout != null) {
            schedulePayout.cancel();
        }

        final long startEndGameTime = System.currentTimeMillis();
        session.setEndTime(startEndGameTime);

        final Set<TaiXiuUnbalancedPlayer> allBettors = bettingManager.getAllBettedPlayers();
        setGameState(TaiXiuGameState.PAYOUT);

        if (resultGamePhase.get() != SECOND_RESULT_GENERATED_PHASE || result == null) {
            handleErrorOnEndGame();
            return;
        }
        session.setRandomStrategy(randomStrategy);

        history.add(new SessionDice(getSessionId(), result.getDice()));
        List<UAItem> userAssets = new ArrayList<>();

        final BettingEntry entryResult = result.getEntryResult();

        final long totalUser = bettingManager.getBetCountEntry(entryResult);
        final long totalBetting = bettingManager.getBettingEntry(entryResult);

        betUserCount = allBettors.size();

        for (TaiXiuUnbalancedPlayer bettors : allBettors) {
            bettingManager.payout(bettors, entryResult);
        }


        for (TaiXiuUnbalancedPlayer bettor : allBettors) {
            final TransactionInfo info = new TransactionInfo(bettor.getAccessToken(),
                    bettor.getAgencyId(), bettor.getMemberId(), bettor.getUserId(),
                    TransactionAction.WIN.name(),
                    assetTransactionManager.getRoundId(), generateTransactionId(),
                    assetTransactionManager.getOrCreateFirstTicketIdByUserId(bettor.getUserId()), null);

            UAItem item = new UAItem(bettor.getAccessToken(), bettor.getUserId(),
                    bettor.getUsername(), bettor.getWinMoney(),
                    AssetType.GOLD.getName(), AssetType.GUARRANTEED_GOLD.getName(), info);
            info.setGameYourBet(buildGameYourBet(bettor, entryResult, null, 0L, this.getSessionId()));
            info.setGameStake(bettor.getTotalBet());
            info.setGameGain(bettor.getWinMoney());
            info.setTax(bettor.getTax());
            info.setGameWinlost(info.getGameGain() - info.getGameStake());

            if (bettor.getWinMoney() >= bettor.getTotalBet()) {
                info.setGameTicketStatus(TransactionInfo.WIN_STATUS);
            } else {
                info.setGameTicketStatus(TransactionInfo.LOSE_STATUS);
            }

            userAssets.add(item);
        }

        allBettors.forEach(player -> {
            getMessenger().endGame(bettingManager, sessionId, result, resultProcessor.getResultAsText(), resultProcessor.getEncryptedResult(), player, null);
        });

        getMessenger().endGame(bettingManager, sessionId, result, resultProcessor.getResultAsText(), resultProcessor.getEncryptedResult(),
                allBettors, null);

        long startTime = System.currentTimeMillis();
        List<Account> accounts = null;
        try {
            accounts = dataAdapter.changeMoney(sessionId, userAssets, GameStatus.ON_SAVE_GAME_UPDATING_WINLOSE.name());
        } catch (Exception e) {
            log.error("Endgame error", e);
        }
        boolean changeMoneyResult = accounts != null;
        if (!changeMoneyResult) {
            log.error("End game error");
        }
        long endTime = System.currentTimeMillis();
        log.info("change money time sessionId {}: {} - result {}", sessionId, endTime - startTime, changeMoneyResult);

        dataAdapter.updateBettings(allBettors, sessionId);
        dataAdapter.updateSession(session, assetTransactionManager.getRoundId(),
                result.getDice(), resultProcessor, randomServiceManager);

        long executeTime = System.currentTimeMillis() - startEndGameTime;
        log.info("---> end session {}, execute time: {}, update money success, num accounts: {}",
                sessionId, executeTime, accounts.size());

        log.info("EndSession Finish {}", sessionId);
        isStart.set(false);

        if (executeTime >= timeForPayout) {
            start();
        } else {
            int delay = (int) (timeForPayout - executeTime);
            log.info("---> next of session {} after {} ms", sessionId, delay);
            schedulePayout = getScheduler().scheduleAtFixRate(delay, 1000, 3, () -> {
                try {
                    start();
                } catch (Exception e) {
                    log.error("StartGameError", e);
                }
            });
        }
    }

    private void handleErrorOnEndGame() {
        log.error("Game result not generated, not able to continue the game. Refund initiated.");
        // refundOnError();

        Map<String, Object> extraData = new HashMap<>();
        extraData.put(CoreTXUF.ENDED, false);
        extraData.put(CoreTXUF.ERROR_MESSAGE, Message.SESSION_FAILED);

        final Set<TaiXiuUnbalancedPlayer> allBettors = bettingManager.getAllBettedPlayers();
        long sessionId = getSessionId();

        allBettors.forEach(player -> {
            getMessenger().endGame(bettingManager, sessionId, null, null, null,
                    player, extraData);
        });
        getMessenger().endGame(bettingManager, sessionId, null, null, null,
                 allBettors, extraData);

        Map<String, Object> updates = new HashMap<>();
        updates.put("failed", true);
        updates.put("md5", ofNullable(resultProcessor).map(ResultProcessor::getEncryptedResult).orElse(null));

        dataAdapter.updateSession(Session.builder().id(sessionId).build(), updates);

        setGameState(TaiXiuGameState.WAITING_FOR_START);
        isStart.set(false);
        start();
    }

    public synchronized TaiXiuUnbalancedPlayer addPlayer(TaiXiuUnbalancedPlayer player) {
        if (!players.containsKey(player.getUsername())) {
            players.put(player.getUsername(), player);
            player.addEventListener("betting", playerBettingHandler);
            player.addEventListener("batchBetting", playerBatchBettingHandler);
        }

        TaiXiuUnbalancedPlayer existsPlayer = players.get(player.getUsername());
        if (existsPlayer != null) {
            existsPlayer.setSessionId(player.getSessionId());
            existsPlayer.setAllIn(player.isAllIn());
            existsPlayer.setSqe(player.isSqe());
        }

        return existsPlayer;
    }

    public TaiXiuUnbalancedPlayer getPlayer(String username) {
        return players.get(username);
    }

    private void reset() {
        players.clear();
        timedOut.set(false);
        bettingManager.reset();
        isEnd.set(false);
        setRandomStrategy(RandomStrategy.Unknown);
        resultProcessor.clear();
        resultGamePhase.set(START_GAME_PHASE);
        result = null;
        assetTransactionManager.endGame();
        endGameLatch = new CountDownLatch(1);
        log.info("Reset end game state");
    }

    public long getSessionId() {
        if (session == null) {
            return 0;
        }
        return session.getId();
    }

    public String getMd5() {
        return resultProcessor.getEncryptedResult();
    }

    public void setResultToMessage(PuObject message) {
        synchronized (synResult) {
            if (getGameState() == TaiXiuGameState.PAYOUT && result != null) {
                message.setString("rs", resultProcessor.getResultAsText());
                message.setInteger("d1", result.getDice().getDice1());
                message.setInteger("d2", result.getDice().getDice2());
                message.setInteger("d3", result.getDice().getDice3());
            }
        }
    }

}
