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

import com.github.benmanes.caffeine.cache.Cache;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.StringType;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * {@code BStringType} represents a String type in ballerina.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class BStringType extends BSemTypeWrapper<BStringType.BStringTypeImpl> implements StringType {

    private static final BasicTypeBitSet BASIC_TYPE_BIT_SET = Builder.getStringType();
    // We are creating separate empty module instead of reusing PredefinedTypes.EMPTY_MODULE to avoid cyclic
    // dependencies.
    private static final Module DEFAULT_MODULE = new Module(null, null, null);
    private static final BStringTypeImpl DEFAULT_B_TYPE =
            new BStringTypeImpl(TypeConstants.STRING_TNAME, DEFAULT_MODULE, TypeTags.STRING_TAG);
    private final SemType shape;

    /**
     * Create a {@code BStringType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BStringType(String typeName, Module pkg) {
        this(() -> new BStringTypeImpl(typeName, pkg, TypeTags.STRING_TAG), typeName, pkg, TypeTags.STRING_TAG,
                Builder.getStringType());
    }

    public BStringType(String typeName, Module pkg, int tag) {
        this(() -> new BStringTypeImpl(typeName, pkg, tag), typeName, pkg, tag, pickSemtype(tag));
    }

    private BStringType(Supplier<BStringTypeImpl> bTypeSupplier, String typeName, Module pkg, int tag,
                        SemType semType) {
        super(new ConcurrentLazySupplier<>(bTypeSupplier), typeName, pkg, tag, semType);
        shape = semType;
    }

    public static BStringType singletonType(String value) {
        return BStringTypeCache.get(value);
    }

    public SemType shape() {
        return shape;
    }

    private static BStringType createSingletonType(String value) {
        return new BStringType(() -> (BStringTypeImpl) DEFAULT_B_TYPE.clone(), TypeConstants.STRING_TNAME,
                DEFAULT_MODULE, TypeTags.STRING_TAG, Builder.getStringConst(value));
    }

    private static SemType pickSemtype(int tag) {
        return switch (tag) {
            case TypeTags.STRING_TAG -> Builder.getStringType();
            case TypeTags.CHAR_STRING_TAG -> Builder.getCharType();
            default -> throw new IllegalStateException("Unexpected string type tag: " + tag);
        };
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE_BIT_SET;
    }

    protected static final class BStringTypeImpl extends BType implements StringType, Cloneable {

        private final int tag;

        private BStringTypeImpl(String typeName, Module pkg, int tag) {
            super(typeName, pkg, String.class, false);
            this.tag = tag;
        }

        @Override
        public <V extends Object> V getZeroValue() {
            return (V) RuntimeConstants.STRING_EMPTY_VALUE;
        }

        @Override
        public <V extends Object> V getEmptyValue() {
            return (V) RuntimeConstants.STRING_EMPTY_VALUE;
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
        public BasicTypeBitSet getBasicType() {
            return BASIC_TYPE_BIT_SET;
        }

        @Override
        public BType clone() {
            return super.clone();
        }
    }

    private static final class BStringTypeCache {

        private static final Cache<String, BStringType> mediumCache = CacheFactory.createCache();
        private static final Map<String, BStringType> smallCache = new WeakHashMap<>();

        public static BStringType get(String value) {
            return switch (Kind.getKind(value)) {
                case SMALL -> smallCache.computeIfAbsent(value, BStringType::createSingletonType);
                case MEDIUM -> mediumCache.get(value, BStringType::createSingletonType);
                case LARGE -> BStringType.createSingletonType(value);
            };
        }

        enum Kind {
            SMALL,
            MEDIUM,
            LARGE;

            public static Kind getKind(String value) {
                if (value.length() < 10) {
                    return SMALL;
                } else if (value.length() < 50) {
                    return MEDIUM;
                } else {
                    return LARGE;
                }
            }
        }
    }
}
