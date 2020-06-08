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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * {@code BJSONType} represents a JSON value.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BJSONType extends BType {

    private final boolean readonly;
    private BIntersectionType immutableType;

    /**
     * Create a {@code BJSONType} which represents the JSON type.
     *
     * @param typeName string name of the type
     * @param pkg of the type
     * @param readonly whether immutable
     */
    public BJSONType(String typeName, BPackage pkg, boolean readonly) {
        super(typeName, pkg, MapValueImpl.class);
        this.readonly = readonly;

        if (!readonly) {
            BJSONType immutableJsonType = new BJSONType(TypeConstants.READONLY_JSON_TNAME, pkg, true);
            this.immutableType = new BIntersectionType(new BType[]{ this, BTypes.typeReadonly }, immutableJsonType,
                                                       TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.ANYDATA,
                                                                        TypeFlags.PURETYPE), true);
        }
    }

    public BJSONType() {
        super(TypeConstants.JSON_TNAME, null, MapValueImpl.class);
        this.readonly = false;
        BJSONType immutableJsonType = new BJSONType(TypeConstants.READONLY_JSON_TNAME, pkg, true);
        this.immutableType = new BIntersectionType(new BType[]{ this, BTypes.typeReadonly }, immutableJsonType,
                                                   TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.ANYDATA,
                                                                    TypeFlags.PURETYPE), true);
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
    public boolean equals(Object obj) {
        if (!(obj instanceof BJSONType)) {
            return false;
        }

        return super.equals(obj) && this.readonly == ((BJSONType) obj).readonly;
    }

    public boolean isNilable() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public BType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(BIntersectionType immutableType) {
        this.immutableType = immutableType;
    }
}
