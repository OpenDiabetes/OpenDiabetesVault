/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.opendiabetes.vault.exporter.csv;

import de.opendiabetes.vault.data.container.RefinedVaultEntryType;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.ExportEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author mswin
 */
public class BucketCsvHeader implements ExportEntry {

    public final List<VaultEntryType> basicHeader = new ArrayList<>();
    public final List<RefinedVaultEntryType> refinedHeader = new ArrayList<>();

    @Override
    public char[] toByteEntryLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        basicHeader.sort(Comparator.<VaultEntryType>naturalOrder());
        refinedHeader.sort(Comparator.<RefinedVaultEntryType>naturalOrder());
        String delimiter = "";

        for (VaultEntryType item : basicHeader) {
            sb.append(delimiter).append(item.toString());
            delimiter = BucketCsvFileExporter.DELIMITER;
        }
        sb.delete(sb.length() - BucketCsvFileExporter.DELIMITER.length(), sb.length());

        for (RefinedVaultEntryType item : refinedHeader) {
            sb.append(delimiter).append(item.toStringWithLabel());
            delimiter = BucketCsvFileExporter.DELIMITER;
        }
        sb.delete(sb.length() - BucketCsvFileExporter.DELIMITER.length(), sb.length());

        return sb.toString().toCharArray();
    }

}
