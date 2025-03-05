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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.FloatType;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.function.Supplier;

import static io.ballerina.runtime.api.types.PredefinedTypes.EMPTY_MODULE;

/**
 * {@code BFloatType} represents a integer which is a 32-bit floating-point number according to the
 * standard IEEE 754 specifications.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class BFloatType extends BSemTypeWrapper<BFloatType.BFloatTypeImpl> implements FloatType {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getFloatType();

    /**
     * Create a {@code BFloatType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BFloatType(String typeName, Module pkg) {
        this(() -> new BFloatTypeImpl(typeName, pkg), typeName, pkg, Builder.getFloatType());
    }

    private BFloatType(Supplier<BFloatTypeImpl> bType, String typeName, Module pkg, SemType semType) {
        super(new ConcurrentLazySupplier<>(bType), typeName, pkg, TypeTags.FLOAT_TAG, semType);
    }

    public static BFloatType singletonType(Double value) {
        return new BFloatType(() -> new BFloatTypeImpl(TypeConstants.FLOAT_TNAME, EMPTY_MODULE),
                TypeConstants.FLOAT_TNAME, EMPTY_MODULE, Builder.getFloatConst(value));
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }

    protected static final class BFloatTypeImpl extends BType implements FloatType {

        private BFloatTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, Double.class, false);
        }

        @Override
        public <V extends Object> V getZeroValue() {
            return (V) new Double(0);
        }

        @Override
        public <V extends Object> V getEmptyValue() {
            return (V) new Double(0);
        }

        @Override
        public int getTag() {
            return TypeTags.FLOAT_TAG;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public BasicTypeBitSet getBasicType() {
            return BASIC_TYPE;
        }
    }
}
