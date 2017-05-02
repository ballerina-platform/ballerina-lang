/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.model.values;


import org.ballerinalang.model.types.BType;

/**
 * The {@code BTypeValue} represents a Type value in Ballerina.
 *
 * @since 0.8.6
 */
public class BTypeValue implements BRefType<BType> {

    private BType typeValue;

    public BTypeValue() {
        this(null);
    }

    public BTypeValue(BType typeValue) {
        this.typeValue = typeValue;
    }

    @Override
    public BType value() {
        return typeValue;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return value();
    }
}
