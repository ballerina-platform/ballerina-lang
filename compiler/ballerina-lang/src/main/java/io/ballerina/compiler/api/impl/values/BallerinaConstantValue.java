/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.values;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.values.ConstantValue;

import java.util.Objects;

/**
 *  Value of the constant.
 *
 *  @since 2201.3.0
 */
public class BallerinaConstantValue implements ConstantValue {
    private final Object value;
    private final TypeSymbol valueType;

    public BallerinaConstantValue(Object value, TypeSymbol valueType) {
        this.value = value;
        this.valueType = valueType;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public TypeSymbol valueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof BallerinaConstantValue that) {

            if (this.valueType != that.valueType) {
                return false;
            }

            return this.value.equals(that.value);
        }

        return this.value.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.valueType);
    }
}
