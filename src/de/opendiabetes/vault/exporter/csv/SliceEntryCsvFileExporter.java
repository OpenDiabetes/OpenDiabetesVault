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

import de.opendiabetes.vault.data.adapter.SliceEntryCsvAdapter;
import de.opendiabetes.vault.data.adapter.VaultEntryCsvAdapter;
import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.ExportEntry;
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.util.SliceEntryUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporter for SliceEntry container to CSV files
 *
 * @author juehv
 */
public class SliceEntryCsvFileExporter extends FileExporter<SliceEntry> {

    public SliceEntryCsvFileExporter(ExporterOptions options) {
        super(options, new SliceEntryUtils());
    }

    @Override
    public String getFileEnding() {
        return "csv";
    }

    @Override
    protected List<ExportEntry> prepareData(List<SliceEntry> data) {
        
        List<ExportEntry> returnValue = new ArrayList<>();
        SliceEntryCsvAdapter adapter = new SliceEntryCsvAdapter();

        returnValue.add(new CsvExportEntry(SliceEntryCsvAdapter.getCsvHeader()));
        for (SliceEntry item : data) {
            returnValue.add(adapter.serialize(item));
        }

        return returnValue;
    }

}
