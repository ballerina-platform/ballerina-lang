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

import org.ballerinalang.model.types.BStructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code StructInfo} contains metadata of a Ballerina struct entry in the program file.
 *
 * @since 0.87
 */
public class StructInfo extends StructureTypeInfo {

    private BStructType structType;
    private List<StructFieldInfo> fieldInfoEntries = new ArrayList<>();
    public Map<String, AttachedFunctionInfo> funcInfoEntries = new HashMap<>();
    public AttachedFunctionInfo initializer;
    public AttachedFunctionInfo defaultsValuesInitFunc;

    public StructInfo(int pkgPathCPIndex, String packagePath, int nameCPIndex, String name, int flags) {
        super(pkgPathCPIndex, packagePath, nameCPIndex, name, flags);
    }

    public BStructType getType() {
        return structType;
    }

    public void setType(BStructType structType) {
        this.structType = structType;
    }

    public void addFieldInfo(StructFieldInfo fieldInfo) {
        fieldInfoEntries.add(fieldInfo);
    }

    public StructFieldInfo[] getFieldInfoEntries() {
        return fieldInfoEntries.toArray(new StructFieldInfo[0]);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgPathCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StructInfo && pkgPathCPIndex == (((StructInfo) obj).pkgPathCPIndex)
                && nameCPIndex == (((StructInfo) obj).nameCPIndex);
    }
}
