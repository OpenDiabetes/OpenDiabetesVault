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

import de.opendiabetes.vault.util.TimestampUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * This class defines a container for a measurable data entry.
 *
 * @author juehv
 */
public class VaultEntry implements Serializable {

    public class Base {

        /**
         * Origin of the entry (Pump, CGM device ...)
         */
        public final String origin;

        /**
         * Import source of the entry (file, nightscout ...)
         */
        public final String source;

        public Base(String origin, String source) {
            this.origin = origin;
            this.source = source;
        }
    }

    private final Base base;
    private final VaultEntryType type;
    private final Date timestamp;
    private double value;
    private Object valueExtension;

    /**
     * A constructor of VaultEntry, setting the type, timestamp and value of the
     * VaultEntry.
     *
     * @param type The parameter that type will be set to.
     * @param timestamp The parameter that timestamp will be set to.
     * @param value The parameter that value will be set to.
     */
    public VaultEntry(final VaultEntryType type, final Date timestamp,
            final double value) {
        this.base = null;
        this.type = type;
        this.timestamp = TimestampUtils.copyTimestamp(timestamp);
        this.value = value;
    }

    /**
     * A constructor of VaultEntry, setting the type, timestamp and value of the
     * VaultEntry.
     *
     * @param origin
     * @param source
     * @param type The parameter that type will be set to.
     * @param timestamp The parameter that timestamp will be set to.
     * @param value The parameter that value will be set to.
     */
    public VaultEntry(final String origin, final String source,
            final VaultEntryType type, final Date timestamp, final double value) {
        this.base = new Base(origin, source);
        this.type = type;
        this.timestamp = TimestampUtils.copyTimestamp(timestamp);
        this.value = value;
    }

    /**
     * A constructor of VaultEntry, copying all values of the VaultEntry passed
     * as argument.
     *
     * @param copy The VaultEntry whose fields will be copied.
     */
    public VaultEntry(final VaultEntry copy) {
        this.base = copy.base;
        this.type = copy.type;
        this.timestamp = copy.timestamp;
        this.value = copy.value;
        this.valueExtension = copy.valueExtension;
    }

    public Base getBase() {
        return base;
    }

    public VaultEntryType getType() {
        return type;
    }

    public Date getTimestamp() {
        return TimestampUtils.copyTimestamp(timestamp);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Object getValueExtension() {
        return valueExtension;
    }

    public void setValueExtension(Object valueExtension) {
        this.valueExtension = valueExtension;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.type);
        hash = 17 * hash + Objects.hashCode(this.timestamp);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        hash = 17 * hash + Objects.hashCode(this.valueExtension);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VaultEntry other = (VaultEntry) obj;
        return true;
    }
    
    

}
