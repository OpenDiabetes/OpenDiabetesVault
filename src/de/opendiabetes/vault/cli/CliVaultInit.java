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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLI class for initializing repositories.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Initializes the OpenDiabesVault repository.",
        name = "init", mixinStandardHelpOptions = true, version = "odv init 0.1")
public class CliVaultInit implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliVaultInit.class.getName());

    @CommandLine.Parameters(index = "0", paramLabel = "REPO", description = "Repository path. (Required)")
    private String repositoryPath;

    @CommandLine.Option(names = {"-f", "--force"}, description = "Forces to override existing repositories.")
    private boolean force;

    @Override
    public Void call() throws Exception {
        File targetDir = new File(new File(repositoryPath).getAbsolutePath()
                .concat(File.separator).concat(CliRepositoryManager.DIR_VAULT));

        if (targetDir.exists() && !force) {
            System.err.println("\"" + repositoryPath + "\" seems to be a ODV repository. Use --force to override. Exit.");
            System.exit(-1);
        } else if (targetDir.exists() && force) {
            // delete old repository
            Files.walk(Paths.get(targetDir.getAbsolutePath()))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        // create new repository
        CliRepositoryManager repMan = CliRepositoryManager.initRepository(
                new File(repositoryPath).getAbsolutePath());

        if (repMan == null) {
            System.err.println("Can't create repository. Exit.");
            System.exit(-1);
        }

        System.out.println("Repository \"" + repositoryPath
                + "\" created in version "
                + CliRepositoryManager.REPOSITORY_VERSION);

        repMan.closeJournal();
        return null;
    }
}
