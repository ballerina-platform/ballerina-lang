/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.types.ReadonlyType;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.ConcurrentLazySupplier;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.values.RefValue;

/**
 * {@code BReadonlyType} represents the shapes that have their read-only bit on.
 *
 * @since 1.3.0
 */
public final class BReadonlyType extends BSemTypeWrapper<BReadonlyType.BReadonlyTypeImpl> implements ReadonlyType {

    private static final BasicTypeBitSet BASIC_TYPE;
    static {
        SemType roType = Builder.getReadonlyType();
        BASIC_TYPE = new BasicTypeBitSet(roType.all() | roType.some());
    }

    public BReadonlyType(String typeName, Module pkg) {
        super(new ConcurrentLazySupplier<>(() -> new BReadonlyTypeImpl(typeName, pkg)), typeName, pkg,
                TypeTags.READONLY_TAG, Builder.getReadonlyType());
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }

    protected static final class BReadonlyTypeImpl extends BType implements ReadonlyType {

        private BReadonlyTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, RefValue.class, false);
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
            return TypeTags.READONLY_TAG;
        }

        public boolean isNilable() {
            return true;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public BasicTypeBitSet getBasicType() {
            return BASIC_TYPE;
        }
    }
}
