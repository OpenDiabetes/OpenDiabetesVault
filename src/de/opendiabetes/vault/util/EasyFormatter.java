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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * container class for standard formatting problems
 *
 * @author juehv
 */
public class EasyFormatter {

    public static final String DOUBLE_FORMAT = "%1$.2f";

    public static String formatDouble(double input) {
        return String.format(Locale.ENGLISH, DOUBLE_FORMAT, input).replace(",", "");
    }

    public static String formatTimestampToDefaultFormat(Date timestamp) {
        return TimestampUtils.timestampToString(timestamp,
                TimestampUtils.TIME_FORMAT_DATASETS);
    }

    public static String formatTimestampToLogEntry(Date timestamp) {
        return TimestampUtils.timestampToString(timestamp,
                "yyyy.MM.dd HH:mm");
    }

    public static String formatTimestampToFilename(Date timestamp) {
        return TimestampUtils.timestampToString(timestamp,
                "yyyy-MM-dd-HHmm");
    }

    public static String formatTimestampToIso8601(Date timestamp) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(TimestampUtils
                                .createCleanTimestamp(timestamp).getTime()),
                        ZoneId.systemDefault()));
    }
}
