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

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

/**
 * Helper methods to test properties of service values.
 *
 * @since 2.0.0
 */
public class ServiceValue {
    private static BObject service;
    private static BObject listener;
    private static boolean started;
    private static String[] names;
    private static MapValue annotationMap; // captured at attach method

    public static BFuture callMethod(Environment env, BObject l, BString name) {

        return env.getRuntime().invokeMethodAsyncConcurrently(l, name.getValue(), null, null, null, new HashMap<>(),
                                                              PredefinedTypes.TYPE_ANY);
    }

    public static BFuture callMethodWithParams(Environment env, BObject l, BString name, ArrayValue arrayValue) {
        Object[] args = new Object[arrayValue.size() * 2 ];
        for (int i = 0, j = 0; i < arrayValue.size(); i += 1, j += 2) {
            args[j] = arrayValue.get(i);
            args[j + 1] = true;
        }
        BFuture k = env.getRuntime().invokeMethodAsyncConcurrently(l, name.getValue(), null, null, null,
                                                                   new HashMap<>(),
                                                                   PredefinedTypes.TYPE_ANY, args);

        return k;
    }

    public static BArray getParamNames(BObject o, BString methodName) {
        ObjectType type = (ObjectType) o.getType();
        if (!(type instanceof ServiceType)) {
            return null;
        }

        for (ResourceMethodType attachedFunction : ((ServiceType) type).getResourceMethods()) {
            if (attachedFunction.getName().equals(methodName.getValue())) {
                String[] paramNames = attachedFunction.getParamNames();
                BArray arrayValue = ValueCreator.createArrayValue(
                        TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING, paramNames.length));
                for (int i = 0; i < paramNames.length; i++) {
                    String paramName = paramNames[i];
                    arrayValue.add(i, StringUtils.fromString(paramName));
                }
                return arrayValue;
            }
        }
        return null;
    }

    public static Object attach(BObject servObj, Object name) {
        ServiceValue.service = servObj;
        if (name == null) {
            names = null;
        } else if (name instanceof BString) {
            names = new String[1];
            names[0] = ((BString) name).getValue();
        } else {
            BArray array = (BArray) name;
            names = new String[array.size()];
            for (int i = 0; i < array.size(); i++) {
                names[i] = array.getBString(i).getValue();
            }
        }

        // Verify annotation availability
        BServiceType serviceType = (BServiceType) servObj.getType();
        try {
            Field annotations = BAnnotatableType.class.getDeclaredField("annotations");
            annotations.setAccessible(true);
            Object annotationMap = annotations.get(serviceType);
            ServiceValue.annotationMap = (MapValue) annotationMap;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError();
        }

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
        ServiceValue.names = new String[0];
        ServiceValue.annotationMap = new MapValueImpl();
    }

    public static BObject getListener() {
        return ServiceValue.listener;
    }

    public static BObject getService() {
        return ServiceValue.service;
    }

    public static BValue getResourceAnnotation(BString methodName, ArrayValue path, BString annotName) {
        String funcName = generateMethodName(methodName, path);

        for (var methodType : ((ServiceType) ServiceValue.service.getType()).getResourceMethods()) {
            if (methodType.getName().equals(funcName)) {
                return (BValue) methodType.getAnnotation(annotName);
            }
        }
        return null;
    }

    private static String generateMethodName(BString methodName, ArrayValue path) {
        StringBuilder funcName = new StringBuilder();
        funcName.append("$").append(methodName.getValue());
        for (int i = 0; i < path.size(); i++) {
            funcName.append("$").append(path.getBString(i).getValue());
        }
        return Utils.encodeFunctionIdentifier(funcName.toString());
    }

    public static BArray getServicePath() {
        return new ArrayValueImpl(names, false);
    }

    public static BMap getAnnotationsAtServiceAttach() {
        return ServiceValue.annotationMap;
    }

    public static BValue getResourceMethodAnnotations(BObject service, BString method, ArrayValue resourcePath,
                                                      BString annotName) {
        String methodName = generateMethodName(method, resourcePath);

        for (var methodType : ((ServiceType) service.getType()).getResourceMethods()) {
            if (methodType.getName().equals(methodName)) {
                return (BValue) methodType.getAnnotation(annotName);
            }
        }
        return null;
    }

    public static BArray getParamDefaultability(BObject service, BString name) {
        ServiceType serviceType = (ServiceType) service.getType();
        Optional<ResourceMethodType> func = Arrays.stream(serviceType.getResourceMethods())
                .filter(r -> r.getName().equals(name.getValue())).findAny();

        if (func.isEmpty()) {
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN, 0));
        }

        ResourceMethodType rt = func.get();

        int len = rt.getParamDefaultability().length;
        BArray arrayValue =
                ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN, len));
        for (int i = 0; i < len; i++) {
            boolean d = rt.getParamDefaultability()[i];
            arrayValue.add(i, d);
        }
        return arrayValue;
    }
}
