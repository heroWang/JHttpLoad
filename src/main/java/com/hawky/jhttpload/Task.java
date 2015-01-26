package com.hawky.jhttpload;

import java.util.Comparator;

public class Task implements Comparable<Task> {

	public TimerTaskCallback proc;
	public long intervalMillis;
	public long execTimeMillis;
	public boolean periodic;

	public Object associatedData;

	public Task(long now, TimerTaskCallback proc, Object associatedData, long intervalMillis, boolean periodic) {
		super();
		this.proc = proc;
		this.intervalMillis = intervalMillis;
		this.periodic = periodic;
		this.associatedData = associatedData;

		this.execTimeMillis = now + intervalMillis;
	}

	public int compareTo(Task t) {
		return this.hashCode() - t.hashCode();
	}

	public static class TaskComparator implements Comparator<Task> {

		public int compare(Task t1, Task t2) {
			return t1.compareTo(t2);
		}

	}

}
