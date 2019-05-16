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
package de.opendiabetes.vault.importer.json.nightscout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.importer.FileImporter;
import de.opendiabetes.vault.util.TimestampUtils;
import de.opendiabetes.vault.util.VaultEntryUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javafx.util.Pair;

/**
 * Imports JSON objects from a Nightscout server as {@link VaultEntry}s.
 *
 * @author unknown, juehv
 */
public class NightscoutImporter extends FileImporter {

    private final JsonParser json;
    private final NightscoutBasalProfilesContainer profiles;

    public static final String SOURCE = "Nightscout";

    public NightscoutImporter(NightscoutImporterOptions options) {
        // TODO NS does not support data types which result in RefinedVaultEntry yet.
        super(options);
        this.json = new JsonParser();
        this.profiles = options.basalProfilesContainer;
    }

    /**
     * Parses the source as a nightscout json representation of vault entries.
     * Expects an array of json objects. If an object in the array contains
     * information for multiple vault entry types, it will be split up into
     * individual entries. Currently supports the following entries: {@link VaultEntryType#GLUCOSE_CGM}, {@link VaultEntryType#BOLUS_NORMAL},
     * {@link VaultEntryType#MEAL_MANUAL} and
     * {@link VaultEntryType#BASAL_MANUAL}.
     *
     * @param fis Data source.
     * @return list of generated vault entries. May contain more entries than
     * the source did.
     */
    @Override
    protected List<VaultEntry> processImport(InputStream fis) {
        Reader reader = new InputStreamReader(fis);
        JsonElement element;
        try {
            element = json.parse(reader);
        } catch (JsonParseException e) {
            LOG.severe("Exception while reading data");
            return null;
        }

        if (!element.isJsonArray()) {
            LOG.severe("Source is not an array");
            return null;
        }

        List<VaultEntry> entries = new ArrayList<>();
        try {
            for (JsonElement e : element.getAsJsonArray()) {
                JsonObject o = e.getAsJsonObject();
                boolean valid = false;

                Date date = null;
                String origin = "unknown";
                // CGM measurements
                if (o.has("type") && o.get("type").getAsString().equals("sgv")) {
                    if (o.has("device") && !o.get("device").getAsString().isEmpty()) {
                        origin = o.get("device").getAsString();
                    }
                    date = TimestampUtils.fromIso8601DateString(o.get("dateString").getAsString());
                    entries.add(new VaultEntry(origin, SOURCE, VaultEntryType.GLUCOSE_CGM, date, o.get("sgv").getAsDouble()));
                    valid = true;
                }

                // insulin bolus
                if (o.has("insulin") && !o.get("insulin").isJsonNull()) {
                    date = TimestampUtils.fromIso8601DateString(o.get("timestamp").getAsString());
                    entries.add(new VaultEntry(origin, SOURCE, VaultEntryType.BOLUS_NORMAL, date, o.get("insulin").getAsDouble()));
                    valid = true;
                }

                // meals
                if (o.has("carbs") && !o.get("carbs").isJsonNull()) {
                    date = TimestampUtils.fromIso8601DateString(o.get("timestamp").getAsString());
                    entries.add(new VaultEntry(origin, SOURCE, VaultEntryType.MEAL_MANUAL, date, o.get("carbs").getAsDouble()));
                    valid = true;
                }

                // temporary basal
                if (o.has("eventType") && o.get("eventType").getAsString().equals("Temp Basal")) {
                    date = TimestampUtils.fromIso8601DateString(o.get("timestamp").getAsString());
                    VaultEntry tmpEntry = new VaultEntry(origin, SOURCE, VaultEntryType.BASAL_TEMP, date, o.get("rate").getAsDouble());
                    tmpEntry.setValueExtension(o.get("duration").getAsDouble());
                    entries.add(tmpEntry);
                    valid = true;
                }

                if (!valid) {
                    LOG.log(Level.WARNING, "Could not parse JSON Object: {0}", o.toString());
                    continue;

                }
            }
        } catch (NumberFormatException | IllegalStateException | NullPointerException e) {
            LOG.severe("Invalid source data");
            return null;
        }

        // sort entries by date
        entries.sort(new VaultEntryUtils());
        Date startDate = entries.get(0).getTimestamp();
        Date endDate = entries.get(entries.size() - 1).getTimestamp();

        // check for big gaps (4 hours)
        Date lastTimestamp = startDate;
        List<Pair<Date, Date>> gaps = new ArrayList<>();
        for (VaultEntry item : entries) {
            if (!TimestampUtils.gapSmallerThan(lastTimestamp, item.getTimestamp(), 240)) {
                gaps.add(new Pair<>(lastTimestamp, item.getTimestamp()));
            }
            lastTimestamp = item.getTimestamp();
        }

        // generate basal entries (except inside gaps)
        Date generateFrom = startDate;
        Date generateTo;
        ArrayList<VaultEntry> basalProfileEntries = new ArrayList<>();
        Iterator<Pair<Date, Date>> iter = gaps.iterator();
        while (iter.hasNext()) {
            // generate for this sub path
            Pair<Date, Date> gap = iter.next();
            generateTo = gap.getKey();

            basalProfileEntries.addAll(generateBasalEntries(generateFrom, generateTo));

            generateFrom = gap.getValue();
        }
        // generate for last path
        generateTo = endDate;
        basalProfileEntries.addAll(generateBasalEntries(generateFrom, generateTo));

        // merge
        entries.addAll(basalProfileEntries);
        entries.sort(new VaultEntryUtils());

        return entries;
    }

    private List<VaultEntry> generateBasalEntries(Date from, Date to) {
        NightscoutBasalProfile profile = profiles.findProfileForDate(from);
        if (profile != null) {
            ArrayList<VaultEntry> basalProfileEntries = new ArrayList<>();

            // get first timestamp
            Date nextTimeStamp = TimestampUtils.getNextFullHour(from);

            // generate entries
            while (nextTimeStamp.before(to)) {
                double value = profile.getBasalForTimeOfDay(nextTimeStamp);
                basalProfileEntries.add(new VaultEntry("Nightscout Basal Profile", SOURCE, VaultEntryType.BASAL_PROFILE,
                        nextTimeStamp, value));
                nextTimeStamp = TimestampUtils.addMinutesToTimestamp(nextTimeStamp, 60);
                LOG.log(Level.INFO, "Generate basal profile item with value: {0}", value);
            }
            return basalProfileEntries;
        } else {
            LOG.warning("Did not find any suitable basal profile! Can't add basal_profile entries.");
        }

        return null;
    }
}
