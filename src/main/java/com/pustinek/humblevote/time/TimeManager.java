package com.pustinek.humblevote.time;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.configs.ConfigManager;
import com.pustinek.humblevote.utils.Manager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TimeManager extends Manager {

    private final Main plugin;
    private static Instant devInstant;
    private static YearMonth devYearMonth;
    private ConfigManager configManager;
    private DateTimeFormatter formatter;

    public TimeManager(Main plugin) {
        this.plugin = plugin;
        configManager = Main.getConfigManager();
        reload();
    }

    @Override
    public void reload() {
            LocalDateTime devLocalTime = LocalDateTime.of(configManager.devYear, configManager.devMonth, configManager.devDay,  configManager.devHour,  configManager.devMinute);
            ZoneOffset zoneOffSet= ZoneOffset.of(configManager.devZoneOffset);
            devInstant =  devLocalTime.toInstant(zoneOffSet);
            devYearMonth = YearMonth.of(devLocalTime.getYear(), devLocalTime.getMonth());

         formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.LONG )
                        .withZone(configManager.getZoneId());

    }



    public Instant getTimeInstant() {
        if(configManager.devEnabled) {
            return devInstant;
        }
        return  Instant.now().atZone(Main.getConfigManager().getZoneId()).toInstant();
    }

    public YearMonth getYearMonth() {
        if(configManager.devEnabled) {
            return devYearMonth;
        }
        return YearMonth.now(Main.getConfigManager().getZoneId());
    }

    public String getReadableTimeFormat() {
        if(configManager.devEnabled) {
            return formatter.format(devInstant);
        }
        return formatter.format(Instant.now());
    }

    public YearMonth InstantToYearMonth(Instant instant) {
        return
                YearMonth.from(instant
                        .atZone(Main.getConfigManager().getZoneId())
                        .toLocalDate());
    }


}
