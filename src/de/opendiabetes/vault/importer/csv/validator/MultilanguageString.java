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
package de.opendiabetes.vault.importer.csv.validator;

import java.util.HashMap;

/**
 * container class to hold header strings for multiple languages.
 *
 * @author juehv
 */
public class MultilanguageString {

    public enum Language {
        EN, DE;
    }

    private final HashMap<Language, String> content = new HashMap<>();

    public MultilanguageString(String stringEN, String stringDE) {
        content.put(Language.EN, stringEN);
        content.put(Language.DE, stringDE);
    }

    public String getStringForLanguage(Language lang) {
        String returnValue = content.get(lang);
        if (returnValue == null || returnValue.isEmpty()) {
            return "";
        } else {
            return returnValue;
        }
    }

}
