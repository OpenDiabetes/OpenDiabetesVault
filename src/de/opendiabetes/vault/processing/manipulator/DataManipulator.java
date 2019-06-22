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
package de.opendiabetes.vault.processing.manipulator;

import de.opendiabetes.vault.processing.manipulator.options.DataManipulatorOptions;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.util.VaultEntryUtils;
import java.util.List;
import java.util.logging.Logger;

/**
 * Template for data manipulation implementations
 *
 * @author Jens
 */
public abstract class DataManipulator {

    protected static final Logger LOG = Logger.getLogger(DataManipulator.class.getName());

    protected final DataManipulatorOptions options;

    public DataManipulator(DataManipulatorOptions options) {
        this.options = options;
    }

    /**
     * Takes a list of lists of VaultEntry and manipulate its content. Output
     * contains the given data.
     *
     * @param data input data as list of list. A list entry refers to a sample.
     * A sample contains data.
     *
     * @return manipulated data
     */
    public List<List<VaultEntry>> manipulate(List<List<VaultEntry>> data) {
        for (List<VaultEntry> item : data) {
            item.sort(new VaultEntryUtils());
        }
        return proceedManipulation(data);
    }

    protected abstract List<List<VaultEntry>> proceedManipulation(List<List<VaultEntry>> data);
}
