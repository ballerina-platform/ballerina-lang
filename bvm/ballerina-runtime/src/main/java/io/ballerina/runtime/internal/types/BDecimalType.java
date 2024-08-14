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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.DecimalType;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static io.ballerina.runtime.api.PredefinedTypes.EMPTY_MODULE;

/**
 * {@code BDecimalType} represents decimal type in Ballerina.
 * This is a 128-bit decimal floating-point number according to the standard IEEE 754-2008 specifications.
 *
 * @since 0.995.0
 */
public final class BDecimalType extends BSemTypeWrapper<BDecimalType.BDecimalTypeImpl> implements DecimalType {

    private static final BDecimalTypeImpl DEFAULT_B_TYPE =
            new BDecimalTypeImpl(TypeConstants.DECIMAL_TNAME, EMPTY_MODULE);

    /**
     * Create a {@code BDecimalType} which represents the decimal type.
     *
     * @param typeName string name of the type
     */
    public BDecimalType(String typeName, Module pkg) {
        this(() -> new BDecimalTypeImpl(typeName, pkg), typeName, Builder.decimalType());
    }

    public static BDecimalType singletonType(BigDecimal value) {
        return new BDecimalType(() -> {
            try {
                return (BDecimalTypeImpl) DEFAULT_B_TYPE.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }, TypeConstants.DECIMAL_TNAME, Builder.decimalConst(value));
    }

    private BDecimalType(Supplier<BDecimalTypeImpl> bType, String typeName, SemType semType) {
        super(bType, typeName, semType);
    }

    protected static final class BDecimalTypeImpl extends BType implements DecimalType, Cloneable {

        private BDecimalTypeImpl(String typeName, Module pkg) {
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

        @Override
        protected Object clone() throws CloneNotSupportedException {
            BType bType = (BType) super.clone();
            bType.setCachedImpliedType(null);
            bType.setCachedReferredType(null);
            return bType;
        }
    }
}
