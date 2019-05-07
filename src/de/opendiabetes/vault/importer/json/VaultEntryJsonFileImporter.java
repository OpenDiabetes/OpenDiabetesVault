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
package de.opendiabetes.vault.importer.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.opendiabetes.vault.data.adapter.VaultEntryGsonAdapter;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.exporter.json.VaultEntryJsonExportObject;
import de.opendiabetes.vault.importer.FileImporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Importer for json files produced by the JsonFileExporter.
 *
 * @author juehv
 */
public class VaultEntryJsonFileImporter extends FileImporter {

    public VaultEntryJsonFileImporter(ImporterOptions options) {
        super(options);
    }

    @Override
    protected List<VaultEntry> processImport(InputStream fis) {
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(fis));

        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(VaultEntry.class, new VaultEntryGsonAdapter());

        Gson gson = gb.create();
        VaultEntryJsonExportObject dataContainer = gson.fromJson(reader, VaultEntryJsonExportObject.class);
        // TODO check version compatiblity

        return dataContainer.data;
    }

}
