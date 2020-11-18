/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.commons;

import io.ballerina.runtime.api.types.Type;

/**
 * Type vector of size two, to hold the source value and the target type.
 *
 * @since 0.995.0
 */
public class TypeValuePair {
    Object sourceValue;
    Type targetType;

    public TypeValuePair(Object sourceValue, Type targetType) {
        this.sourceValue = sourceValue;
        this.targetType = targetType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeValuePair)) {
            return false;
        }
        TypeValuePair other = (TypeValuePair) obj;
        return this.sourceValue.equals(other.sourceValue) && this.targetType.equals(other.targetType);
    }
}
