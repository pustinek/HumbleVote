package com.pustinek.humblevote.utils;

import com.pustinek.humblevote.Main;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private static YearMonth yearMonth;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


    public void init(Main plugin) {

        OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC ) ;




        Runnable task = new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 1);
                long delay =
                        calendar.getTimeInMillis() - System.currentTimeMillis();

                try {

                } finally {
                    executorService.schedule(this, delay, TimeUnit.MILLISECONDS);
                }

            }
        };

        int dayOfMonth = 1;

        Calendar calendar = Calendar.getInstance();
        calendar.get(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        executorService.schedule(task,
                calendar.getTimeInMillis() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);

    }
}
