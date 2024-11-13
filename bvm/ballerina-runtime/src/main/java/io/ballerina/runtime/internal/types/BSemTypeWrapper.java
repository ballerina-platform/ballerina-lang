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
import io.ballerina.runtime.internal.types.semtype.ImmutableSemType;
import io.ballerina.runtime.api.types.semtype.SemType;

// TODO: make this a sealed class with clearly defined extensions

/**
 * Decorator on {@code BTypes} allowing them to behave as {@code SemType}. All {@code Types} that needs to behave as
 * both a {@code BType} and a {@code SemType} should extend this class.
 *
 * @since 2201.10.0
 */
public non-sealed class BSemTypeWrapper extends ImmutableSemType implements Type {

    private final BType bType;
    protected final String typeName; // Debugger uses this field to show the type name

    BSemTypeWrapper(BType bType, SemType semType) {
        super(semType);
        this.bType = bType;
        this.typeName = bType.typeName;
    }

    public <V extends Object> Class<V> getValueClass() {
        return bType.getValueClass();
    }

    public <V extends Object> V getZeroValue() {
        return bType.getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return bType.getEmptyValue();
    }

    @Override
    public int getTag() {
        return bType.getTag();
    }

    @Override
    public String toString() {
        return bType.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BSemTypeWrapper other)) {
            return false;
        }
        return bType.equals(other.bType);
    }

    @Override
    public boolean isNilable() {
        return bType.isNilable();
    }

    @Override
    public int hashCode() {
        return bType.hashCode();
    }

    @Override
    public String getName() {
        return bType.getName();
    }

    @Override
    public String getQualifiedName() {
        return bType.getQualifiedName();
    }

    @Override
    public Module getPackage() {
        return bType.getPackage();
    }

    @Override
    public boolean isPublic() {
        return bType.isPublic();
    }

    @Override
    public boolean isNative() {
        return bType.isNative();
    }

    // TODO: use semtype
    @Override
    public boolean isAnydata() {
        return bType.isAnydata();
    }

    @Override
    public boolean isPureType() {
        return bType.isPureType();
    }

    // TODO: use semtype
    @Override
    public boolean isReadOnly() {
        return bType.isReadOnly();
    }

    @Override
    public Type getImmutableType() {
        return bType.getImmutableType();
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        bType.setImmutableType(immutableType);
    }

    @Override
    public Module getPkg() {
        return bType.getPkg();
    }

    @Override
    public long getFlags() {
        return bType.getFlags();
    }

    @Override
    public void setCachedReferredType(Type type) {
        bType.setCachedReferredType(type);
    }

    @Override
    public Type getCachedReferredType() {
        return bType.getCachedReferredType();
    }

    @Override
    public void setCachedImpliedType(Type type) {
        bType.setCachedImpliedType(type);
    }

    @Override
    public Type getCachedImpliedType() {
        return bType.getCachedImpliedType();
    }
}
