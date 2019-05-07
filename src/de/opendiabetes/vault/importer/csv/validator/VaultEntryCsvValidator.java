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
public class VaultEntryCsvValidator extends CsvValidator {

    private static final MultilanguageString HEADER_TIMESTAMP = new MultilanguageString("timestamp", "timestamp");
    private static final MultilanguageString HEADER_TYPE = new MultilanguageString("type", "type");
    private static final MultilanguageString HEADER_VALUE = new MultilanguageString("value", "value");
    private static final MultilanguageString HEADER_VALUE_EXTENSION = new MultilanguageString("valueExtension", "valueExtension");
    private static final MultilanguageString HEADER_ORIGIN = new MultilanguageString("origin", "origin");
    private static final MultilanguageString HEADER_SOURCE = new MultilanguageString("source", "source");

    private static Map<MultilanguageString.Language, String[]> createHeaderMap() {
        HashMap<MultilanguageString.Language, String[]> headerMultilanguage = new HashMap<>();
        headerMultilanguage.put(MultilanguageString.Language.EN, new String[]{
            HEADER_TIMESTAMP.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_TYPE.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_VALUE.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_VALUE_EXTENSION.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_ORIGIN.getStringForLanguage(MultilanguageString.Language.EN),
            HEADER_SOURCE.getStringForLanguage(MultilanguageString.Language.EN)
        });
        headerMultilanguage.put(MultilanguageString.Language.DE, new String[]{
            HEADER_TIMESTAMP.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_TYPE.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_VALUE.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_VALUE_EXTENSION.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_ORIGIN.getStringForLanguage(MultilanguageString.Language.DE),
            HEADER_SOURCE.getStringForLanguage(MultilanguageString.Language.DE)
        });
        return headerMultilanguage;
    }

    public VaultEntryCsvValidator() {
        super(createHeaderMap());
    }

    public String getTimestamp(CsvReader creader) throws IOException {
        return creader.get(HEADER_TIMESTAMP.getStringForLanguage(languageSelection));
    }

    public String getType(CsvReader creader) throws IOException {
        return creader.get(HEADER_TYPE.getStringForLanguage(languageSelection));
    }

    public String getValue(CsvReader creader) throws IOException {
        return creader.get(HEADER_VALUE.getStringForLanguage(languageSelection));
    }

    public String getValueExtension(CsvReader creader) throws IOException {
        return creader.get(HEADER_VALUE_EXTENSION.getStringForLanguage(languageSelection));
    }

    public String getSource(CsvReader creader) throws IOException {
        return creader.get(HEADER_SOURCE.getStringForLanguage(languageSelection));
    }

    public String getOrigin(CsvReader creader) throws IOException {
        return creader.get(HEADER_ORIGIN.getStringForLanguage(languageSelection));
    }

}
