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
import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.subtypedata.IntSubtype;

import java.util.Optional;

import static io.ballerina.runtime.api.TypeTags.INT_TAG;
import static io.ballerina.runtime.api.TypeTags.SIGNED16_INT_TAG;
import static io.ballerina.runtime.api.TypeTags.SIGNED32_INT_TAG;
import static io.ballerina.runtime.api.TypeTags.SIGNED8_INT_TAG;
import static io.ballerina.runtime.api.TypeTags.UNSIGNED16_INT_TAG;
import static io.ballerina.runtime.api.TypeTags.UNSIGNED32_INT_TAG;
import static io.ballerina.runtime.api.TypeTags.UNSIGNED8_INT_TAG;

/**
 * {@code BIntegerType} represents an integer which is a 32-bit signed number.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BIntegerType extends BType implements IntegerType {

    private final int tag;
    private final SemType semType;

    /**
     * Create a {@code BIntegerType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BIntegerType(String typeName, Module pkg) {
        super(typeName, pkg, Long.class);
        tag = INT_TAG;
        semType = null;
    }

    public BIntegerType(String typeName, Module pkg, SemType semType) {
        super(typeName, pkg, Long.class);
        tag = INT_TAG;
        this.semType = semType;
    }

    public BIntegerType(String typeName, Module pkg, int tag) {
        super(typeName, pkg, Long.class);
        this.tag = tag;
        semType = null;
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

    @Override
    public Optional<SemType> getSemTypeComponent() {
        if (semType != null) {
            return Optional.of(semType);
        }
        SemType ty = switch (tag) {
            case INT_TAG -> PredefinedType.INT;
            case SIGNED8_INT_TAG -> IntSubtype.intWidthSigned(8);
            case SIGNED16_INT_TAG -> IntSubtype.intWidthSigned(16);
            case SIGNED32_INT_TAG -> IntSubtype.intWidthSigned(32);
            case UNSIGNED8_INT_TAG -> IntSubtype.intWidthUnsigned(8);
            case UNSIGNED16_INT_TAG -> IntSubtype.intWidthUnsigned(16);
            case UNSIGNED32_INT_TAG -> IntSubtype.intWidthUnsigned(32);
            default -> throw new IllegalStateException("Unexpected tag value: " + tag);
        };
        return Optional.of(ty);
    }

    @Override
    public BType getBTypeComponent() {
        return (BType) PredefinedTypes.TYPE_NEVER;
    }
}
