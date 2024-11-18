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

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.semtype.ErrorUtils;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.util.Optional;
import java.util.Set;

/**
 * {@code BErrorType} represents error type in Ballerina.
 *
 * @since 0.995.0
 */
public class BErrorType extends BAnnotatableType implements ErrorType, TypeWithShape {

    public Type detailType = PredefinedTypes.TYPE_DETAIL;
    public BTypeIdSet typeIdSet;
    private IntersectionType intersectionType = null;
    private volatile DistinctIdSupplier distinctIdSupplier;

    public BErrorType(String typeName, Module pkg, Type detailType) {
        super(typeName, pkg, ErrorValue.class);
        this.detailType = detailType;
    }

    public BErrorType(String typeName, Module pkg) {
        super(typeName, pkg, ErrorValue.class);
    }

    public void setTypeIdSet(BTypeIdSet typeIdSet) {
        this.typeIdSet = typeIdSet;
        synchronized (this) {
            this.distinctIdSupplier = null;
        }
    }

    @Override
    public <V> V getZeroValue() {
        return null;
    }

    @Override
    public <V> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.ERROR_TAG;
    }

    public void setDetailType(Type detailType) {
        this.detailType = detailType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BErrorType other)) {
            return false;
        }

        if (detailType == other.detailType) {
            return true;
        }

        return detailType.equals(other.detailType);
    }

    @Override
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(typeName);
    }

    @Override
    public Type getDetailType() {
        return detailType;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public BTypeIdSet getTypeIdSet() {
        return typeIdSet;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    @Override
    public SemType createSemType() {
        SemType err;
        if (detailType == null || isTopType()) {
            err = Builder.getErrorType();
        } else {
            err = ErrorUtils.errorDetail(tryInto(getDetailType()));
        }

        initializeDistinctIdSupplierIfNeeded();
        return distinctIdSupplier.get().stream().map(ErrorUtils::errorDistinct).reduce(err, Core::intersect);
    }

    private void initializeDistinctIdSupplierIfNeeded() {
        if (distinctIdSupplier == null) {
            synchronized (this) {
                if (distinctIdSupplier == null) {
                    distinctIdSupplier = new DistinctIdSupplier(TypeChecker.context().env, getTypeIdSet());
                }
            }
        }
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return detailType instanceof MayBeDependentType mayBeDependentType &&
                mayBeDependentType.isDependentlyTyped(visited);
    }

    private boolean isTopType() {
        return detailType == PredefinedTypes.TYPE_DETAIL;
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        if (!couldInherentTypeBeDifferent()) {
            return Optional.of(getSemType());
        }
        BError errorValue = (BError) object;
        Object details = errorValue.getDetails();
        if (!(details instanceof MapValueImpl<?, ?> errorDetails)) {
            return Optional.empty();
        }
        initializeDistinctIdSupplierIfNeeded();
        // Should we actually pass the readonly shape supplier here?
        return BMapType.shapeOfInner(cx, shapeSupplier, errorDetails)
                .map(ErrorUtils::errorDetail)
                .map(err -> distinctIdSupplier.get().stream().map(ErrorUtils::errorDistinct)
                        .reduce(err, Core::intersect));
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object) {
        BError errorValue = (BError) object;
        Object details = errorValue.getDetails();
        if (!(details instanceof MapValueImpl<?, ?> errorDetails)) {
            return Optional.empty();
        }
        return BMapType.shapeOfInner(cx, shapeSupplierFn, errorDetails).map(ErrorUtils::errorDetail);
    }

    @Override
    public Optional<SemType> acceptedTypeOf(Context cx) {
        return Optional.of(getSemType());
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        // TODO: consider properly handling this
        return true;
    }
}
