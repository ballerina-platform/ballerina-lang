/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BTableType} represents a type of a table in Ballerina.
 * <p>
 * This is created to support BRunUtil class only
 * <p>
 *
 * @since slp8
 */
public class BTableType extends BType implements BIndexedType {

    private BType constraint;

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, BMap.class);
        this.constraint = constraint;
    }

    @Override
    public BType getElementType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return (V) new BMap(this);
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public String toString() {
        if (constraint == BTypes.typeAny) {
            return super.toString();
        } else {
            return "table" + "<" + constraint.getName() + ">";
        }
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }
}
