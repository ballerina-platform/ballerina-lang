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
import io.ballerina.runtime.api.types.ByteType;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.function.Supplier;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.types.PredefinedTypes.EMPTY_MODULE;

/**
 * {@code BByteType} represents byte type in Ballerina.
 *
 * @since 0.995.0
 */
public final class BByteType extends BSemTypeWrapper<BByteType.BByteTypeImpl> implements ByteType {

    private static final BByteTypeImpl DEFAULT_B_TYPE = new BByteTypeImpl(TypeConstants.BYTE_TNAME, EMPTY_MODULE);
    private static final BasicTypeBitSet BASIC_TYPE_BIT_SET = Builder.getIntType();

    /**
     * Create a {@code BByteType} which represents the byte type.
     *
     * @param typeName string name of the type
     */
    public BByteType(String typeName, Module pkg) {
        this(() -> new BByteTypeImpl(typeName, pkg), typeName, EMPTY_MODULE,
                Builder.createIntRange(0, UNSIGNED8_MAX_VALUE));
    }

    private BByteType(Supplier<BByteTypeImpl> bTypeSupplier, String typeName, Module pkg, SemType semType) {
        super(new ConcurrentLazySupplier<>(bTypeSupplier), typeName, pkg, TypeTags.BYTE_TAG, semType);
    }

    public static BByteType singletonType(long value) {
        return new BByteType(() -> (BByteTypeImpl) DEFAULT_B_TYPE.clone(), TypeConstants.BYTE_TNAME, EMPTY_MODULE,
                Builder.getIntConst(value));
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE_BIT_SET;
    }

    protected static final class BByteTypeImpl extends BType implements ByteType, Cloneable {

        private BByteTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, Integer.class, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V extends Object> V getZeroValue() {
            return (V) new Integer(0);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V extends Object> V getEmptyValue() {
            return (V) new Integer(0);
        }

        @Override
        public int getTag() {
            return TypeTags.BYTE_TAG;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public BasicTypeBitSet getBasicType() {
            return BASIC_TYPE_BIT_SET;
        }

        @Override
        public BType clone() {
            return super.clone();
        }
    }
}
