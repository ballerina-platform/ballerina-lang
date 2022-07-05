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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BValue;

/**
 * This is a temporary BType created to implement handle type related test cases.
 *
 * @since 1.0.0
 */
public class BHandleType extends BType {

    protected BHandleType(String typeName, String pkgPath, Class<? extends BValue> valueClass) {
        super(typeName, pkgPath, valueClass);
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
        return TypeTags.HANDLE_TAG;
    }
}
