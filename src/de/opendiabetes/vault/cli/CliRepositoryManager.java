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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class to manage data repository.
 *
 * @author juehv
 */
public class CliRepositoryManager {

    private static CliRepositoryManager INSTANCE;

    private static final Logger LOG = Logger.getLogger(CliVaultInit.class.getName());
    public static final String DIR_VAULT = ".vault";
    public static final String DIR_IMPORT = "import";
    public static final String DIR_EXPORT = "export";
    public static final String FILE_JOURNAL = "journal.txt";
    public static final String FILE_DATA = "data.json.gz";
    public static final String REPOSITORY_VERSION = "0.1";

    private final FileWriter journalWriter;
    private final File importDir;
    private final File exportDir;
    private final File journalFile;
    private final File dataFile;

    private CliRepositoryManager(File importDir, File exportDir, File journalFile, File dataFile) throws IOException {
        this.journalWriter = new FileWriter(journalFile, true);
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
            INSTANCE = new CliRepositoryManager(importDir, exportDir,
                    journalFile, dataFile);

            INSTANCE.writeLineToJournal("OpenDiabetes Vault Repository Journal\n");
            INSTANCE.writeLineToJournal("-------------------------------------\n");
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

            // create journal writer and object
            try {
                INSTANCE = new CliRepositoryManager(importDir, exportDir,
                        journalFile, dataFile);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error writing journal file.", ex);
                return null;
            }
        }
        return INSTANCE;
    }

    public void writeLineToJournal(String line) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(EasyFormatter.formatTimestampToLogEntry(new Date()));
        sb.append(" - ");
        sb.append(line);
        sb.append("\n");
        journalWriter.write(sb.toString());
        journalWriter.flush();
        System.out.println(sb.toString());
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

    public void closeJournal() throws IOException {
        journalWriter.close();
    }

    public void mergeDataToRepository(List<VaultEntry> data) throws IllegalAccessException {
        LOG.info("Merge data to repository.");
        // read old dataset
        List<VaultEntry> entries = getAllRepositoryData();

        // merge data
        entries.addAll(data);
        entries = VaultEntryUtils.removeDublicates(entries);
        entries.sort(new VaultEntryUtils());

        // write new dataset
        dataFile.delete();
        VaultEntryJsonFileExporter exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
        exporter.exportDataToFile(dataFile.getAbsolutePath(), entries, true);
    }

    public List<VaultEntry> getAllRepositoryData() throws IllegalAccessException {
        LOG.info("Read complete repository.");
        List<VaultEntry> entries = new ArrayList<>();
        if (dataFile.exists() && dataFile.length() > 0) {
            VaultEntryJsonFileImporter importer = new VaultEntryJsonFileImporter(new ImporterOptions());
            entries.addAll(importer.importDataFromFile(dataFile.getAbsolutePath()));
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

    public File getExportFile(FileExporter exporter, boolean deflate) {
        String fileEnding = exporter.getFileEnding();
        if (deflate) {
            fileEnding += ".gz";
        }
        File targetFile = new File(exportDir.getAbsolutePath() + "/" + EasyFormatter
                .formatTimestampToFilename(new Date()) + "_export." + fileEnding);
        if (targetFile.exists()) {
            return new File(exportDir.getAbsolutePath() + "/" + EasyFormatter
                    .formatTimestampToFilename(new Date()) + "_export-"
                    + new Random().nextInt() + "." + fileEnding);
        }
        return targetFile;
    }

}

// TODO make threadsafe(er) with lock file
