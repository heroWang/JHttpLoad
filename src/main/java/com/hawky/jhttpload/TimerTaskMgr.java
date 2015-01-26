package com.hawky.jhttpload;

import java.util.Iterator;

import com.google.common.collect.TreeMultimap;

public class TimerTaskMgr {
	private TreeMultimap<Long,Task> tasks =  TreeMultimap.create();

	public Task submitTask(long now, TimerTaskCallback proc, Object associatedData, long interval, boolean periodic) {
		Task task = new Task(now, proc, associatedData, interval, periodic);
		submitTask(task);
		return task;
	}

	public boolean submitTask(Task task) {
		return tasks.put(task.execTimeMillis,task);
	}


	public TreeMultimap<Long,Task> getTasks() {
		return tasks;
	}

	public boolean cancelTask(Task task) {
		return	tasks.remove(task.execTimeMillis, task);
	}


	public void resetTask(long now, Task task) {
		if (!tasks.containsValue(task)) {
			return;
		}
		this.cancelTask(task);
		task.execTimeMillis = now + task.intervalMillis;
		this.submitTask(task);
	}
	
	public int size(){
		return this.tasks.values().size();
	}

	public void runTimer(long now) {
		Iterator<Task> itr = tasks.values().iterator();

		
		while (itr.hasNext()) {
			Task task = itr.next();
			if (task.execTimeMillis > now) {
				break;
			}

			task.proc.run(task.associatedData, now);
			itr.remove();
			if (task.periodic) {
				// reschedule.
				task.execTimeMillis += task.intervalMillis;
				this.submitTask(task);
			}
		}
	}

}
