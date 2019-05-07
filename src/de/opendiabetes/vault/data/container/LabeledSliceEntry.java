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
package de.opendiabetes.vault.data.container;

import java.util.Date;

/**
 * Container for labeling data snippets (e.g. for CGM classification).
 *
 * @author juehv
 */
public class LabeledSliceEntry extends SliceEntry {

    // Label source (e.g., manual, CNN, DTW ...) 
    public final String labelSource;
    public final LabelType type;

    public LabeledSliceEntry(String labelSource, LabelType type,
            Date startTimestamp, int durationInMinutes) {
        super(startTimestamp, durationInMinutes);
        this.labelSource = labelSource;
        this.type = type;
    }

}
