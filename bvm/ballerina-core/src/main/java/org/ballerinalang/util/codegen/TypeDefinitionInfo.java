/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.ballerinalang.model.types.BFiniteType;

import java.util.Objects;

/**
 * Represent serializable unit of defined type Definition item.
 */
public class TypeDefinitionInfo extends StructureTypeInfo {
    private BFiniteType finiteType;


    public TypeDefinitionInfo(int pkgPathCPIndex, String packagePath,
                              int nameCPIndex, String name, int flags) {
        super(pkgPathCPIndex, packagePath, nameCPIndex, name, flags);
    }

    public BFiniteType getType() {
        return finiteType;
    }

    public void setType(BFiniteType finiteType) {
        this.finiteType = finiteType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgPathCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeDefinitionInfo && pkgPathCPIndex == (((TypeDefinitionInfo) obj).pkgPathCPIndex)
                && nameCPIndex == (((TypeDefinitionInfo) obj).nameCPIndex);
    }
}
