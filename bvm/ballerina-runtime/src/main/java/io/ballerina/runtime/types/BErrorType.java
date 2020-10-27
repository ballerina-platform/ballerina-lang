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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.values.ErrorValue;

/**
 * {@code BErrorType} represents error type in Ballerina.
 *
 * @since 0.995.0
 */
public class BErrorType extends BAnnotatableType implements ErrorType {

    public Type detailType;
    public BTypeIdSet typeIdSet;

    public BErrorType(String typeName, Module pkg, Type detailType) {
        super(typeName, pkg, ErrorValue.class);
        this.detailType = detailType;
    }

    public BErrorType(String typeName, Module pkg) {
        super(typeName, pkg, ErrorValue.class);
    }

    public void setTypeIdSet(BTypeIdSet typeIdSet) {
        this.typeIdSet = typeIdSet;
    }

    @Override
    public <V> V getZeroValue() {
        return null;
    }

    @Override
    public <V> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.ERROR_TAG;
    }

    public void setDetailType(Type detailType) {
        this.detailType = detailType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BErrorType)) {
            return false;
        }

        BErrorType other = (BErrorType) obj;
        if (detailType == other.detailType) {
            return true;
        }

        return detailType.equals(other.detailType);
    }

    @Override
    public String getAnnotationKey() {
        return typeName;
    }

    public Type getDetailType() {
        return detailType;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
