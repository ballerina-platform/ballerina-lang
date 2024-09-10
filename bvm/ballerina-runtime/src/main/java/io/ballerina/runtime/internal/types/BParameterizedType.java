/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ParameterizedType;
import io.ballerina.runtime.api.types.Type;

/**
 * {@code ParameterizedType} represents the parameterized type in dependently-typed functions.
 *
 * @since 2.0.0
 */
public class BParameterizedType extends BType implements ParameterizedType {

    private Type paramValueType;
    private int paramIndex;

    public BParameterizedType(Type paramValueType, int paramIndex) {
        super(null, null, null);
        this.paramValueType = paramValueType;
        this.paramIndex = paramIndex;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return paramValueType.getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.PARAMETERIZED_TYPE_TAG;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BParameterizedType otherParameterizedType)) {
            return false;
        }

        return paramIndex == otherParameterizedType.paramIndex &&
                paramValueType.equals(otherParameterizedType.getParamValueType());
    }

    @Override
    public boolean isReadOnly() {
        return paramValueType.isReadOnly();
    }

    @Override
    public Type getParamValueType() {
        return this.paramValueType;
    }

    @Override
    public int getParamIndex() {
        return this.paramIndex;
    }
}
