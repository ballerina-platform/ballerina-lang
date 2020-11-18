/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;

/**
 * Represents runtime type of an error.
 *
 * @since 0.983.0
 */
public class BErrorType extends BType {

    public BType reasonType;
    public BType detailType;

    public BErrorType(String typeName, BType reasonType, BType detailType, String pkgPath) {
        super(typeName, pkgPath, BError.class);
        this.reasonType = reasonType;
        this.detailType = detailType;
    }

    public BErrorType(BType reasonType, BType detailType) {
        super(TypeConstants.ERROR, null, BError.class);
        this.reasonType = reasonType;
        this.detailType = detailType;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.ERROR_TAG;
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
}
