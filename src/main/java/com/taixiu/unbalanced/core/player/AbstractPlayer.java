package com.taixiu.unbalanced.core.player;

import com.nhb.common.data.PuObject;
import com.nhb.eventdriven.impl.BaseEventDispatcher;
import lombok.Getter;

import java.util.UUID;

public abstract class AbstractPlayer extends BaseEventDispatcher implements Player {

	private int customerId;
	private String userId;
	private String username;

	private String userIdToClient;

	private String avatar = "";
	private int gender;
	private boolean mercenaryType;
	private long money;
	private boolean isDisconnected;
	private boolean quitWhilePlaying;
	private String quitGameReason;
	@Getter
    private String displayName;
	private boolean isPlaying;
	private boolean isReady;
	private int platformId;
	private PlayerState state;
	private String ipAddress;
	private String ipSocketGame;

	private PuObject userAsset;
	private float taxPercent;
	private long tax;

	private String ticketId;
	private String accessToken;

	private Long agencyId;
	private Long memberId;
	private boolean banker;
	private boolean isViewing;
	private String agencyCode;

	@Override
	public String getDisplayNameWithAgencyCode() {
		return String.format("%s_%s", agencyCode, displayName);
	}

	public String getAgencyCode() {
		return agencyCode;
	}

	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserIdToClient() {
		return userIdToClient;
	}

	public void setUserIdToClient(String userIdToClient) {
		this.userIdToClient = userIdToClient;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public long getMoney() {
		return money;
	}

	public void setMoney(long money) {
		this.money = money;
	}

	@Override
	public boolean isDisconnected() {
		return isDisconnected;
	}

	public void setDisconnected(boolean isDisconnected) {
		this.isDisconnected = isDisconnected;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

    public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
		this.ticketId = null;
		if (this.isPlaying) {
			this.ticketId = UUID.randomUUID().toString();
		}
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	public int getPlatformId() {
		return platformId;
	}

	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}

	public PlayerState getState() {
		return state;
	}

	public void setState(PlayerState state) {
		this.state = state;
	}

	public void kick(String userId) {
		this.dispatchEvent(new PlayerEvent(PlayerEvent.KICK, this, userId));
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpSocketGame() {
		return ipSocketGame;
	}

	public void setIpSocketGame(String ipSocketGame) {
		this.ipSocketGame = ipSocketGame;
	}

	public void disconnected() {
		this.dispatchEvent(new PlayerEvent(PlayerEventType.DISCONNECT, this));
	}

	public void reconnect() {
		this.dispatchEvent(new PlayerEvent(PlayerEventType.RECONNECT, this));
	}

	public PuObject getUserAsset() {
		return userAsset;
	}

	public void setUserAsset(PuObject userAsset) {
		this.userAsset = userAsset;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public float getTaxPercent() {
		return taxPercent;
	}

	public void setTaxPercent(float taxPercent) {
		this.taxPercent = taxPercent;
	}

	public long getTax() {
		return tax;
	}

	public void setTax(long tax) {
		this.tax = tax;
	}

	public void changeTax(long value) {
		this.tax += value;
	}

	@Override
	public void reset() {
		setReady(false);
		setPlaying(false);
		setTax(0);
	}

	public String getTicketId() {
		return this.ticketId;
	}

	public void setAccessToken(String token) {
		this.accessToken = token;
	}

	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public Long getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}

	@Override
	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public boolean isQuitWhilePlaying() {
		return quitWhilePlaying;
	}

	public void setQuitWhilePlaying(boolean quitWhilePlaying) {
		this.quitWhilePlaying = quitWhilePlaying;
	}

	public String getQuitGameReason() {
		return quitGameReason;
	}

	public void setQuitGameReason(String quitGameReason) {
		this.quitGameReason = quitGameReason;
	}

	public void setBanker(boolean isBanker) {
		this.banker = isBanker;
	}

	public boolean isBanker() {
		return banker;
	}

	public void setMercenaryType(boolean type) {
		this.mercenaryType = type;
	}

	public boolean getMercenaryType() {
		return this.mercenaryType;
	}

	@Override
	public boolean isMercenary() {
		return getMercenaryType();
	}

	@Override
	public boolean isViewing() {
		return isViewing;
	}

	public void setViewing(boolean viewing) {
		isViewing = viewing;
	}

}
