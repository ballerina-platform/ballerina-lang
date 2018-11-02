/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BJSONType} represents a JSON value.
 *
 * @since 0.8.0
 */
public class BJSONType extends BType {

    private BType constraint;

    /**
     * Create a {@code BJSONType} which represents the JSON type.
     *
     * @param typeName string name of the type
     * @param pkgPath of the type
     */
    public BJSONType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BRefType.class);
    }

    public BJSONType(BType constraint) {
        super(TypeConstants.JSON_TNAME, null, BRefType.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BMap();
    }

    @Override
    public int getTag() {
        return TypeTags.JSON_TAG;
    }

    @Override
    public String toString() {
        if (constraint == null) {
            return super.toString();
        } else {
            return "json" + "<" + constraint.getName() + ">";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BJSONType)) {
            return false;
        }

        BJSONType other = (BJSONType) obj;
        if (constraint == other.constraint) {
            return true;
        } else if (constraint == null || other.constraint == null) {
            return false;
        }

        return constraint.equals(other.constraint);
    }
}
