package com.hawky.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.joda.time.DateTimeConstants;
import org.junit.Test;

import com.hawky.jhttpload.Task;
import com.hawky.jhttpload.TimerTaskCallback;
import com.hawky.jhttpload.TimerTaskMgr;

public class TimerTaskMgrTest {

	@Test
	public void testTimeoutTask() throws InterruptedException{
		TimerTaskMgr mgr = new TimerTaskMgr();
		long now =getMillisOfDay();
		Task task = mgr.submitTask(now, new TimerTaskCallback() {
			public void run(Object associatedData, long now) {
				System.out.print("task is excuted");
			}
		}, null, 1 * 1000L, false);

		assertEquals(1,mgr.size());
//		
//		assertTrue(mgr.cancelTask(task));
//		
//		assertEquals(0,mgr.size());
		
		Thread.sleep(1000L);
		 now =getMillisOfDay();
		mgr.runTimer(now);
		
		assertEquals(0,mgr.size());
	
	}
	
	@Test
	public void testReset(){
		TimerTaskMgr mgr = new TimerTaskMgr();
		long now =getMillisOfDay();
		Task task = mgr.submitTask(now, new TimerTaskCallback() {
			public void run(Object associatedData, long now) {
				System.out.print("task is excuted");
			}
		}, null, 1 * 1000L, false);

		assertEquals(1,mgr.size());
		
		now =getMillisOfDay();
		mgr.resetTask(now, task);
		
		assertEquals(1,mgr.size());
	}
	
	public void testSubmit(){
		
	}
	
	@Test
	public void testCancle(){
		TimerTaskMgr mgr = new TimerTaskMgr();
		long now =getMillisOfDay();
		Task task1 = mgr.submitTask(now, new TimerTaskCallback() {
			public void run(Object associatedData, long now) {
				System.out.print("task is excuted");
			}
		}, null, 1 * 1000L, false);
	
		Task task2 = mgr.submitTask(now, new TimerTaskCallback() {
			public void run(Object associatedData, long now) {
				System.out.print("task is excuted");
			}
		}, null, 1 * 1000L, false);
		
		assertEquals(2,mgr.size());
		
		now =getMillisOfDay();
		assertTrue( mgr.cancelTask(task1));
		
		assertEquals(1,mgr.size());
		
		Iterator<Task> itr = mgr.getTasks().values().iterator();
		boolean hit =false;
		while(itr.hasNext()){
			Task t = itr.next();
			if(t  == task1){
				hit = true;
			}
		}
		
		assertFalse(hit);
	}
	
	private long getMillisOfDay() {
		return System.currentTimeMillis() % DateTimeConstants.MILLIS_PER_DAY;

	}
}
