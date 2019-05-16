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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.util.EasyFormatter;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This class implements a JSON serializer for GSON based VaultEntries.
 *
 * @author juehv
 */
public class VaultEntryJsonAdapter implements JsonSerializer<VaultEntry>, JsonDeserializer<VaultEntry> {

    private static final Logger LOG = Logger.getLogger(VaultEntryJsonAdapter.class.getName());

    /**
     * Serializer for VaultEntries.
     *
     * @param entry VaultEntry to be serialized.
     * @param type Type of the entry.
     * @param jsc Context for the serializer.
     * @return Serialized VaultEntry as JSON element.
     */
    @Override
    public JsonElement serialize(final VaultEntry entry, final Type type,
            final JsonSerializationContext jsc) {
        JsonObject obj = new JsonObject();
        if (entry.getBase() != null) {
            obj.addProperty("origin", entry.getBase().origin);
            obj.addProperty("source", entry.getBase().source);
        }
        obj.addProperty("type", entry.getType().toString());
        obj.addProperty("epoch", entry.getTimestamp().getTime());
        obj.addProperty("isoTime", EasyFormatter.formatTimestampToIso8601(
                entry.getTimestamp()));

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
                // nothing to do here
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
                obj.addProperty("value", EasyFormatter.formatDouble(entry.getValue()));
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
                obj.addProperty("valueExtension", (Double) entry.getValueExtension());
                break;

            case CGM_TIME_SYNC:
            case PUMP_TIME_SYNC:
                obj.addProperty("valueExtension", ((Date) entry.getValueExtension()).getTime());
                break;

            case TAG:
                obj.addProperty("valueExtension", (String) entry.getValueExtension());
                break;

            default:
                break;
        }

        return obj;
    }

    /**
     * Deserializer for JSON data.
     *
     * @param element The JSON element to deserialize.
     * @param type The type of the element.
     * @param jdc Context for the deserializer.
     * @return De-serialized JSON element.
     * @throws JsonParseException Thrown if JSON element is faulty.
     */
    @Override
    public VaultEntry deserialize(final JsonElement element, final Type type,
            final JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();

        String origin = null;
        String source = null;
        VaultEntryType vType = null;
        Date timestamp = null;
        Double value = null;
        Object valueExtension = null;

        if (obj.get("origin") != null) {
            origin = obj.get("origin").getAsString();
        }
        if (obj.get("source") != null) {
            source = obj.get("source").getAsString();
        }

        if (obj.get("type") != null) {
            vType = VaultEntryType.valueOfIgnoreCase(obj.get("type").getAsString());
        }
        if (vType == null) {
            throw new JsonParseException("No VaultEntryType found.");
        }

        if (obj.get("epoch") != null) {
            timestamp = new Date(obj.get("epoch").getAsLong());
        }
        if (timestamp == null) {
            throw new JsonParseException("No timestamp found.");
        }

        if (obj.get("value") != null) {
            value = obj.get("value").getAsDouble();
        }
        if (value == null) {
            value = 0.0;
        }

        if (obj.get("valueExtension") != null) {

            switch (vType) {

                case BOLUS_SQUARE:
                case BASAL_TEMP:
                case BLOOD_PRESSURE:
                    valueExtension = obj.get("valueExtension").getAsDouble();
                    break;

                case CGM_TIME_SYNC:
                case PUMP_TIME_SYNC:
                    valueExtension = new Date(obj.get("valueExtension").getAsLong());
                    break;

                case TAG:
                    valueExtension = obj.get("valueExtension").getAsString();
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
