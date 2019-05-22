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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import net.openhft.compiler.CompilerUtils;

/**
 * CLI class for filtering data within a repository.
 *
 * @author juehv
 */
@CommandLine.Command(description = "Takes a filter class and filters data accourdingly within the OpenDiabes Vault repository.",
        name = "process", mixinStandardHelpOptions = true, version = "odv filter 0.1")
public class CliProcessing implements Callable<Void> {

    private static final Logger LOG = Logger.getLogger(CliProcessing.class.getName());

    @CommandLine.Option(names = {"-t", "--tag"}, description = "Specifies the repository tag to write data. If tag does exist data will be overwritten.")
    private String tag;

    @CommandLine.Option(names = {"-i", "--input-tag"}, description = "Specifies an input tag to source data. Uses complete data-set as default.")
    private String input = "all";

    @CommandLine.Option(names = {"-f", "--container-file"}, description = "Specifies the file containing the ProcessingContainerImpl class. File can be java source code (.java) or a compiled java class (.class). Use skeleton for implementation.")
    private File processingContainerFile;

    @CommandLine.Option(names = {"--generate-skeleton"}, description = "Generates a skeleton file for a loadable filter class.")
    private boolean generateSkeleton;

    @Override
    public Void call() throws Exception {
        if (generateSkeleton) {
            System.out.println(PROCESSING_CONTAINER_SKELETON);
            System.exit(0);
        }

        CliRepositoryManager repMan = CliRepositoryManager.getCurrentRepository();
        if (repMan == null) {
            System.err.println("Not a ODV repository. Exit.");
            System.exit(-1);
        }
        List<List<VaultEntry>> inputData = null;
        if (input.equalsIgnoreCase("all")) {
            inputData = new ArrayList<>();
            inputData.add(repMan.getCompleteData());
        } else {
            // load tag data
            inputData = new ArrayList<>();
            inputData.add(repMan.getDateFromTag(input));
        }

        if (inputData.isEmpty()) {
            System.err.println("Could not load input data. Does Tag exist?");
            System.exit(-1);
        }

        ProcessingContainer processingContainer = null;
        if (!processingContainerFile.exists() || !processingContainerFile.canRead()) {
            System.err.println("Can't read processing container file. Exit.");
            System.exit(-1);
        } else if (processingContainerFile.getName().endsWith(".java")) {
            processingContainer = loadAndCompileContainerClass();
        } else if (processingContainerFile.getName().endsWith(".class")) {
            processingContainer = loadContainerClass();
        } else {
            System.err.println("Can't find a supported file ending. Exit.");
            System.exit(-1);
        }

        if (processingContainer != null) {
            List<List<VaultEntry>> outputData = processingContainer.processData(inputData);
            if (outputData != null && !outputData.isEmpty()) {
                List<VaultEntry> saveData;
                if (outputData.size() > 1) {
                    LOG.info("Output contains more than one slice. A slice file will be exported.");
                    // TODO generate slice file

                    // merge samples
                    saveData = new ArrayList<>();
                    for (List<VaultEntry> item : outputData) {
                        saveData.addAll(item);
                    }
                } else {
                    saveData = outputData.get(0);
                }
                // save to repository
                repMan.createTagFromData(saveData, tag);
            } else {
                System.out.println("Output data was empty.");
            }
        } else {
            System.err.println("Error loading processing container.");
        }

        System.out.println("Fished successfully.");

        return null;
    }

    private ProcessingContainer loadContainerClass() {
        try {
            // get class name
            String className = processingContainerFile.getName()
                    .substring(0, processingContainerFile.getName().indexOf(".class"));

            // Convert directory of container File to a URL
            URL url = processingContainerFile.getParentFile().getAbsoluteFile().toURI().toURL();
            URL[] urls = new URL[]{url};

            // Create a new class loader with the directory
            ClassLoader cl = new URLClassLoader(urls);

            // Load in the class; MyClass.class should be located in
            // the directory file:/c:/myclasses/com/mycompany
            // as the directory structure is annoying we suggest default package
            Class cls = cl.loadClass(className);
            return (ProcessingContainer) cls.newInstance();
        } catch (InstantiationException | IllegalAccessException | MalformedURLException | ClassNotFoundException ex) {
            Logger.getLogger(CliProcessing.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Can't load processing container. Exit.");
            System.exit(-1);
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
        String className = processingContainerFile.getName()
                .substring(0, processingContainerFile.getName().indexOf(".java"));
        System.out.println(className);

        String classSource = new String(Files.readAllBytes(
                Paths.get(processingContainerFile.getAbsolutePath())));

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
            = "import de.opendiabetes.vault.data.container.VaultEntry;\n"
            + "import de.opendiabetes.vault.processing.*;\n"
            + "import java.util.ArrayList;\n"
            + "import java.util.List;\n"
            + "\n"
            + "public class ProcessingContainerImpl implements ProcessingContainer {\n"
            + "\n"
            + "    @Override\n"
            + "    public List<List<VaultEntry>> processData(List<List<VaultEntry>> inputData) {\n"
            + "        List<List<VaultEntry>> returnValue = new ArrayList<>();\n"
            + "        List<VaultEntry> currentSample = new ArrayList<>();\n"
            + "\n"
            + "        // do something\n"
            + "        \n"
            + "        returnValue.add(currentSample);\n"
            + "        return returnValue;\n"
            + "    }\n"
            + "}";

}
