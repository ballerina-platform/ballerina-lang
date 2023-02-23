/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.RecordField;
import io.ballerina.runtime.api.types.Type;

/**
 * {@code BRecordField} represents a field in user defined record type in Ballerina.
 *
 * @since 2201.6.0
 */
public class BRecordField extends BField implements RecordField {

    private final String defaultFunctionName;

    public BRecordField(Type fieldType, String fieldName, long flags, String defaultFunctionName) {
        super(fieldType, fieldName, flags);
        this.defaultFunctionName = defaultFunctionName;
    }

    @Override
    public String getDefaultFunctionName() {
        return defaultFunctionName;
    }
}
