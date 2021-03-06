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
package de.opendiabetes.vault.data.container;

import java.util.Date;

/**
 * Container type for data that can be calculated from one or more VaultEntries
 *
 * @author juehv
 */
public class RefinedVaultEntry extends VaultEntry {

    private final RefinedVaultEntryType type;
    private final VaultEntryType underlyingType;

    public RefinedVaultEntry(final RefinedVaultEntryType type,
            final VaultEntryType underlyingType, final Date timestamp,
            final double value) {
        super(VaultEntryType.REFINED_VAULT_ENTRY, timestamp, value);
        this.type = type;
        this.underlyingType = underlyingType;
    }

    public RefinedVaultEntry(final RefinedVaultEntryType type, final Date timestamp,
            final double value) {
        this(type, VaultEntryType.REFINED_VAULT_ENTRY, timestamp, value);
    }

    public RefinedVaultEntryType getRefinedType() {
        return type;
    }

    public VaultEntryType getUnderlyingType() {
        return underlyingType;
    }
}
