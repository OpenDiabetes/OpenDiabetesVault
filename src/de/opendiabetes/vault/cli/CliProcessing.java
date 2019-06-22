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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import de.opendiabetes.vault.processing.ProcessingContainer;
import de.opendiabetes.vault.util.TimestampUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import net.openhft.compiler.CompilerUtils;

/**
 * CLI class for filtering data within a repository.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Takes a class implementing the ProcessingContainer interface and processes data accordingly within the OpenDiabes Vault repository.",
        name = "process", mixinStandardHelpOptions = true, version = "odv process 0.1")
public class CliProcessing implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliProcessing.class.getName());

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    private Exclusive exclusive;

    private static class Exclusive {

        @CommandLine.Option(required = true, names = {"--generate-skeleton"},
                paramLabel = "CLASSNAME",
                description = "Generates a skeleton file for a loadable processing container.")
        private String generateSkeletonFileName;

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
        ForceCombination combination;

        static class ForceCombination {

            @CommandLine.Option(names = {"-i", "--input-tag"}, paramLabel = "SOURCE-TAG",
                    description = "Specifies an input tag to source data. Uses complete data-set as default.")
            private String input = CliRepositoryManager.COMPLETE_DATA;

            @CommandLine.Option(required = true, names = {"-o", "--output-tag"},
                    paramLabel = "TARGET-TAG", description = "Specifies the repository tag to write output data. If tag does exist, data will be overwritten.")
            private String tag;

            @CommandLine.Option(required = true, names = {"-f", "--container-file"},
                    paramLabel = "CONTAINER-FILE", description = "Specifies the file containing a class implementing the ProcessingContainer interface. File can be java source code (.java) or a compiled java class (.class). Use skeleton for implementation.")
            private File processingContainerFile;
        }
    }

    @Override
    public Void call() throws Exception {
        if (exclusive.generateSkeletonFileName != null) {
            System.out.println("skeleton not null");
            // generate skeleton option
            if (!exclusive.generateSkeletonFileName.endsWith(".java")) {
                exclusive.generateSkeletonFileName
                        = exclusive.generateSkeletonFileName.concat(".java");
            }

            // generate proper file path & check stuff 
            File skeletonFile = new File(exclusive.generateSkeletonFileName);
            if (!Character.isUpperCase(skeletonFile.getName().charAt(0))) {
                skeletonFile = new File(skeletonFile.getAbsoluteFile()
                        .getParentFile().getAbsolutePath().concat(File.separator)
                        .concat(Character
                                .toString(skeletonFile.getName().charAt(0))
                                .toUpperCase())
                        .concat(skeletonFile.getName().substring(1,
                                skeletonFile.getName().length())));
            }
            if (skeletonFile.exists()) {
                CliManager.exitWithError(exclusive.generateSkeletonFileName
                        + " already exists. Exit.", null);
            }

            // prepare string
            String className = skeletonFile.getName().substring(0,
                    skeletonFile.getName().indexOf(".java"));
            String[] lines = PROCESSING_CONTAINER_SKELETON
                    .replaceAll("CLASSNAME", className).split("\n");

            // write file
            Files.write(Paths.get(skeletonFile.getAbsolutePath()),
                    Arrays.asList(lines), StandardCharsets.UTF_8);
            System.exit(0);
        }

        // process option
        CliRepositoryManager repMan = CliManager.openRepository();

        List<List<VaultEntry>> inputData = null;
        if (exclusive.combination.input.equalsIgnoreCase(CliRepositoryManager.COMPLETE_DATA)) {
            inputData = new ArrayList<>();
            inputData.add(repMan.getCompleteData());
        } else {
            // check if tag exists
            if (repMan.getTagNameList().contains(exclusive.combination.input)) {
                // load tag data
                inputData = new ArrayList<>();
                inputData.add(repMan.getDateFromTag(exclusive.combination.input));
            } else {
                // tag not found --> exit
                CliManager.exitWithError("Can't load input data. Tag does not exist! Exit.",
                        repMan);
            }
        }

        if (inputData.isEmpty()) {
            CliManager.exitWithError("Can't load input data. Exit.",
                    repMan);
        }

        // load processing container
        ProcessingContainer processingContainer = null;
        if (!exclusive.combination.processingContainerFile.exists()
                || !exclusive.combination.processingContainerFile.canRead()) {
            CliManager.exitWithError("Can't read processing container file. Exit.",
                    repMan);
        } else if (exclusive.combination.processingContainerFile
                .getName().endsWith(".java")) {
            // source file
            processingContainer = loadAndCompileContainerClass();
        } else if (exclusive.combination.processingContainerFile
                .getName().endsWith(".class")) {
            // compiled class
            processingContainer = loadContainerClass();
        } else {
            CliManager.exitWithError("Can't find a supported file ending. Exit.",
                    repMan);
        }

        if (processingContainer != null) {
            List<List<VaultEntry>> outputData = processingContainer.processData(inputData);
            List<SliceEntry> slices = null;
            if (outputData != null && !outputData.isEmpty()) {
                List<VaultEntry> saveData;
                if (outputData.size() > 1) {
                    LOG.info("Output contains more than one slice. A slice file will be exported to the tag.");
                    // generate slice entries
                    slices = new ArrayList<>();
                    for (List<VaultEntry> item : outputData) {
                        if (!item.isEmpty()) {
                            Date startDate = item.get(0).getTimestamp();
                            Date endDate = item.get(item.size() - 1).getTimestamp();

                            slices.add(new SliceEntry(startDate,
                                    TimestampUtils.getDurationInMinutes(startDate, endDate)));
                        }
                    }

                    // merge samples
                    saveData = new ArrayList<>();
                    for (List<VaultEntry> item : outputData) {
                        saveData.addAll(item);
                    }
                } else {
                    saveData = outputData.get(0);
                }

                // TODO add slices to tag
                // save to repository
                repMan.createTagFromData(saveData, exclusive.combination.tag);
            } else {
                System.out.println("Output data was empty.");
            }
        } else {
            CliManager.exitWithError("Error loading processing container.",
                    repMan);
        }

        System.out.println("Fished successfully.");
        repMan.closeJournal();
        return null;
    }

    private ProcessingContainer loadContainerClass() {
        try {
            // get class name
            String className = exclusive.combination.processingContainerFile.getName()
                    .substring(0, exclusive.combination.processingContainerFile
                            .getName().indexOf(".class"));

            // Convert directory of container File to a URL
            URL url = exclusive.combination.processingContainerFile
                    .getParentFile().getAbsoluteFile().toURI().toURL();
            URL[] urls = new URL[]{url};

            // Create a new class loader with the directory
            ClassLoader cl = new URLClassLoader(urls);

            // Load in the class; MyClass.class should be located in
            // the directory file:/c:/myclasses/com/mycompany
            // as the directory structure is annoying we suggest default package
            Class cls = cl.loadClass(className);
            return (ProcessingContainer) cls.newInstance();
        } catch (InstantiationException | IllegalAccessException
                | MalformedURLException | ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Error while loading compiled processing class.", ex);
        }
        return null;
    }

    private ProcessingContainer loadAndCompileContainerClass() throws IOException,
            ClassNotFoundException, InstantiationException, IllegalAccessException {
        // check if environment contains compiler classes
        try {
            Class.forName("com.sun.tools.javac.api.JavacTool");
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Error loading java compiler.", ex);
            System.err.println("Can't find java compiler. Please run with jdk environment. Exit.");
            System.exit(-1);
        }

        // get class name
        String className = exclusive.combination.processingContainerFile.getName()
                .substring(0, exclusive.combination.processingContainerFile
                        .getName().indexOf(".java"));
        System.out.println(className);

        String classSource = new String(Files.readAllBytes(Paths
                .get(exclusive.combination.processingContainerFile.getAbsolutePath())));

        // compile
        Class aClass = CompilerUtils.CACHED_COMPILER
                .loadFromJava(className, classSource);
        Object importedRunContainer = aClass.newInstance();
        if (importedRunContainer instanceof ProcessingContainer) {
            ProcessingContainer container = (ProcessingContainer) importedRunContainer;
            return container;
        }
        return null;
    }

    private static final String PROCESSING_CONTAINER_SKELETON
            = "/**\n"
            + " * Copyright (C) 2019 Jens Heuschkel\n *\n"
            + " * This program is free software: you can redistribute it and/or modify it under\n"
            + " * the terms of the GNU General Public License as published by the Free Software\n"
            + " * Foundation, either version 3 of the License, or (at your option) any later\n"
            + " * version.\n *\n"
            + " * This program is distributed in the hope that it will be useful, but WITHOUT\n"
            + " * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\n"
            + " * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more\n"
            + " * details.\n *\n"
            + " * You should have received a copy of the GNU General Public License along with\n"
            + " * this program. If not, see <http://www.gnu.org/licenses/>.\n"
            + " */\n"
            + "// Do not specify a package!\n\n"
            + "import de.opendiabetes.vault.data.container.VaultEntry;\n"
            + "import de.opendiabetes.vault.data.container.VaultEntryType;\n"
            + "import de.opendiabetes.vault.processing.*;\n"
            + "import de.opendiabetes.vault.processing.filter.*;\n"
            + "import de.opendiabetes.vault.processing.filter.options.*;\n"
            + "import de.opendiabetes.vault.processing.manipulator.*;\n"
            + "import de.opendiabetes.vault.processing.manipulator.options.*;\n"
            + "import java.util.ArrayList;\n"
            + "import java.util.List;\n"
            + "import java.util.logging.Level;\n"
            + "import java.util.logging.Logger;\n\n"
            + "/**\n"
            + " * Skeleton class for implementing a ProcessingContainer.\n"
            + " * Some hints:\n"
            + " * - Data will come from the repository, which might be already processed from a previous step.\n"
            + " * - Data is organized in slices (one entry of the list).\n"
            + " * - Every slice contains a sorted time series of VaultEntry data.\n"
            + " * - Slices will be safed to the tag.\n"
            + " * - Always sort your data before you add it to the slice list with .sort(new VaultEntryUtils())\n"
            + " * - The slices should be ordered by times.\n"
            + " * - Return null if a fatal error occurred.\n"
            + " * - Use the default Java logger with LOG.log() to trace your processing steps.\n"
            + " * - To precompile the processing class use \"javac -cp OpenDiabetesVault.jar CLASSNAME.java\" on the commandline.\n"
            + " *\n"
            + " * @author juehv, YOU\n"
            + " */\n"
            + "public class CLASSNAME implements ProcessingContainer {\n\n"
            + "    private static final Logger LOG = Logger.getLogger(CLASSNAME.class.getName());\n\n"
            + "    @Override\n"
            + "    public List<List<VaultEntry>> processData(List<List<VaultEntry>> inputData) {\n"
            + "        List<List<VaultEntry>> returnValue = new ArrayList<>();\n"
            + "        List<VaultEntry> currentSlice = new ArrayList<>();\n\n"
            + "        // do something\n\n"
            + "        returnValue.add(currentSlice);\n"
            + "        return returnValue;\n"
            + "    }\n"
            + "}";

}
