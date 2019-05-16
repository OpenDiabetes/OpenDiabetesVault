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
package de.opendiabetes.vault.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 * Abstract base class for file based importers.
 *
 * @author juehv
 */
public abstract class FileImporter<T> extends Importer<T> {

    /**
     * Takes a filepath and converts it to an InputStream.
     *
     * @param filePath
     * @return FileInputStream for the given file, or null if an error ocurs.
     * @throws IllegalAccessException when file does not exist or is not
     * readable.
     */
    public static InputStream convertFileToStream(String filePath) throws IllegalAccessException {
        if (filePath == null) {
            String msg = "filePath is null";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Check if file exists and is readable
        File file = new File(filePath);
        if (!file.exists() || !file.canRead() || !file.isFile()) {
            String msg = filePath + " does not represent a normal readable file";
            LOG.severe(msg);
            throw new IllegalAccessException(msg);
        }

        // check if file is a gzip
        boolean isDeflated = false;
        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        if (extension.equalsIgnoreCase("gzip") || extension.equalsIgnoreCase("gz")) {
            isDeflated = true;
        }

        // Convert to InputStream
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            if (isDeflated) {
                return new GZIPInputStream(fis);
            } else {
                return fis;
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error opening a FileInputStream for File "
                    + filePath, ex);
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    // at least I've tried...
                }
            }
            return null;
        }
    }

    public FileImporter(ImporterOptions options) {
        super(options);
    }

    @Override
    public List<T> importData(InputStream source) {
        if (source == null) {
            String msg = "Source is null";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!(source instanceof FileInputStream) && !(source instanceof GZIPInputStream)) {
            String msg = "PROGRAMMING ERROR: YOU HAVE TO USE A FILEINPUTSTREAM FOR FILE IMPORTS!";
            LOG.severe(msg);
            throw new Error(msg);
        }

        return processImport(source);
    }

    public List<T> importDataFromFile(String filePath) throws IllegalAccessException {
        return importData(convertFileToStream(filePath));
    }

    protected abstract List<T> processImport(InputStream fis);

}
