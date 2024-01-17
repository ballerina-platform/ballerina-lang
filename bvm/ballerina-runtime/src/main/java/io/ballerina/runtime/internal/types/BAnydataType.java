/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.AnydataType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.values.RefValue;

/**
 * {@code BAnydataType} represents the data types in Ballerina.
 *
 * @since 0.995.0
 */
public class BAnydataType extends BUnionType implements AnydataType {

    /**
     * Create a {@code BAnydataType} which represents the anydata type.
     *
     * @param typeName string name of the type
     */
    public BAnydataType(String typeName, Module pkg, boolean readonly) {
        super(typeName, pkg, readonly, RefValue.class);
        if (!readonly) {
            BAnydataType immutableAnydataType = new BAnydataType(TypeConstants.READONLY_ANYDATA_TNAME, pkg, true);
            this.immutableType = new BIntersectionType(pkg, new Type[]{ this, PredefinedTypes.TYPE_READONLY},
                                                       immutableAnydataType, TypeFlags.asMask(TypeFlags.NILABLE,
                                                        TypeFlags.ANYDATA, TypeFlags.PURETYPE), true);
        }
        this.mergeUnionType((BUnionType) PredefinedTypes.TYPE_ANYDATA);
    }

    public BAnydataType(BUnionType unionType, String typeName, boolean readonly) {
        super(unionType, typeName, readonly);
        if (!readonly) {
            BAnydataType immutableAnydataType = new BAnydataType(unionType, TypeConstants.READONLY_ANYDATA_TNAME, true);
            this.immutableType = new BIntersectionType(pkg, new Type[]{this, PredefinedTypes.TYPE_READONLY},
                    immutableAnydataType, TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.ANYDATA, TypeFlags.PURETYPE),
                    true);
        }
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
        return TypeTags.ANYDATA_TAG;
    }

    public boolean isNilable() {
        return true;
    }

    @Override
    public String toString() {
        if (this.typeName != null) {
            return this.typeName;
        }
        return super.toString();
    }
}
