package com.taixiu.unbalanced.core;

public interface GameState {
	int getState();

	boolean isPlaying();
	
	boolean isPrepareToStart();
}
