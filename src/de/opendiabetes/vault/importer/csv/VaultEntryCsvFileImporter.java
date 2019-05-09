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
import de.opendiabetes.vault.data.adapter.VaultEntryCsvAdapter;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.exporter.csv.VaultEntryCsvExportEntry;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.csv.validator.VaultEntryCsvValidator;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for simple VaultEntry csv files.
 *
 * @author juehv
 */
public class VaultEntryCsvFileImporter extends CsvFileImporter {

    public VaultEntryCsvFileImporter(ImporterOptions options) {
        super(options, new VaultEntryCsvValidator(), new char[]{','});
    }

    @Override
    protected List<VaultEntry> parseEntry(CsvReader creader) throws Exception {
        VaultEntryCsvAdapter adapter = new VaultEntryCsvAdapter();
        ArrayList<VaultEntry> returnValue = new ArrayList<>();
        ArrayList<String> csvRecord = new ArrayList<>();
        VaultEntryCsvValidator veValidator = (VaultEntryCsvValidator) validator;

        // read csv line
        csvRecord.add(veValidator.getTimestamp(creader));
        csvRecord.add(veValidator.getType(creader));
        csvRecord.add(veValidator.getValue(creader));
        csvRecord.add(veValidator.getValueExtension(creader));
        csvRecord.add(veValidator.getOrigin(creader));
        csvRecord.add(veValidator.getSource(creader));

        // deserialize
        VaultEntryCsvExportEntry entry = new VaultEntryCsvExportEntry(csvRecord.toArray(new String[]{}));
        returnValue.add(adapter.deserialize(entry));

        return returnValue;
    }

}
