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
import picocli.CommandLine;

/**
 * CLI class for managing all cli (sub-)commands
 *
 * @author juehv
 */
@CommandLine.Command(description = "Prints the checksum (MD5 by default) of a file to STDOUT.",
        name = "checksum", mixinStandardHelpOptions = true, version = "checksum 3.0")
public class CliManager implements Callable<Void> {

//    @CommandLine.Parameters(index = "0", description = "The file whose checksum to calculate.")
//    private File file;
//
//    @CommandLine.Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
//    private String algorithm = "SHA-1";
    public static void main(String[] args) throws Exception {
        CommandLine commandLine = new CommandLine(new CliManager());

        commandLine.addSubcommand("import", new CliVaultImport())
                .addSubcommand("export", new CliVaultExport())
                .addSubcommand("journal", new CliJournal())
                .addSubcommand("init", new CliVaultInit());

        List<Object> result = commandLine.parseWithHandler(new CommandLine.RunAll(), args);
    }

    @Override
    public Void call() throws Exception {
//        byte[] fileContents = Files.readAllBytes(file.toPath());
//        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
//        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(digest));
        return null;
    }
}
