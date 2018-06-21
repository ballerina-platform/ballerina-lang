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

import org.ballerinalang.model.types.BRecordType;

/**
 * {@code RecordTypeInfo} contains metadata of a Ballerina record entry in the program file.
 *
 * @since 0.971.0
 */
public class RecordTypeInfo extends StructureTypeInfo {

    private BRecordType recordType;

    private int restFieldSigCPIndex;
    private String restFieldTypeSignature;

    public RecordTypeInfo() {
    }

    public BRecordType getType() {
        return recordType;
    }

    public void setType(BRecordType structType) {
        this.recordType = structType;
    }

    public int getRestFieldSignatureCPIndex() {
        return restFieldSigCPIndex;
    }

    public void setRestFieldSignatureCPIndex(int restFieldSigCPIndex) {
        this.restFieldSigCPIndex = restFieldSigCPIndex;
    }

    public String getRestFieldTypeSignature() {
        return restFieldTypeSignature;
    }

    public void setRestFieldTypeSignature(String restFieldTypeSignature) {
        this.restFieldTypeSignature = restFieldTypeSignature;
    }
}
