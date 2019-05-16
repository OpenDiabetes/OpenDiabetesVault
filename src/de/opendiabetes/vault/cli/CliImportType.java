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
package de.opendiabetes.vault.cli;

/**
 * CLI options for importing data (represents the different importer classes)
 *
 * @author juehv
 */
public enum CliImportType {
    ODV_CSV,
    ODV_JSON,
    NIGHTSCOUT,
//    CARELINK,
//    FREESTYLE_LIBRE,
//    MI_BAND,
//    SWR12_DUMP,
//    GARMIN,
//    MYSUGR,
//    GOOGLE_MAPS,

}
