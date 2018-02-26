/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;

/**
 * Describes an error handing section defined using try block in a Ballerina program.
 */
public class ErrorTableEntry {

    protected int ipFrom;
    protected int ipTo;
    protected int ipTarget;
    // Defined order in try catch.
    protected int priority;
    protected int errorStructCPIndex = -100;

    // Cache values.
    private StructInfo error;
    private PackageInfo packageInfo;

    public ErrorTableEntry(int ipFrom, int ipTo, int ipTarget, int priority, int errorStructCPIndex) {
        this.ipFrom = ipFrom;
        this.ipTo = ipTo;
        this.ipTarget = ipTarget;
        this.priority = priority;
        this.errorStructCPIndex = errorStructCPIndex;
    }

    public int getIpFrom() {
        return ipFrom;
    }

    public int getIpTo() {
        return ipTo;
    }

    public int getIpTarget() {
        return ipTarget;
    }


    public int getPriority() {
        return priority;
    }

    /**
     * returns ErrorStructCPEntryIndex.
     *
     * @return ErrorStructCPEntryIndex, if unhandled error returns -1.
     */
    public int getErrorStructCPIndex() {
        return errorStructCPIndex;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        // Load Cache values.
        if (errorStructCPIndex < 0) {
            return;
        }
        StructureRefCPEntry structureRefCPEntry = (StructureRefCPEntry)
                packageInfo.getCPEntry(errorStructCPIndex);
//        this.error = (StructInfo) structureRefCPEntry.getStructureTypeInfo();
    }

    public boolean matchRange(int currentIP) {
        if (currentIP >= ipFrom && currentIP <= ipTo) {
            return true;
        }
        return false;
    }

    private static class MatchedEntry {
        protected ErrorTableEntry errorTableEntry;
        // 0 - exact, 1 - equivalent, 2 - any.
        int status;
        int ipSize;
    }
}
