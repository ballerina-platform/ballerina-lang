/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.types.semtype.ImmutableSemType;

import java.util.function.Supplier;

// TODO: make this a sealed class with clearly defined extensions

/**
 * Decorator on {@code BTypes} allowing them to behave as {@code SemType}. All {@code Types} that needs to behave as
 * both a {@code BType} and a {@code SemType} should extend this class.
 *
 * @since 2201.10.0
 */
public non-sealed class BSemTypeWrapper<E extends BType> extends ImmutableSemType implements Type {

    // FIXME: turn this to a lazy supplier to avoid intialization if not needed
    private E bType;
    private final Supplier<E> bTypeSupplier;
    protected final String typeName; // Debugger uses this field to show the type name

    BSemTypeWrapper(Supplier<E> bTypeSupplier, String typeName, SemType semType) {
        super(semType);
        this.bTypeSupplier = bTypeSupplier;
        this.typeName = typeName;
    }

    public <V extends Object> Class<V> getValueClass() {
        return getbType().getValueClass();
    }

    public <V extends Object> V getZeroValue() {
        return getbType().getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getbType().getEmptyValue();
    }

    @Override
    public int getTag() {
        return getbType().getTag();
    }

    @Override
    public String toString() {
        return getbType().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BSemTypeWrapper other)) {
            return false;
        }
        return getbType().equals(other.getbType());
    }

    @Override
    public boolean isNilable() {
        return getbType().isNilable();
    }

    @Override
    public int hashCode() {
        return getbType().hashCode();
    }

    @Override
    public String getName() {
        return getbType().getName();
    }

    @Override
    public String getQualifiedName() {
        return getbType().getQualifiedName();
    }

    @Override
    public Module getPackage() {
        return getbType().getPackage();
    }

    @Override
    public boolean isPublic() {
        return getbType().isPublic();
    }

    @Override
    public boolean isNative() {
        return getbType().isNative();
    }

    // TODO: use semtype
    @Override
    public boolean isAnydata() {
        return getbType().isAnydata();
    }

    @Override
    public boolean isPureType() {
        return getbType().isPureType();
    }

    // TODO: use semtype
    @Override
    public boolean isReadOnly() {
        return getbType().isReadOnly();
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
        return getbType().getPkg();
    }

    @Override
    public long getFlags() {
        return getbType().getFlags();
    }

    @Override
    public void setCachedReferredType(Type type) {
        getbType().setCachedReferredType(type);
    }

    @Override
    public Type getCachedReferredType() {
        return getbType().getCachedReferredType();
    }

    @Override
    public void setCachedImpliedType(Type type) {
        getbType().setCachedImpliedType(type);
    }

    @Override
    public Type getCachedImpliedType() {
        return getbType().getCachedImpliedType();
    }

    protected synchronized E getbType() {
        if (bType == null) {
            bType = bTypeSupplier.get();
        }
        return bType;
    }
}
