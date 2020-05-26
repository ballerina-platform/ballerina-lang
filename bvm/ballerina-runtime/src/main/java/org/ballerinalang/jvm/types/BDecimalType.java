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

package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.DecimalValue;

import java.math.BigDecimal;

/**
 * {@code BDecimalType} represents decimal type in Ballerina.
 * This is a 128-bit decimal floating-point number according to the standard IEEE 754-2008 specifications.
 *
 * @since 0.995.0
 */
public class BDecimalType extends BType {

    /**
     * Create a {@code BDecimalType} which represents the decimal type.
     *
     * @param typeName string name of the type
     */
    BDecimalType(String typeName, BPackage pkg) {
        super(typeName, pkg, DecimalValue.class);
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
}
