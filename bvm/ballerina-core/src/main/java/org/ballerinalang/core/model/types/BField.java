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
package org.ballerinalang.core.model.types;

/**
 * {@code BField} represents a field in user defined type in Ballerina.
 *
 * @since 0.971.0
 */
public class BField {

    public BType fieldType;
    public String fieldName;
    public int flags;

    public BField(BType fieldType, String fieldName, int flags) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.flags = flags;
    }

    public BType getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }
}

