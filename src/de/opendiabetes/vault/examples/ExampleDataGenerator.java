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

import de.opendiabetes.vault.data.container.LabelType;
import de.opendiabetes.vault.data.container.LabeledSliceEntry;
import de.opendiabetes.vault.data.container.RefinedVaultEntry;
import de.opendiabetes.vault.data.container.RefinedVaultEntryType;
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

    private static final String[] ALLOWED_TYPES = {"BOLUS_NORMAL", "BASAL_PROFILE", "GLUCOSE_CGM"};

    public static List<VaultEntry> generateVaultEntries(int noOfEntries) {
        Random random = new Random();
        Date timestamp = new Date();
        ArrayList<VaultEntry> entries = new ArrayList<>();

        for (int i = 0; i < noOfEntries; i++) {
            int randomType = random.nextInt(ALLOWED_TYPES.length);
            VaultEntry tmpEntry = new VaultEntry("Random_Generator", "Example_Code",
                    VaultEntryType.valueOfIgnoreCase(ALLOWED_TYPES[randomType]),
                    TimestampUtils.addMinutesToTimestamp(timestamp, i),
                    random.nextDouble() * 100);
            entries.add(tmpEntry);
        }

        return entries;
    }

    public static List<LabeledSliceEntry> generateLabeledSliceEntries(int noOfEntries) {
        Random random = new Random();
        Date timestamp = new Date();
        ArrayList<LabeledSliceEntry> entries = new ArrayList<>();

        for (int i = 0; i < noOfEntries; i++) {
            int randomType = random.nextInt(LabelType.values().length);
            LabeledSliceEntry tmpEntry = new LabeledSliceEntry("random generator",
                    LabelType.values()[randomType],
                    TimestampUtils.addMinutesToTimestamp(timestamp, i),
                    random.nextInt(300));
            entries.add(tmpEntry);
        }

        return entries;
    }

    static List<VaultEntry> generateMixedVaultEntries(int noOfEntries) {
        int splitNoOfEntries = (int) Math.round(noOfEntries * 0.80);
        List<VaultEntry> entries = generateVaultEntries(splitNoOfEntries);
        Date timestamp = entries.get(entries.size() - 1).getTimestamp();
        List<Double> values = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            values.add((random.nextInt(500) + 1) / 1000.0);
        }
        for (int i = 0; i < (noOfEntries - splitNoOfEntries); i++) {
            RefinedVaultEntryType usedType = RefinedVaultEntryType.CGM_PREDICTION;
            usedType.label = String.valueOf(random.nextInt(4));
            RefinedVaultEntry tmpEntry = new RefinedVaultEntry(
                    usedType,
                    TimestampUtils.addMinutesToTimestamp(timestamp, i + 1),
                    values.size());
            tmpEntry.setValueExtension(values);
            entries.add(tmpEntry);
        }
        return entries;
    }
}
