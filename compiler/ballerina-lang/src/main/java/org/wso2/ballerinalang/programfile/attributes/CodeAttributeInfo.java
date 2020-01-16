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
package org.wso2.ballerinalang.programfile.attributes;

/**
 * {@code CodeAttributeInfo} contains bytecode instructions of an callable unit in Ballerina .
 *
 * @since 0.87
 */
@Deprecated
public class CodeAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    public int attributeNameIndex;

    public int maxLongLocalVars;
    public int maxDoubleLocalVars;
    public int maxStringLocalVars;
    public int maxIntLocalVars;
    public int maxRefLocalVars;

    // 4 bytes per register

    public int maxLongRegs;
    public int maxDoubleRegs;
    public int maxStringRegs;
    public int maxIntRegs;
    public int maxRefRegs;

    // Base code address in the instruction array
    public int codeAddrs = -1;

    @Override
    public Kind getKind() {
        return Kind.CODE_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }

}
