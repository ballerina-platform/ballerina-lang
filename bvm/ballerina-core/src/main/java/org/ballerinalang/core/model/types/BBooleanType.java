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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BBooleanType} represents boolean type in Ballerina.
 *
 * @since 0.8.0
 */
class BBooleanType extends BType {

    /**
     * Create a {@code BBooleanType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    BBooleanType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BBoolean.class);
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getZeroValue() {
        return (V) BBoolean.FALSE;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) BBoolean.FALSE;
    }

    @Override
    public int getTag() {
        return TypeTags.BOOLEAN_TAG;
    }
}
