package com.taixiu.unbalanced.core.player;

public enum PlayerEventType {
	BEFORE_JOIN_GAME("beforeJoinGame"),
	AFTER_JOIN_GAME("afterJoinGame"),
	BEFORE_QUIT_GAME("beforeQuitGame"),
	AFTER_QUIT_GAME("afterQuitGame"),
	DISCONNECT("disconnect"),
	RECONNECT("reconnect"),
	QUIT_WHILE_PLAYING("quitWhilePlaying"),

	BEFORE_VIEW_GAME("beforeViewGame"),
	AFTER_JOIN_VIEW_GAME("afterJoinViewGame"),
	BEFORE_EXIT_VIEW_GAME("beforeExitViewGame"),
	AFTER_EXIT_VIEW_GAME("afterExitViewGame");

	private String type;

	PlayerEventType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}
