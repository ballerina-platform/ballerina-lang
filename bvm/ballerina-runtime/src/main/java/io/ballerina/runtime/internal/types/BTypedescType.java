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

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.types.semtype.TypedescUtils;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;

import java.util.Map;
import java.util.Set;

/**
 * {@code BTypedescType} represents a type of a type in the Ballerina type system.
 *
 * @since 0.995.0
 */
public class BTypedescType extends BType implements TypedescType {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getTypeDescType();

    private final Type constraint;

    public BTypedescType(String typeName, Module pkg) {
        super(typeName, pkg, Object.class, true);
        constraint = null;
    }

    public BTypedescType(Type constraint) {
        super(TypeConstants.TYPEDESC_TNAME, null, TypedescValue.class, false);
        this.constraint = constraint;
        var flyweight = TypeCheckFlyweightStore.get(constraint);
        this.typeCheckCache = flyweight.typeCheckCache();
        this.typeId = flyweight.typeId();
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new TypedescValueImpl(PredefinedTypes.TYPE_NULL);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TYPEDESC_TAG;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BTypedescType typedescType) {
            return constraint.equals(typedescType.getConstraint());
        }
        return false;
    }

    @Override
    public Type getConstraint() {
        return constraint;
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
    public String toString() {
        return "typedesc" + "<" + constraint.toString() + ">";
    }

    @Override
    public SemType createSemType(Context cx) {
        if (constraint == null) {
            return Builder.getTypeDescType();
        }
        SemType constraint = tryInto(cx, getConstraint());
        return TypedescUtils.typedescContaining(cx.env, constraint);
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return constraint instanceof MayBeDependentType constraintType &&
                constraintType.isDependentlyTyped(visited);
    }

    private static class TypeCheckFlyweightStore {

        private static final LoadingCache<Integer, TypeCheckFlyweight> unnamedTypeCache =
                CacheFactory.createCache(TypeCheckFlyweight::new);

        private static final TypeCheckFlyweight[] reservedLAT =
                new TypeCheckFlyweight[TypeIdSupplier.MAX_RESERVED_ID];

        private static final Map<Integer, TypeCheckFlyweight> namedTypeCache = CacheFactory.createCachingHashMap();

        public static TypeCheckFlyweight get(Type constraint) {
            if (constraint instanceof CacheableTypeDescriptor cacheableTypeDescriptor) {
                return getInner(cacheableTypeDescriptor.typeId());
            }
            return new TypeCheckFlyweight(TypeIdSupplier.getAnonId(), TypeCheckCacheFactory.create());
        }

        private static TypeCheckFlyweight getInner(int typeId) {
            return switch (TypeIdSupplier.kind(typeId)) {
                case RESERVED -> getReserved(typeId);
                case NAMED -> getNamed(typeId);
                case UNNAMED -> getUnnamed(typeId);
            };
        }

        private static TypeCheckFlyweight getUnnamed(int typeId) {
            return unnamedTypeCache.get(typeId);
        }

        private static TypeCheckFlyweight getNamed(int typeId) {
            var cached = namedTypeCache.get(typeId);
            if (cached != null) {
                return cached;
            }
            cached = new TypeCheckFlyweight(typeId);
            namedTypeCache.put(typeId, cached);
            return cached;
        }

        private static TypeCheckFlyweight getReserved(int typeId) {
            TypeCheckFlyweight o = reservedLAT[typeId];
            if (o == null) {
                o = new TypeCheckFlyweight(typeId);
                reservedLAT[typeId] = o;
            }
            return o;
        }

        private record TypeCheckFlyweight(int typeId, TypeCheckCache typeCheckCache) {

            public TypeCheckFlyweight(Integer constraintId) {
                this(TypeIdSupplier.getAnonId(), TypeCheckCacheFactory.create());
            }
        }
    }
}
