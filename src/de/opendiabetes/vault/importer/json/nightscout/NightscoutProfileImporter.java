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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * Parser for NightscoutProfile. Needed to import Nightscout data with basal
 * information.
 *
 * @author juehv
 */
public class NightscoutProfileImporter {

    protected Gson gson;

    /**
     * Constructor that also initiates Gson and Serialzation of "null"-values
     *
     */
    public NightscoutProfileImporter() {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(NightscoutBasalProfilesContainer.class, new NightscoutProfileJsonAdapter());
        gson = gb.create();
    }

    /**
     *
     * @param pathToFile path to (already checked) file. Can't handle null.
     * @return
     * @throws FileNotFoundException
     */
    public NightscoutBasalProfilesContainer readProfileFile(String pathToFile) throws FileNotFoundException {
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(new File(pathToFile))));

        NightscoutBasalProfilesContainer dataContainer = gson.fromJson(reader, NightscoutBasalProfilesContainer.class);

        return dataContainer;
    }
}
