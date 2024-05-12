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
import io.ballerina.runtime.api.types.BooleanType;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.SemType.SubType;

import java.util.List;

/**
 * {@code BBooleanType} represents boolean type in Ballerina.
 *
 * @since 0.995.0
 */
public class BBooleanType extends BType implements BooleanType, SemType {

    private final SemType semType;
    private final static BBooleanType TRUE =
            new BBooleanType(TypeConstants.BOOLEAN_TNAME, PredefinedTypes.EMPTY_MODULE, Builder.booleanConst(true));
    private final static BBooleanType FALSE =
            new BBooleanType(TypeConstants.BOOLEAN_TNAME, PredefinedTypes.EMPTY_MODULE, Builder.booleanConst(false));

    /**
     * Create a {@code BBooleanType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BBooleanType(String typeName, Module pkg) {
        this(typeName, pkg, Builder.booleanType());
    }

    public static BBooleanType singletonType(boolean value) {
        return value ? TRUE : FALSE;
    }

    private BBooleanType(String typeName, Module pkg, SemType semType) {
        super(typeName, pkg, Boolean.class);
        this.semType = semType;
    }

    @SuppressWarnings("unchecked")
    public <V extends Object> V getZeroValue() {
        return (V) Boolean.FALSE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) Boolean.FALSE;
    }

    @Override
    public int getTag() {
        return TypeTags.BOOLEAN_TAG;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    SemType createSemType() {
        return semType;
    }

    @Override
    public int all() {
        return get().all();
    }

    @Override
    public int some() {
        return get().some();
    }

    @Override
    public List<SubType> subTypeData() {
        return get().subTypeData();
    }
}
