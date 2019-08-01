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

import java.util.logging.Logger;

/**
 * Possible labels for classification.
 *
 * @author juehv
 */
public enum LabelType {
    // Bolus classification
    BOLUS_INTENDED,
    BOLUS_CORRECITON,
    BOLUS_AMOUNT_LOW,
    BOLUS_AMOUNT_HIGH,
    BOLUS_TIME_EARLY,
    BOLUS_TIME_LATE,
    FREE_LABEL;

    private static final Logger LOG = Logger.getLogger(LabelType.class.getName());
    private String labelName = null;

    public String getLabelName() {
        if (labelName != null) {
            return labelName;
        } else {
            return this.toString();
        }
    }

    public static LabelType createFreeLabel(String label) {
        LOG.warning("You are using free labels. Please implement needed labels into the system!");
        LabelType returnType = FREE_LABEL;
        returnType.labelName = label.toUpperCase();
        return returnType;
    }

    public static LabelType valueOfIgnoreCase(String value) {
        try {
            return LabelType.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            if (value != null) {
                return createFreeLabel(value);
            } else {
                return null;
            }
        }
    }
}
