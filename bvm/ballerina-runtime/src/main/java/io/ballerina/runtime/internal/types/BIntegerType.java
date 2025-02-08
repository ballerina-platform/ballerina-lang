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
import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.function.Supplier;

import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.types.PredefinedTypes.EMPTY_MODULE;

/**
 * {@code BIntegerType} represents an integer which is a 32-bit signed number.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class BIntegerType extends BSemTypeWrapper<BIntegerType.BIntegerTypeImpl> implements IntegerType {

    private static final BIntegerTypeImpl DEFAULT_B_TYPE =
            new BIntegerTypeImpl(TypeConstants.INT_TNAME, EMPTY_MODULE, TypeTags.INT_TAG);

    /**
     * Create a {@code BIntegerType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BIntegerType(String typeName, Module pkg) {
        this(() -> new BIntegerTypeImpl(typeName, pkg, TypeTags.INT_TAG), typeName, pkg, TypeTags.INT_TAG,
                Builder.getIntType());
    }

    public BIntegerType(String typeName, Module pkg, int tag) {
        this(() -> new BIntegerTypeImpl(typeName, pkg, tag), typeName, pkg, tag, pickSemType(tag));
    }

    private BIntegerType(Supplier<BIntegerTypeImpl> bIntegerTypeSupplier, String typeName, Module pkg, int tag,
                         SemType semType) {
        super(new ConcurrentLazySupplier<>(bIntegerTypeSupplier), typeName, pkg, tag, semType);
    }

    private static SemType pickSemType(int tag) {
        return switch (tag) {
            case TypeTags.INT_TAG -> Builder.getIntType();
            case TypeTags.SIGNED8_INT_TAG -> Builder.createIntRange(SIGNED8_MIN_VALUE, SIGNED8_MAX_VALUE);
            case TypeTags.SIGNED16_INT_TAG -> Builder.createIntRange(SIGNED16_MIN_VALUE, SIGNED16_MAX_VALUE);
            case TypeTags.SIGNED32_INT_TAG -> Builder.createIntRange(SIGNED32_MIN_VALUE, SIGNED32_MAX_VALUE);
            case TypeTags.UNSIGNED8_INT_TAG, TypeTags.BYTE_TAG -> Builder.createIntRange(0, UNSIGNED8_MAX_VALUE);
            case TypeTags.UNSIGNED16_INT_TAG -> Builder.createIntRange(0, UNSIGNED16_MAX_VALUE);
            case TypeTags.UNSIGNED32_INT_TAG -> Builder.createIntRange(0, UNSIGNED32_MAX_VALUE);
            default -> throw new UnsupportedOperationException("Unexpected int tag");
        };
    }

    public static BIntegerType singletonType(long value) {
        if (value >= IntegerTypeCache.CACHE_MIN_VALUE && value <= IntegerTypeCache.CACHE_MAX_VALUE) {
            return IntegerTypeCache.cache[(int) value - IntegerTypeCache.CACHE_MIN_VALUE];
        }
        return createSingletonType(value);
    }

    private static BIntegerType createSingletonType(long value) {
        return new BIntegerType(() -> (BIntegerTypeImpl) DEFAULT_B_TYPE.clone(), TypeConstants.INT_TNAME, EMPTY_MODULE,
                TypeTags.INT_TAG, Builder.getIntConst(value));
    }

    protected static final class BIntegerTypeImpl extends BType implements IntegerType, Cloneable {

        private final int tag;

        private BIntegerTypeImpl(String typeName, Module pkg, int tag) {
            super(typeName, pkg, Long.class);
            this.tag = tag;
        }

        @Override
        public <V extends Object> V getZeroValue() {
            return (V) new Long(0);
        }

        @Override
        public <V extends Object> V getEmptyValue() {
            return (V) new Long(0);
        }

        @Override
        public int getTag() {
            return tag;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public BType clone() {
            return super.clone();
        }
    }

    private static final class IntegerTypeCache {

        private static final BIntegerType[] cache;
        private static final int CACHE_MAX_VALUE = 127;
        private static final int CACHE_MIN_VALUE = -128;
        static {
            cache = new BIntegerType[CACHE_MAX_VALUE - CACHE_MIN_VALUE + 1];
            for (int i = CACHE_MIN_VALUE; i <= CACHE_MAX_VALUE; i++) {
                cache[i - CACHE_MIN_VALUE] = createSingletonType(i);
            }
        }
    }
}
