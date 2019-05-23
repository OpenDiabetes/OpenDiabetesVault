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

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLI class for managing all cli (sub-)commands
 *
 * @author juehv
 */
@CommandLine.Command(description = "OpenDiabetesVault Commandline Interface. Manages a git-like data vault for diabetes data processing.",
        name = "odv", mixinStandardHelpOptions = true, version = "odv 0.1",
        subcommands = {CliVaultInit.class, CliVaultImport.class, CliVaultExport.class,
            CliVaultTag.class, CliProcessing.class, CliVaultStatus.class})
public class CliManager implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliManager.class.getName());

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = new CommandLine(new CliManager());
        commandLine.parseWithHandler(new CommandLine.RunLast(), args);
    }

    @Override
    public Void call() throws Exception {
        // user should never end here.
        new CommandLine(this).usage(System.err);

        return null;
    }

    /**
     * Helper function to open current repository.
     *
     * @return Reposiory or exits if not possible to open.
     */
    static CliRepositoryManager openRepository() {
        CliRepositoryManager repMan = CliRepositoryManager.getCurrentRepository();
        if (repMan == null) {
            System.err.println("Not an Opendiabetes Vault repository. Exit.");
            System.exit(-1);
        }
        return repMan;
    }

    /**
     * Helper function to exit commandline program with an user message.
     *
     * @param msg Message to user.
     * @param repMan Repository handle.
     */
    static void exitWithError(String msg, CliRepositoryManager repMan) {
        if (repMan != null) {
            repMan.closeJournal();
        }
        System.err.println(msg);
        System.exit(-1);
    }
}
