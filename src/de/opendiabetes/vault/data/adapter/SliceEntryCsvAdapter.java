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
package de.opendiabetes.vault.data.adapter;

import com.google.gson.JsonParseException;
import de.opendiabetes.vault.data.container.LabelType;
import de.opendiabetes.vault.data.container.LabeledSliceEntry;
import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.exporter.csv.CsvExportEntry;
import de.opendiabetes.vault.util.EasyFormatter;
import de.opendiabetes.vault.util.TimestampUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Adapter to convert SliceEntry and LabeledSliceEntry to CsvExportEntry for
 * CsvFileExporter.
 *
 * @author juehv
 */
public class SliceEntryCsvAdapter {

    private static final Logger LOG = Logger.getLogger(SliceEntryCsvAdapter.class.getName());

    public static String[] getCsvHeader() {
        return new String[]{
            "timestamp",
            "duration",
            "labelSource",
            "label"
        };
    }

    public CsvExportEntry serialize(final SliceEntry entry) {
        ArrayList<String> csvRecord = new ArrayList<>();

        csvRecord.add(EasyFormatter.formatTimestampToIso8601(entry.startTimestamp));
        csvRecord.add(EasyFormatter.formatDouble(entry.durationInMinutes));

        if (entry instanceof LabeledSliceEntry) {
            csvRecord.add(((LabeledSliceEntry) entry).labelSource);
            csvRecord.add(((LabeledSliceEntry) entry).type.toString());
        }

        return new CsvExportEntry(csvRecord.toArray(new String[]{}));
    }

    public SliceEntry deserialize(final CsvExportEntry entry) {
        Date timestamp = null;
        Double duration = null;
        LabelType vType = null;
        String source = null;

        if (!entry.toCsvRecord()[0].isEmpty()) {
            timestamp = TimestampUtils.fromIso8601DateString(entry.toCsvRecord()[0]);
        }
        if (timestamp == null) {
            throw new JsonParseException("No timestamp found.");
        }

        if (!entry.toCsvRecord()[1].isEmpty()) {
            duration = Double.parseDouble(entry.toCsvRecord()[1]);
        }
        if (duration == null) {
            duration = 0.0;
        }

        if (entry.toCsvRecord().length >= 2) {
            if (!entry.toCsvRecord()[2].isEmpty()) {
                source = entry.toCsvRecord()[2];
            }

            if (!entry.toCsvRecord()[3].isEmpty()) {
                vType = LabelType.valueOf(entry.toCsvRecord()[3]);
            }
        }

        // build and return
        SliceEntry returnValue;
        if (source != null || vType != null) {
            returnValue = new LabeledSliceEntry(source, vType, timestamp, (int) Math.round(duration));
        } else {
            returnValue = new SliceEntry(timestamp, (int) Math.round(duration));
        }

        return returnValue;
    }
}
