/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.semtype.ImmutableSemType;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Decorator on {@code BTypes} allowing them to behave as {@code SemType}. All
 * {@code Types} that needs to behave as both a {@code BType} and a
 * {@code SemType} should extend this class.
 *
 * @param <E> The type of the {@code BType} that is being wrapped.
 * @since 2201.12.0
 */
public abstract sealed class BSemTypeWrapper<E extends BType> extends ImmutableSemType
        implements Type, MayBeDependentType
        permits BAnyType, BBooleanType, BByteType, BDecimalType, BFloatType, BHandleType, BIntegerType, BNullType,
        BReadonlyType, BStringType {

    private Type cachedReferredType = null;
    private Type cachedImpliedType = null;

    private final Supplier<E> bTypeSupplier;
    private final int tag;
    protected final String typeName; // Debugger uses this field to show the type name
    private final Module pkg;

    protected BSemTypeWrapper(Supplier<E> bTypeSupplier, String typeName, Module pkg, int tag, SemType semType) {
        super(semType);
        this.bTypeSupplier = bTypeSupplier;
        this.typeName = typeName;
        this.tag = tag;
        this.pkg = pkg;
    }

    public final <V extends Object> Class<V> getValueClass() {
        return getbType().getValueClass();
    }

    @Override
    public final <V extends Object> V getZeroValue() {
        return getbType().getZeroValue();
    }

    @Override
    public final <V extends Object> V getEmptyValue() {
        return getbType().getEmptyValue();
    }

    @Override
    public final int getTag() {
        return tag;
    }

    @Override
    public final String toString() {
        return getbType().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BSemTypeWrapper<?> other)) {
            return false;
        }
        return Objects.equals(this.typeName, other.typeName) && Objects.equals(this.pkg, other.pkg);
    }

    @Override
    public final boolean isNilable() {
        return Core.containsBasicType(this, Builder.getNilType());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.typeName, this.pkg);
    }

    @Override
    public String getName() {
        return typeName == null ? "" : typeName;
    }

    @Override
    public String getQualifiedName() {
        String name = getName();
        if (name.isEmpty()) {
            return "";
        }

        return pkg == null ? name : pkg + ":" + name;
    }

    @Override
    public Module getPackage() {
        return pkg;
    }

    @Override
    public boolean isPublic() {
        return getbType().isPublic();
    }

    @Override
    public boolean isNative() {
        return getbType().isNative();
    }

    @Override
    public boolean isAnydata() {
        return Core.isSubtypeSimple(this, Builder.getAnyDataType());
    }

    @Override
    public boolean isPureType() {
        return Core.isSubtypeSimple(this, Builder.getErrorType()) || isAnydata();
    }

    @Override
    public boolean isReadOnly() {
        if (Core.isSubtypeSimple(this, Builder.getInherentlyImmutable())) {
            return true;
        }
        Context cx = TypeChecker.context();
        return Core.isSubType(cx, this, Builder.getReadonlyType());
    }

    @Override
    public Type getImmutableType() {
        return getbType().getImmutableType();
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        getbType().setImmutableType(immutableType);
    }

    @Override
    public Module getPkg() {
        return pkg;
    }

    @Override
    public long getFlags() {
        return getbType().getFlags();
    }

    @Override
    public void setCachedReferredType(Type type) {
        cachedReferredType = type;
    }

    @Override
    public Type getCachedReferredType() {
        return cachedReferredType;
    }

    @Override
    public void setCachedImpliedType(Type type) {
        cachedImpliedType = type;
    }

    @Override
    public Type getCachedImpliedType() {
        return cachedImpliedType;
    }

    protected E getbType() {
        return bTypeSupplier.get();
    }

    @Override
    public boolean isDependentlyTyped() {
        return false;
    }

    @Override
    public boolean isDependentlyTyped(Set<MayBeDependentType> visited) {
        return isDependentlyTyped();
    }
}
