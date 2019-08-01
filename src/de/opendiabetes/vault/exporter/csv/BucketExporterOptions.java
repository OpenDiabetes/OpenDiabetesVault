/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.opendiabetes.vault.exporter.csv;

import de.opendiabetes.vault.exporter.ExporterOptions;

/**
 * Options Class for BucketCsvFileExporter. 
 *
 * @author juehv
 */
public class BucketExporterOptions extends ExporterOptions {

    /** 
     * Specifies the bucket size in minutes.
     */
    public final int bucketSizeInMinutes;

    public BucketExporterOptions(int bucketSizeInMinutes) {
        this.bucketSizeInMinutes = bucketSizeInMinutes;
        super.exportRefinedVaultEntries = true;
    }

}
