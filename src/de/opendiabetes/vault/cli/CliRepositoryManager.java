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
import de.opendiabetes.vault.exporter.ExporterOptions;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.exporter.json.SliceEntryJsonFileExporter;
import de.opendiabetes.vault.exporter.json.VaultEntryJsonFileExporter;
import de.opendiabetes.vault.importer.ImporterOptions;
import de.opendiabetes.vault.importer.json.SliceEntryJsonFileImporter;
import de.opendiabetes.vault.importer.json.VaultEntryJsonFileImporter;
import de.opendiabetes.vault.util.EasyFormatter;
import de.opendiabetes.vault.util.TimestampUtils;
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
    public static final String TAG_EXTENSION = ".tag.gz";
    public static final String TAG_SLICE_EXTENSION = ".tag-slices.gz";
    public static final String REPOSITORY_VERSION = "0.2";

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

    //**************
    //region Folder management
    private static boolean createDir(File dir) {
        boolean result = dir.mkdirs();
        if (!result) {
            LOG.log(Level.SEVERE, "Can't create directory {0}", dir.getAbsolutePath());
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

        // create dirs
        if (!targetDir.exists() && !createDir(targetDir)) {
            LOG.severe("Can't create target directory.");
            return null;
        } else if (targetDir.exists() && !targetDir.isDirectory()) {
            LOG.severe("Target path exists but is not a directory.");
            return null;
        }

        if (!vaultDir.exists() && !createDir(vaultDir)) {
            LOG.severe("Can't create new .vault directory.");
            return null;
        }

        if (!importDir.exists() && !createDir(importDir)) {
            LOG.severe("Can't create new importBackup directory.");
            return null;
        } else if (importDir.exists() && !importDir.isDirectory()) {
            LOG.severe("Import backup path exists but is not a directory.");
            return null;
        }

        if (!exportDir.exists() && !createDir(exportDir)) {
            LOG.severe("Can't create new export directory.");
            return null;
        } else if (importDir.exists() && !importDir.isDirectory()) {
            LOG.severe("Export path exists but is not a directory.");
            return null;
        }

        // create instance and journal
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

    public void saveFilesToImportFolder(List<File> importFile) throws FileNotFoundException, IOException {
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
    public String exportDataToExportFolder(List exportData, FileExporter exporter, boolean deflate) {
        // prepare file name
        String fileEnding = exporter.getFileEnding();
        if (deflate) {
            fileEnding += ".gz";
        }
        File targetFile = new File(exportDir.getAbsolutePath() + "/" + EasyFormatter
                .formatTimestampToFilename(new Date()) + "_export." + fileEnding);
        while (targetFile.exists()) {
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

    // endregion
    //**************
    //region Journal management
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

    // endregion
    //**************
    // region Master (complete dataset) management
    public void mergeDataIntoMaster(List<VaultEntry> data) throws IllegalAccessException {
        LOG.info("Merge data to repository.");
        // read old dataset
        List<VaultEntry> entries = getDataFromMaster();

        // merge data
        entries.addAll(data);
        entries = VaultEntryUtils.removeDublicates(entries);
        entries.sort(new VaultEntryUtils());

        // write new dataset
        dataFile.delete();
        VaultEntryJsonFileExporter exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
        exporter.exportDataToFile(dataFile.getAbsolutePath(), entries, true);
    }

    public List<VaultEntry> getDataFromMaster() throws IllegalAccessException {
        LOG.info("Read complete repository.");
        List<VaultEntry> entries = new ArrayList<>();
        if (dataFile.exists() && dataFile.length() > 0) {
            VaultEntryJsonFileImporter importer = new VaultEntryJsonFileImporter(new ImporterOptions());
            entries.addAll(importer.importDataFromFile(dataFile.getAbsolutePath()));
        }
        return entries;
    }

    // endregion
    //**************
    // retion TAG management
    public List<Map.Entry<String, Date>> getTagList() {
        LOG.info("Read tag list.");
        File[] tagFiles = vaultDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String nameOfFile = pathname.getName();
                return (nameOfFile != null && !nameOfFile.isEmpty() && nameOfFile.endsWith(TAG_EXTENSION));
            }
        });

        List<Map.Entry<String, Date>> returnValue = new ArrayList<>();
        for (File item : tagFiles) {
            returnValue.add(new AbstractMap.SimpleEntry<>(
                    item.getName().substring(0, item.getName().indexOf(TAG_EXTENSION)),
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
                return (nameOfFile != null && !nameOfFile.isEmpty() && nameOfFile.endsWith(TAG_EXTENSION));
            }
        });

        List<String> returnValue = new ArrayList<>();
        for (File item : tagFiles) {
            returnValue.add(
                    item.getName().substring(0, item.getName().indexOf(TAG_EXTENSION)));
        }
        return returnValue;
    }

    List<List<VaultEntry>> getDataFromTag(String input) throws IllegalAccessException {
        LOG.info("Read tag repository");
        List<List<VaultEntry>> returnValue = new ArrayList<>();

        // get the data
        File tagFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(input).concat(TAG_EXTENSION));
        if (!tagFile.exists() || !tagFile.canRead()) {
            LOG.log(Level.SEVERE, "Can't read tag file: {0}", tagFile.getName());
            return null;
        }

        List<VaultEntry> entries = new ArrayList<>();
        if (tagFile.length() > 0) {
            VaultEntryJsonFileImporter importer = new VaultEntryJsonFileImporter(new ImporterOptions());
            entries.addAll(importer.importDataFromFile(tagFile.getAbsolutePath()));
            LOG.info("Got data from tag file.");
        }

        // get the slices
        File tagSliceFile = new File(vaultDir.getAbsolutePath().concat(File.separator).concat(input).concat(TAG_SLICE_EXTENSION));
        if (!tagSliceFile.exists() || !tagSliceFile.canRead()) {
            LOG.log(Level.WARNING, "Can't read slice tag file: {0}. No slicing.", tagFile.getName());
            returnValue.add(entries);
            return returnValue;
        }

        if (tagSliceFile.length() > 0) {
            SliceEntryJsonFileImporter importer = new SliceEntryJsonFileImporter(new ImporterOptions());
            List<SliceEntry> slices = importer.importDataFromFile(tagSliceFile.getAbsolutePath());
            LOG.info("Got slices from slice file. Slicing ...");

            // slicing
            for (SliceEntry slice : slices) {
                List<VaultEntry> tmpSlice = new ArrayList<>();
                // create start and end point with +/- 1 minute to use .after & .before
                Date startTimestamp = TimestampUtils.addMinutesToTimestamp(
                        TimestampUtils.createCleanTimestamp(slice.startTimestamp), -1);
                Date endTimestamp = TimestampUtils.addMinutesToTimestamp(startTimestamp,
                        slice.durationInMinutes + 2);
                for (VaultEntry entry : entries) {
                    Date entryTimestamp = TimestampUtils.createCleanTimestamp(entry.getTimestamp());

                    if (entryTimestamp.after(startTimestamp)
                            && entryTimestamp.before(endTimestamp)) {
                        tmpSlice.add(entry);
                    }
                }
                returnValue.add(tmpSlice);
            }
        }

        return returnValue; // returns empty list on error of slicing.
    }

    public void createTagFromData(List<List<VaultEntry>> data, String targetTag) {
        File targetFile = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(targetTag).concat(TAG_EXTENSION));
        File targetSliceFile = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(targetTag).concat(TAG_SLICE_EXTENSION));

        List<SliceEntry> slices = null;
        if (data != null && !data.isEmpty()) {
            List<VaultEntry> mergedData;
            if (data.size() > 1) {
                LOG.info("Output contains more than one slice. A slice file will be exported to the tag.");
                // generate slice entries
                slices = new ArrayList<>();
                for (List<VaultEntry> item : data) {
                    if (!item.isEmpty()) {
                        Date startDate = item.get(0).getTimestamp();
                        Date endDate = item.get(item.size() - 1).getTimestamp();

                        slices.add(new SliceEntry(startDate,
                                TimestampUtils.getDurationInMinutes(startDate, endDate)));
                    }
                }

                // merge slices
                mergedData = new ArrayList<>();
                for (List<VaultEntry> item : data) {
                    mergedData.addAll(item);
                }
            } else {
                mergedData = data.get(0);
            }

            // sanity jobs
            mergedData = VaultEntryUtils.removeDublicates(mergedData);
            mergedData.sort(new VaultEntryUtils());

            // write new dataset
            VaultEntryJsonFileExporter exporter = new VaultEntryJsonFileExporter(new ExporterOptions());
            exporter.exportDataToFile(targetFile.getAbsolutePath(), mergedData, true);

            writeLineToJournal("Created new tag: " + targetTag);

            //TODO sort and remove duplicates
            // write slices
            SliceEntryJsonFileExporter sliceExporter = new SliceEntryJsonFileExporter(new ExporterOptions());
            sliceExporter.exportDataToFile(targetSliceFile.getAbsolutePath(), slices, true);

            writeLineToJournal("Created new slice file for tag: " + targetTag);
        } else {
            LOG.warning("Given data was empty.");
        }
    }

    public void copyTag(String sourceTag, String targetTag) throws IOException {
        LOG.log(Level.INFO, "search for source tag: {0}", sourceTag);
        File sourceFile;
        File sourceSliceFile = null;
        if (sourceTag.equalsIgnoreCase(COMPLETE_DATA)) {
            sourceFile = dataFile;
        } else {
            sourceFile = new File(vaultDir.getAbsolutePath()
                    .concat(File.separator).concat(sourceTag).concat(TAG_EXTENSION));
            sourceSliceFile = new File(vaultDir.getAbsolutePath()
                    .concat(File.separator).concat(sourceTag).concat(TAG_SLICE_EXTENSION));
        }
        File targetFile = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(targetTag).concat(TAG_EXTENSION));
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

        if (sourceSliceFile != null && sourceSliceFile.exists()) {
            File targetSliceFile = new File(vaultDir.getAbsolutePath()
                    .concat(File.separator).concat(targetTag).concat(TAG_SLICE_EXTENSION));
            Path copyPath = Paths.get(targetSliceFile.getAbsolutePath());
            Path originalPath = Paths.get(sourceSliceFile.getAbsolutePath());
            Files.copy(originalPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Tag slices copy successful.");
            writeLineToJournal("Slices for tag \"" + targetTag
                    + "\" successfully created from \"" + sourceTag + "\".");

        } else {
            LOG.warning("Can't read/find source slice file. No target slice file created.");
        }
    }

    public void removeTag(String remove) throws IOException {
        File removeTag = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(remove).concat(TAG_EXTENSION));
        File removeTagSlice = new File(vaultDir.getAbsolutePath()
                .concat(File.separator).concat(remove).concat(TAG_SLICE_EXTENSION));

        if (removeTag.exists()) {
            removeTag.delete();
            LOG.log(Level.INFO, "Removed tag \"{0}\" successfully.", remove);
            writeLineToJournal("Removed tag \"" + remove + "\".");
        } else {
            LOG.log(Level.WARNING, "Could not find tag \"{0}\" to remove.", remove);
        }

        if (removeTagSlice.exists()) {
            removeTagSlice.delete();
            LOG.log(Level.INFO, "Removed slice file for tag \"{0}\" successfully.", remove);
            writeLineToJournal("Removed slices for tag \"" + remove + "\".");
        } else {
            LOG.log(Level.WARNING, "Could not find slice file for tag \"{0}\" to remove.", remove);
        }
    }

    // endregion
}
