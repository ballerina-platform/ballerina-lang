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

import org.ballerinalang.util.codegen.cpentries.PackageCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code FunctionInfo} contains metadata of a Ballerina function entry in the program file.
 *
 * @since 0.87
 */
public class FunctionInfo {

    private String pkgName;
    private String funcName;
    private boolean isNative;

    private PackageCPEntry packageCPEntry;
    private UTF8CPEntry nameCPEntry;
    private UTF8CPEntry descCPEntry;

    // Index to the PackageCPEntry
    private int pkgCPEntryIndex;

    // Index to the FunctionCPEntry
    private int funcCPEntryIndex;

    // Index to the UTF8CPEntry which contains the function descriptor
    private int descIndex;

    private String[] paramTypeSigs;
    private String[] retParamTypeSigs;

    CodeAttributeInfo codeAttributeInfo;

    List<AttributeInfo> attributeInfoList = new ArrayList<>();

    public FunctionInfo(int pkgCPEntryIndex, int funcCPEntryIndex, int descIndex) {
        this.pkgCPEntryIndex = pkgCPEntryIndex;
        this.funcCPEntryIndex = funcCPEntryIndex;
        this.descIndex = descIndex;

        codeAttributeInfo = new CodeAttributeInfo();
        attributeInfoList.add(codeAttributeInfo);
    }

    public int getPackageNameIndex() {
        return pkgCPEntryIndex;
    }

    public int getFunctionCPEntryIndex() {
        return funcCPEntryIndex;
    }

    public int getDescriptorIndex() {
        return descIndex;
    }

    public CodeAttributeInfo getCodeAttributeInfo() {
        return codeAttributeInfo;
    }

    public List<AttributeInfo> getAttributeInfoList() {
        return attributeInfoList;
    }

    public String[] getParamTypeSigs() {
        return paramTypeSigs;
    }

    public void setParamTypeSigs(String[] paramTypeSigs) {
        this.paramTypeSigs = paramTypeSigs;
    }

    public String[] getRetParamTypeSigs() {
        return retParamTypeSigs;
    }

    public void setRetParamTypeSigs(String[] retParamTypeSigs) {
        this.retParamTypeSigs = retParamTypeSigs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgCPEntryIndex, funcCPEntryIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FunctionInfo && pkgCPEntryIndex == (((FunctionInfo) obj).pkgCPEntryIndex)
                && funcCPEntryIndex == (((FunctionInfo) obj).funcCPEntryIndex);
    }
}
