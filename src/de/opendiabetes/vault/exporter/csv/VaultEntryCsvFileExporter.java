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
package de.opendiabetes.vault.exporter.csv;

import de.opendiabetes.vault.data.adapter.VaultEntryCsvAdapter;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.ExportEntry;
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.FileExporter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporter class for exporting VaultEntries to a simple csv format.
 *
 * @author juehv
 */
public class VaultEntryCsvFileExporter extends FileExporter {

    public VaultEntryCsvFileExporter(ExporterOptions options) {
        super(options);
    }

    @Override
    protected List<ExportEntry> prepareData(List<VaultEntry> data) {
        List<ExportEntry> returnValue = new ArrayList<>();
        VaultEntryCsvAdapter adapter = new VaultEntryCsvAdapter();

        returnValue.add(VaultEntryCsvExportEntry.getHeaderEntry());
        for (VaultEntry item : data) {
            if (!options.exportRefinedVaultEntries
                    && item.getType() == VaultEntryType.REFINED_VAULT_ENTRY) {
                continue;
            }
            returnValue.add(adapter.serialize(item));
        }

        return returnValue;
    }

    @Override
    public String getFileEnding() {
        return "csv";
    }

}
