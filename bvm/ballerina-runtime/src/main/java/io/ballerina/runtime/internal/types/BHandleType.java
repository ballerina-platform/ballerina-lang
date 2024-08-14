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
import io.ballerina.runtime.api.types.HandleType;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.internal.values.RefValue;

/**
 * {@code BHandleType} represents a handle type in Ballerina. A handle value is a reference to a storage managed
 * externally by a Ballerina program.
 *
 * @since 1.0.0
 */
public final class BHandleType extends BSemTypeWrapper<BHandleType.BHandleTypeImpl> implements HandleType {

    /**
     * Create a {@code BHandleType} which represents the handle type.
     *
     * @param typeName string name of the type
     */
    public BHandleType(String typeName, Module pkg) {
        super(() -> BHandleTypeImpl.create(typeName, pkg), typeName, Builder.handleType());
    }

    protected static final class BHandleTypeImpl extends BType implements HandleType {

        private static final BHandleTypeImpl DEFAULT =
                new BHandleTypeImpl(TypeConstants.HANDLE_TNAME, PredefinedTypes.EMPTY_MODULE);

        private static BHandleTypeImpl create(String typeName, Module pkg) {
            if (typeName.equals(TypeConstants.HANDLE_TNAME) && pkg == PredefinedTypes.EMPTY_MODULE) {
                return DEFAULT;
            }
            return new BHandleTypeImpl(typeName, pkg);
        }

        private BHandleTypeImpl(String typeName, Module pkg) {
            super(typeName, pkg, RefValue.class);
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
            return TypeTags.HANDLE_TAG;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

    }
}
