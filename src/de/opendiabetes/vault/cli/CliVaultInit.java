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
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLI class for initializing repositories.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Initializes the OpenDiabes Vault repository",
        name = "odv init", mixinStandardHelpOptions = true, version = "odv init 0.1")
public class CliVaultInit implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliVaultInit.class.getName());

    public final String DIR_VAULT = ".vault";
    public final String DIR_IMPORT = "import";
    public final String DIR_EXPORT = "export";

    @CommandLine.Parameters(index = "0", description = "repository name")
    private String repositoryName;

    @CommandLine.Option(names = {"-f", "--force"}, description = "overrides existing repositoriy")
    private boolean force;

    @Override
    public Void call() throws Exception {
        File targetDir = new File(repositoryName);
        System.out.println("" + targetDir.exists());
        if (targetDir.exists() && force) {
            Files.walk(Paths.get(targetDir.getAbsolutePath()))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        CliRepositoryManager manager = CliRepositoryManager.initRepository(
                targetDir.getAbsolutePath());

        if (manager == null) {
            LOG.severe("Can't create repository. Exit.");
            System.exit(-1);
        }

        manager.closeJournal();

        LOG.log(Level.INFO, "Repository \"{0}\" created in version {1}"
                + CliRepositoryManager.REPOSITORY_VERSION, repositoryName);

        return null;
    }
}
