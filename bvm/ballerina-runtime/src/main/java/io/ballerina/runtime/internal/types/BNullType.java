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
import io.ballerina.runtime.api.types.NullType;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.function.Supplier;

/**
 * {@code BNullType} represents the type of a {@code NullLiteral}.
 *
 * @since 0.995.0
 */
public class BNullType extends BSemTypeWrapper<BNullType.BNullTypeImpl> implements NullType {

    /**
     * Create a {@code BNullType} represents the type of a {@code NullLiteral}.
     *
     * @param typeName string name of the type
     * @param pkg package path
     */
    public BNullType(String typeName, Module pkg) {
        this(() -> new BNullTypeImpl(typeName, pkg), typeName, Builder.nilType());
    }

    protected BNullType(String typeName, Module pkg, SemType semType) {
        this(() -> new BNullTypeImpl(typeName, pkg), typeName, semType);
    }

    private BNullType(Supplier<BNullTypeImpl> bNullType, String typeName, SemType semType) {
        super(bNullType, typeName, semType);
    }

    protected static final class BNullTypeImpl extends BType implements NullType {

        private BNullTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, null);
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
            return TypeTags.NULL_TAG;
        }

        @Override
        public boolean isNilable() {
            return true;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public SemType createSemType() {
            return Builder.nilType();
        }
    }
}
