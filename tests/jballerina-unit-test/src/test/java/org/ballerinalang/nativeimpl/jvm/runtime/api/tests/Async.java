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
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BServiceType;

/**
 * This class contains a set of utility methods required for runtime invoke async api testing.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class Async {

    public static long getFieldValWithNoArgs(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithNoArgs");
    }

    public static long getFieldValWithRequiredArg(Environment env, BObject obj, long num) {
        return startIsolatedWorker(env, obj, "getFieldValWithRequiredArg", num);

    }

    public static long getFieldValWithOptionalArgDefaultVal(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithOptionalArg", StringUtils.fromString("any value here"));
    }

    public static long getFieldValWithMultipleOptionalArgsDefaultVal(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithMultipleOptionalArgs");
    }

    public static long getFieldValWithMultipleOptionalArgsDefaultValAsync(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithMultipleOptionalArgsAsync");
    }

    public static long getFieldValWithProvidedOptionalArgVal(Environment env, BObject obj, BString fieldName) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithOptionalArg", fieldName);
    }

    public static long getFieldValWithDefaultValSpecialChars(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithDefaultValSpecialChars", 0, 0, 0);
    }

    public static long getFieldValWithDefaultValSpecialCharsAsync(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getFieldValWithDefaultValSpecialCharsAsync", 0, 0, 0);
    }

    public static long getA(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "getA");
    }

    public static long getResourceA(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "$gen$$getA$&0046");
    }

    public static long isolatedGetA(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "getA");
    }

    public static boolean isolatedClassIsIsolated(BObject obj) {
        return ((BObjectType) obj.getOriginalType()).isIsolated();
    }

    public static boolean isolatedClassIsIsolatedFunction(BObject obj) {
        return isRemoteMethodIsolated(obj);
    }

    public static boolean isIsolatedFunctionWithName(BObject obj, BString method) {
        ObjectType objectType = ((BObjectType) obj.getOriginalType());
        return objectType.isIsolated() && objectType.isIsolated(method.getValue());
    }

    public static long nonIsolatedGetA(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "getA");
    }

    public static long getNonIsolatedResourceA(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "$gen$$getA$&0046");

    }

    public static long getNonIsolatedResourceB(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "$gen$$getB$&0046");
    }

    public static boolean nonIsolatedClassIsIsolated(BObject obj) {
        return ((BObjectType) obj.getOriginalType()).isIsolated();
    }

    public static boolean nonIsolatedClassIsIsolatedFunction(BObject obj) {
        return isRemoteMethodIsolated(obj);
    }

    public static long isolatedServiceGetA(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "$gen$$getA$&0046");

    }

    public static boolean isolatedServiceIsIsolated(BObject obj) {
        return ((BObjectType) obj.getOriginalType()).isIsolated();
    }

    public static boolean isolatedServiceIsIsolatedFunction(BObject obj) {
        return isResourceMethodIsolated(obj);
    }

    public static long nonIsolatedServiceGetA(Environment env, BObject obj) {
        return startNonIsolatedWorker(env, obj, "$gen$$getA$&0046");
    }

    public static boolean nonIsolatedServiceIsIsolated(BObject obj) {
        return ((BObjectType) obj.getOriginalType()).isIsolated();
    }

    public static boolean nonIsolatedServiceIsIsolatedFunction(BObject obj) {
        return isResourceMethodIsolated(obj);
    }

    public static Object callAsyncNullObject(Environment env) {
        return startIsolatedWorker(env, null, "getA");

    }

    public static Object callAsyncNullObjectMethod(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, null);
    }

    public static Object callAsyncInvalidObjectMethod(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "foo");
    }

    public static Object callAsyncNullObjectSequentially(Environment env) {
        return startIsolatedWorker(env, null, "getA");
    }

    public static Object callAsyncNullObjectMethodSequentially(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, null);
    }

    public static Object callAsyncInvalidObjectMethodSequentially(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "foo");
    }

    public static Object callAsyncNullObjectConcurrently(Environment env) {
        return startIsolatedWorker(env, null, "getA");
    }

    public static Object callAsyncNullObjectMethodConcurrently(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, null);
    }

    public static Object callAsyncInvalidObjectMethodConcurrently(Environment env, BObject obj) {
        return startIsolatedWorker(env, obj, "foo");
    }

    private static long startNonIsolatedWorker(Environment env, BObject obj, String methodName, Object... args) {
        env.markAsync();
        return (long) env.getRuntime().startNonIsolatedWorker(obj, methodName, methodName,
                null, null, args).get();
    }

    private static long startIsolatedWorker(Environment env, BObject obj, String methodName, Object... args) {
        env.markAsync();
        return (long) env.getRuntime().startNonIsolatedWorker(obj, methodName, methodName,
                null, null, args).get();
    }

    private static boolean isResourceMethodIsolated(BObject obj) {
        MethodType[] methods = ((BServiceType) obj.getOriginalType()).getResourceMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("$gen$$getA$&0046")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }

    private static boolean isRemoteMethodIsolated(BObject obj) {
        MethodType[] methods = ((BObjectType) obj.getOriginalType()).getMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("getA")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }
}
