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

package org.wso2.ballerinalang.programfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represent serializable unit of defined type Definition item.
 */
public class TypeDefinitionInfo extends StructureTypeInfo {

    public List<ValueSpaceItemInfo> valueSpaceItemInfos = new ArrayList<>();

    public TypeDefinitionInfo(int pkgNameCPIndex, int nameCPIndex, int flags) {
        super(pkgNameCPIndex, nameCPIndex, flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgNameCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeDefinitionInfo && pkgNameCPIndex == (((TypeDefinitionInfo) obj).pkgNameCPIndex)
                && nameCPIndex == (((TypeDefinitionInfo) obj).nameCPIndex);
    }
}

