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
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.exporter.json.VaultEntryJsonFileExporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.json.VaultEntryJsonFileImporter;
import de.opendiabetes.vault.util.EasyFormatter;
import de.opendiabetes.vault.util.VaultEntryUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class to manage data-set repository. This class is not threadsafe at all.
 *
 * @author juehv
 */
public class CliRepositoryManager {

    private static CliRepositoryManager INSTANCE;
    public static final String COMPLETE_DATA = "all";

    private static final Logger LOG = Logger.getLogger(CliVaultInit.class.getName());
    public static final String DIR_VAULT = ".vault";
    public static final String DIR_IMPORT = "importBackup";
    public static final String DIR_EXPORT = "export";
    public static final String FILE_JOURNAL = "journal.txt";
    public static final String FILE_DATA = "data.json.gz";
    public static final String REPOSITORY_VERSION = "0.1";

    private final FileWriter journalWriter;
    private final File vaultDir;
    private final File importDir;
    private final File exportDir;
    private final File journalFile;
    private final File dataFile;

    private CliRepositoryManager(File vaultDir, File importDir, File exportDir, File journalFile, File dataFile) throws IOException {
        this.journalWriter = new FileWriter(journalFile, true);
        this.vaultDir = vaultDir;
        this.importDir = importDir;
        this.exportDir = exportDir;
        this.journalFile = journalFile;
        this.dataFile = dataFile;
    }

    private static boolean createDir(File dir) {
        boolean result = dir.mkdirs();
        if (!result) {
            LOG.log(Level.SEVERE, "Can''t create directory {0}", dir.getAbsolutePath());
        }
        return result;
    }

    public static CliRepositoryManager initRepository(String basePath) {
        // create files 
        File targetDir = new File(basePath);
        File vaultDir = new File(targetDir.getAbsolutePath().concat(File.separator).concat(DIR_VAULT));
        File importDir = new File(targetDir.getAbsolutePath().concat(File.separator).concat(DIR_IMPORT));
        File exportDir = new File(targetDir.getAbsolutePath().concat(File.separator).concat(DIR_EXPORT));
        File journalFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(FILE_JOURNAL));
        File dataFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(FILE_DATA));

        // check path
        if (targetDir.exists()) {
            LOG.severe("Directory already exists.");
            return null;
        }

        // create dirs
        if (!createDir(targetDir)) {
            return null;
        }
        if (!createDir(vaultDir)) {
            return null;
        }
        if (!createDir(importDir)) {
            return null;
        }
        if (!createDir(exportDir)) {
            return null;
        }

        // create config file and data file
        try {
            INSTANCE = new CliRepositoryManager(vaultDir, importDir, exportDir,
                    journalFile, dataFile);

            INSTANCE.writeLineToJournal("OpenDiabetes Vault Repository Journal");
            INSTANCE.writeLineToJournal("-------------------------------------");
            INSTANCE.writeLineToJournal("Repository created in version " + REPOSITORY_VERSION);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing journal file.", ex);
            return null;
        }

        return INSTANCE;
    }

    public static CliRepositoryManager getCurrentRepository() {
        LOG.info("Try to open repositoy");
        if (INSTANCE == null) {
            // prepare
            File targetDir = new File(".");
            File vaultDir = new File(targetDir.getAbsolutePath().concat(File.separator).concat(DIR_VAULT));
            File importDir = new File(targetDir.getAbsolutePath().concat(File.separator).concat(DIR_IMPORT));
            File exportDir = new File(targetDir.getAbsolutePath().concat(File.separator).concat(DIR_EXPORT));
            File journalFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(FILE_JOURNAL));
            File dataFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(FILE_DATA));

            // check repository structure
            if (!vaultDir.exists() || !importDir.exists() || !exportDir.exists()
                    || !journalFile.exists()) {
                LOG.severe("Not a OpenDiabetesVault repository. Create repository with init fist.");
                return null;
            }

            if (!vaultDir.canWrite() || !importDir.canWrite() || !exportDir.canWrite()
                    || !journalFile.canWrite()) {
                LOG.severe("No write permission for this repository.");
                return null;
            }

            if (!vaultDir.isDirectory() || !importDir.isDirectory() || !exportDir.isDirectory()) {
                LOG.severe("Some directories are corrupted.");
                return null;
            }

            // create journal writer and object
            try {
                INSTANCE = new CliRepositoryManager(vaultDir, importDir, exportDir,
                        journalFile, dataFile);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error writing journal file.", ex);
                return null;
            }
        }
        return INSTANCE;
    }

    public void writeLineToJournal(String line) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(EasyFormatter.formatTimestampToLogEntry(new Date()));
            sb.append(" - ");
            sb.append(line);
            sb.append("\n");
            journalWriter.write(sb.toString());
            journalWriter.flush();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing to journal.", ex);
        }
    }

    public String readJournal() throws IOException {
        journalWriter.flush();

        BufferedReader br = new BufferedReader(new FileReader(journalFile));

        StringBuilder sb = new StringBuilder();
        String st;
        while ((st = br.readLine()) != null) {
            sb.append(st).append("\n");
        }
        br.close();
        return sb.toString();
    }

    public void closeJournal() {
        try {
            journalWriter.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error while closing journal", ex);
        }
    }

    public void mergeDataIntoRepository(List<VaultEntry> data) throws IllegalAccessException {
        LOG.info("Merge data to repository.");
        // read old dataset
        List<VaultEntry> entries = getCompleteData();

        // merge data
        entries.addAll(data);
        entries = VaultEntryUtils.removeDublicates(entries);
        entries.sort(new VaultEntryUtils());

        // write new dataset
        dataFile.delete();
        VaultEntryJsonFileExporter exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
        exporter.exportDataToFile(dataFile.getAbsolutePath(), entries, true);
    }

    public List<VaultEntry> getCompleteData() throws IllegalAccessException {
        LOG.info("Read complete repository.");
        List<VaultEntry> entries = new ArrayList<>();
        if (dataFile.exists() && dataFile.length() > 0) {
            VaultEntryJsonFileImporter importer = new VaultEntryJsonFileImporter(new ImporterOptions());
            entries.addAll(importer.importDataFromFile(dataFile.getAbsolutePath()));
        }
        return entries;
    }

    List<VaultEntry> getDateFromTag(String input) throws IllegalAccessException {
        LOG.info("Read tag repository");

        File tagFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(input).concat(".tag.gz"));
        if (!tagFile.exists() || !tagFile.canRead()) {
            LOG.log(Level.SEVERE, "Can''t read tag file: {0}", tagFile.getName());
            return null;
        }

        List<VaultEntry> entries = new ArrayList<>();
        if (tagFile.length() > 0) {
            VaultEntryJsonFileImporter importer = new VaultEntryJsonFileImporter(new ImporterOptions());
            entries.addAll(importer.importDataFromFile(tagFile.getAbsolutePath()));
        }
        return entries;
    }

    public void addFilesToImportFolder(List<File> importFile) throws FileNotFoundException, IOException {
        LOG.info("Backup import files");
        // create file
        File outputFile = new File(importDir,
                EasyFormatter.formatTimestampToFilename(new Date()) + "_import.zip");
        if (outputFile.exists()) {
            outputFile = new File(importDir,
                    EasyFormatter.formatTimestampToFilename(new Date()) + "_import-"
                    + new Random().nextInt() + ".zip");
        }

        // dump files
        FileOutputStream fos = new FileOutputStream(outputFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File fileToZip : importFile) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
    }

    /**
     * Exports given data to a file wihtin the export folder. Generates a
     * suitable filename.
     *
     * @param exportData data to export.
     * @param exporter used exporter.
     * @param deflate indicates if export data should be compressed.
     *
     * @return the file name or null if an error occurred.
     */
    public String exportDataToFile(List<VaultEntry> exportData, FileExporter exporter, boolean deflate) {
        // prepare file name
        String fileEnding = exporter.getFileEnding();
        if (deflate) {
            fileEnding += ".gz";
        }
        File targetFile = new File(exportDir.getAbsolutePath() + "/" + EasyFormatter
                .formatTimestampToFilename(new Date()) + "_export." + fileEnding);
        if (targetFile.exists()) {
            targetFile = new File(exportDir.getAbsolutePath() + "/" + EasyFormatter
                    .formatTimestampToFilename(new Date()) + "_export-"
                    + new Random().nextInt() + "." + fileEnding);
        }

        // export data
        int result = exporter.exportDataToFile(targetFile.getAbsolutePath(), exportData, deflate);

        if (result != FileExporter.RESULT_OK) {
            return null;
        }
        writeLineToJournal("Exported data to File: " + targetFile.getName());
        return targetFile.getName();
    }

    public List<Map.Entry<String, Date>> getTagList() {
        LOG.info("Read tag list.");
        File[] tagFiles = vaultDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String nameOfFile = pathname.getName();
                return (nameOfFile != null && !nameOfFile.isEmpty() && nameOfFile.endsWith(".tag.gz"));
            }
        });

        List<Map.Entry<String, Date>> returnValue = new ArrayList<>();
        for (File item : tagFiles) {
            returnValue.add(new AbstractMap.SimpleEntry<>(
                    item.getName().substring(0, item.getName().indexOf(".tag.gz")),
                    new Date(item.lastModified())));
        }
        return returnValue;
    }

    public List<String> getTagNameList() {
        LOG.info("Read tag list.");
        File[] tagFiles = vaultDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String nameOfFile = pathname.getName();
                return (nameOfFile != null && !nameOfFile.isEmpty() && nameOfFile.endsWith(".tag.gz"));
            }
        });

        List<String> returnValue = new ArrayList<>();
        for (File item : tagFiles) {
            returnValue.add(
                    item.getName().substring(0, item.getName().indexOf(".tag.gz")));
        }
        return returnValue;
    }

    public void createTagFromData(List<VaultEntry> data, String targetTag) {
        File targetFile = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(targetTag).concat(".tag.gz"));

        // sanity jobs
        data = VaultEntryUtils.removeDublicates(data);
        data.sort(new VaultEntryUtils());

        // write new dataset
        VaultEntryJsonFileExporter exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
        exporter.exportDataToFile(targetFile.getAbsolutePath(), data, true);

        writeLineToJournal("Created new tag: " + targetTag);
    }

    public void copyTag(String sourceTag, String targetTag) throws IOException {
        LOG.log(Level.INFO, "search for source tag: {0}", sourceTag);
        File sourceFile;
        if (sourceTag.equalsIgnoreCase(COMPLETE_DATA)) {
            sourceFile = dataFile;
        } else {
            sourceFile = new File(vaultDir.getAbsolutePath()
                    .concat(File.separator).concat(sourceTag).concat(".tag.gz"));
        }
        File targetFile = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(targetTag).concat(".tag.gz"));
        if (sourceFile.exists() && sourceFile.canRead()) {
            Path copyPath = Paths.get(targetFile.getAbsolutePath());
            Path originalPath = Paths.get(sourceFile.getAbsolutePath());
            Files.copy(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Tag copy successful.");
            writeLineToJournal("Tag \"" + targetTag
                    + "\" successfully created from \"" + sourceTag + "\".");
        } else {
            LOG.warning("Can't read source file.");
        }
    }

    public void removeTag(String remove) throws IOException {
        File removeTag = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(remove).concat(".tag.gz"));
        if (removeTag.exists()) {
            removeTag.delete();
            LOG.log(Level.INFO, "Removed tag \"{0}\" successfully.", remove);
            writeLineToJournal("Removed tag \"" + remove + "\".");
        } else {
            LOG.log(Level.WARNING, "Could not find tag \"{0}\" to remove.", remove);
        }
    }
}
