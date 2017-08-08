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
package org.ballerinalang.util.codegen.attributes;

import org.ballerinalang.util.codegen.LocalVariableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code LocalVariableAttributeInfo} contains common metadata of parameters
 * of a Ballerina function/resource/action in the program file.
 *
 * @since 0.88
 */
public class LocalVariableAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    protected List<LocalVariableInfo> localVariables = new ArrayList<>();

    public LocalVariableAttributeInfo(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public List<LocalVariableInfo> getLocalVariables() {
        return localVariables;
    }

    public LocalVariableInfo[] getLocalVariableInfoEntries() {
        return localVariables.toArray(new LocalVariableInfo[0]);
    }

    public void setLocalVariables(List<LocalVariableInfo> localVariables) {
        this.localVariables = localVariables;
    }

    public void addLocalVarInfo(LocalVariableInfo localVariableInfo) {
        this.localVariables.add(localVariableInfo);
    }

    public LocalVariableInfo getLocalVariableDetails(int index) {
        return this.localVariables.get(index);
    }

    @Override
    public Kind getKind() {
        return Kind.LOCAL_VARIABLES_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}
