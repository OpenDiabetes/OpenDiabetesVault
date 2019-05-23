/**
 * Copyright (C) 2019 Jens Heuschkel
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.opendiabetes.vault.util;

import de.opendiabetes.vault.data.container.VaultEntry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * Class for handling standard timestamp related problems.
 *
 * @author juehv, tiweGH
 */
public class TimestampUtils {

    public static final String TIME_FORMAT_LIBRE_DE = "yyyy.MM.dd HH:mm";
    public static final String TIME_FORMAT_DATASETS = "yyyy.MM.dd-HH:mm";

    public static Date createCleanTimestamp(String dateTime, String format) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Date rawDate = df.parse(dateTime);
        return createCleanTimestamp(rawDate);
    }

    /**
     * creates a timestamp with the format "yyyy.MM.dd-HH:mm"
     *
     * @param dateTime
     * @return
     * @throws ParseException
     */
    public static Date createCleanTimestamp(String dateTime) throws ParseException {
        return createCleanTimestamp(dateTime, TIME_FORMAT_DATASETS);
    }

    public static String timestampToString(Date timestamp, String format) {
        return new SimpleDateFormat(format).format(timestamp);
    }

    public static Date createCleanTimestamp(Date rawDate) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(rawDate);
        // round to 5 minutes
        //        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        //        int mod = unroundedMinutes % 5;
        //        calendar.add(Calendar.MINUTE, mod < 3 ? -mod : (5 - mod));
        // round to 1 minute
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static int getSecondsOfDay(Date rawDate) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(rawDate);
        int secondsOfDay = 0;
        secondsOfDay += calendar.get(Calendar.SECOND);
        secondsOfDay += calendar.get(Calendar.MINUTE) * 60;
        secondsOfDay += calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        return secondsOfDay;
    }

    public static Date addMinutesToTimestamp(Date timestamp, long minutes) {
        return new Date(addMinutesToTimestamp(timestamp.getTime(), minutes));
    }

    public static long addMinutesToTimestamp(long timestamp, long minutes) {
        timestamp += minutes * 60000; // 1 m = 60000 ms
        return timestamp;
    }

    public static boolean isGapSmallerThan(Date timestampOne, Date timestampTwo, int gapSizeInMinutes) {
        long gapMillies = Math.abs(timestampOne.getTime() - timestampTwo.getTime());
        int gapMinutes = (int) gapMillies / 60000;
        return gapSizeInMinutes > gapMinutes;
    }

    public static Date fromLocalDate(LocalDate inputDate) {
        return fromLocalDate(inputDate, 0);
    }

    public static Date getNextFullHour(Date timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        if (c.get(Calendar.MINUTE) != 0
                || c.get(Calendar.SECOND) != 0
                || c.get(Calendar.MILLISECOND) != 0) {
            c.add(Calendar.HOUR, 1);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        }
        return c.getTime();
    }

    /**
     * Converts a date and time string to a {@link Date} object in local time.
     * Strips seconds and milliseconds
     *
     * @param iso8601DateString ISO 8601 compliant date time string
     * @return local Date object representing the input with seconds and
     * milliseconds set to zero
     */
    public static Date fromIso8601DateString(String iso8601DateString) {
        TemporalAccessor t = DateTimeFormatter.ISO_DATE_TIME.parse(iso8601DateString);
        // dates are automatically converted to local time by the toInstant() method of ZonedDateTime
        Date date = Date.from(ZonedDateTime.from(t).toInstant());
        return TimestampUtils.createCleanTimestamp(date);
    }

    public static LocalTime dateToLocalTime(Date inputDate) {
        //https://blog.progs.be/542/date-to-java-time
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(inputDate.getTime()), ZoneId.systemDefault()).toLocalTime();
    }

    public static LocalDate dateToLocalDate(Date inputDate) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(inputDate.getTime()), ZoneId.systemDefault()).toLocalDate();
    }

    public static Date fromLocalDate(LocalDate inputDate, long offestInMilliseconds) {
        Date tmpInputDate = Date.from(Instant.from(inputDate
                .atStartOfDay(ZoneId.systemDefault())));
        if (offestInMilliseconds > 0) {
            tmpInputDate = new Date(tmpInputDate.getTime() + offestInMilliseconds);
        }
        return tmpInputDate;
    }

    public static int getHourOfDay(Date timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinuteOfHour(Date timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        return cal.get(Calendar.MINUTE);
    }

    /**
     * Changes the day but keeps the time unchanged
     *
     * @param timestamp The day which will be changed
     * @param source source for the new date
     * @return a Date with a changed year_month_day but the same time
     */
    public static Date setDayOfDate(Date timestamp, Date source) {
        Calendar calSource = Calendar.getInstance();
        calSource.setTime(source);
        return setDayOfDate(timestamp, calSource.get(Calendar.DAY_OF_YEAR), calSource.get(Calendar.YEAR));

    }

    /**
     * Changes the day but keeps the time unchanged
     *
     * @param timestamp The day which will be changed
     * @param sourceDay day-of-year for the new date
     * @param sourceYear year for the new date
     * @return
     */
    public static Date setDayOfDate(Date timestamp, int sourceDay, int sourceYear) {
        Calendar calTimestamp = Calendar.getInstance();
        calTimestamp.setTime(timestamp);
        calTimestamp.set(Calendar.DAY_OF_YEAR, sourceDay);
        calTimestamp.set(Calendar.YEAR, sourceYear);
        return calTimestamp.getTime();
    }

    public static Date setTimeOfDate(Date timestamp, int newHour, int newMinutes) {
        Calendar calTimestamp = Calendar.getInstance();
        calTimestamp.setTime(timestamp);
        calTimestamp.set(Calendar.HOUR_OF_DAY, newHour);
        calTimestamp.set(Calendar.MINUTE, newMinutes);
        return calTimestamp.getTime();
    }

    public static Date setTimeOfDate(Date timestamp, LocalTime newTime) {
        return setTimeOfDate(timestamp, newTime.getHour(), newTime.getMinute());
    }

    /**
     * Calculates the Date in the exact middle between two given dates
     *
     * @param first
     * @param second
     * @return
     */
    public static Date getMidDate(Date first, Date second) {
        long start;
        long end;
        if (first.before(second)) {
            start = first.getTime();
            end = second.getTime();
        } else {
            start = second.getTime();
            end = first.getTime();
        }
        return createCleanTimestamp(new Date(start + (end - start) / 2));
    }

    /**
     *
     * @param startTime Start of the time span
     * @param endTime end of the time span
     * @param timePoint checks if this time point is within timespan //not-found
     * param respectToDate if false, date will be ignored and just hour and
     * minutes are checked
     * @return
     */
    public static boolean withinDateTimeSpan(Date startTime, Date endTime,
            Date timePoint) {
        return startTime.before(timePoint) && endTime.after(timePoint)
                || startTime.equals(timePoint) || endTime.equals(timePoint);

    }

    public static boolean withinTimeSpan(LocalTime startTime, LocalTime endTime, LocalTime timepoint) {
        if (startTime.isBefore(endTime)) {
            // timespan is wihtin a day
            return (timepoint.isAfter(startTime) || timepoint.equals(startTime))
                    && (timepoint.isBefore(endTime) || timepoint.equals(endTime));
        } else {
            // timespan is not within a day (through midnight, e.g.  23:30 - 0:15)
            return (timepoint.isAfter(startTime) || timepoint.equals(startTime))
                    || (timepoint.isBefore(endTime) || timepoint.equals(endTime));
        }
    }

    public static boolean withinTimeSpan(LocalTime startTime, LocalTime endTime, Date timepoint) {

        LocalTime tp = dateToLocalTime(timepoint);
        if (startTime.isBefore(endTime)) {
            // timespan is wihtin a day
            return (tp.isAfter(startTime) || tp.equals(startTime))
                    && (tp.isBefore(endTime) || tp.equals(endTime));
        } else {
            // timespan is not within a day (through midnight, e.g.  23:30 - 0:15)
            return (tp.isAfter(startTime) || tp.equals(startTime))
                    || (tp.isBefore(endTime) || tp.equals(endTime));
        }
    }

    /**
     * Gets a series of time spans and merges overlapping spans according to the
     * <code>marginInMinutes</code> value set in the constructor
     *
     * @param timeSeries time spans to be merged
     * @param marginBefore margin before each timespamp
     * @param marginAfter margin after each timespamp
     * @return merged time series
     */
    public static List<Map.Entry<Date, Date>> normalizeTimeSeries(List<Map.Entry<Date, Date>> timeSeries, int marginBefore, int marginAfter) {
        List<Map.Entry<Date, Date>> result = new ArrayList<>();
        Date startOfCurentTimeSeries = null;
        Date lastTimeStamp = null;
        Date tempTimeStamp = null;
        for (Map.Entry<Date, Date> p : timeSeries) {
            if (startOfCurentTimeSeries == null) {
                //initial run of the loop
                startOfCurentTimeSeries = TimestampUtils.addMinutesToTimestamp(p.getKey(), (long) -1 * marginBefore);
                lastTimeStamp = TimestampUtils.addMinutesToTimestamp(p.getValue(), marginAfter);
                //for the comparison, adds 1 to lastTimeStamp to include Timestamps starting directly the minute after the last
            } else if (TimestampUtils.withinDateTimeSpan(startOfCurentTimeSeries, TimestampUtils.addMinutesToTimestamp(lastTimeStamp, 1), p.getKey())
                    || TimestampUtils.withinDateTimeSpan(startOfCurentTimeSeries, TimestampUtils.addMinutesToTimestamp(lastTimeStamp, 1), TimestampUtils.addMinutesToTimestamp(p.getKey(), (long) -1 * marginBefore))) {
                //Dates which start within the current time span, or would start within after margin has been applied
                tempTimeStamp = TimestampUtils.addMinutesToTimestamp(p.getValue(), marginAfter);
                if (!(TimestampUtils.withinDateTimeSpan(startOfCurentTimeSeries, lastTimeStamp, tempTimeStamp))) {
                    //the current time span extends to the end of the merged time span
                    lastTimeStamp = tempTimeStamp;
                }
            } else {
                //if no othe time span can be merged to the current span, it will be added to the result and the next span starts
                result.add(new AbstractMap.SimpleEntry<>(startOfCurentTimeSeries, lastTimeStamp));
                startOfCurentTimeSeries = TimestampUtils.addMinutesToTimestamp(p.getKey(), -1 * marginBefore);
                lastTimeStamp = TimestampUtils.addMinutesToTimestamp(p.getValue(), marginAfter);
            }

        }
        if (timeSeries.size() > 0) {
            result.add(new AbstractMap.SimpleEntry<>(startOfCurentTimeSeries, lastTimeStamp));
        }
        return result;
    }

    /**
     * Gets a series of time spans and merges overlapping spans according to the
     * <code>marginInMinutes</code> value set in the constructor
     *
     * @param timeSeries time spans to be merged
     * @param margin margin before and after each timespamp
     * @return merged time series
     */
    public static List<Map.Entry<Date, Date>> normalizeTimeSeries(List<Map.Entry<Date, Date>> timeSeries, int margin) {
        return normalizeTimeSeries(timeSeries, margin, margin);
    }

    /**
     * Generates a normalized TimeSeries with entry-Timestamps as input if no
     * pre-processed TimeSeries is aviable.
     * <p>
     * Has NOT the same effect as getting the filterResult.timeSeries of e.g.
     * NoneFilter, which would return a single Timestamp containing all entries.
     * This will return a List of ALL entry-timestamps, merged depending on the
     * margin
     *
     * @param data List of VaultEntrys, containing Timestamps
     * @param marginBefore margin before each timespamp
     * @param marginAfter margin after each timespamp
     * @return merged time series
     */
    public static List<Map.Entry<Date, Date>> getNormalizedTimeSeries(List<VaultEntry> data, int marginBefore, int marginAfter) {
        List<Map.Entry<Date, Date>> result = new ArrayList<>();
        for (VaultEntry vaultEntry : data) {
            result.add(new AbstractMap.SimpleEntry<>(vaultEntry.getTimestamp(), vaultEntry.getTimestamp()));
        }
        result = normalizeTimeSeries(result, marginBefore, marginAfter);
        return result;
    }

    /**
     * Generates a normalized TimeSeries with entry-Timestamps as input if no
     * pre-processed TimeSeries is aviable.
     * <p>
     * Has NOT the same effect as getting the filterResult.timeSeries of e.g.
     * NoneFilter, which would return a single Timestamp containing all entries.
     * This will return a List of ALL entry-timestamps, merged depending on the
     * margin
     *
     * @param data List of VaultEntrys, containing Timestamps
     * @param margin margin before and after each timespamp
     * @return merged time series
     */
    public static List<Map.Entry<Date, Date>> getNormalizedTimeSeries(List<VaultEntry> data, int margin) {
        return getNormalizedTimeSeries(data, margin, margin);
    }

    /**
     * Checks if the given date is within one of the given timeSeries.
     *
     * @param timeSeries
     * @param date
     * @return true, if the date is within one of the timeSeries
     */
    public static boolean withinTimeSeries(List<Map.Entry<Date, Date>> timeSeries, Date date) {
        boolean result = false;
        if (timeSeries != null) {
            for (Map.Entry<Date, Date> p : timeSeries) {
                if (TimestampUtils.withinDateTimeSpan(p.getKey(), p.getValue(), date)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Copies the specified date for secure getting and setting. This addresses
     * the possible malicious code vulnerability introduced by the mutability of
     * dates.
     *
     * @param date The date to be copied.
     * @return The safe copy of the date.
     */
    public static Date copyTimestamp(final Date date) {
        return new Date(date.getTime());
    }

    public static int getDurationInMinutes(Date start, Date stop) {
        return (int) (getDurationInSeconds(start, stop) / 60);
    }

    public static int getDurationInSeconds(Date start, Date stop) {
        return (int) ((stop.getTime() - start.getTime()) / 1000);
    }
}
