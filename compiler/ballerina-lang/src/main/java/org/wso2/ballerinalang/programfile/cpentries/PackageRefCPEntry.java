/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile.cpentries;

/**
 * {@code PackageRefCPEntry} represents a Ballerina package name in the constant pool.
 *
 * @since 0.87
 */
@Deprecated
public class PackageRefCPEntry implements ConstantPoolEntry {

    // Index of UTF8 CP entry contains the package path
    public int nameCPIndex;

    // Index of CP entry which contains package version
    public int versionCPIndex;

    public PackageRefCPEntry(int nameCPIndex, int versionCPIndex) {
        this.nameCPIndex = nameCPIndex;
        this.versionCPIndex = versionCPIndex;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_PACKAGE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageRefCPEntry that = (PackageRefCPEntry) o;
        return nameCPIndex == that.nameCPIndex && versionCPIndex == that.versionCPIndex;
    }

    @Override
    public int hashCode() {
        int result = nameCPIndex;
        result = 31 * result + versionCPIndex;
        return result;
    }
}
