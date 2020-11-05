/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;

/**
 * {@code BField} represents a field in user defined type in Ballerina.
 *
 * @since 0.995.0
 */
public class BField implements Field {

    public Type type;
    public String name;
    public int flags;

    public BField(Type fieldType, String fieldName, int flags) {
        this.type = fieldType;
        this.name = fieldName;
        this.flags = flags;
    }

    public Type getFieldType() {
        return type;
    }

    public String getFieldName() {
        return name;
    }

    public int getFlags() {
        return flags;
    }
}
