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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.FloatType;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.SemType.SubType;

import java.util.List;

import static io.ballerina.runtime.api.PredefinedTypes.EMPTY_MODULE;

/**
 * {@code BFloatType} represents a integer which is a 32-bit floating-point number according to the
 * standard IEEE 754 specifications.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BFloatType extends BType implements FloatType, SemType {

    /**
     * Create a {@code BFloatType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    private final SemType semType;

    public BFloatType(String typeName, Module pkg) {
        this(typeName, pkg, Builder.floatType());
    }

    private BFloatType(String typeName, Module pkg, SemType semType) {
        super(typeName, pkg, Double.class);
        this.semType = semType;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new Double(0);
    }
    
    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new Double(0);
    }

    @Override
    public int getTag() {
        return TypeTags.FLOAT_TAG;
    }

    public static BFloatType singletonType(Double value) {
        return new BFloatType(TypeConstants.FLOAT_TNAME, EMPTY_MODULE, Builder.floatConst(value));
    }

    @Override
    public boolean isReadOnly() {
        return true;
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

    @Override
    SemType createSemType() {
        return semType;
    }
}
