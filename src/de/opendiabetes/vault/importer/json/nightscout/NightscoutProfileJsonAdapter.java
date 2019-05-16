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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.opendiabetes.vault.util.TimestampUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 * This class implements a JSON serializer for GSON based VaultEntries.
 *
 * @author juehv
 */
public class NightscoutProfileJsonAdapter implements JsonDeserializer<NightscoutBasalProfilesContainer> {

    private static final Logger LOG = Logger.getLogger(NightscoutProfileJsonAdapter.class.getName());

    @Override
    public NightscoutBasalProfilesContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        NightscoutBasalProfilesContainer importObject = new NightscoutBasalProfilesContainer();
        JsonArray profileRecords = json.getAsJsonArray();

        // iterate records of the NS profile 
        Iterator<JsonElement> iter = profileRecords.iterator();
        while (iter.hasNext()) {
            JsonObject obj = iter.next().getAsJsonObject();
            JsonObject profileStore = null;

            Date validFrom = null;
            List<NightscoutBasalProfile> profiles = new ArrayList<>();

            if (obj.get("startDate") != null) {
                validFrom = TimestampUtils.fromIso8601DateString(obj.get("startDate").getAsString());
            }
            if (obj.get("store") != null) {
                profileStore = obj.get("store").getAsJsonObject();
            }
            if (profileStore != null) {
                // iterate store for this record and add profiles
                for (String item : profileStore.keySet()) {
                    JsonObject profile = profileStore.get(item).getAsJsonObject();
                    if (profile.get("basal") != null) {
                        // iterate basal time steps to create incomplete profile
                        List<Pair<Integer, Double>> profileTimeSteps = new ArrayList<>();
                        Iterator<JsonElement> basalIter = profile.get("basal").getAsJsonArray().iterator();
                        while (basalIter.hasNext()) {
                            JsonObject basalTimeStep = basalIter.next().getAsJsonObject();
                            if (basalTimeStep.get("timeAsSeconds") != null && basalTimeStep.get("timeAsSeconds") != null) {
                                int startSeconds = basalTimeStep.get("timeAsSeconds").getAsInt();
                                double value = basalTimeStep.get("value").getAsDouble();
                                profileTimeSteps.add(new Pair<>(startSeconds, value));
                            }
                        }

                        // create complete profile from data
                        profiles.add(NightscoutBasalProfile.fromIncompleteList(profileTimeSteps));
                    }
                }
            }
            if (validFrom != null && !profiles.isEmpty()) {
                importObject.records.put(validFrom, profiles);
            }
        }
        return importObject;
    }

}
