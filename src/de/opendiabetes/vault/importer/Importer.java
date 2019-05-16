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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for any type of importer.
 *
 * @author juehv
 */
public abstract class Importer<T> {

    protected final static Logger LOG = Logger.getLogger(Importer.class.getName());
    protected final ImporterOptions options;

    public Importer(ImporterOptions options) {
        //force successor to implement constructor with options
        this.options = options;

        if (this.options == null) {
            String msg = "PROGRAMMING ERRROR: YOU HAVE TO PROVIDE IMPORTER OPTIONS";
            LOG.severe(msg);
            throw new Error(msg);
        }
    }

    /**
     * Imports data from a given source and returns vault entries.
     *
     * @param source Data source.
     * @return VaultEntries representing the data from input source.
     */
    public abstract List<T> importData(InputStream source);

    /**
     * Importer specific post processing if a second pass is needed after
     * importing multiple sources. Should at least remove dublicates.
     *
     * @param importedData Data to clean.
     * @return Cleaned list of VaultEntries
     */
    public List<T> postProcessingData(List<T> importedData) {
        List<T> cleanedData = new ArrayList<>();
        for (T item : importedData) {
            boolean duplicate = false;
            for (T savedItem : cleanedData) {
                if (item.equals(savedItem)) {
                    // identified item as duplicate
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                cleanedData.add(item);
            }
        }

        return cleanedData;
    }
}
