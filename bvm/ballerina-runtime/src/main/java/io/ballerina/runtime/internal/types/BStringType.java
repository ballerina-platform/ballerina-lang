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
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.SemType.SubType;
import io.ballerina.runtime.api.types.StringType;

import java.util.List;

/**
 * {@code BStringType} represents a String type in ballerina.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BStringType extends BType implements StringType, SemType {

    private final int tag;
    private final SemType semType;

    /**
     * Create a {@code BStringType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BStringType(String typeName, Module pkg) {
        this(typeName, pkg, TypeTags.STRING_TAG, Builder.stringType());
    }

    public BStringType(String typeName, Module pkg, int tag) {
        this(typeName, pkg, tag, pickSemtype(tag));
    }

    private BStringType(String typeName, Module pkg, int tag, SemType semType) {
        super(typeName, pkg, String.class);
        this.tag = tag;
        this.semType = semType;
    }

    public static BStringType singletonType(String value) {
        return new BStringType(TypeConstants.STRING_TNAME, PredefinedTypes.EMPTY_MODULE, TypeTags.STRING_TAG,
                Builder.stringConst(value));
    }

    private static SemType pickSemtype(int tag) {
        return switch (tag) {
            case TypeTags.STRING_TAG -> Builder.stringType();
            case TypeTags.CHAR_STRING_TAG -> Builder.charType();
            default -> throw new IllegalStateException("Unexpected string type tag: " + tag);
        };
    }

    public <V extends Object> V getZeroValue() {
        return (V) RuntimeConstants.STRING_EMPTY_VALUE;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) RuntimeConstants.STRING_EMPTY_VALUE;
    }

    @Override
    public int getTag() {
        return tag;
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
