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

import java.util.List;

/**
 * This for identify the fields that are same names with different data type in given json schema and bVarsymbol.
 */
public class OneOfTypeValidation extends ValidationError {
    String fieldName;
    Constants.Type type;
    List<ValidationError> blockErrors;

    public OneOfTypeValidation() {
        fieldName = null;
        type = null;
        blockErrors = null;
    }
    public OneOfTypeValidation(String fieldName, Constants.Type type, List<ValidationError> validationErrors) {
        this.fieldName = fieldName;
        this.type = type;
        this.blockErrors = validationErrors;
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
    public void setBlockErrors(List<ValidationError> validationErrors) {
        this.blockErrors = validationErrors;
    }
    public List<ValidationError> getBlockErrors() {
        return blockErrors;
    }
}
