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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.runtime.api.types.semtype.Builder;
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

/**
 * {@code BIntegerType} represents an integer which is a 32-bit signed number.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class BIntegerType extends BSemTypeWrapper<BIntegerType.BIntegerTypeImpl> implements IntegerType {

    private static final BIntegerTypeImpl DEFAULT_B_TYPE =
            new BIntegerTypeImpl(TypeConstants.INT_TNAME, PredefinedTypes.EMPTY_MODULE, TypeTags.INT_TAG);

    /**
     * Create a {@code BIntegerType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BIntegerType(String typeName, Module pkg) {
        this(() -> new BIntegerTypeImpl(typeName, pkg, TypeTags.INT_TAG), typeName, Builder.intType());
    }

    public BIntegerType(String typeName, Module pkg, int tag) {
        this(() -> new BIntegerTypeImpl(typeName, pkg, tag), typeName, pickSemType(tag));
    }

    private BIntegerType(Supplier<BIntegerTypeImpl> bType, String typeName, SemType semType) {
        super(bType, typeName, semType);
    }

    private static SemType pickSemType(int tag) {
        return switch (tag) {
            case TypeTags.INT_TAG -> Builder.intType();
            case TypeTags.SIGNED8_INT_TAG -> Builder.intRange(SIGNED8_MIN_VALUE, SIGNED8_MAX_VALUE);
            case TypeTags.SIGNED16_INT_TAG -> Builder.intRange(SIGNED16_MIN_VALUE, SIGNED16_MAX_VALUE);
            case TypeTags.SIGNED32_INT_TAG -> Builder.intRange(SIGNED32_MIN_VALUE, SIGNED32_MAX_VALUE);
            case TypeTags.UNSIGNED8_INT_TAG, TypeTags.BYTE_TAG -> Builder.intRange(0, UNSIGNED8_MAX_VALUE);
            case TypeTags.UNSIGNED16_INT_TAG -> Builder.intRange(0, UNSIGNED16_MAX_VALUE);
            case TypeTags.UNSIGNED32_INT_TAG -> Builder.intRange(0, UNSIGNED32_MAX_VALUE);
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
        return new BIntegerType(() -> {
            try {
                return (BIntegerTypeImpl) DEFAULT_B_TYPE.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }, TypeConstants.INT_TNAME, Builder.intConst(value));
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
        protected Object clone() throws CloneNotSupportedException {
            BType bType = (BType) super.clone();
            bType.setCachedImpliedType(null);
            bType.setCachedReferredType(null);
            return bType;
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
