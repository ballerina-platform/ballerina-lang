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
package org.wso2.ballerinalang.programfile.cpentries;


import org.wso2.ballerinalang.programfile.WorkerDataChannelInfo;

import java.util.Objects;

/**
 * {@code WorkerDataChannelRefCPEntry} represents a Ballerina worker in the constant pool.
 *
 * @since 0.90
 */
@Deprecated
public class WorkerDataChannelRefCPEntry implements ConstantPoolEntry {
    // Index to a valid Package entry in the constant pool
    private int uniqueNameCPIndex;
    private String uniqueName;

    private WorkerDataChannelInfo workerDataChannelInfo;

    public WorkerDataChannelRefCPEntry(int uniqueNameCPIndex, String uniqueName) {
        this.uniqueNameCPIndex = uniqueNameCPIndex;
        this.uniqueName = uniqueName;
    }

    public int getUniqueNameCPIndex() {
        return uniqueNameCPIndex;
    }

    public void setUniqueNameCPIndex(int uniqueNameCPIndex) {
        this.uniqueNameCPIndex = uniqueNameCPIndex;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public WorkerDataChannelInfo getWorkerDataChannelInfo() {
        return workerDataChannelInfo;
    }

    public void setWorkerDataChannelInfo(WorkerDataChannelInfo workerDataChannelInfo) {
        this.workerDataChannelInfo = workerDataChannelInfo;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_WRKR_DATA_CHNL_REF;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueNameCPIndex, uniqueName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkerDataChannelRefCPEntry && uniqueNameCPIndex ==
                (((WorkerDataChannelRefCPEntry) obj).uniqueNameCPIndex) &&
                uniqueName == ((WorkerDataChannelRefCPEntry) obj).uniqueName;
    }
}
