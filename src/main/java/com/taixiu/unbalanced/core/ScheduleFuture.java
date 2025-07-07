package com.taixiu.unbalanced.core;

public interface ScheduleFuture {
	int getId();

	void cancel();

	long getRemainingDelay();
}