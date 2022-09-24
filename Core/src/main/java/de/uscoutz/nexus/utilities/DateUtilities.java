package de.uscoutz.nexus.utilities;

import de.uscoutz.nexus.NexusPlugin;

public class DateUtilities {

    public static String getTime(long start, long current, NexusPlugin plugin) {
        long different = current-start;

        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        int days = 0;

        while (different > 1000) {
            different-=1000;
            seconds++;
        }
        while (seconds > 60) {
            seconds-=60;
            minutes++;
        }
        while (minutes > 60) {
            minutes-=60;
            hours++;
        }
        while(hours > 24) {
            hours-=24;
            days++;
        }

        String daysLocale = plugin.getLocaleManager().translate("de_DE", "days", String.valueOf(days));
        if(days == 1) {
            daysLocale = plugin.getLocaleManager().translate("de_DE", "day");
        }
        String hoursLocale = plugin.getLocaleManager().translate("de_DE", "hours", String.valueOf(hours));
        if(hours == 1) {
            hoursLocale = plugin.getLocaleManager().translate("de_DE", "hour");
        }
        String minutesLocale = plugin.getLocaleManager().translate("de_DE", "minutes", String.valueOf(minutes));
        if(minutes == 1) {
            minutesLocale = plugin.getLocaleManager().translate("de_DE", "minute");
        }
        String secondsLocale = plugin.getLocaleManager().translate("de_DE", "seconds", String.valueOf(seconds));
        if(seconds == 1) {
            secondsLocale = plugin.getLocaleManager().translate("de_DE", "second");
        }
        if(days == 0) {
            if(hours == 0) {
                if(minutes == 0) {
                    return secondsLocale;
                } else {
                    return minutesLocale + " " + secondsLocale;
                }
            } else {
                return hoursLocale + " " + minutesLocale;
            }
        } else {
            return daysLocale + " " + hoursLocale;
        }
    }
}
