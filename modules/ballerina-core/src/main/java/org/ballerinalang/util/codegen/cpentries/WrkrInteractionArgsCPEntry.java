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
import java.util.Objects;

/**
 * {@code WrkrInteractionArgsCPEntry} represents a Ballerina worker interaction arguments CP entry.
 *
 * @since 0.90
 */
public class WrkrInteractionArgsCPEntry implements ConstantPoolEntry {
    private int typesSignatureCPIndex;
    private BType[] bTypes;
    // Registers which contains worker incoming arguments
    private int[] argRegs;

    public WrkrInteractionArgsCPEntry(int[] argRegs, BType[] btypes) {
        this.argRegs = argRegs;
        this.bTypes = btypes;
    }

    public int[] getArgRegs() {
        return argRegs;
    }

    public BType[] getbTypes() {
        return bTypes;
    }

    public int getTypesSignatureCPIndex() {
        return typesSignatureCPIndex;
    }

    public void setTypesSignatureCPIndex(int typesSignatureCPIndex) {
        this.typesSignatureCPIndex = typesSignatureCPIndex;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_WRKR_INTERACTION;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bTypes, argRegs);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WrkrInteractionArgsCPEntry
                && Arrays.equals(argRegs, ((WrkrInteractionArgsCPEntry) obj).argRegs)
                && Arrays.equals(bTypes, ((WrkrInteractionArgsCPEntry) obj).bTypes);
    }
}
