/*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.programfile;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code ForkjoinInfo} represents a fork join in Ballerina program file.
 *
 * @since 0.90
 */
@Deprecated
public class ForkjoinInfo {

    private int index;
    private int indexCPIndex;

    // Registers which contains worker incoming arguments
    private int[] argRegs;

    protected Map<String, WorkerInfo> workerInfoMap = new HashMap<>();

    private boolean isTimeoutAvailable;

    private int joinTypeCPIndex;
    private String joinType;

    private int workerCount;

    private int[] joinWrkrNameIndexes;
    private String[] joinWorkerNames;

    public ForkjoinInfo(int[] argRegs) {
        this.argRegs = argRegs;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndexCPIndex() {
        return indexCPIndex;
    }

    public void setIndexCPIndex(int indexCPIndex) {
        this.indexCPIndex = indexCPIndex;
    }

    public int[] getArgRegs() {
        return argRegs;
    }

    public Map<String, WorkerInfo> getWorkerInfoMap() {
        return workerInfoMap;
    }

    public WorkerInfo[] getWorkerInfos() {
        return workerInfoMap.values().toArray(new WorkerInfo[0]);
    }

    public boolean isTimeoutAvailable() {
        return isTimeoutAvailable;
    }

    public void setTimeoutAvailable(boolean timeoutAvailable) {
        isTimeoutAvailable = timeoutAvailable;
    }

    public int getJoinTypeCPIndex() {
        return joinTypeCPIndex;
    }

    public void setJoinTypeCPIndex(int joinTypeCPIndex) {
        this.joinTypeCPIndex = joinTypeCPIndex;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public int[] getJoinWrkrNameIndexes() {
        return joinWrkrNameIndexes;
    }

    public void setJoinWrkrNameIndexes(int[] joinWrkrNameIndexes) {
        this.joinWrkrNameIndexes = joinWrkrNameIndexes;
    }

    public String[] getJoinWorkerNames() {
        return joinWorkerNames;
    }

    public void setJoinWorkerNames(String[] joinWorkerNames) {
        this.joinWorkerNames = joinWorkerNames;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public WorkerInfo getWorkerInfo(String workerName) {
        return workerInfoMap.get(workerName);
    }

    public void addWorkerInfo(String workerName, WorkerInfo workerInfo) {
        workerInfoMap.put(workerName, workerInfo);
    }

}
