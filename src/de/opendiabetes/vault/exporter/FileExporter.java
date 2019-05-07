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
package de.opendiabetes.vault.exporter;

import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.util.SortVaultEntryByDate;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

/**
 * Base class for file based exporters.
 *
 * @author juehv
 */
public abstract class FileExporter extends Exporter {

    public final static int RESULT_OK = 0;
    public final static int RESULT_ERROR = -1;
    public final static int RESULT_NO_DATA = -2;
    public final static int RESULT_FILE_ACCESS_ERROR = -3;

    protected FileExporter(ExporterOptions options) {
        super(options);
    }

    @Override
    public void exportData(OutputStream sink, List<VaultEntry> data) {
        if (sink == null || !(sink instanceof FileOutputStream)) {
            String msg = "PROGRAMMING ERROR: FileExporter can only write to FileOutputStream!";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        int result = exportDataImpl((FileOutputStream) sink, data);
        LOG.log(Level.INFO, "Exported Data to File with result: {0}", result);
    }

    /**
     * Exports data to a FileOutputStream. Leverages prepareData() to convert
     * VaultEntries to exportable Data
     *
     * @param sink
     * @param data
     * @return int with result status.
     */
    private int exportDataImpl(OutputStream sink, List<VaultEntry> data) {
        // check output stream
        if (sink == null) {
            String msg = "PROGRAMMING ERROR: YOU MUST PROVIDE AN OUTPUT STREAM!";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // sort data by date
        data.sort(new SortVaultEntryByDate());

        // create exportable data
        List<ExportEntry> exportData = prepareData(data);
        if (exportData == null || exportData.isEmpty()) {
            return RESULT_NO_DATA;
        }

        // write to file
        try {
            writeToFile(sink, exportData);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing export file", ex);
            return RESULT_ERROR;
        } finally {
            try {
                sink.close();
            } catch (IOException ex) {
                //don't care
            }
        }
        return RESULT_OK;
    }

    /**
     * Convenience method for exporting to a filePath instead of a output
     * stream. Does neccesary checks on the given file path.
     *
     * @param filePath
     * @param data
     * @return int with result status.
     */
    public int exportDataToFile(String filePath, List<VaultEntry> data, boolean deflate) {
        // check file stuff  
        File checkFile = new File(filePath);
        String extension = checkFile.getName().substring(checkFile.getName().lastIndexOf('.') + 1);
        if (deflate && (extension.equalsIgnoreCase("gzip") || extension.equalsIgnoreCase("gz")))  {
            filePath += ".gzip";
        }
        if (checkFile.exists()
                && (!checkFile.isFile() || !checkFile.canWrite())) {
            LOG.warning("File Access checks failed!");
            return RESULT_FILE_ACCESS_ERROR;
        }

        // export using output stream
        try {
            FileOutputStream fileOutpuStream = new FileOutputStream(checkFile);
            LOG.log(Level.INFO, "Try exporting data to: {0}",
                    checkFile.getAbsolutePath());
            if (deflate) {
                GZIPOutputStream zippedOutputStream = new GZIPOutputStream(fileOutpuStream);
                return exportDataImpl(zippedOutputStream, data);
            } else {
                return exportDataImpl(fileOutpuStream, data);
            }

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error accessing file for output stream", ex);
            return RESULT_FILE_ACCESS_ERROR;
        }
    }

    /**
     * Writes converted exportable data to a file.
     *
     * @param fileOutputStream
     * @param data
     * @throws IOException
     */
    protected void writeToFile(OutputStream fileOutputStream, List<ExportEntry> data) throws IOException {
        BufferedWriter fc = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

        for (ExportEntry entry : data) {
            char[] messageBytes = entry.toByteEntryLine();
            fc.write(messageBytes);
            fc.newLine();
        }

        fc.close();
    }
}
