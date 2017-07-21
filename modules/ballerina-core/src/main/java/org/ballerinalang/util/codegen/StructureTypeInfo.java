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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

/**
 * @since 0.87
 */
public class StructureTypeInfo {

    protected int pkgPathCPIndex;

    protected int nameCPIndex;

    protected int[] fieldCount;

    // TODO Remove. Temporary field
    protected BType[] fieldTypes;

    protected BType structureType;
    // Cache Values.
    private String name;
    private PackageInfo packageInfo;

    public StructureTypeInfo(int pkgPathCPIndex, int nameCPIndex) {
        this.pkgPathCPIndex = pkgPathCPIndex;
        this.nameCPIndex = nameCPIndex;
    }

    public int[] getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int[] fieldCount) {
        this.fieldCount = fieldCount;
    }

    public BType[] getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(BType[] fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    public BType getType() {
        return structureType;
    }

    public void setType(BType type) {
        this.structureType = type;
    }

    public String getName() {
        return name;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    protected void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        // Update Cache values.
        name = ((UTF8CPEntry) packageInfo.getCPEntry(nameCPIndex)).getValue();
    }
}
