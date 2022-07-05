/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.ballerinalang.core.model.values.BIterator;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BIteratorType} represents the type of iterator in ballerina.
 *
 * @since 0.96.0
 */
public class BIteratorType extends BType {

    BIteratorType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BIterator.class);
    }

    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.ITERATOR_TAG;
    }
}
