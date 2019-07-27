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

import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.exporter.csv.SliceEntryCsvFileExporter;
import de.opendiabetes.vault.exporter.csv.VaultEntryCsvFileExporter;
import de.opendiabetes.vault.exporter.json.SliceEntryJsonFileExporter;
import de.opendiabetes.vault.exporter.json.VaultEntryJsonFileExporter;
import de.opendiabetes.vault.util.VaultEntryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * CLU class for exporting data of a repository.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Exports data from the OpenDiabes Vault repository.",
        name = "export", mixinStandardHelpOptions = true, version = "odv export 0.1")
public class CliVaultExport implements Callable<Void> {

    @CommandLine.Option(required = true, names = {"-t", "--type"}, paramLabel = "EXPORT-TYPE",
            description = "Exporter Type. Valid values: ${COMPLETION-CANDIDATES}")
    private CliExportType exportType;

    @CommandLine.Option(names = {"-i", "--input-tag"}, paramLabel = "INPUT-TAG",
            description = "Exports data of respective tag. Exports complete data if not set.")
    private String tag;

    @CommandLine.Option(names = {"-c", "--compress"}, description = "Activates compression.")
    private boolean deflate;

    @Override
    public Void call() throws Exception {
        CliRepositoryManager repMan = CliManager.openRepository();

        // gather data
        List<VaultEntry> exportData = null;
        List<SliceEntry> exportSlices = null;
        if (tag != null && !tag.isEmpty()) {
            // export from tag
            if (repMan.getTagNameList().contains(tag)) {
                List<List<VaultEntry>> tmpData = repMan.getDataFromTag(tag);
                exportSlices = VaultEntryUtils.computeSlicesEntries(tmpData);
                exportData = VaultEntryUtils.mergeSlices(tmpData);
            } else {
                CliManager.exitWithError("Tag not found. Exit.", repMan);
            }
        } else {
            // export complete data
            exportData = repMan.getDataFromMaster();
        }

        if (exportData == null || exportData.isEmpty()) {
            CliManager.exitWithError("No data for export found. Exit.", repMan);
        }

        // export data
        FileExporter exporter;
        String exportName = null;
        String exportSliceName = null;
        switch (exportType) {
            case ODV_CSV:
                exporter = new VaultEntryCsvFileExporter(new ExporterOptions());
                exportName = repMan.exportDataToExportFolder(exportData, exporter, deflate);
                if (exportSlices != null && !exportSlices.isEmpty()) {
                    exportSliceName = repMan.exportDataToExportFolder(exportSlices,
                            new SliceEntryCsvFileExporter(new ExporterOptions()),
                            deflate);
                }
                break;
            case ODV_JSON:
                exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
                exportName = repMan.exportDataToExportFolder(exportData, exporter, deflate);
                if (exportSlices != null && !exportSlices.isEmpty()) {
                    exportSliceName = repMan.exportDataToExportFolder(exportSlices,
                            new SliceEntryJsonFileExporter(new ExporterOptions()),
                            deflate);
                }
                break;
            case CUSTOM_CSV:
                CliManager.exitWithError("Not supported yet. Exit.", repMan);
                break;
            default:
                throw new AssertionError("PROGRAMMING ERROR: Missing case for this type!");
        }

        if (exportName != null && !exportName.isEmpty()) {
            System.out.println("Export data to file: " + exportName);
            if (exportSliceName != null && !exportSliceName.isEmpty()) {
                System.out.println("Export slice information to file: " + exportSliceName);
            }
            System.out.println("Finished successfully.");
        } else {
            CliManager.exitWithError("Error while exporting data. See log.", repMan);
        }
        repMan.closeJournal();
        return null;
    }

}
