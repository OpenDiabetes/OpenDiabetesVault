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

import de.opendiabetes.vault.util.TimestampUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 * Container Object for a Nightscout basal Profile
 *
 * @author juehv
 */
public class NightscoutBasalProfile {

    private static final Logger LOG = Logger.getLogger(NightscoutBasalProfile.class.getName());

    private final List<Double> profile;

    public NightscoutBasalProfile(List<Double> profile) {
        this.profile = profile;
        if (profile.size() != 24) {
            throw new AssertionError("PROGRAMMING ERROR: Basal profile has to contain 24 entries. Use fromIncompleteList instead.");
        }
    }

    public static NightscoutBasalProfile fromIncompleteList(List<Pair<Integer, Double>> incompleteBasalProfile) {
        if (incompleteBasalProfile == null || incompleteBasalProfile.isEmpty()) {
            return null;
        }

        List< Double> completeBasalProfile = new ArrayList<>();

        // sort input data
        incompleteBasalProfile.sort(new Comparator<Pair<Integer, Double>>() {
            @Override
            public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // create complete profile
        int lastTimeStep = 0;
        for (int nextTimeStep = 3600; nextTimeStep <= 86400; nextTimeStep += 3600) {
            List<Pair<Integer, Double>> tmpEntries = new ArrayList<>();
            // find entires for this time step
            for (Pair<Integer, Double> item : incompleteBasalProfile) {
                if (lastTimeStep <= item.getKey() && item.getKey() < nextTimeStep) {
                    tmpEntries.add(item);
                } else if (item.getKey() >= nextTimeStep) {
                    break;
                }
            }

            // add entries for this time step
            if (tmpEntries.isEmpty()) {
                // no entry for this timestamp --> copy last one
                if (completeBasalProfile.isEmpty()) {
                    // no first entry found .. this person has no idea how to use a pump .. we'll use the first entry we can find
                    completeBasalProfile.add(incompleteBasalProfile.get(0).getValue());
                } else {
                    double lastValue = completeBasalProfile.get(
                            completeBasalProfile.size() - 1);
                    completeBasalProfile.add(lastValue);
                }
            } else if (tmpEntries.size() == 1) {
                // found one entry --> add it
                completeBasalProfile.add(tmpEntries.get(0).getValue());
            } else {
                // found more than one entry --> patient should go to some training ... but anyway, we'll calculate an avg
                double avgValue = 0;
                for (Pair<Integer, Double> item : tmpEntries) {
                    avgValue += item.getValue();
                }
                avgValue /= tmpEntries.size();
                completeBasalProfile.add(avgValue);
            }
            // prepare for next time step
            lastTimeStep = nextTimeStep;
        }

        return new NightscoutBasalProfile(completeBasalProfile);
    }

    public double getBasalForTimeOfDay(Date timePointOfDay) {
        return getBasalForTimeOfDay(TimestampUtils
                .getSecondsOfDay(timePointOfDay));
    }

    public double getBasalForTimeOfDay(int secondsOfDay) {
        int slot = secondsOfDay / 3600;
        if (slot > 23) {
            LOG.log(Level.WARNING, "Requested impossible Basal slot: {0}", slot);
            slot = 23;
        }
        return profile.get(slot);
    }

    public List<Double> getProfile() {
        return profile;
    }

    public double getAmount() {
        double amount = 0;
        for (Double item : profile) {
            amount += item;
        }
        return amount;
    }

}
