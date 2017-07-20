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
 * {@code WorkerInvokeCPEntry} represents a Ballerina worker invocation in the constant pool.
 *
 * @since 0.90
 */
public class WorkerInvokeCPEntry implements ConstantPoolEntry {
    private int typesSignatureCPIndex;
    private BType[] bTypes;
    // Registers which contains worker incoming arguments
    private int[] argRegs;

    public WorkerInvokeCPEntry(int[] argRegs, BType[] btypes) {
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

    public ConstantPoolEntry.EntryType getEntryType() {
        return EntryType.CP_ENTRY_WORKER_INVOKE;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(argRegs);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkerInvokeCPEntry && Arrays.equals(argRegs, ((WorkerInvokeCPEntry) obj).argRegs);
    }
}
