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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for validating CSV headers
 *
 * @author juehv
 */
public abstract class CsvValidator {

    protected static final Logger LOG = Logger.getLogger(CsvValidator.class.getName());

    final Map<MultilanguageString.Language, String[]> headerMultilanguage;
    protected MultilanguageString.Language languageSelection;

    public CsvValidator(Map<MultilanguageString.Language, String[]> headerMultilanguage) {
        this.headerMultilanguage = headerMultilanguage;

        if (this.headerMultilanguage == null) {
            String msg = "PROGRAMMING ERRROR: YOU HAVE TO PROVIDE A CSV HEADER";
            LOG.severe(msg);
            throw new Error(msg);
        }
    }

    public boolean validateHeader(String[] header) {
        boolean result = true;
        Set<String> headerSet = new TreeSet<>(Arrays.asList(header));

        // iterate header languages
        for (MultilanguageString.Language language : headerMultilanguage.keySet()) {
            LOG.log(Level.INFO, "Try with {0} headers", language.toString());
            String[] headers = headerMultilanguage.get(language);

            result = true;
            for (String item : headers) {
                result &= headerSet.contains(item);
                if (!result) {
                    LOG.log(Level.INFO, "{0} not found in header!", item);
                    break;
                }
            }

            if (result == true) {
                LOG.log(Level.INFO, "Verified header. {0} it is.", language.toString());
                languageSelection = language;
                break;
            }
        }
        return result;
    }
}
