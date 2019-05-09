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
package de.opendiabetes.vault.examples;

import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.util.TimestampUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Generator for random example data (does not reflect real data but is used to
 * test exporter).
 *
 * @author jeuhv
 */
public class ExampleDataGenerator {

    private static final String[] allowedTypes = {"BOLUS_NORMAL", "BASAL_PROFILE", "GLUCOSE_CGM"};

    public static List<VaultEntry> generate(int noOfEntries) {
        Random random = new Random();
        Date timestamp = new Date();
        ArrayList<VaultEntry> entries = new ArrayList<>();

        for (int i = 0; i < noOfEntries; i++) {
            int randomType = random.nextInt(allowedTypes.length);
            VaultEntry tmpEntry = new VaultEntry("Random_Generator", "Example_Code",
                    VaultEntryType.valueOfIgnoreCase(allowedTypes[randomType]),
                    TimestampUtils.addMinutesToTimestamp(timestamp, i),
                    random.nextDouble() * 100);
            entries.add(tmpEntry);
        }

        return entries;
    }
}
