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
package de.opendiabetes.vault.exporter.json;

import de.opendiabetes.vault.data.container.SliceEntry;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Export container for JsonFileExporter.
 *
 * @author juehv
 */
public class SliceEntryJsonExportObject implements Serializable {

    public final String titel = "OpenDiabetesVault SliceEntry JSON Export";
    public final String version = "1";
    public final String exportDate = String.format("%tF %tR", new Date(), new Date());
    public final List<SliceEntry> data;

    public SliceEntryJsonExportObject(List<SliceEntry> data) {
        this.data = data;
    }

}
