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
import de.opendiabetes.vault.data.container.LabelType;
import de.opendiabetes.vault.data.container.LabeledSliceEntry;
import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.util.EasyFormatter;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This class implements a JSON serializer for GSON based SliceEntries and LabeledSliceEntries.
 *
 * @author juehv
 */
public class SliceEntryJsonAdapter implements JsonSerializer<SliceEntry>, JsonDeserializer<SliceEntry> {

    private static final Logger LOG = Logger.getLogger(SliceEntryJsonAdapter.class.getName());

    /**
     * Serializer for VaultEntries.
     *
     * @param entry VaultEntry to be serialized.
     * @param type Type of the entry.
     * @param jsc Context for the serializer.
     * @return Serialized VaultEntry as JSON element.
     */
    @Override
    public JsonElement serialize(final SliceEntry entry, final Type type,
            final JsonSerializationContext jsc) {
        JsonObject obj = new JsonObject();
        obj.addProperty("epoch", entry.startTimestamp.getTime());
        obj.addProperty("isoTime", EasyFormatter.formatTimestampToIso8601(
                entry.startTimestamp));
        obj.addProperty("duration", entry.durationInMinutes);

        if (entry instanceof LabeledSliceEntry) {
            obj.addProperty("label", ((LabeledSliceEntry) entry).type.toString());
            obj.addProperty("labelSource", ((LabeledSliceEntry) entry).labelSource);
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
    public SliceEntry deserialize(final JsonElement element, final Type type,
            final JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();

        Date timestamp = null;
        Integer duration = null;
        String source = null;
        LabelType label = null;


        if (obj.get("epoch") != null) {
            timestamp = new Date(obj.get("epoch").getAsLong());
        }
        if (timestamp == null) {
            throw new JsonParseException("No timestamp found.");
        }

        if (obj.get("duration") != null) {
            duration = obj.get("duration").getAsInt();
        }
        if (duration == null) {
            duration = 0;
        }
        
        
        if (obj.get("labelSource") != null) {
            source = obj.get("labelSource").getAsString();
        }

        if (obj.get("label") != null && !obj.get("label").getAsString().isEmpty()) {
            label = LabelType.valueOfIgnoreCase(obj.get("label").getAsString());
        }

        // build and return
        SliceEntry returnValue;
        if (source != null || label != null) {
            returnValue = new LabeledSliceEntry(source, label, timestamp, (int) Math.round(duration));
        } else {
            returnValue = new SliceEntry(timestamp, (int) Math.round(duration));
        }

        return returnValue;
    }

}
