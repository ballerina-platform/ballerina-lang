/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BValue;
import org.wso2.ballerinalang.util.Lists;


/**
 * {@code BSingletonType} represents a Singleton Type in Ballerina.
 */
public class BSingletonType extends BType {

    public BType superSetType;
    public BValue valueSpace;

    public BSingletonType(BType superSetType, BValue valueSpace) {
        super(null, null, BValue.class);
        this.superSetType = superSetType;
        this.valueSpace = valueSpace;
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
        return new TypeSignature(TypeSignature.SIG_SINGLETON, Lists.of(superSetType.getSig()));
    }

    @Override
    public int getTag() {
        return TypeTags.SINGLETON_TAG;
    }
}
