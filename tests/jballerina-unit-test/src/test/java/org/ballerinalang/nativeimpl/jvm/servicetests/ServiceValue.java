/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.values.FutureValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.ObjectValue;

/**
 * Helper methods to test properties of service values.
 *
 * @since 2.0
 */
public class ServiceValue {
    private static BObject service;
    private static BObject listener;
    private static boolean started;

    public static FutureValue callMethod(Environment env, ObjectValue l, BString name) {
        FutureValue k = env.getRuntime().invokeMethodAsync(l, name.getValue(), null, null, null, new MapValueImpl<>(),
                PredefinedTypes.TYPE_ANY);

        return k;
    }

    public static Object attach(BObject servObj) {
        ServiceValue.service = servObj;
        return null;
    }

    public static Object start(BObject listener) {
        ServiceValue.started = true;
        return null;
    }

    public static Object listenerInit(BObject listener) {
        ServiceValue.listener = listener;
        return null;
    }

    public static void reset() {
        ServiceValue.service = null;
        ServiceValue.listener = null;
        ServiceValue.started = false;
    }

    public static BObject getListener() {
        return ServiceValue.listener;
    }

    public static BObject getService() {
        return ServiceValue.service;
    }
}
