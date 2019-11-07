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
package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.HashMap;

/**
 * <p>
 * Interface to be implemented by all the ballerina objects.
 * </p>
 *
 * @since 1.1.0
 */
public interface BObject extends ObjectValue {

    Object call(Strand strand, String funcName, Object... args);

    FutureValue start(Strand strand, String funcName, Object... args);

    BObjectType getType();

    Object get(String fieldName);

    long getIntValue(String fieldName);

    double getFloatValue(String fieldName);

    String getStringValue(String fieldName);

    boolean getBooleanValue(String fieldName);

    MapValueImpl getMapValue(String fieldName);

    BObject getObjectValue(String fieldName);

    ArrayValue getArrayValue(String fieldName);

    void addNativeData(String key, Object data);

    Object getNativeData(String key);

    HashMap<String, Object> getNativeData();

    void set(String fieldName, Object value);
}
