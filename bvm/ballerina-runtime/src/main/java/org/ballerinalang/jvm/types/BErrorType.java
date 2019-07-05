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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.ErrorValue;

/**
 * {@code BErrorType} represents error type in Ballerina.
 *
 * @since 0.995.0
 */
public class BErrorType extends AnnotatableType {

    public BType reasonType;
    public BType detailType;

    public BErrorType(String typeName, BPackage pkg, BType reasonType, BType detailType) {
        super(typeName, pkg, ErrorValue.class);
        this.reasonType = reasonType;
        this.detailType = detailType;
    }

    public BErrorType(String typeName, BPackage pkg, BType reasonType) {
        super(typeName, pkg, ErrorValue.class);
        this.reasonType = reasonType;
    }

    public BErrorType(BType reasonType) {
        super(TypeConstants.ERROR, null, ErrorValue.class);
        this.reasonType = reasonType;
        this.detailType = detailType;
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

    public void setDetailType(BType detailType) {
        this.detailType = detailType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BErrorType)) {
            return false;
        }

        BErrorType other = (BErrorType) obj;
        if (reasonType == other.reasonType && detailType == other.detailType) {
            return true;
        }

        return reasonType.equals(other.reasonType) && detailType.equals(other.detailType);
    }

    @Override
    public String getAnnotationKey() {
        return typeName;
    }

    public BType getDetailType() {
        return detailType;
    }
}
