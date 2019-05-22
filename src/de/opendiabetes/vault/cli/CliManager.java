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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * CLI class for managing all cli (sub-)commands
 *
 * @author juehv
 */
@CommandLine.Command(description = "OpenDiabetesVault Commandline Interface. Manages a git-like data vault for diabetes data processing.",
        name = "odv", mixinStandardHelpOptions = true, version = "odv 0.1")
public class CliManager implements Callable<Void> {

//    @CommandLine.Parameters(index = "0", description = "The file whose checksum to calculate.")
//    private File file;
//
//    @CommandLine.Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
//    private String algorithm = "SHA-1";
    public static void main(String[] args) throws Exception {
        LOG.setLevel(Level.WARNING);

        CommandLine commandLine = new CommandLine(new CliManager());

        commandLine.addSubcommand("init", new CliVaultInit())
                .addSubcommand("import", new CliVaultImport())
                .addSubcommand("export", new CliVaultExport())
                .addSubcommand("tag", new CliTag())
                .addSubcommand("process", new CliProcessing())
                .addSubcommand(CliStatus.COMMAND, new CliStatus());

        List<Object> result = commandLine.parseWithHandler(new CommandLine.RunAll(), args);
    }
    private static final Logger LOG = Logger.getLogger(CliManager.class.getName());

    @Override
    public Void call() throws Exception {
//        byte[] fileContents = Files.readAllBytes(file.toPath());
//        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
//        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(digest));
        return null;
    }
}
