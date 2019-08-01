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
import de.opendiabetes.vault.exporter.csv.BucketCsvFileExporter;
import de.opendiabetes.vault.exporter.csv.BucketExporterOptions;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.json.VaultEntryJsonFileImporter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class to illustrate the usage of the BucketCsvFileExporter. The idea behind a
 * bucket is to combine a bunch of values in one bucket (one line of the csv).
 * The temporal distance between buckets (lines of csv file) is fixed.
 *
 * The data has to be preprocessed to provide the needed data for the exporter:
 * * Provide at least the same (or a smaller) time scale in data (e.g. every
 * minute in data for 5 minute buckets). * Interpolate missing values (e.g. for
 * CGM data) * Add empty entries (e.g. Bolus of 0.0) to fill gaps
 *
 *
 * @author Jens
 */
public class ExportBucketCsvFileExample {

    static final String filePath = System.getProperty("user.home") + "/exampleBuckets.csv";


    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.IllegalAccessException
     */
    public static void main(String[] args) throws IOException, IllegalAccessException {
        // get data
        List<VaultEntry> entries = ExampleDataGenerator.generateMixedVaultEntries(1000);

        // process data to fit the bucket format
        //TODO
        // export to 5 min buckets
        BucketCsvFileExporter exporter = new BucketCsvFileExporter(new BucketExporterOptions(5));
        exporter.exportDataToFile(filePath, entries, false);

        System.out.println("Delete example file at:" + filePath + " ? (y/n)");
        char key = (char) System.in.read();
        if (key == 'y') {
            new File(filePath).delete();
        }
    }
}
