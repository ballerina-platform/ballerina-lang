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
package org.ballerinalang.util.codegen.cpentries;

import org.ballerinalang.util.codegen.PackageInfo;

/**
 * {@code PackageRefCPEntry} represents a Ballerina package name in the constant pool.
 *
 * @since 0.87
 */
public class PackageRefCPEntry implements ConstantPoolEntry {

    // Index of UTF8 CP entry contains the package path
    private int nameCPIndex;
    private String packageName;
    private String packageVersion;

    // Index of CP entry which contains package version
    private int versionCPIndex;

    private PackageInfo packageInfo;

    public PackageRefCPEntry(int nameCPIndex, String packageName, int versionCPIndex, String packageVersion) {
        this.nameCPIndex = nameCPIndex;
        this.packageName = packageName;
        this.versionCPIndex = versionCPIndex;
        this.packageVersion = packageVersion;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
    }

    public String getPackageName() {
        return packageName;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_PACKAGE;
    }

    @Override
    public int hashCode() {
        return nameCPIndex;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PackageRefCPEntry && nameCPIndex == ((PackageRefCPEntry) obj).nameCPIndex;
    }
}
