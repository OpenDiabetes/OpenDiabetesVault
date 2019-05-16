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
import de.opendiabetes.vault.importer.FileImporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.csv.VaultEntryCsvFileImporter;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutImporter;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutImporterOptions;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutProfileImporter;
import de.opendiabetes.vault.importer.json.VaultEntryJsonFileImporter;
import de.opendiabetes.vault.importer.json.nightscout.NightscoutBasalProfilesContainer;
import de.opendiabetes.vault.util.FileCopyUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLI class for importing data into a repository.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Imports data into the OpenDiabes Vault repository",
        name = "odv import", mixinStandardHelpOptions = true, version = "odv import 0.1")
public class CliVaultImport implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliVaultImport.class.getName());

    @CommandLine.Option(names = {"-t", "--type"}, description = "Importer Type. Valid values: ${COMPLETION-CANDIDATES}")
    private CliImportType importType;

    @CommandLine.Parameters
    private List<File> importFiles;

    @Override
    public Void call() throws Exception {
        CliRepositoryManager repMan = CliRepositoryManager.getCurrentRepository();
        if (repMan == null) {
            LOG.severe("Can't open repository. Exit.");
            System.exit(-1);
        }

        ArrayList<String> fileChecksums = new ArrayList<>();
        ArrayList<File> filteredImportFiles = new ArrayList<>();
        for (File item : importFiles) {
            String chkSm = FileCopyUtil.getFileChecksumMD5(item);
            if (fileChecksums.contains(chkSm)) {
                LOG.warning("Found file duplicate. Do not import.");
            } else {
                filteredImportFiles.add(item);
                fileChecksums.add(chkSm);
            }
        }

        List<VaultEntry> importData = new ArrayList<>();
        switch (importType) {
            case ODV_CSV:
                for (File item : filteredImportFiles) {
                    LOG.log(Level.INFO, "Import file:{0}", item.getName());
                    importData.addAll(checkAndImportFile(new VaultEntryCsvFileImporter(
                            new ImporterOptions()), item));
                    repMan.writeLineToJournal("Imported file: " + item.getName());
                }
                break;
            case ODV_JSON:
                for (File item : filteredImportFiles) {
                    LOG.log(Level.INFO, "Import file:{0}", item.getName());
                    importData.addAll(checkAndImportFile(new VaultEntryJsonFileImporter(
                            new ImporterOptions()), item));
                    repMan.writeLineToJournal("Imported file:" + item.getName());
                }
                break;
            case NIGHTSCOUT:
                // check basic input
                if (filteredImportFiles.size() < 2) {
                    LOG.warning("Wrong number of files. For Nightscout import one profile file and one or more data files are needed. Exit.");
                    return null;
                }

                // import profile
                File profileFile = null;
                NightscoutBasalProfilesContainer profiles = null;
                NightscoutProfileImporter profileImporter = new NightscoutProfileImporter();
                for (File item : filteredImportFiles) {
                    if (item.exists() && item.canRead()) {
                        profiles = profileImporter.readProfileFile(item.getAbsolutePath());
                        if (!profiles.records.isEmpty()) {
                            profileFile = item;
                            break;
                        }
                    }
                }

                if (profileFile == null) {
                    LOG.warning("Missing profile file. For Nightscout import one profile file and one or more data files are needed. Exit.");
                    return null;
                }
                ArrayList<File> filteredImportFilesWithoutProfile = new ArrayList<>();
                filteredImportFilesWithoutProfile.addAll(filteredImportFiles);
                filteredImportFilesWithoutProfile.remove(profileFile);

                // import data using profile
                NightscoutImporterOptions options = new NightscoutImporterOptions(
                        profiles);
                NightscoutImporter nsImporter = new NightscoutImporter(options);
                for (File item : filteredImportFilesWithoutProfile) {
                    LOG.log(Level.INFO, "Import file:{0}", item.getName());
                    importData.addAll(checkAndImportFile(nsImporter, item));
                    repMan.writeLineToJournal("Imported file:" + item.getName());
                }

                break;
            default:
                throw new AssertionError("PROGRAMMING ERROR: Missing case for this type!");
        }

        if (!importData.isEmpty()) {
            repMan.writeLineToJournal("Import successful");
            repMan.mergeDataToRepository(importData);
            repMan.addFilesToImportFolder(filteredImportFiles);
            repMan.writeLineToJournal("Import files backuped");
        }
        repMan.closeJournal();

        return null;
    }

    private List<VaultEntry> checkAndImportFile(FileImporter importer, File importFile) throws IllegalAccessException {
        if (importFile.exists() && importFile.canRead()) {
            return importer.importDataFromFile(
                    importFile.getAbsolutePath());
        } else {
            LOG.log(Level.SEVERE, "File does not exist or is not readable: {0}", importFile);
            System.exit(-1);
        }
        return null;
    }

}
