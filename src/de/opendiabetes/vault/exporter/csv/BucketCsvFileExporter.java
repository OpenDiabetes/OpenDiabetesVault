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
package de.opendiabetes.vault.exporter.csv;

import de.opendiabetes.vault.data.container.RefinedVaultEntry;
import de.opendiabetes.vault.data.container.RefinedVaultEntryType;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.ExportEntry;
import de.opendiabetes.vault.exporter.FileExporter;
import de.opendiabetes.vault.util.TimestampUtils;
import de.opendiabetes.vault.util.VaultEntryUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Exporter to export data as buckets within a CSV file.
 *
 * @author juehv
 */
public class BucketCsvFileExporter extends FileExporter<VaultEntry> {
    
    public static final String DELIMITER = ",";
    public static final String ARRAY_DELIMITER = ",";
    
    public BucketCsvFileExporter(BucketExporterOptions options) {
        super(options, new VaultEntryUtils());
    }
    
    @Override
    public String getFileEnding() {
        return ".csv";
    }
    
    @Override
    protected List<ExportEntry> prepareData(List<VaultEntry> data) {
        // TODO add option to fill empty fields with 0
        BucketExporterOptions options = (BucketExporterOptions) super.options;
        List<ExportEntry> returnValue = new ArrayList<>();
        List<VaultEntryType> basicHeader = new ArrayList<>();
        List<RefinedVaultEntryType> refinedHeader = new ArrayList<>();
        
        if (data != null && !data.isEmpty()) {
            List<ExportEntry> buckets = new ArrayList<>();

            // prepare
            data.sort(new VaultEntryUtils());
            Date start = data.get(0).getTimestamp();
            Date end = TimestampUtils.addMinutesToTimestamp(start, options.bucketSizeInMinutes);
            List<VaultEntry> tmpEntries = new ArrayList<>();

            // iterate data to sort into buckets and create header
            for (VaultEntry item : data) {
                // create header
                if (item.getType().equals(VaultEntryType.REFINED_VAULT_ENTRY)) {
                    RefinedVaultEntry refinedItem = (RefinedVaultEntry) item;
                    if (!RefinedVaultEntryType.containIncludingLabel(refinedHeader, refinedItem.getRefinedType())) {
                        refinedHeader.add(refinedItem.getRefinedType());
                    }
                } else {
                    if (!basicHeader.contains(item.getType())) {
                        basicHeader.add(item.getType());
                    }
                }

                // sort into bucket 
                if (item.getTimestamp().before(end)) {
                    tmpEntries.add(item);
                } else {
                    buckets.add(new Bucket(tmpEntries, start, end));
                    start = end;
                    end = TimestampUtils.addMinutesToTimestamp(start,
                            options.bucketSizeInMinutes);
                }
            }

            // update header on all entries
            for (ExportEntry item : buckets) {
                Bucket bkt = (Bucket) item;
                bkt.basicHeader.addAll(basicHeader);
                bkt.refinedHeader.addAll(refinedHeader);
            }
            // TODO save overlapping entries for next bucket

            // add header entry at first postion
            BucketCsvHeader header = new BucketCsvHeader();
            header.basicHeader.addAll(basicHeader);
            header.refinedHeader.addAll(refinedHeader);
            returnValue.add(header);
            returnValue.addAll(buckets);
        }
        
        return returnValue;
    }
    
}
