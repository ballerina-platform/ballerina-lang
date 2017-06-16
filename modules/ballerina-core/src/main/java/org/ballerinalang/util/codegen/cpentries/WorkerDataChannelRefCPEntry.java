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
import org.ballerinalang.runtime.worker.WorkerDataChannel;

import java.util.Objects;
/**
 * {@code WorkerDataChannelRefCPEntry} represents a Ballerina worker in the constant pool.
 *
 * @since 0.90
 */
public class WorkerDataChannelRefCPEntry implements ConstantPoolEntry {
    // Index to a valid Package entry in the constant pool
    private int packageCPIndex;

    // Index to a valid name index in the constant pool
    private int nameCPIndex;

    private BType[] types;

    private WorkerDataChannel workerDataChannel;

    public WorkerDataChannelRefCPEntry(int packageCPIndex, int nameCPIndex) {
        this.packageCPIndex = packageCPIndex;
        this.nameCPIndex = nameCPIndex;
    }

    public int getPackageCPIndex() {
        return packageCPIndex;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
    }

    public WorkerDataChannel getWorkerDataChannel() {
        return workerDataChannel;
    }

    public void setWorkerDataChannel(WorkerDataChannel workerDataChannel) {
        this.workerDataChannel = workerDataChannel;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_FUNCTION_REF;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkerDataChannelRefCPEntry && packageCPIndex ==
                (((WorkerDataChannelRefCPEntry) obj).packageCPIndex) &&
                nameCPIndex == ((WorkerDataChannelRefCPEntry) obj).nameCPIndex;
    }


    public BType[] getTypes() {
        return types;
    }

    public void setTypes(BType[] types) {
        this.types = types;
    }
}
