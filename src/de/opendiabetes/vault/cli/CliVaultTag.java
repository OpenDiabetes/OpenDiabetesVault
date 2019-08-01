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
import de.opendiabetes.vault.importer.FileImporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.csv.SliceEntryCsvFileImporter;
import de.opendiabetes.vault.importer.json.SliceEntryJsonFileImporter;
import de.opendiabetes.vault.util.EasyFormatter;
import de.opendiabetes.vault.util.VaultEntryUtils;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLI class for managing repository tags
 *
 * @author juehv
 */
@CommandLine.Command(description = "Manages git-like data-set-tags to save intermediate/filter results within the repository.",
        name = "tag", mixinStandardHelpOptions = true, version = "odv tag 0.1")
public class CliVaultTag implements Callable<Void> {

    public static final String COMMAND = "tag";

    private static final Logger LOG = Logger.getLogger(CliVaultTag.class.getName());

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    private Exclusive exclusive;

    private static class Exclusive {

        @CommandLine.Option(required = true, names = {"-l", "--list"},
                description = "List existing tags.")
        private boolean list;

        @CommandLine.Option(required = true, names = {"-r", "--remove"}, paramLabel = "TAG",
                description = "Removes the specified tag.")
        private String remove;

        @CommandLine.Option(required = true, names = {"-c", "--copy"},
                arity = "1..2", paramLabel = "TAG",
                description = "Copies data to the specified tag. Source can be defined by a second tag optionally, but complete data is default.")
        private List<String> copy;

        @CommandLine.Option(required = true, names = {"-s", "--create-from-slices"},
                arity = "2", paramLabel = "FILE TAG",
                description = "Creates a tag from a slice file (ODV_CSV oder ODV_JSON). Source is always the complete dataset. Make sure to import data first.")
        private List<String> slice;
    }

    @Override
    public Void call() throws Exception {
        CliRepositoryManager repMan = CliManager.openRepository();

        if (exclusive.list) {
            // list tags
            List<Map.Entry<String, Date>> tags = repMan.getTagList();
            if (tags != null && !tags.isEmpty()) {
                System.out.println("Found the following tags:\n");
                System.out.println("Last Modification -- Tag Name");
                System.out.println("-----------------------------");
                for (Map.Entry<String, Date> item : tags) {
                    System.out.print(" ");
                    System.out.print(EasyFormatter
                            .formatTimestampToLogEntry(item.getValue()));
                    System.out.print(" -- ");
                    System.out.println(item.getKey());
                }
            } else {
                System.out.println("No tags found.");
            }
        } else if (exclusive.remove != null && !exclusive.remove.isEmpty()) {
            // remove a tag
            if (!repMan.getTagNameList().contains(exclusive.remove)) {
                CliManager.exitWithError("Tag name not found. Exit.", repMan);
            }
            repMan.removeTag(exclusive.remove);
        } else if (exclusive.copy != null && !exclusive.copy.isEmpty()) {
            // copy a tag
            if (exclusive.copy.size() == 1) {
                // copy all data to a tag --> set source to all
                exclusive.copy.add(CliRepositoryManager.COMPLETE_DATA);
            } else {
                // copy specified tag to a new tag --> check if exist     
                if (!repMan.getTagNameList().contains(exclusive.copy.get(1))) {
                    CliManager.exitWithError("Source tag \"" + exclusive.copy
                            + "\" not found. Exit.", repMan);
                }
            }
            // check target tag if exist    
            if (repMan.getTagNameList().contains(exclusive.copy.get(0))) {
                CliManager.exitWithError("Target tag already exists. Exit.", repMan);

            }

            // do the copy operation
            repMan.copyTag(exclusive.copy.get(1), exclusive.copy.get(0));
        } else if (exclusive.slice != null && exclusive.slice.size() == 2) {
            // -> import slice and create tag
            // prepare names
            File slicesFile = null;
            String fileName = null;
            String fileEnding = null;
            String tagName = null;
            try {
                fileName = exclusive.slice.get(0);
                fileEnding = fileName.substring(fileName.lastIndexOf(".") + 1);
                tagName = exclusive.slice.get(1);
            } catch (Exception ex) {
                try {
                    fileName = exclusive.slice.get(1);
                    fileEnding = fileName.substring(fileName.lastIndexOf(".") + 1);
                    tagName = exclusive.slice.get(0);
                } catch (Exception ex2) {
                    //can't find propper file
                    fileEnding = null;
                }
            }

            // check for propper slice file
            if (fileEnding == null || 
                    (!fileEnding.equalsIgnoreCase("json") && !fileEnding.equalsIgnoreCase("csv"))) {
                CliManager.exitWithError("No ODV_CSV or ODV_JSON slice file given. Exit.", repMan);
            }

            slicesFile = new File(fileName);
            if (!slicesFile.exists() || !slicesFile.canRead() || slicesFile.isDirectory()) {
                CliManager.exitWithError("Can't open given slice file. Exit.", repMan);
            }

            // prepare importer
            FileImporter<SliceEntry> importer = null;
            if (fileEnding.equalsIgnoreCase("csv")) {
                importer = new SliceEntryCsvFileImporter(new ImporterOptions());
            } else if (fileEnding.equalsIgnoreCase("json")) {
                importer = new SliceEntryJsonFileImporter(new ImporterOptions());
            } else {
                // we never end here!
                CliManager.exitWithError("PROGRAMMING ERROR. Exit.", repMan);
            }

            // import slices
            List<SliceEntry> slices = importer.importDataFromFile(
                    slicesFile.getAbsolutePath());

            // slice data
            List<List<VaultEntry>> slicedData = VaultEntryUtils.slice(
                    repMan.getDataFromMaster(), slices);

            // create tag from data
            repMan.createTagFromData(slicedData, tagName);
            
            System.out.println("Finished successfully.");
        }

        repMan.closeJournal();
        return null;
    }

}
