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

import java.io.IOException;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * CLI class for managing journal
 *
 * @author juehv
 */
@CommandLine.Command(description = "Manages repository journal",
        name = "odv journal", mixinStandardHelpOptions = true, version = "odv journal 0.1")
public class CliJournal implements Callable<Void> {

    @CommandLine.Option(required = true, names = "print", description = "Prints the whole journal.")
    boolean printOption;

    @Override
    public Void call() throws Exception {
        if (printOption) {
            printJournal();
        }
        return null;
    }

    private void printJournal() throws IOException {
        CliRepositoryManager repMan = CliRepositoryManager.getCurrentRepository();
        if (repMan != null) {
            System.out.println(repMan.readJournal());
        }
    }

}
