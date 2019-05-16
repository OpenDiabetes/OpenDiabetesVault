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

import de.opendiabetes.vault.importer.Importer;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for any type of data exporter.
 * 
 * @author juehv
 */
public abstract class Exporter<T> {

    protected final static Logger LOG = Logger.getLogger(Importer.class.getName());
    protected final ExporterOptions options;
    
    public Exporter(ExporterOptions options) {
        //force successor to implement constructor with options
        this.options = options;
        
        if (this.options == null){
            String msg = "PROGRAMMING ERRROR: YOU HAVE TO PROVIDE EXPORTER OPTIONS";
            LOG.severe(msg);
            throw new Error(msg);
        }
    }

    /**
     * Exports data from a given list to a given sink.
     * 
     * @param sink target for export (e.g., a file)
     * @param data data to be exported
     */
    public abstract void exportData(OutputStream sink, List<T> data);

    

    /**
     * Prepare eata for export (put it into a exportable container)
     *
     * @return
     */
    protected abstract List<ExportEntry> prepareData(List<T> data);
    
}
