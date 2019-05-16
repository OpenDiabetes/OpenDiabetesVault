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
import de.opendiabetes.vault.importer.FileImporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.csv.validator.CsvValidator;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Base class for csv file based importers (such as medtronic csv).
 *
 * @author juehv
 */
public abstract class CsvFileImporter<T> extends FileImporter<T> {

    protected final CsvValidator validator;
    protected char[] delimiter;

    public CsvFileImporter(ImporterOptions options, CsvValidator validator, char[] delimiter) {
        super(options);
        this.validator = validator;
        this.delimiter = delimiter;

        if (this.validator == null) {
            String msg = "PROGRAMMING ERRROR: YOU HAVE TO PROVIDE A VALIDATOR";
            LOG.severe(msg);
            throw new Error(msg);
        }

        if (this.delimiter.length == 0) {
            String msg = "PROGRAMMING ERRROR: YOU HAVE TO SPECIFY AT LEAST ONE DELIMITER!";
            LOG.severe(msg);
            throw new Error(msg);
        }
    }

    @Override
    protected List<T> processImport(InputStream fileSource) {
        List<T> importedData = new ArrayList<>();

        CsvReader creader = null;
        try {
            // find correct delimiter
            for (char delItem : delimiter) {
                // open file
                creader = new CsvReader(fileSource, delItem, Charset.forName("UTF-8"));

                // validate header
                boolean headerValid = true;
                do {
                    if (!creader.readHeaders()) {
                        // no more lines --> no valid header
                        headerValid = false;
                        LOG.log(Level.WARNING, "No valid header found");
                        return null;
                    }
                } while (!validator.validateHeader(creader.getHeaders()));
                if (headerValid) {
                    LOG.info("found working delimiter: " + delItem);
                    break;
                }
            }

            // read entries
            // creader will never be null at this point
            while (creader.readRecord()) {
                List<T> entryList = parseEntry(creader);
                if (entryList != null && !entryList.isEmpty()) {
                    for (T item : entryList) {
                        importedData.add(item);
                        LOG.log(Level.FINE, "Got Entry: {0}", entryList.toString());
                    }
                }
            }

        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error while parsing CSV File", ex);
        }
        return importedData;
    }

    protected abstract List<T> parseEntry(CsvReader creader) throws Exception;

}
