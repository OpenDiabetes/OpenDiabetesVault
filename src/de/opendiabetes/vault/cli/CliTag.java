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

import de.opendiabetes.vault.util.EasyFormatter;
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
@CommandLine.Command(description = "Manages a git-like data-set tags to save intermediate/filter results of the data-set.",
        name = "tag", mixinStandardHelpOptions = true, version = "odv tag 0.1")
public class CliTag implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliTag.class.getName());

    @CommandLine.Option(names = {"-l", "--list"}, description = "List existing tags.")
    private boolean list;

    @CommandLine.Option(names = {"-c", "--copy"}, defaultValue = "", description = "Copys the specified tag or complete data if left blank.")
    private String copy;

    @CommandLine.Option(names = {"-r", "--remove"}, description = "Removes the specified tag.")
    private String remove;

    @CommandLine.Parameters(index = "0", defaultValue = "", description = "Name for copied tag.")
    private String copyTarget;

    @Override
    public Void call() throws Exception {
        CliRepositoryManager repMan = CliRepositoryManager.getCurrentRepository();
        if (repMan == null) {
            LOG.severe("Not an Opendiabetes Vault repository. Exit.");
            System.exit(-1);
        }

        if (list) {
            // list tags
            List<Map.Entry<String, Date>> tags = repMan.getTagList();
            if (tags != null && !tags.isEmpty()) {
                System.out.println("Found the following tags:");
                for (Map.Entry<String, Date> item : tags) {
                    System.out.print(item.getKey());
                    System.out.print(" -- last modification: ");
                    System.out.println(EasyFormatter
                            .formatTimestampToLogEntry(item.getValue()));
                }
            } else {
                System.out.println("No tags found.");
            }
        } else if (copy != null && copyTarget != null
                && !copy.isEmpty()) {
            // copy a tag
            if (copyTarget.isEmpty()) {
                // all
                copyTarget = copy;
                copy = CliRepositoryManager.COMPLETE_DATA;
            } else {
                // copy specific tag
                if (!repMan.getTagNameList().contains(copy)) {
                    System.err.println("Source tag \"" + copy + "\" not found. Exit.");
                }
            }
            if (repMan.getTagNameList().contains(copyTarget)) {
                System.err.println("Target tag already exists. Exit.");
                System.exit(-1);
            }
            repMan.copyTag(copy, copyTarget);
        } else if (remove != null && !remove.isEmpty()) {
            // remove a tag
            if (!
            repMan.getTagNameList().contains(remove)
                    ){
                System.err.println("Tag name not found. Exit.");
                System.exit(-1);
            }
            
            repMan.removeTag(remove);
        } else {
            // print help
            System.err.println("Invalid argument combination.");
            new CommandLine(this).usage(System.err);
        }

        return null;
    }

}
