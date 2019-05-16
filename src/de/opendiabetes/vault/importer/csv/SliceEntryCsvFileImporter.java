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
package de.opendiabetes.vault.importer.csv;

import com.csvreader.CsvReader;
import de.opendiabetes.vault.data.adapter.SliceEntryCsvAdapter;
import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.exporter.csv.CsvExportEntry;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.csv.validator.SliceEntryCsvValidator;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for simple SliceEntry csv files.
 *
 * @author juehv
 */
public class SliceEntryCsvFileImporter extends CsvFileImporter<SliceEntry> {

    public SliceEntryCsvFileImporter(ImporterOptions options) {
        super(options, new SliceEntryCsvValidator(), new char[]{','});
    }

    @Override
    protected List<SliceEntry> parseEntry(CsvReader creader) throws Exception {
        SliceEntryCsvAdapter adapter = new SliceEntryCsvAdapter();
        ArrayList<SliceEntry> returnValue = new ArrayList<>();
        ArrayList<String> csvRecord = new ArrayList<>();
        SliceEntryCsvValidator veValidator = (SliceEntryCsvValidator) validator;

        // read csv line
        csvRecord.add(veValidator.getTimestamp(creader));
        csvRecord.add(veValidator.getDuration(creader));
        csvRecord.add(veValidator.getSource(creader));
        csvRecord.add(veValidator.getLabel(creader));

        // deserialize
        CsvExportEntry entry = new CsvExportEntry(csvRecord.toArray(new String[]{}));
        returnValue.add(adapter.deserialize(entry));

        return returnValue;
    }

}
