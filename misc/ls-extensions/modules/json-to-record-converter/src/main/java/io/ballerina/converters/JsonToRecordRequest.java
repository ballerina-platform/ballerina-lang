/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.converters;

/**
 * Request format for JsonToBalRecord endpoint.
 *
 * @since 2.0.0
 */
public class JsonToRecordRequest {

    private String jsonString;
    private String recordName;
    private boolean isRecordTypeDesc;
    private boolean isClosed;
    private boolean forceFormatRecordFields;
    private String filePathUri;

    public JsonToRecordRequest(String jsonString, String recordName, boolean isRecordTypeDesc, boolean isClosed,
                               boolean forceFormatRecordFields, String filePathUri) {
        this.jsonString = jsonString;
        this.recordName = recordName;
        this.isRecordTypeDesc = isRecordTypeDesc;
        this.isClosed = isClosed;
        this.forceFormatRecordFields = forceFormatRecordFields;
        this.filePathUri = filePathUri;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public boolean getIsRecordTypeDesc() {
        return isRecordTypeDesc;
    }

    public void setIsRecordTypeDesc(boolean isRecordTypeDesc) {
        this.isRecordTypeDesc = isRecordTypeDesc;
    }

    public boolean getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    public boolean getForceFormatRecordFields() {
        return forceFormatRecordFields;
    }

    public void setForceFormatRecordFields(boolean forceFormatRecordFields) {
        this.forceFormatRecordFields = forceFormatRecordFields;
    }

    public String getFilePathUri() {
        return filePathUri;
    }

    public void setFilePathUri(String filePathUri) {
        this.filePathUri = filePathUri;
    }
}
