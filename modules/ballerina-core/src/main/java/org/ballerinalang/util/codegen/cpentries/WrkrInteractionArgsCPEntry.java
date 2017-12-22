/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.codegen.cpentries;

import org.ballerinalang.model.types.BType;

import java.util.Arrays;

/**
 * {@code WrkrInteractionArgsCPEntry} represents a Ballerina worker interaction arguments CP entry.
 *
 * @since 0.90
 */
public class WrkrInteractionArgsCPEntry implements ConstantPoolEntry {
    private UTF8CPEntry typesSignatureCPEntry;
    private BType[] bTypes;
    // Registers which contains worker incoming arguments
    private int[] argRegs;

    public WrkrInteractionArgsCPEntry(int[] argRegs, UTF8CPEntry typesSignatureCPEntry) {
        this.argRegs = argRegs;
        this.typesSignatureCPEntry = typesSignatureCPEntry;
    }

    public int[] getArgRegs() {
        return argRegs;
    }

    public BType[] getBTypes() {
        return bTypes;
    }

    public void setBTypes(BType[] bTypes) {
        this.bTypes = bTypes;
    }

    public UTF8CPEntry getTypesSignatureCPEntry() {
        return typesSignatureCPEntry;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_WRKR_INTERACTION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrkrInteractionArgsCPEntry)) {
            return false;
        }
        WrkrInteractionArgsCPEntry that = (WrkrInteractionArgsCPEntry) o;
        if (typesSignatureCPEntry != null ? !typesSignatureCPEntry.equals(that.typesSignatureCPEntry) : that
                .typesSignatureCPEntry != null) {
            return false;
        }
        return Arrays.equals(argRegs, that.argRegs);
    }

    @Override
    public int hashCode() {
        int result = typesSignatureCPEntry != null ? typesSignatureCPEntry.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(argRegs);
        return result;
    }
}
