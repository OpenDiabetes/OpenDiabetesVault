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
import de.opendiabetes.vault.importer.json.nightscout.NightscoutBasalProfilesContainer;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutImporter;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutImporterOptions;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutProfileImporter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Example how to use Nightscout importer (as it needs a profile import first). Note that you need data to run this example.
 *
 * @author juehv
 */
public class ImportNightscoutDataExample {

    /**
     * @param args the command line arguments
     * 
     * @throws java.io.FileNotFoundException
     * @throws java.lang.IllegalAccessException
     */
    public static void main(String[] args) throws FileNotFoundException, IllegalAccessException {
        File profile = new File("./profile.json");
        File entries = new File("./entries.json");
        
        if (profile.exists() && profile.canRead()
                && entries.exists() && entries.canRead()) {
            NightscoutProfileImporter json = new NightscoutProfileImporter();

            NightscoutBasalProfilesContainer result = json.readProfileFile(
                    profile.getAbsolutePath());
            result.toString();

            NightscoutImporter entryImporter = new NightscoutImporter(
                    new NightscoutImporterOptions(result));
            List<VaultEntry> importResult = entryImporter.importDataFromFile(entries.getAbsolutePath());
            System.out.println("Imported " + importResult.size() + " entries!");
        } else {
            System.out.println("File not found");
        }
    }

}
