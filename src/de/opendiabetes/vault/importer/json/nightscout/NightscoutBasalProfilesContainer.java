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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Container class for nightscout profile records
 *
 * @author juehv
 */
public class NightscoutBasalProfilesContainer {

    public final HashMap<Date, List<NightscoutBasalProfile>> records = new HashMap<>();

    @Override
    public String toString() {
        return "NightscoutBasalProfilesContainer{" + "records=" + records + '}';
    }

    /**
     * As we currently have no idea how to find out which profile was active, we
     * choose one which was valid and has many different values (as this looks
     * more refined). However, OpenAPS users usualy run on tmp basal all the
     * time.
     *
     * @param date
     * @return a more or less randomly selected Nightscout Profile
     */
    public NightscoutBasalProfile findProfileForDate(Date date) {
        if (records.isEmpty()) {
            return null;
        }

        // find best fitting record
        Iterator<Date> iter = records.keySet().iterator();
        Date bestBet = null;
        // - prime
        while (iter.hasNext()) {
            bestBet = iter.next();
            if (records.get(bestBet).isEmpty()) {
                bestBet = null;
            } else {
                break;
            }
        }
        if (bestBet == null) {
            return null;
        }

        // - search
        for (Date item : records.keySet()) {
            if (item.after(bestBet) && item.before(date)) {
                if (!records.get(item).isEmpty()) {
                    bestBet = item;
                }
            }
        }

        // choose profile of record
        List<NightscoutBasalProfile> profiles = records.get(bestBet);
        NightscoutBasalProfile bestCandidate = null;
        int changes = 0;
        double amount = 0;

        for (NightscoutBasalProfile item : profiles) {
            int tmpChanges = 0;
            double lastValue = 0;
            for (Double value : item.getProfile()) {
                if (value != lastValue) {
                    tmpChanges++;
                    lastValue = value;
                }
            }
            if (tmpChanges > changes) {
                // we prefere more changes as it feels better (no science here)
                changes = tmpChanges;
                amount = item.getAmount();
                bestCandidate = item;
            } else if (tmpChanges == changes && item.getAmount() < amount && item.getAmount() > 0) {
                // we prefere lower values as it should contain less silent meals
                amount = item.getAmount();
                bestCandidate = item;
            }
        }
        return bestCandidate;
    }

}
