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

import de.opendiabetes.vault.exporter.ExportEntry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class implementing the CSV Entry data structure.
 *
 * @author juehv
 */
public class VaultEntryCsvExportEntry implements ExportEntry {

    /**
     * Declaration of the delimiter used.
     */
    public static final char CSV_DELIMITER = ',';
    /**
     * Declaration of the list delimiter used.
     */
    public static final char CSV_LIST_DELIMITER = ':';
    /**
     * Data source for the enty
     */
    private final String[] data;

    public VaultEntryCsvExportEntry(String[] data) {
        if (data.length != (getCsvHeader()).length) {
            throw new AssertionError("PROGRAMMING ERROR: Data does not match header.");
        }
        this.data = data;
    }

    public static VaultEntryCsvExportEntry getHeaderEntry() {
        VaultEntryCsvExportEntry header = new VaultEntryCsvExportEntry(getCsvHeader());
        return header;
    }

    public static String[] getCsvHeader() {
        return new String[]{
            "timestamp",
            "type",
            "value",
            "valueExtension",
            "origin",
            "source"
        };
    }

    /**
     * Method to convert entries to CSV records.
     *
     * @return The CSV records.
     */
    public String[] toCsvRecord() {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] toByteEntryLine() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os = writeStringArray(os, toCsvRecord());

        return os.toByteArray();
    }

    /**
     * Method to write a string array onto an output stream.
     *
     * @param outputStream The output stream.
     * @param array The string array.
     * @return Output stream as a byte array.
     * @throws IOException Thrown if string can not be converted to UTF-8.
     */
    private ByteArrayOutputStream writeStringArray(final ByteArrayOutputStream outputStream, final String[] array) throws IOException {
        ByteArrayOutputStream os = outputStream;
        byte[] delimiterConverted = new String(new char[]{CSV_DELIMITER}).getBytes(Charset.forName("UTF-8"));
        byte[] delimiter = new byte[]{};

        for (String line : array) {
            try {
                os.write(delimiter);
                os.write(line.getBytes(Charset.forName("UTF-8")));
                delimiter = delimiterConverted;
            } catch (IOException ex) {
                Logger.getLogger(VaultEntryCsvExportEntry.class.getName()).log(Level.SEVERE,
                        "Error converting String in UTF8", ex);
                throw ex;
            }
        }

        return os;
    }

}
