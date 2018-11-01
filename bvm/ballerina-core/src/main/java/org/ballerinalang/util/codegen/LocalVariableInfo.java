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

/**
 * {@code LocalVariableAttributeInfo} contains common metadata of a given local variable.
 *
 * @since 0.88
 */
public class LocalVariableInfo {

    private int varNameCPIndex;
    private int varIndex;

    private String varName;

    private int varTypeSigCPIndex;
    private BType varType;

    private int scopeStartLineNumber;
    private int scopeEndLineNumber;

    int[] attachmentIndexes = new int[0];

    public LocalVariableInfo(String varName, int varNameCPIndex, int varIndex, int varTypeSigCPIndex, BType varType,
                             int scopeStartLineNumber, int scopeEndLineNumber) {
        this.varName = varName;
        this.varNameCPIndex = varNameCPIndex;
        this.varIndex = varIndex;
        this.varTypeSigCPIndex = varTypeSigCPIndex;
        this.varType = varType;
        this.scopeStartLineNumber = scopeStartLineNumber;
        this.scopeEndLineNumber = scopeEndLineNumber;
    }

    public int[] getAttachmentIndexes() {
        return attachmentIndexes;
    }

    public void setAttachmentIndexes(int[] attachmentIndexes) {
        this.attachmentIndexes = attachmentIndexes;
    }

    public int getVariableNameCPIndex() {
        return varNameCPIndex;
    }

    public int getVariableIndex() {
        return varIndex;
    }

    public int getVarTypeSigCPIndex() {
        return varTypeSigCPIndex;
    }

    public BType getVariableType() {
        return varType;
    }

    public String getVariableName() {
        return varName;
    }

    public int getScopeStartLineNumber() {
        return scopeStartLineNumber;
    }

    public int getScopeEndLineNumber() {
        return scopeEndLineNumber;
    }
}
