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
@CommandLine.Command(description = "Imports data into the OpenDiabes Vault repository.",
        name = "import", mixinStandardHelpOptions = true, version = "odv import 0.1")
public class CliVaultImport implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliVaultImport.class.getName());

    @CommandLine.Option(required = true, names = {"-t", "--type"}, paramLabel = "IMPORT-TYPE",
            description = "Importer Type. Valid values: ${COMPLETION-CANDIDATES}")
    private CliImportType importType;

    @CommandLine.Parameters(arity = "1..", paramLabel = "IMPORT-FILE",
            description = "File(s) to be imported. Be sure to match importer type and file format.")
    private List<File> importFiles;

    @Override
    public Void call() throws Exception {
        CliRepositoryManager repMan = CliManager.openRepository();

        // check for double files
        ArrayList<String> fileChecksums = new ArrayList<>();
        ArrayList<File> filteredImportFiles = new ArrayList<>();
        for (File item : importFiles) {
            if (item.exists() && item.canRead()) {
                String chkSm = FileCopyUtil.getFileChecksumMD5(item);
                if (fileChecksums.contains(chkSm)) {
                    System.out.println("Found file duplicate. Do not import "
                            + item.getName() + "!");
                } else {
                    filteredImportFiles.add(item);
                    fileChecksums.add(chkSm);
                }
            } else {
                System.out.println("Can't read file \"" + item.getName() + "\". Skip.");
            }
        }

        // import files
        List<VaultEntry> importData = new ArrayList<>();
        switch (importType) {
            case ODV_CSV:
                for (File item : filteredImportFiles) {
                    LOG.log(Level.INFO, "Import file:{0}", item.getName());
                    List<VaultEntry> tmpImport = new VaultEntryCsvFileImporter(
                            new ImporterOptions()).importDataFromFile(item.getAbsolutePath());
                    if (tmpImport != null && !tmpImport.isEmpty()) {
                        importData.addAll(tmpImport);
                    }
                    repMan.writeLineToJournal("Imported file:" + item.getName());
                }
                break;
            case ODV_JSON:
                for (File item : filteredImportFiles) {
                    LOG.log(Level.INFO, "Import file:{0}", item.getName());
                    List<VaultEntry> tmpImport = new VaultEntryJsonFileImporter(
                            new ImporterOptions()).importDataFromFile(item.getAbsolutePath());
                    if (tmpImport != null && !tmpImport.isEmpty()) {
                        importData.addAll(tmpImport);
                    }
                    repMan.writeLineToJournal("Imported file:" + item.getName());
                }
                break;
            case NIGHTSCOUT:
                // check basic input
                if (filteredImportFiles.size() < 2) {
                    CliManager.exitWithError("Wrong number of files. "
                            + "For Nightscout import one profile file and one "
                            + "or more data files are needed. Exit.",
                            repMan);
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
                            System.out.println("Use file as profile: "
                                    + item.getName());
                            break;
                        }
                    }
                }

                if (profileFile == null) {
                    CliManager.exitWithError("Missing profile file. For Nightscout "
                            + "import one profile file and one or more data files "
                            + "are needed. Exit.",
                            repMan);
                }

                // prepare remaining import files
                ArrayList<File> filteredImportFilesWithoutProfile = new ArrayList<>();
                filteredImportFilesWithoutProfile.addAll(filteredImportFiles);
                filteredImportFilesWithoutProfile.remove(profileFile);

                // import data using the imported profile
                NightscoutImporterOptions options = new NightscoutImporterOptions(
                        profiles);
                NightscoutImporter nsImporter = new NightscoutImporter(options);
                for (File item : filteredImportFilesWithoutProfile) {
                    LOG.log(Level.INFO, "Import file:{0}", item.getName());
                    List<VaultEntry> tmpImport = nsImporter.importDataFromFile(item.getAbsolutePath());
                    if (tmpImport != null && !tmpImport.isEmpty()) {
                        importData.addAll(tmpImport);
                    }
                    repMan.writeLineToJournal("Imported file:" + item.getName());
                }

                break;
            default:
                throw new AssertionError("PROGRAMMING ERROR: Missing case for this type!");
        }

        // backup imported files
        if (!importData.isEmpty()) {
            repMan.writeLineToJournal("Import successful.");
            repMan.mergeDataIntoMaster(importData);
            repMan.saveFilesToImportFolder(filteredImportFiles);
            repMan.writeLineToJournal("Import files backuped.");
            System.out.println("Finished successfully.");
        } else {
            System.err.println("No data has been imported. See log.");
        }

        repMan.closeJournal();
        return null;
    }

}
