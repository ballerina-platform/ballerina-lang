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

package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BValue;

import java.util.HashSet;
import java.util.Set;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 */
public class BFiniteType extends BType {

    public Set<BValue> valueSpace;

    public BFiniteType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BValue.class);
        this.valueSpace = new HashSet<>();
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
    public TypeSignature getSig() {
        String packagePath = (pkgPath == null) ? "." : pkgPath;
        return new TypeSignature(TypeSignature.SIG_FINITE_TYPE, packagePath, typeName);
    }

    @Override
    public int getTag() {
        return TypeTags.FINITE_TYPE_TAG;
    }
}
