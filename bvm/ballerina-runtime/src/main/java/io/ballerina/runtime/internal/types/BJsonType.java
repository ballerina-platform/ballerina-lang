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
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.JsonType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.util.stream.Stream;

/**
 * {@code BJSONType} represents a JSON value.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BJsonType extends BUnionType implements JsonType {

    private static final BasicTypeBitSet BASIC_TYPE = createBasicType();

    private static BasicTypeBitSet createBasicType() {
        int bitset =
                Stream.of(Builder.getNilType(), Builder.getBooleanType(), Builder.getIntType(), Builder.getFloatType(),
                        Builder.getDecimalType(), Builder.getStringType(), Builder.getListType(),
                        Builder.getMappingType()).map(
                        SemType::all).reduce(0, (accum, bits) -> accum | bits);
        return new BasicTypeBitSet(bitset);
    }

    private static final int readonlyTypeId = TypeIdSupplier.getAnonId();
    private static final int mutalbeTypeId = TypeIdSupplier.getAnonId();
    private final int typeId;
    /**
     * Create a {@code BJSONType} which represents the JSON type.
     *
     * @param typeName string name of the type
     * @param pkg of the type
     * @param readonly whether immutable
     */
    public BJsonType(String typeName, Module pkg, boolean readonly) {
        super(typeName, pkg, readonly, MapValueImpl.class);
        if (!readonly) {
            BJsonType immutableJsonType = new BJsonType(TypeConstants.READONLY_JSON_TNAME, pkg, true);
            this.immutableType = new BIntersectionType(pkg, new Type[]{this, PredefinedTypes.TYPE_READONLY},
                                                       immutableJsonType,
                                                       TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.ANYDATA,
                                                                        TypeFlags.PURETYPE), true);
        }
        typeId = readonly ? readonlyTypeId : mutalbeTypeId;
    }

    public BJsonType() {
        super(TypeConstants.JSON_TNAME, null, false, MapValueImpl.class);
        BJsonType immutableJsonType = new BJsonType(TypeConstants.READONLY_JSON_TNAME, pkg, true);
        this.immutableType = new BIntersectionType(pkg, new Type[]{ this, PredefinedTypes.TYPE_READONLY},
                                                   immutableJsonType,
                                                   TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.ANYDATA,
                                                                    TypeFlags.PURETYPE), true);
        typeId = mutalbeTypeId;
    }

    public BJsonType(BUnionType unionType, String typeName, boolean readonly) {
        super(unionType, typeName, readonly);
        if (!readonly) {
            BJsonType immutableJsonType = new BJsonType(unionType, TypeConstants.READONLY_JSON_TNAME, true);
            this.immutableType = new BIntersectionType(pkg, new Type[]{this, PredefinedTypes.TYPE_READONLY},
                    immutableJsonType,
                    TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.ANYDATA,
                            TypeFlags.PURETYPE), true);
        }

        typeId = readonly ? readonlyTypeId : mutalbeTypeId;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new MapValueImpl<>(this);
    }

    @Override
    public int getTag() {
        return TypeTags.JSON_TAG;
    }

    @Override
    public boolean isNilable() {
        return true;
    }

    @Override
    public boolean shouldCache() {
        return true;
    }

    @Override
    public int typeId() {
        return typeId;
    }

    @Override
    public String toString() {
        if (this.typeName != null) {
            return this.typeName;
        }
        return super.toString();
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }
}
