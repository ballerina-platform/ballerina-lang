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
package org.ballerinalang.util.codegen.cpentries;

import org.ballerinalang.util.codegen.TransformerInfo;

import java.util.Objects;

/**
 * {@code TransformerRefCPEntry} represents a Ballerina transformer in the constant pool.
 *
 * @since 0.94.2
 */
public class TransformerRefCPEntry implements ConstantPoolEntry {

    // Index to a valid Package entry in the constant pool
    private int packageCPIndex;
    private String packagePath;

    // Index to a valid name index in the constant pool
    private int nameCPIndex;
    private String transformerName;

    private TransformerInfo transformerInfo;

    public TransformerRefCPEntry(int packageCPIndex, String packagePath, int nameCPIndex, String transformerName) {
        this.packageCPIndex = packageCPIndex;
        this.packagePath = packagePath;
        this.nameCPIndex = nameCPIndex;
        this.transformerName = transformerName;
    }

    public int getPackageCPIndex() {
        return packageCPIndex;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
    }

    public String getTransformerName() {
        return transformerName;
    }

    public TransformerInfo getTransformerInfo() {
        return transformerInfo;
    }

    public void setTransformerInfo(TransformerInfo functionInfo) {
        this.transformerInfo = functionInfo;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_TRANSFORMER_REF;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TransformerRefCPEntry && packageCPIndex == (((TransformerRefCPEntry) obj).packageCPIndex)
                && nameCPIndex == ((TransformerRefCPEntry) obj).nameCPIndex;
    }

    @Override
    public String toString() {
        return "TransformerRefCPEntry{" +
                "packageCPIndex=" + packageCPIndex +
                ", packagePath='" + packagePath + '\'' +
                ", nameCPIndex=" + nameCPIndex +
                ", transformerName='" + transformerName + '\'' +
                ", transformerInfo=" + transformerInfo +
                '}';
    }
}
