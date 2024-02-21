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
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.AnyType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.values.RefValue;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.UniformTypeBitSet;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@code BAnyType} represents any type in Ballerina. It is the root of the Ballerina type system.
 *
 * @since 0.995.0
 */
public class BAnyType extends BType implements AnyType {

    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private static final SemType semType = createSemType();

    /**
     * Create a {@code BAnyType} which represents the any type.
     *
     * @param typeName string name of the type
     */
    public BAnyType(String typeName, Module pkg, boolean readonly) {
        super(typeName, pkg, RefValue.class);
        this.readonly = readonly;

        if (!readonly) {
            BAnyType immutableAnyType = new BAnyType(TypeConstants.READONLY_ANY_TNAME, pkg, true);
            this.immutableType = new BIntersectionType(pkg, new Type[]{ this, PredefinedTypes.TYPE_READONLY},
                                                       immutableAnyType, TypeFlags.asMask(TypeFlags.NILABLE), true);
        }
    }

    private static SemType createSemType() {
        // BTypeHack: we have only implemented semtype for these components of any type
        return Stream.of(PredefinedType.INT,
                        PredefinedType.FLOAT,
                        PredefinedType.DECIMAL,
                        PredefinedType.STRING,
                        PredefinedType.BOOLEAN,
                        PredefinedType.NIL)
                .reduce(PredefinedType.NEVER,
                        (semType, predefinedType) -> (UniformTypeBitSet) SemTypes.union(semType, predefinedType));
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.ANY_TAG;
    }

    public boolean isNilable() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
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
    public SemType getSemTypeComponent() {
        return semType;
    }

    @Override
    public BType getBTypeComponent() {
        // BTypeHack: this is wrong, but it is working because for types that we have implemented
        // we return NEVER as the bTypeComponent which is a subtype of every type.
        // Also note that Any is any way not a subtype of any other type don't contain any.
        // Correct solution is to return only the unimplemented parts.
        return this;
    }
}
