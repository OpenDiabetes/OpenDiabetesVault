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
import de.opendiabetes.vault.data.adapter.SliceEntryJsonAdapter;
import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.exporter.ExportEntry;
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.util.SliceEntryUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporter class to export SliceEntry to Json (using SliceEntryJsonAdapter)
 *
 * @author juehv
 */
public class SliceEntryJsonFileExporter extends FileExporter<SliceEntry> {

    public SliceEntryJsonFileExporter(ExporterOptions options) {
        super(options, new SliceEntryUtils());
    }

    @Override
    protected List<ExportEntry> prepareData(List<SliceEntry> data) {
        // prepare export object
        SliceEntryJsonExportObject export = new SliceEntryJsonExportObject(data);

        // prepare GSON exporter
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(SliceEntry.class, new SliceEntryJsonAdapter());

        Gson gson = gb.create();
        String json = gson.toJson(export);

        // put into container
        List<ExportEntry> returnValue = new ArrayList<>();
        returnValue.add(new JsonExportEntry(json));

        return returnValue;
    }

    @Override
    public String getFileEnding() {
        return "json";
    }

}
