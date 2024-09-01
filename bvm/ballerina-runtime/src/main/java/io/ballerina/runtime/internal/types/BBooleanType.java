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
import io.ballerina.runtime.api.types.BooleanType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.function.Supplier;

/**
 * {@code BBooleanType} represents boolean type in Ballerina.
 *
 * @since 0.995.0
 */
public final class BBooleanType extends BSemTypeWrapper<BBooleanType.BBooleanTypeImpl> implements BooleanType {

    private static final BBooleanType TRUE =
            new BBooleanType(() -> new BBooleanTypeImpl(TypeConstants.BOOLEAN_TNAME, PredefinedTypes.EMPTY_MODULE),
                    TypeConstants.BOOLEAN_TNAME, Builder.booleanConst(true));
    private static final BBooleanType FALSE =
            new BBooleanType(() -> new BBooleanTypeImpl(TypeConstants.BOOLEAN_TNAME, PredefinedTypes.EMPTY_MODULE),
                    TypeConstants.BOOLEAN_TNAME, Builder.booleanConst(false));

    /**
     * Create a {@code BBooleanType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BBooleanType(String typeName, Module pkg) {
        this(() -> new BBooleanTypeImpl(typeName, pkg), typeName, Builder.booleanType());
    }

    public static BBooleanType singletonType(boolean value) {
        return value ? TRUE : FALSE;
    }

    private BBooleanType(Supplier<BBooleanTypeImpl> bTypeSupplier, String typeName, SemType semType) {
        super(new ConcurrentLazySupplier<>(bTypeSupplier), typeName, TypeTags.BOOLEAN_TAG, semType);
    }

    protected static final class BBooleanTypeImpl extends BType implements BooleanType {

        private BBooleanTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, Boolean.class);
        }

        @Override
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
    }
}
