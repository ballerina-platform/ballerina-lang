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
package org.ballerinalang.util.codegen;

import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private TypeDefInfo error;
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
    }

    public TypeDefInfo getError() {
        return error;
    }

    public void setError(TypeDefInfo error) {
        this.error = error;
    }

    public boolean matchRange(int currentIP) {
        if (currentIP >= ipFrom && currentIP <= ipTo) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "\t\t" + ipFrom + "\t\t" + ipTo + "\t" + ipTarget + "\t\t" + (error != null ? error.getName() : "any");

    }

    private static class MatchedEntry {
        protected ErrorTableEntry errorTableEntry;
        // 0 - exact, 1 - equivalent, 2 - any.
        int status;
        int ipSize;
    }

    public static ErrorTableEntry getMatch(PackageInfo packageInfo, int currentIP, final BMap<String, BValue> error) {
        ErrorTableAttributeInfo errorTable =
                (ErrorTableAttributeInfo) packageInfo.getAttributeInfo(AttributeInfo.Kind.ERROR_TABLE);
        List<ErrorTableEntry> errorTableEntries = errorTable != null ?
                errorTable.getErrorTableEntriesList() : new ArrayList<>();
        List<MatchedEntry> rangeMatched = new ArrayList<>();
        errorTableEntries.stream().filter(errorTableEntry -> errorTableEntry.matchRange(currentIP)).forEach
                (errorTableEntry -> {
                    MatchedEntry entry = new MatchedEntry();
                    entry.errorTableEntry = errorTableEntry;
                    entry.ipSize = errorTableEntry.ipTo - errorTableEntry.ipFrom;
                    if (errorTableEntry.getErrorStructCPIndex() == -1) {
                        // match any.
                        entry.status = 2;
                        rangeMatched.add(entry);
                    } else if (errorTableEntry.getError().typeInfo.getType().equals(error.getType())) {
                        // exact match.
                        entry.status = 0;
                        rangeMatched.add(entry);
                    } else if (CPU.checkStructEquivalency((BStructureType) error.getType(),
                            ((StructureTypeInfo) errorTableEntry.getError().typeInfo).getType())) {
                        entry.status = 1;
                        rangeMatched.add(entry);
                    }
                });
        if (rangeMatched.size() == 0) {
            return null;
        }
        if (rangeMatched.size() == 1) {
            return rangeMatched.get(0).errorTableEntry;
        }
        MatchedEntry[] matchedEntries = rangeMatched.stream().sorted(Comparator.comparingInt(o -> o.ipSize)).toArray
                (MatchedEntry[]::new);
        int currentSize = 0;
        ErrorTableEntry errorTableEntry = null;
        for (int i = 0; i < matchedEntries.length; i++) {
            MatchedEntry entry = matchedEntries[i];
            if (currentSize < entry.ipSize) {
                if (errorTableEntry == null) {
                    // Expand scope.
                    currentSize = entry.ipSize;
                } else {
                    // Return best match.
                    return errorTableEntry;
                }
            }
            if (entry.status == 0) {
                // Best case.
                return entry.errorTableEntry;
            } else {
                if (errorTableEntry == null) {
                    errorTableEntry = entry.errorTableEntry;
                } else {
                    if (errorTableEntry.priority > entry.errorTableEntry.priority) {
                        // found a high order entry
                        errorTableEntry = entry.errorTableEntry;
                    }
                }
            }
        }
        return errorTableEntry;
    }
}
