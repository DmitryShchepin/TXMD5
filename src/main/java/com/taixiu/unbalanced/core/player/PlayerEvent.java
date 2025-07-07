package com.taixiu.unbalanced.core.player;

import com.nhb.eventdriven.impl.BaseEvent;

import java.util.List;

public class PlayerEvent extends BaseEvent {
	private static final long serialVersionUID = 1L;
	public static final String KICK = "kick";

	private Player player;
	private Object data;
	private List<Player> players;

	public PlayerEvent(String type, Player player) {
		super(type);
		setPlayer(player);
	}

	public PlayerEvent(String type, Player player, Object data) {
		this(type, player);
		setData(data);
	}

	public PlayerEvent(PlayerEventType type, Player player) {
		super(type.getType());
		this.player = player;
	}

	public PlayerEvent(PlayerEventType type, Player player, Object data) {
		this(type, player);
		setData(data);
	}

	public PlayerEvent(PlayerEventType type, List<Player> players) {
		super(type.getType());
		this.players = players;
	}

    public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
