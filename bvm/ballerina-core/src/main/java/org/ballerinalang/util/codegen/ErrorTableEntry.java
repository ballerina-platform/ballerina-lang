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

import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes an error handing section defined using try block in a Ballerina program.
 */
public class ErrorTableEntry {

    private int ipFrom;
    private int ipTo;
    public int ipTarget;
    public int regIndex;

    ErrorTableEntry(int ipFrom, int ipTo, int ipTarget, int regIndex) {
        this.ipFrom = ipFrom;
        this.ipTo = ipTo;
        this.ipTarget = ipTarget;
        this.regIndex = regIndex;
    }

    private boolean matchRange(int currentIP) {
        return currentIP >= ipFrom && currentIP <= ipTo;
    }

    @Override
    public String toString() {
        return "\t\t" + ipFrom + "\t\t" + ipTo + "\t" + ipTarget + "\t\t" + regIndex;
    }

    public static ErrorTableEntry getMatch(PackageInfo packageInfo, int currentIP) {
        ErrorTableAttributeInfo errorTable =
                (ErrorTableAttributeInfo) packageInfo.getAttributeInfo(AttributeInfo.Kind.ERROR_TABLE);
        List<ErrorTableEntry> errorTableEntries = errorTable != null ?
                errorTable.getErrorTableEntriesList() : new ArrayList<>();
        // error tables entries are added in FILO order, so first matched, should have the smallest range.
        return errorTableEntries.stream().filter(errorTableEntry -> errorTableEntry.matchRange(currentIP)).findFirst()
                .orElse(null);
    }
}
