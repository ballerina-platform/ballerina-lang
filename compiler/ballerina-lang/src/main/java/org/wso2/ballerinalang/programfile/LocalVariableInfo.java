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
package org.wso2.ballerinalang.programfile;

/**
 * {@code LocalVariableAttributeInfo} contains common metadata of a given local variable.
 *
 * @since 0.88
 */
@Deprecated
public class LocalVariableInfo {

    public int varNameCPIndex;
    public int varTypeSigCPIndex;
    public int varIndex;

    public int[] attachmentIndexes = new int[0];

    public int scopeStartLineNumber;
    public int scopeEndLineNumber;

    public boolean isIdentifierLiteral;

    public LocalVariableInfo(int varNameCPIndex, int varTypeSigCPIndex, int varIndex) {
        this.varNameCPIndex = varNameCPIndex;
        this.varTypeSigCPIndex = varTypeSigCPIndex;
        this.varIndex = varIndex;
    }
}
