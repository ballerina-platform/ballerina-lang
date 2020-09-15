/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.ballerinalang.openapi.validator.error;

import org.ballerinalang.openapi.validator.Constants;

/**
 * This for identify the missing field in json schema against to given bVarSymbol.
 */
public class MissingFieldInJsonSchema extends ValidationError {
    String fieldName;
    Constants.Type type;
    String recordName;

    public MissingFieldInJsonSchema(String fieldName, Constants.Type type) {
        this.fieldName = fieldName;
        this.type = type;
        this.recordName = null;
    }
    public MissingFieldInJsonSchema(String fieldName, Constants.Type type, String recordName) {
        this.fieldName = fieldName;
        this.type = type;
        this.recordName = recordName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public void setType(Constants.Type type) {
        this.type = type;
    }
    public String getFieldName() {
        return fieldName;
    }
    public  Constants.Type getType() {
        return type;
    }
    public String getRecordName() {
        return this.recordName;
    }

}
