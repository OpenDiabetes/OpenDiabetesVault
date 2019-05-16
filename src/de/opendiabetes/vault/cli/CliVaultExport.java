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

import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.exporter.csv.VaultEntryCsvFileExporter;
import de.opendiabetes.vault.exporter.json.VaultEntryJsonFileExporter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLU class for exporting data of a repository.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Exports data from the OpenDiabes Vault repository",
        name = "odv export", mixinStandardHelpOptions = true, version = "odv export 0.1")
public class CliVaultExport implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliVaultExport.class.getName());

    @CommandLine.Option(names = {"-t", "--type"}, description = "Exporter Type. Valid values: ${COMPLETION-CANDIDATES}")
    private CliExportType exportType;

    @CommandLine.Option(names = {"-a", "--all"}, description = "Export all available data. Overrides state tags.")
    private boolean allData;

    @CommandLine.Option(names = {"-c", "--compress"}, description = "Activates compression.")
    private boolean deflate;

    @Override
    public Void call() throws Exception {
        // gather data
        List<VaultEntry> exportData = null;
        CliRepositoryManager repMan = CliRepositoryManager.getCurrentRepository();
        if (repMan != null) {
            if (allData) {
                exportData = repMan.getAllRepositoryData();
            } // TODO state tag management
        }

        if (exportData == null) {
            LOG.severe("No data for export found.");
        }

        // export data
        FileExporter exporter = null;
        switch (exportType) {
            case ODV_CSV:
                exporter = new VaultEntryCsvFileExporter(new ExporterOptions());
                exportToFile(repMan, exporter, exportData);
                break;
            case ODV_JSON:
                exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
                exportToFile(repMan, exporter, exportData);
                break;
            case CUSTOM_CSV:
                LOG.severe("not supported yet");
                System.exit(-1);
                break;
            default:
                throw new AssertionError("PROGRAMMING ERROR: Missing case for this type!");
        }

        return null;
    }

    private void exportToFile(CliRepositoryManager repMan, FileExporter exporter,
            List<VaultEntry> exportData) throws IOException {
        File filePath = repMan.getExportFile(exporter, deflate);
        LOG.log(Level.INFO, "Export to file: {0}", filePath.getName());
        repMan.writeLineToJournal("Export to file: " + filePath.getName());

        exporter.exportDataToFile(filePath.getAbsolutePath(), exportData, deflate);

    }

}
