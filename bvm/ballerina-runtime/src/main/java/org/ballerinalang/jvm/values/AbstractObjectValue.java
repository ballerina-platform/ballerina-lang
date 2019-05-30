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

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class to be extended by all the ballerina objects.
 * 
 * @since 0.995.0
 */
public abstract class AbstractObjectValue implements ObjectValue {

    private BObjectType type;

    private final HashMap<String, Object> nativeData = new HashMap<>();

    public AbstractObjectValue(BObjectType type) {
        this.type = type;
    }

    @Override
    public abstract Object call(Strand strand, String funcName, Object... args);

    @Override
    public abstract Object get(String fieldName);

    @Override
    public abstract void set(String fieldName, Object value);

    @Override
    public void addNativeData(String key, Object data) {
        this.nativeData.put(key, data);
    }

    @Override
    public Object getNativeData(String key) {
        return this.nativeData.get(key);
    }

    @Override
    public long getIntValue(String fieldName) {
        return (long) get(fieldName);
    }

    @Override
    public double getFloatValue(String fieldName) {
        return (double) get(fieldName);
    }

    @Override
    public String getStringValue(String fieldName) {
        return (String) get(fieldName);
    }

    @Override
    public boolean getBooleanValue(String fieldName) {
        return (boolean) get(fieldName);
    }

    @Override
    public MapValueImpl getMapValue(String fieldName) {
        return (MapValueImpl) get(fieldName);
    }

    @Override
    public ObjectValue getObjectValue(String fieldName) {
        return (ObjectValue) get(fieldName);
    }

    @Override
    public ArrayValue getArrayValue(String fieldName) {
        return (ArrayValue) get(fieldName);
    }

    @Override
    public BObjectType getType() {
        return type;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }
}
