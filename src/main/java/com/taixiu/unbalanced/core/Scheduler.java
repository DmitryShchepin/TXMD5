package com.taixiu.unbalanced.core;

public interface Scheduler {

	ScheduleFuture scheduleAtFixRate(int delay, int period, int times, ScheduleCallback callback);

	/**
	 * 
	 * @param delay
	 *            delay time to execute task in miliseconds
	 * @param times
	 *            numbers of times excute
	 * @param callback
	 *            implement how to excute
	 * @return
	 */
	ScheduleFuture scheduleAtFixRate(int delay, int times, ScheduleCallback callback);
	
	void execute(ScheduleCallback callback);
	
	void shutdown();
}
