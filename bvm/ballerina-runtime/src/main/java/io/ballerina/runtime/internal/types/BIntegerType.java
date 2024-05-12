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
import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.SemType.SubType;

import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;

/**
 * {@code BIntegerType} represents an integer which is a 32-bit signed number.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BIntegerType extends BType implements IntegerType, SemType {

    private final int tag;
    private final SemType semType;

    /**
     * Create a {@code BIntegerType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BIntegerType(String typeName, Module pkg) {
        this(typeName, pkg, TypeTags.INT_TAG, pickSemType(TypeTags.INT_TAG));
    }

    public BIntegerType(String typeName, Module pkg, int tag) {
        this(typeName, pkg, tag, pickSemType(tag));
    }

    private BIntegerType(String typeName, Module pkg, int tag, SemType semType) {
        super(typeName, pkg, Long.class);
        this.tag = tag;
        this.semType = semType;
    }

    public static BIntegerType singletonType(Long value) {
        return new BIntegerType(TypeConstants.INT_TNAME, PredefinedTypes.EMPTY_MODULE, TypeTags.INT_TAG,
                Builder.intConst(value));
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new Long(0);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new Long(0);
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    private static SemType pickSemType(int tag) {
        return switch (tag) {
            case TypeTags.INT_TAG -> Builder.intType();
            case TypeTags.SIGNED8_INT_TAG -> Builder.intRange(SIGNED8_MIN_VALUE, SIGNED8_MAX_VALUE);
            case TypeTags.SIGNED16_INT_TAG -> Builder.intRange(SIGNED16_MIN_VALUE, SIGNED16_MAX_VALUE);
            case TypeTags.SIGNED32_INT_TAG -> Builder.intRange(SIGNED32_MIN_VALUE, SIGNED32_MAX_VALUE);
            case TypeTags.UNSIGNED8_INT_TAG, TypeTags.BYTE_TAG -> Builder.intRange(0, UNSIGNED8_MAX_VALUE);
            case TypeTags.UNSIGNED16_INT_TAG -> Builder.intRange(0, UNSIGNED16_MAX_VALUE);
            case TypeTags.UNSIGNED32_INT_TAG -> Builder.intRange(0, UNSIGNED32_MAX_VALUE);
            default -> throw new UnsupportedOperationException("Unexpected int tag");
        };
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
