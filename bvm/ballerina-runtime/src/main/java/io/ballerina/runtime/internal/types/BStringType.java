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
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.types.StringType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;

import static io.ballerina.runtime.api.TypeTags.CHAR_STRING_TAG;
import static io.ballerina.runtime.api.TypeTags.STRING_TAG;

/**
 * {@code BStringType} represents a String type in ballerina.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BStringType extends BType implements StringType {

    private final int tag;
    private final SemType semType;

    /**
     * Create a {@code BStringType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BStringType(String typeName, Module pkg) {
        super(typeName, pkg, String.class);
        tag = STRING_TAG;
        semType = null;
    }

    public BStringType(String typeName, Module pkg, SemType semType) {
        super(typeName, pkg, String.class);
        tag = STRING_TAG;
        this.semType = semType;
    }

    public BStringType(String typeName, Module pkg, int tag) {
        super(typeName, pkg, String.class);
        this.tag = tag;
        semType = null;
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
    public SemType getSemTypeComponent() {
        if (semType != null) {
            return semType;
        }
        return switch (tag) {
            case STRING_TAG -> PredefinedType.STRING;
            case CHAR_STRING_TAG -> PredefinedType.STRING_CHAR;
            default -> throw new IllegalStateException("Unexpected tag value: " + tag);
        };
    }

    @Override
    public BType getBTypeComponent() {
        return (BType) PredefinedTypes.TYPE_NEVER;
    }
}
