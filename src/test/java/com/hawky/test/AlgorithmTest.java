package com.hawky.test;

import java.util.Iterator;

import org.junit.Test;

import com.google.common.collect.TreeMultiset;
import com.hawky.jhttpload.Task;
import com.hawky.jhttpload.TimerTaskCallback;

public class AlgorithmTest {
	@Test
	public void testTreeMultiset() {
		TreeMultiset<Task> tasks = TreeMultiset.create();
		long now = System.currentTimeMillis();
		tasks.add(new Task(now, new TimerTaskCallback() {

			public void run(Object associatedData, long now) {
				System.out.println("should be the first.");

			}
		}, null, 1000L, false));
		tasks.add(new Task(now, new TimerTaskCallback() {

			public void run(Object associatedData, long now) {
				System.out.println("should be the forth.");

			}
		}, null, 4000L, false));
		tasks.add(new Task(now, new TimerTaskCallback() {

			public void run(Object associatedData, long now) {
				System.out.println("should be the first.");

			}
		}, null, 1000L, false));
		tasks.add(new Task(now, new TimerTaskCallback() {

			public void run(Object associatedData, long now) {
				System.out.println("should be the third.");

			}
		}, null, 3000L, false));
		tasks.add(new Task(now, new TimerTaskCallback() {

			public void run(Object associatedData, long now) {
				System.out.println("should be the forth.");

			}
		}, null, 4000L, false));

		tasks.add(new Task(now, new TimerTaskCallback() {

			public void run(Object associatedData, long now) {
				System.out.println("should be the second.");

			}
		}, null, 2000L, false));

		Iterator<Task> itr = tasks.iterator();
		while (itr.hasNext()) {
			Task t = itr.next();
			tasks.add(new Task(now, new TimerTaskCallback() {

				public void run(Object associatedData, long now) {
					System.out.println("should be the first.");

				}
			}, null, 6000L, false));
			itr.remove();
			t.proc.run(t.associatedData, now);
		}
		
		itr = tasks.iterator();
		while (itr.hasNext()) {
			Task t = itr.next();
			t.proc.run(t.associatedData, now);
		}
	}

}
