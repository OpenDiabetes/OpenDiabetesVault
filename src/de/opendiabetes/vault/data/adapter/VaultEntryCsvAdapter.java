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
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.csv.VaultEntryCsvExportEntry;
import de.opendiabetes.vault.util.EasyFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Adapter to convert VaultEntry to CsvExportEntry for CsvFileExporter.
 *
 * @author juehv
 */
public class VaultEntryCsvAdapter {

    public VaultEntryCsvExportEntry serialize(final VaultEntry entry) {
        ArrayList<String> csvRecord = new ArrayList<>();

        csvRecord.add(String.valueOf(entry.getTimestamp().getTime()));
        csvRecord.add(entry.getType().toString());

        // add value if needed
        switch (entry.getType()) {
            case CGM_SENSOR_START:
            case CGM_SENSOR_FINISHED:
            case CGM_CONNECTION_ERROR:
            case CGM_CALIBRATION_ERROR:
            case CGM_TIME_SYNC:
            case PUMP_REWIND:
            case PUMP_FILL:
            case PUMP_NO_DELIVERY:
            case PUMP_UNSUSPEND:
            case PUMP_RESERVOIR_EMPTY:
            case PUMP_TIME_SYNC:
            case TAG:
                csvRecord.add("");
                break;

            case REFINED_VAULT_ENTRY:
                LOG.warning("Exported RefinedVaultEntry without second value.");
            case BOLUS_NORMAL:
            case BOLUS_SQUARE:
            case BASAL_PROFILE:
            case BASAL_TEMP:
            case EXERCISE_MANUAL:
            case EXERCISE_OTHER:
            case EXERCISE_LOW:
            case EXERCISE_MID:
            case EXERCISE_HIGH:
            case GLUCOSE_CGM:
            case GLUCOSE_CGM_RAW:
            case GLUCOSE_CGM_ALERT:
            case GLUCOSE_CGM_CALIBRATION:
            case GLUCOSE_BG:
            case GLUCOSE_BG_MANUAL:
            case GLUCOSE_BOLUS_CALCULATION:
            case MEAL_BOLUS_CALCULATOR:
            case MEAL_MANUAL:
            case PUMP_PRIME:
            case PUMP_SUSPEND:
            case PUMP_AUTONOMOUS_SUSPEND:
            case PUMP_UNTRACKED_ERROR:
            case PUMP_CGM_PREDICTION:
            case SLEEP_LIGHT:
            case SLEEP_REM:
            case SLEEP_DEEP:
            case HEART_RATE:
            case HEART_RATE_VARIABILITY:
            case STRESS:
            case WEIGHT:
            case KETONES_BLOOD:
            case KETONES_URINE:
            case LOC_TRANSITION:
            case LOC_WORK:
            case LOC_FOOD:
            case LOC_SPORTS:
            case LOC_OTHER:
            case BLOOD_PRESSURE:
                csvRecord.add(EasyFormatter.formatDouble(entry.getValue()));
                break;
            default:
                LOG.severe("Programing Error: missing case for at least one type");
                throw new AssertionError("Programming Error: missing case for at least one type");
        }

        // add value extension if needed
        switch (entry.getType()) {

            case BOLUS_SQUARE:
            case BASAL_TEMP:
            case BLOOD_PRESSURE:
                csvRecord.add(EasyFormatter.formatDouble((Double) entry.getValueExtension()));
                break;

            case CGM_TIME_SYNC:
            case PUMP_TIME_SYNC:
                csvRecord.add(String.valueOf(((Date) entry.getValueExtension()).getTime()));
                break;

            case TAG:
                csvRecord.add((String) entry.getValueExtension());
                break;

            default:
                csvRecord.add("");
                break;
        }

        if (entry.getBase() != null) {
            csvRecord.add(entry.getBase().origin);
            csvRecord.add(entry.getBase().source);
        } else {
            csvRecord.add("");
            csvRecord.add("");
        }

        return new VaultEntryCsvExportEntry(csvRecord.toArray(new String[]{}));
    }
    private static final Logger LOG = Logger.getLogger(VaultEntryCsvAdapter.class.getName());

    public VaultEntry deserialize(final VaultEntryCsvExportEntry entry) {
        String origin = null;
        String source = null;
        VaultEntryType vType = null;
        Date timestamp = null;
        Double value = null;
        Object valueExtension = null;

        if (!entry.toCsvRecord()[4].isEmpty()) {
            origin = entry.toCsvRecord()[4];
        }
        if (!entry.toCsvRecord()[5].isEmpty()) {
            source = entry.toCsvRecord()[5];
        }

        if (!entry.toCsvRecord()[1].isEmpty()) {
            vType = VaultEntryType.valueOfIgnoreCase(entry.toCsvRecord()[1]);
        }
        if (vType == null) {
            throw new JsonParseException("No VaultEntryType found.");
        }

        if (!entry.toCsvRecord()[0].isEmpty()) {
            timestamp = new Date(Long.parseLong(entry.toCsvRecord()[0]));
        }
        if (timestamp == null) {
            throw new JsonParseException("No timestamp found.");
        }

        if (!entry.toCsvRecord()[2].isEmpty()) {
            value = Double.parseDouble(entry.toCsvRecord()[2]);
        }
        if (value == null) {
            value = 0.0;
        }

        if (!entry.toCsvRecord()[3].isEmpty()) {

            switch (vType) {

                case BOLUS_SQUARE:
                case BASAL_TEMP:
                case BLOOD_PRESSURE:
                    valueExtension = Double.parseDouble(entry.toCsvRecord()[3]);
                    break;

                case CGM_TIME_SYNC:
                case PUMP_TIME_SYNC:
                    valueExtension = new Date(Long.parseLong(entry.toCsvRecord()[3]));
                    break;

                case TAG:
                    valueExtension = entry.toCsvRecord()[3];
                    break;

                default:
                    LOG.warning("Did not import ValueExtension!");
                    break;
            }
        }

        // build and return
        VaultEntry returnValue;
        if (origin != null && source != null) {
            returnValue = new VaultEntry(origin, source, vType, timestamp, value);
        } else {
            returnValue = new VaultEntry(vType, timestamp, value);
        }
        if (valueExtension != null) {
            returnValue.setValueExtension(valueExtension);
        }

        return returnValue;
    }
}
