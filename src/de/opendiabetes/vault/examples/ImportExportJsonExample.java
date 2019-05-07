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
import de.opendiabetes.vault.exporter.json.VaultEntryJsonFileExporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.json.VaultEntryJsonFileImporter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Example how to use Json exporter.
 *
 * @author juehv
 */
public class ImportExportJsonExample {

    static final String jsonFilePath = System.getProperty("user.home") + "/example.json";

    public static void exportJson() {
        // create some example data        
        List<VaultEntry> entries = ExampleDataGenerator.generate(1000);

        // export
        VaultEntryJsonFileExporter exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
        exporter.exportDataToFile(jsonFilePath, entries);
    }

    public static void importJson() throws IllegalAccessException {
        VaultEntryJsonFileImporter importer = new VaultEntryJsonFileImporter(new ImporterOptions());
        List<VaultEntry> entries = importer.importDataFromFile(jsonFilePath);
        System.out.println("Imported " + entries.size() + " Entries!");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, IllegalAccessException {
        exportJson();
        importJson();

        System.out.println("Delete example file at:" + jsonFilePath + " ? (y/n)");
        char key = (char) System.in.read();
        if (key == 'y') {
            new File(jsonFilePath).delete();
        }
    }

}
