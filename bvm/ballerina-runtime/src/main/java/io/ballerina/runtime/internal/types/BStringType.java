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
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.StringType;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.SemType;

/**
 * {@code BStringType} represents a String type in ballerina.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class BStringType extends BSemTypeWrapper implements StringType {

    protected final String typeName;
    // We are creating separate empty module instead of reusing PredefinedTypes.EMPTY_MODULE to avoid cyclic
    // dependencies.
    private static final BStringTypeImpl DEFAULT_B_TYPE =
            new BStringTypeImpl(TypeConstants.STRING_TNAME, new Module(null, null, null), TypeTags.STRING_TAG);

    /**
     * Create a {@code BStringType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    public BStringType(String typeName, Module pkg) {
        this(new BStringTypeImpl(typeName, pkg, TypeTags.STRING_TAG), Builder.stringType());
    }

    public BStringType(String typeName, Module pkg, int tag) {
        this(new BStringTypeImpl(typeName, pkg, tag), pickSemtype(tag));
    }

    private BStringType(BStringTypeImpl bType, SemType semType) {
        super(bType, semType);
        this.typeName = bType.typeName;
    }

    public static BStringType singletonType(String value) {
        try {
            return new BStringType((BStringTypeImpl) DEFAULT_B_TYPE.clone(), Builder.stringConst(value));
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private static SemType pickSemtype(int tag) {
        return switch (tag) {
            case TypeTags.STRING_TAG -> Builder.stringType();
            case TypeTags.CHAR_STRING_TAG -> Builder.charType();
            default -> throw new IllegalStateException("Unexpected string type tag: " + tag);
        };
    }

    private static final class BStringTypeImpl extends BType implements StringType, Cloneable {

        private final int tag;

        private BStringTypeImpl(String typeName, Module pkg, int tag) {
            super(typeName, pkg, String.class);
            this.tag = tag;
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
        protected Object clone() throws CloneNotSupportedException {
            BType bType = (BType) super.clone();
            bType.setCachedImpliedType(null);
            bType.setCachedReferredType(null);
            return bType;
        }
    }
}
