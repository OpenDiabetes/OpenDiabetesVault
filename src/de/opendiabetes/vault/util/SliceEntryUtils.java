/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.opendiabetes.vault.util;

import de.opendiabetes.vault.data.container.SliceEntry;
import de.opendiabetes.vault.data.container.VaultEntry;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author mswin
 */
public class SliceEntryUtils implements Comparator<SliceEntry> {

    @Override
    public int compare(SliceEntry o1, SliceEntry o2) {
        return o1.startTimestamp.compareTo(o2.startTimestamp);
    }

    public static List<SliceEntry> removeDublicates(List<SliceEntry> list) {
        if (list == null) {
            return null;
        }

        List<SliceEntry> returnValue = list.stream().distinct()
                .collect(Collectors.toList());
        return returnValue;
    }
}
