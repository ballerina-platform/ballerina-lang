/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.servicetests;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.MapValue;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Helper methods to test service annotations.
 *
 * @since 2.0.0
 */
public class ServiceAnnotValue {
    static HashMap<Integer, MapValue> serviceAnnotMap = new HashMap();
    private static BObject listener;
    private static boolean started;
    private static int serviceCount = 0;

    public static Object attach(BObject servObj, Object name) {
        // get service annotation and add it to the hash map
        BServiceType serviceType = (BServiceType) servObj.getType();
        try {
            Field annotations = BAnnotatableType.class.getDeclaredField("annotations");
            annotations.setAccessible(true);
            MapValue annotationMap = (MapValue) annotations.get(serviceType);
            serviceCount++;
            serviceAnnotMap.put(serviceCount, annotationMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError();
        }

        return null;
    }

    public static Object start(BObject listener) {
        ServiceAnnotValue.started = true;
        return null;
    }

    public static Object listenerInit(BObject listener) {
        ServiceAnnotValue.listener = listener;
        return null;
    }

    public static void reset() {
        ServiceAnnotValue.listener = null;
        ServiceAnnotValue.started = false;
        ServiceAnnotValue.serviceAnnotMap = new HashMap<>();
        ServiceAnnotValue.serviceCount = 0;
    }

    public static BObject getListener() {
        return ServiceAnnotValue.listener;
    }

    public static int getServiceCount() {
        return serviceCount;
    }

    public static BMap getAnnotationsAtServiceAttach(int serviceNum) {
        return serviceAnnotMap.get(serviceNum);
    }
}
