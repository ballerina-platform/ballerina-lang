/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.DecimalType;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * {@code BDecimalType} represents decimal type in Ballerina.
 * This is a 128-bit decimal floating-point number according to the standard IEEE 754-2008 specifications.
 *
 * @since 0.995.0
 */
public class BDecimalType extends BType implements DecimalType {

    /**
     * Create a {@code BDecimalType} which represents the decimal type.
     *
     * @param typeName string name of the type
     */
    private final SemType semType;
    public BDecimalType(String typeName, Module pkg) {
        super(typeName, pkg, DecimalValue.class);
        this.semType = null;
    }

    public BDecimalType(String typeName, Module pkg, SemType semType) {
        super(typeName, pkg, DecimalValue.class);
        this.semType = semType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Object> V getZeroValue() {
        return (V) new DecimalValue(BigDecimal.ZERO);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Object> V getEmptyValue() {
        return (V) new DecimalValue(BigDecimal.ZERO);
    }

    @Override
    public int getTag() {
        return TypeTags.DECIMAL_TAG;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public SemType getSemTypeComponent() {
        return Objects.requireNonNullElse(this.semType, PredefinedType.DECIMAL);
    }

    @Override
    public BType getBTypeComponent() {
        return (BType) PredefinedTypes.TYPE_NEVER;
    }
}
