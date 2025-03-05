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
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.DecimalType;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static io.ballerina.runtime.api.types.PredefinedTypes.EMPTY_MODULE;

/**
 * {@code BDecimalType} represents decimal type in Ballerina.
 * This is a 128-bit decimal floating-point number according to the standard IEEE 754-2008 specifications.
 *
 * @since 0.995.0
 */
public final class BDecimalType extends BSemTypeWrapper<BDecimalType.BDecimalTypeImpl> implements DecimalType {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getDecimalType();

    private static final BDecimalTypeImpl DEFAULT_B_TYPE =
            new BDecimalTypeImpl(TypeConstants.DECIMAL_TNAME, EMPTY_MODULE);
    private final SemType shape;

    /**
     * Create a {@code BDecimalType} which represents the decimal type.
     *
     * @param typeName string name of the type
     */
    public BDecimalType(String typeName, Module pkg) {
        this(() -> new BDecimalTypeImpl(typeName, pkg), typeName, pkg, Builder.getDecimalType());
    }

    public static BDecimalType singletonType(BigDecimal value) {
        return new BDecimalType(() -> (BDecimalTypeImpl) DEFAULT_B_TYPE.clone(), TypeConstants.DECIMAL_TNAME,
                EMPTY_MODULE, Builder.getDecimalConst(value));
    }

    private BDecimalType(Supplier<BDecimalTypeImpl> bType, String typeName, Module pkg, SemType semType) {
        super(new ConcurrentLazySupplier<>(bType), typeName, pkg, TypeTags.DECIMAL_TAG, semType);
        shape = semType;
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }

    public SemType shape() {
        return shape;
    }

    protected static final class BDecimalTypeImpl extends BType implements DecimalType, Cloneable {

        private BDecimalTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, DecimalValue.class, false);
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
        public BasicTypeBitSet getBasicType() {
            return BASIC_TYPE;
        }

        @Override
        public BType clone() {
            return super.clone();
        }
    }
}
