/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.RefValue;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 * 
 * @since 0.995.0
 */
public class BFiniteType extends BType {

    public Set<Object> valueSpace;

    public BFiniteType() {
        super(null, null, RefValue.class);
        this.valueSpace = new LinkedHashSet<>();
    }

    public BFiniteType(Set<Object> values) {
        super(null, null, RefValue.class);
        this.valueSpace = values;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.FINITE_TYPE_TAG;
    }
}
