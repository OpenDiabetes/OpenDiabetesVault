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
package de.opendiabetes.vault.importer.json.nightscout;

import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutBasalProfilesContainer;

/**
 * Options class for Nightscout importer with profile information
 *
 * @author juehv
 */
public class NightscoutImporterOptions extends ImporterOptions {

    public final NightscoutBasalProfilesContainer basalProfilesContainer;

    public NightscoutImporterOptions(NightscoutBasalProfilesContainer basalProfilesContainer) {
        this.basalProfilesContainer = basalProfilesContainer;
    }

}
