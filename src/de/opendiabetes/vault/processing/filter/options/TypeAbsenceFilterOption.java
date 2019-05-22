/*
 * Copyright (C) 2018 tiweGH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.opendiabetes.vault.processing.filter.options;

import de.opendiabetes.vault.data.container.VaultEntryType;
import java.util.HashMap;

/**
 *
 * @author tiweGH
 */
public class TypeAbsenceFilterOption extends FilterOption {

    private final long marginAfterTrigger; // minutes after a trigger until data becomes interesting again.
    private final VaultEntryType vaultEntryType;

    /**
     * The Filter gets an EntryType and excludes all entries from the
     * FilterResult, whose EntryType match or are located in the time margin
     * after a trigger of the group occurs
     *
     * @param marginAfterTrigger minutes after a trigger until data becomes
     * interesting again
     * @param vaultEntryType
     */
    public TypeAbsenceFilterOption(VaultEntryType vaultEntryType, long marginAfterTrigger) {
        super(new HashMap<>(), null);
        super.getParameterNameAndType().put("VaultEntryType", VaultEntryType.class);
        super.getParameterNameAndType().put("MarginAfterTrigger", long.class);

        this.vaultEntryType = vaultEntryType;
        this.marginAfterTrigger = marginAfterTrigger;
    }

    public VaultEntryType getVaultEntryType() {
        return vaultEntryType;
    }

    public long getMargingAfterTrigger() {
        return marginAfterTrigger;
    }

}
