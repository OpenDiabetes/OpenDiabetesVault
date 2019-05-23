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
import picocli.CommandLine;

/**
 * CLI class for managing journal
 *
 * @author juehv
 */
@CommandLine.Command(description = "Manages repository journal.",
        name = "status", mixinStandardHelpOptions = true, version = "odv status 0.1")
public class CliVaultStatus implements Callable<Void> {

    @CommandLine.Option(names = {"-n", "--add-note"}, paramLabel = "NOTE", description = "Adds a note to the journal.")
    private String note;

    @Override
    public Void call() throws Exception {
        CliRepositoryManager repMan = CliManager.openRepository();
        if (note != null && !note.isEmpty()) {
            // add note to journal            
            repMan.writeLineToJournal("NOTE: " + note);
        } else {
            // print journal            
            System.out.println(repMan.readJournal());
        }

        repMan.closeJournal();
        return null;
    }
}
