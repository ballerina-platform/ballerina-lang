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

import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * {@code BJSONType} represents a JSON value.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BJSONType extends BType {

    /**
     * Create a {@code BJSONType} which represents the JSON type.
     *
     * @param typeName string name of the type
     * @param pkg of the type
     */
    public BJSONType(String typeName, BPackage pkg) {
        super(typeName, pkg, MapValueImpl.class);
    }

    public BJSONType() {
        super(TypeConstants.JSON_TNAME, null, MapValueImpl.class);
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new MapValueImpl<>(this);
    }

    @Override
    public int getTag() {
        return TypeTags.JSON_TAG;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof BJSONType;
    }

    public boolean isNilable() {
        return true;
    }
}
