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
package de.opendiabetes.vault.exporter.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.adapter.VaultEntryJsonAdapter;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.exporter.ExportEntry;
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.util.VaultEntryUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporter class to export VaultEntry to Json (using VaultEntryGSONAdapter)
 *
 * @author juehv
 */
public class VaultEntryJsonFileExporter extends FileExporter<VaultEntry> {

    public VaultEntryJsonFileExporter(ExporterOptions options) {
        super(options, new VaultEntryUtils());
    }

    @Override
    protected List<ExportEntry> prepareData(List<VaultEntry> data) {
        // filter data if needed
        List<VaultEntry> filteredData;
        if (options.exportRefinedVaultEntries) {
            filteredData = data;
        } else {
            filteredData = new ArrayList<>();
            for (VaultEntry item : data) {
                if (item.getType() != VaultEntryType.REFINED_VAULT_ENTRY) {
                    filteredData.add(item);
                }
            }
        }

        // prepare export object
        VaultEntryJsonExportObject export = new VaultEntryJsonExportObject(filteredData);

        // prepare GSON exporter
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(VaultEntry.class, new VaultEntryJsonAdapter());

        Gson gson = gb.create();
        String json = gson.toJson(export);

        // put into container
        List<ExportEntry> returnValue = new ArrayList<>();
        returnValue.add(new VaultEntryJsonExportEntry(json));

        return returnValue;
    }

    @Override
    public String getFileEnding() {
        return "json";
    }

}
