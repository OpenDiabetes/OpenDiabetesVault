/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.opendiabetes.vault.processing;

import de.opendiabetes.vault.data.container.VaultEntry;
import java.util.List;

/**
 * Interface for loadable processing logic
 *
 * @author juehv
 */
public interface ProcessingContainer {

    List<List<VaultEntry>> processData(List<List<VaultEntry>> inputData);
}
