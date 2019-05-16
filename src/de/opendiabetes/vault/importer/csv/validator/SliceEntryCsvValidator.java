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

import com.csvreader.CsvReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Validator for simple VaultEntry csv files.
 *
 * @author juehv
 */
public class SliceEntryCsvValidator extends CsvValidator {

    private static final MultilanguageString HEADER_TIMESTAMP = new MultilanguageString("timestamp", "timestamp");
    private static final MultilanguageString HEADER_DURATION = new MultilanguageString("duration", "duration");
    private static final MultilanguageString HEADER_LABEL = new MultilanguageString("label", "label");
    private static final MultilanguageString HEADER_SOURCE = new MultilanguageString("labelSource", "labelSource");

    private static Map<MultilanguageString.Language, String[]> createHeaderMap() {
        HashMap<MultilanguageString.Language, String[]> headerMultilanguage = new HashMap<>();
        headerMultilanguage.put(MultilanguageString.Language.EN, new String[]{
            HEADER_TIMESTAMP.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_DURATION.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_LABEL.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_SOURCE.getStringForLanguage(MultilanguageString.Language.EN)
        });
        headerMultilanguage.put(MultilanguageString.Language.DE, new String[]{
            HEADER_TIMESTAMP.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_DURATION.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_LABEL.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_SOURCE.getStringForLanguage(MultilanguageString.Language.DE)
        });
        return headerMultilanguage;
    }

    public SliceEntryCsvValidator() {
        super(createHeaderMap());
    }

    public String getTimestamp(CsvReader creader) throws IOException {
        return creader.get(HEADER_TIMESTAMP.getStringForLanguage(languageSelection));
    }

    public String getDuration(CsvReader creader) throws IOException {
        return creader.get(HEADER_DURATION.getStringForLanguage(languageSelection));
    }

    public String getSource(CsvReader creader) throws IOException {
        return creader.get(HEADER_SOURCE.getStringForLanguage(languageSelection));
    }

    public String getLabel(CsvReader creader) throws IOException {
        return creader.get(HEADER_LABEL.getStringForLanguage(languageSelection));
    }

}
