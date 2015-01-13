package com.hawky.jhttpload;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;


public class SimpleTimer {
    private final AtomicLong beginMillis = new AtomicLong(0L);
    private final AtomicLong endMillis = new AtomicLong(0L);

    public void begin() {
        reset(); // better be safe
        beginMillis.set(Calendar.getInstance().getTimeInMillis());
    }

    public void end() {
        endMillis.set(Calendar.getInstance().getTimeInMillis());
    }

    public long elapsed() {
        return endMillis.get() - beginMillis.get();
    }

    public void reset() {
        beginMillis.set(0L);
        endMillis.set(0L);
    }
}
