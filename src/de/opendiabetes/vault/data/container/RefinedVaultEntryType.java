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

import java.util.List;

/**
 * Enumeration for types of computable vault entries --> RefinedVaultEntry
 * Contains a label, since one type can get used multiple time, based on
 * different underlying data.
 *
 * @author juehv
 */
public enum RefinedVaultEntryType {
    CGM_PREDICTION,
    IOB_BASAL,
    IOB_BOLUS,
    IOB_ALL,
    COB,
    WINDOW,
    NORMALIZED,
    ONE_HOT;

    public String label = "";

    public String toStringWithLabel() {
        return this.toString() + "(" + label + ")";
    }

    public static boolean containIncludingLabel(List<RefinedVaultEntryType> list, RefinedVaultEntryType item) {
        for (RefinedVaultEntryType listItem : list) {
            if (listItem.equals(item)) {
                return listItem.label.equalsIgnoreCase(item.label);
            }
        }
        return false;
    }
}

// for fast switch-case statement:
//
//    case CGM_PREDICTION:
//    case IOB_BASAL:
//    case IOB_BOLUS:
//    case IOB_ALL:
//    case COB:
//    case WINDOW:
//    case NORMALIZED:
//    case ONE_HOT:
