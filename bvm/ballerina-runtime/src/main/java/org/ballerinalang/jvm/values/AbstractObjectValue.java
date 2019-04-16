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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.types.BType;

import java.util.Map;

/**
 * Abstract class to be extended by all the ballerina objects.
 * 
 * @since 0.995.0
 */
public abstract class AbstractObjectValue implements ObjectValue {

    private BType type;

    public AbstractObjectValue(BType type) {
        this.type = type;
    }

    public abstract Object call(String funcName, Object... args);

    public abstract Object get(String fieldName);

    public abstract void set(String fieldName, Object value);

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void stamp(BType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }
}
