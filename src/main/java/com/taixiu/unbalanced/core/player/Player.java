package com.taixiu.unbalanced.core.player;

import com.nhb.common.data.PuObject;
import com.nhb.eventdriven.EventDispatcher;

public interface Player extends EventDispatcher {
	
	int getCustomerId();

	String getUserId();

	String getUserIdToClient();

	String getUsername();

	String getDisplayName();

	default boolean isViewing() {
		return false;
	}

	int getGender();

	int getPlatformId();
	
	String getAvatar();

	long getMoney();

	boolean isPlaying();

	boolean isReady();

	boolean isDisconnected();

	PlayerState getState();
	
	PuObject getUserAsset();

	void reset();

	String getTicketId();
	
	String getAccessToken();

	Long getMemberId();

	Long getAgencyId();

	boolean getMercenaryType();

	boolean isMercenary();

	default String getBonusCardDesc() {
		return null;
	}

	String getIpAddress();

	String getDisplayNameWithAgencyCode();
}
