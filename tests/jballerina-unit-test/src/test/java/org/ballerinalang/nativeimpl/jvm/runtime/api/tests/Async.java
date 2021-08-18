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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.types.BServiceType;

/**
 * This class contains a set of utility methods required for runtime api @{@link Runtime} testing.
 *
 * @since 2.0.0
 */
public class Async {

    public static long isolatedGetA(Environment env, BObject obj) {
        Future future = env.markAsync();
        env.getRuntime().invokeMethodAsync(obj, "getA", null, null, true, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
        return 0;
    }

    public static boolean isolatedClassIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean isolatedClassIsIsolatedFunction(BObject obj) {
        MethodType[] methods = obj.getType().getMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("getA")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }

    public static long nonIsolatedGetA(Environment env, BObject obj) {
        Future future = env.markAsync();
        env.getRuntime().invokeMethodAsync(obj, "getA", null, null, false, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
        return 0;
    }

    public static boolean nonIsolatedClassIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean nonIsolatedClassIsIsolatedFunction(BObject obj) {
        MethodType[] methods = obj.getType().getMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("getA")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }

    public static long isolatedServiceGetA(Environment env, BObject obj) {
        Future future = env.markAsync();
        env.getRuntime().invokeMethodAsync(obj, "$gen$$getA$$0046", null, null, true, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
        return 0;
    }

    public static boolean isolatedServiceIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean isolatedServiceIsIsolatedFunction(BObject obj) {
        MethodType[] methods = ((BServiceType) obj.getType()).getResourceMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("$gen$$getA$$0046")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }

    public static long nonIsolatedServiceGetA(Environment env, BObject obj) {
        Future future = env.markAsync();
        env.getRuntime().invokeMethodAsync(obj, "$gen$$getA$$0046", null, null, true, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
        return 0;
    }

    public static boolean nonIsolatedServiceIsIsolated(BObject obj) {
        return obj.getType().isIsolated();
    }

    public static boolean nonIsolatedServiceIsIsolatedFunction(BObject obj) {
        MethodType[] methods = ((BServiceType) obj.getType()).getResourceMethods();
        for (MethodType method : methods) {
            if (method.getName().equals("$gen$$getA$$0046")) {
                return method.isIsolated();
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("getA not found"));
    }

    public static Object callAsyncWithoutObject(Environment env) {
        Future future = env.markAsync();
        try {
            env.getRuntime().invokeMethodAsync(null, "getA", null, null, true, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    future.complete(result);
                }

                @Override
                public void notifyFailure(BError error) {
                    future.complete(error);
                }
            }, null, PredefinedTypes.TYPE_INT);
        } catch (BError e) {
            future.complete(e);
        }
        return 0;
    }

    public static Object callAsyncWithoutObjectMethod(Environment env, BObject obj) {
        Future future = env.markAsync();
        try {
            env.getRuntime().invokeMethodAsync(obj, null, null, null, true, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    future.complete(result);
                }

                @Override
                public void notifyFailure(BError error) {
                    future.complete(error);
                }
            }, null, PredefinedTypes.TYPE_INT);
        } catch (BError e) {
            future.complete(e);
        }
        return 0;
    }

    public static Object callAsyncInvalidObjectMethod(Environment env, BObject obj) {
        Future future = env.markAsync();
        env.getRuntime().invokeMethodAsync(obj, "foo", null, null, true, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                future.complete(result);
            }

            @Override
            public void notifyFailure(BError error) {
                future.complete(error);
            }
        }, null, PredefinedTypes.TYPE_INT);
        return 0;
    }
}
