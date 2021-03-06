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
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.csv.VaultEntryCsvFileExporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.csv.VaultEntryCsvFileImporter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Example code for exporting and importing data from/to csv files
 *
 * @author mswin
 */
public class ImportExportCsvExample {

    static final String csvFilePath = System.getProperty("user.home") + "/example.csv";

    public static void exportCsv(List<VaultEntry> entries) {

        // export
        VaultEntryCsvFileExporter exporter = new VaultEntryCsvFileExporter(new ExporterOptions());
        exporter.exportDataToFile(csvFilePath, entries, false);
        System.out.println(entries.size() + " entries exported.");
    }

    public static List<VaultEntry> importCsv() throws IllegalAccessException {
        VaultEntryCsvFileImporter importer = new VaultEntryCsvFileImporter(new ImporterOptions());
        List<VaultEntry> entries = importer.importDataFromFile(csvFilePath);
        System.out.println("Imported " + entries.size() + " entries!");
        return entries;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, IllegalAccessException {
        // create some example data
        List<VaultEntry> entries = ExampleDataGenerator.generateVaultEntries(1000);
        exportCsv(entries);
        List<VaultEntry> importedEntries = importCsv();

        // check entries
        if (entries.equals(importedEntries)) {
            System.out.println("Entries equal.");
        } else {
            System.err.println("Entries NOT equal!!!");
        }

        System.out.println("Delete example file at:" + csvFilePath + " ? (y/n)");
        char key = (char) System.in.read();
        if (key == 'y') {
            new File(csvFilePath).delete();
        }
    }
}
