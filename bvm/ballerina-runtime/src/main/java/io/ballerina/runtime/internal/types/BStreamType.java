/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
import io.ballerina.runtime.internal.types.semtype.StreamDefinition;
import io.ballerina.runtime.internal.values.StreamValue;

import java.util.Objects;
import java.util.Set;

/**
 * {@link BStreamType} represents streaming data in Ballerina.
 *
 * @since 1.2.0
 */
public class BStreamType extends BType implements StreamType {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getStreamType();
    private final Type constraint;
    private final Type completionType;
    private final DefinitionContainer<StreamDefinition> definition = new DefinitionContainer<>();

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param typeName   string name of the type
     * @param constraint the type by which this stream is constrained
     * @param completionType the type which indicates the completion of the stream
     * @param pkgPath    package path
     */
    public BStreamType(String typeName, Type constraint, Type completionType, Module pkgPath) {
        super(typeName, pkgPath, StreamValue.class, false);
        this.constraint = constraint;
        this.completionType = completionType;
        BStreamTypeCache.Value val = BStreamTypeCache.get(constraint, completionType);
        this.typeCheckCache = val.cache;
        this.typeId = val.typeId;
    }

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param constraint     the type by which this stream is constrained
     * @param completionType the type which indicates the completion of the stream
     */
    public BStreamType(Type constraint, Type completionType) {
        this(TypeConstants.STREAM_TNAME, constraint, completionType, null);
    }

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param typeName   string name of the type
     * @param constraint the type by which this stream is constrained
     * @param pkgPath    package path
     * @deprecated use {@link #BStreamType(String, Type, Type, Module)} instead.
     */
    @Deprecated
    public BStreamType(String typeName, Type constraint, Module pkgPath) {
        this(typeName, constraint, PredefinedTypes.TYPE_NULL, pkgPath);
    }

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param constraint the type by which this stream is constrained
     * @deprecated use {@link #BStreamType(Type, Type)} instead.
     */
    @Deprecated
    public BStreamType(Type constraint) {
        this(TypeConstants.STREAM_TNAME, constraint, null);
    }

    @Override
    public Type getConstrainedType() {
        return constraint;
    }

    @Override
    public Type getCompletionType() {
        return completionType;
    }

    @Override
    public <V> V getZeroValue() {
        return (V) new StreamValue(this);
    }

    @Override
    public <V> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.STREAM_TAG;
    }

    @Override
    public String toString() {
        return super.toString() + "<" + constraint.toString() +
                ((completionType != null && completionType.getTag() != TypeTags.NULL_TAG)
                        ? "," + completionType.toString() : "") + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BStreamType other)) {
            return false;
        }

        if (constraint == other.constraint && completionType == other.completionType) {
            return true;
        }

        if (constraint == null || other.constraint == null) {
            return false;
        }

        return Objects.equals(constraint, other.constraint)
                && Objects.equals(completionType, other.completionType);
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }

    @Override
    public SemType createSemType(Context cx) {
        if (constraint == null) {
            return Builder.getStreamType();
        }
        Env env = cx.env;
        if (definition.isDefinitionReady()) {
            return definition.getSemType(env);
        }
        var result = definition.trySetDefinition(StreamDefinition::new);
        if (!result.updated()) {
            return definition.getSemType(env);
        }
        StreamDefinition sd = result.definition();
        return sd.define(env, tryInto(cx, constraint), tryInto(cx, completionType));
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return (constraint instanceof MayBeDependentType constrainedType &&
                constrainedType.isDependentlyTyped(visited)) ||
                (completionType instanceof MayBeDependentType completionType &&
                        completionType.isDependentlyTyped(visited));
    }


    private static final class BStreamTypeCache {
        record Key(int constrainId, int completionId) { }
        record Value(int typeId, TypeCheckCache cache) { }
        private static final LoadingCache<Key, Value> CACHE = CacheFactory.createCache(
                BStreamTypeCache::createNewValue);

        public static Value get(Type constraint, Type completion) {
            return CACHE.get(new Key(((CacheableTypeDescriptor) constraint).typeId(),
                        ((CacheableTypeDescriptor) completion).typeId()));
        }

        private static Value createNewValue(Key key) {
            return new Value(TypeIdSupplier.getAnonId(), TypeCheckCacheFactory.create());
        }
    }
}
