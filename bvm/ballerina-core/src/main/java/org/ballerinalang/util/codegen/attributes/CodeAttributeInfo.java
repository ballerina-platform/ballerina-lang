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

/**
 * {@code CodeAttributeInfo} contains bytecode instructions of an callable unit in Ballerina .
 *
 * @since 0.87
 */
public class CodeAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    private int maxLongLocalVars;
    private int maxDoubleLocalVars;
    private int maxStringLocalVars;
    private int maxIntLocalVars;
    private int maxByteLocalVars;
    private int maxBValueLocalVars;

    // 4 bytes per register

    public int maxLongRegs;
    public int maxDoubleRegs;
    public int maxStringRegs;
    public int maxIntRegs;
    public int maxByteRegs;
    public int maxBValueRegs;

    // Base code address in the instruction array
    private int codeAddrs = -1;

    public void setAttributeNameIndex(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public int getMaxIntLocalVars() {
        return maxIntLocalVars;
    }

    public void setMaxIntLocalVars(int maxIntLocalVars) {
        this.maxIntLocalVars = maxIntLocalVars;
    }

    public int getMaxLongLocalVars() {
        return maxLongLocalVars;
    }

    public void setMaxLongLocalVars(int maxLongLocalVars) {
        this.maxLongLocalVars = maxLongLocalVars;
    }

    public int getMaxDoubleLocalVars() {
        return maxDoubleLocalVars;
    }

    public void setMaxDoubleLocalVars(int maxDoubleLocalVars) {
        this.maxDoubleLocalVars = maxDoubleLocalVars;
    }

    public int getMaxStringLocalVars() {
        return maxStringLocalVars;
    }

    public void setMaxStringLocalVars(int maxStringLocalVars) {
        this.maxStringLocalVars = maxStringLocalVars;
    }

    public int getMaxRefLocalVars() {
        return maxBValueLocalVars;
    }

    public void setMaxRefLocalVars(int maxBValueLocalVars) {
        this.maxBValueLocalVars = maxBValueLocalVars;
    }

    public int getMaxIntRegs() {
        return maxIntRegs;
    }

    public void setMaxIntRegs(int maxIntRegs) {
        this.maxIntRegs = maxIntRegs;
    }

    public int getMaxLongRegs() {
        return maxLongRegs;
    }

    public void setMaxLongRegs(int maxLongRegs) {
        this.maxLongRegs = maxLongRegs;
    }

    public int getMaxDoubleRegs() {
        return maxDoubleRegs;
    }

    public void setMaxDoubleRegs(int maxDoubleRegs) {
        this.maxDoubleRegs = maxDoubleRegs;
    }

    public int getMaxStringRegs() {
        return maxStringRegs;
    }

    public void setMaxStringRegs(int maxStringRegs) {
        this.maxStringRegs = maxStringRegs;
    }

    public int getMaxRefRegs() {
        return maxBValueRegs;
    }

    public void setMaxRefRegs(int maxBValueRegs) {
        this.maxBValueRegs = maxBValueRegs;
    }

    public int getCodeAddrs() {
        return codeAddrs;
    }

    public void setCodeAddrs(int codeAddrs) {
        this.codeAddrs = codeAddrs;
    }

    public int getMaxByteLocalVars() {
        return maxByteLocalVars;
    }

    public void setMaxByteLocalVars(int maxByteLocalVars) {
        this.maxByteLocalVars = maxByteLocalVars;
    }

    public int getMaxByteRegs() {
        return maxByteRegs;
    }

    public void setMaxByteRegs(int maxByteRegs) {
        this.maxByteRegs = maxByteRegs;
    }

    @Override
    public Kind getKind() {
        return Kind.CODE_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }

}
