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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BServiceType;

/**
 * This class contains a set of utility methods required for runtime invoke async api testing.
 *
 * @since 2.0.0
 */
public class Async {

    public static long getA(Environment env, BObject obj) {
        invokeAsync(env, obj, "getA");
        return 0;
    }

    public static long getResourceA(Environment env, BObject obj) {
        invokeAsync(env, obj, "$gen$$getA$$0046");
        return 0;
    }

    public static long isolatedGetA(Environment env, BObject obj) {
        invokeMethodAsyncConcurrently(env, obj, "getA");
        return 0;
    }

    public static boolean isolatedClassIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean isolatedClassIsIsolatedFunction(BObject obj) {
        return isRemoteMethodIsolated(obj);
    }

    public static boolean isIsolatedFunctionWithName(BObject obj, BString method) {
        ObjectType objectType = obj.getType();
        return objectType.isIsolated() && objectType.isIsolated(method.getValue());
    }

    public static long nonIsolatedGetA(Environment env, BObject obj) {
        invokeMethodAsyncSequentially(env, obj, "getA");
        return 0;
    }

    public static long getNonIsolatedResourceA(Environment env, BObject obj) {
        invokeMethodAsyncConcurrently(env, obj, "$gen$$getA$$0046");
        return 0;
    }

    public static long getNonIsolatedResourceB(Environment env, BObject obj) {
        invokeMethodAsyncConcurrently(env, obj, "$gen$$getB$$0046");
        return 0;
    }

    public static boolean nonIsolatedClassIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean nonIsolatedClassIsIsolatedFunction(BObject obj) {
        return isRemoteMethodIsolated(obj);
    }

    public static long isolatedServiceGetA(Environment env, BObject obj) {
        invokeMethodAsyncConcurrently(env, obj, "$gen$$getA$$0046");
        return 0;
    }

    public static boolean isolatedServiceIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean isolatedServiceIsIsolatedFunction(BObject obj) {
        return isResourceMethodIsolated(obj);
    }

    public static long nonIsolatedServiceGetA(Environment env, BObject obj) {
        invokeMethodAsyncSequentially(env, obj, "$gen$$getA$$0046");
        return 0;
    }

    public static boolean nonIsolatedServiceIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean nonIsolatedServiceIsIsolatedFunction(BObject obj) {
        return isResourceMethodIsolated(obj);
    }

    public static Object callAsyncNullObject(Environment env) {
        invokeAsync(env, null, "getA");
        return 0;
    }

    public static Object callAsyncNullObjectMethod(Environment env, BObject obj) {
        invokeAsync(env, obj, null);
        return 0;
    }

    public static Object callAsyncInvalidObjectMethod(Environment env, BObject obj) {
        invokeAsync(env, obj, "foo");
        return 0;
    }

    public static Object callAsyncNullObjectSequentially(Environment env) {
        invokeAsync(env, null, "getA");
        return 0;
    }

    public static Object callAsyncNullObjectMethodSequentially(Environment env, BObject obj) {
        invokeAsync(env, obj, null);
        return 0;
    }

    public static Object callAsyncInvalidObjectMethodSequentially(Environment env, BObject obj) {
        invokeAsync(env, obj, "foo");
        return 0;
    }

    public static Object callAsyncNullObjectConcurrently(Environment env) {
        invokeAsync(env, null, "getA");
        return 0;
    }

    public static Object callAsyncNullObjectMethodConcurrently(Environment env, BObject obj) {
        invokeAsync(env, obj, null);
        return 0;
    }

    public static Object callAsyncInvalidObjectMethodConcurrently(Environment env, BObject obj) {
        invokeAsync(env, obj, "foo");
        return 0;
    }

    private static void invokeMethodAsyncSequentially(Environment env, BObject obj, String methodName) {
        Future future = env.markAsync();
        BFuture bFuture = env.getRuntime().invokeMethodAsyncSequentially(obj, methodName, null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
    }

    private static void invokeMethodAsyncConcurrently(Environment env, BObject obj, String methodName) {
        Future future = env.markAsync();
        BFuture bFuture = env.getRuntime().invokeMethodAsyncConcurrently(obj, methodName, null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
    }

    private static void invokeAsync(Environment env, BObject obj, String methodName) {
        Future future = env.markAsync();
        Object result = env.getRuntime().invokeMethodAsync(obj, methodName, null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        });
    }

    private static boolean isResourceMethodIsolated(BObject obj) {
        MethodType[] methods = ((BServiceType) obj.getType()).getResourceMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("$gen$$getA$$0046")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }

    private static boolean isRemoteMethodIsolated(BObject obj) {
        MethodType[] methods = obj.getType().getMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("getA")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }
}
