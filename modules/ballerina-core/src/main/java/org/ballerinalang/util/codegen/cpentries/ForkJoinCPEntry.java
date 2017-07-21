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

import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code ForkJoinCPEntry} represents a Ballerina fork join statement in the constant pool.
 *
 * @since 0.90
 */
public class ForkJoinCPEntry implements ConstantPoolEntry {
    // Registers which contains worker incoming arguments
    private int[] argRegs;

    // Registers to which return  values to be copied
    private int[] retRegs;

    protected Map<String, WorkerInfo> workerInfoMap = new HashMap<>();

    protected ForkJoinStmt forkJoinStmt;
    private boolean isTimeoutAvailable;
    private CallableUnitInfo parentCallableUnitInfo;

    public ForkJoinCPEntry(int[] argRegs, int[] retRegs, ForkJoinStmt forkJoinStmt) {
        this.argRegs = argRegs;
        this.retRegs = retRegs;
        this.forkJoinStmt = forkJoinStmt;
    }


    public ForkJoinStmt getForkJoinStmt() {
        return forkJoinStmt;
    }


    public boolean isTimeoutAvailable() {
        return isTimeoutAvailable;
    }

    public void setTimeoutAvailable(boolean timeoutAvailable) {
        isTimeoutAvailable = timeoutAvailable;
    }

    public int[] getArgRegs() {
        return argRegs;
    }

    public int[] getRetRegs() {
        return retRegs;
    }


    public ConstantPoolEntry.EntryType getEntryType() {
        return ConstantPoolEntry.EntryType.CP_ENTRY_FORK_JOIN;
    }

    public WorkerInfo getWorkerInfo(String workerName) {
        return workerInfoMap.get(workerName);
    }

    public void addWorkerInfo(String attributeName, WorkerInfo workerInfo) {
        workerInfoMap.put(attributeName, workerInfo);
    }

    public Map<String, WorkerInfo> getWorkerInfoMap() {
        return workerInfoMap;
    }

    @Override
    public int hashCode() {
        int[] combined = new int[argRegs.length + retRegs.length];
        System.arraycopy(argRegs, 0, combined, 0, argRegs.length);
        System.arraycopy(retRegs, 0, combined, argRegs.length, retRegs.length);
        return Arrays.hashCode(combined);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForkJoinCPEntry && Arrays.equals(argRegs, ((ForkJoinCPEntry) obj).argRegs)
                && Arrays.equals(retRegs, ((ForkJoinCPEntry) obj).retRegs);
    }

    public CallableUnitInfo getParentCallableUnitInfo() {
        return parentCallableUnitInfo;
    }

    public void setParentCallableUnitInfo(CallableUnitInfo parentCallableUnitInfo) {
        this.parentCallableUnitInfo = parentCallableUnitInfo;
    }

}
