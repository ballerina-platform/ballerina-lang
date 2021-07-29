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

package org.ballerinalang.debugadapter.runtime;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.StringValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static io.ballerina.runtime.api.creators.TypeCreator.createErrorType;

/**
 * This class contains the set of runtime helper util methods to support debugger expression evaluation.
 * <p>
 * These utils methods must be class-loaded into the program JVM to evaluate
 * <ul>
 *  <li> functions
 *  <li> object methods
 *  <li> remote call actions
 *  <li> wait actions
 *  </ul>
 * invocations using the debugger expression evaluation engine.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class DebuggerRuntime {

    private static final String EVALUATOR_STRAND_NAME = "evaluator-strand";

    /**
     * Invokes Ballerina object methods in blocking manner.
     *
     * @param bObject    ballerina object instance
     * @param methodName name of the object method to be invoked
     * @param args       object method arguments
     * @return return values
     */
    public static Object invokeObjectMethod(BObject bObject, String methodName, Object... args) {
        try {
            Scheduler scheduler = new Scheduler(1, false);
            CountDownLatch latch = new CountDownLatch(1);
            final Object[] finalResult = new Object[1];
            final Object[] paramValues = args[0] instanceof Strand ? Arrays.copyOfRange(args, 1, args.length) : args;

            Function<?, ?> func = o -> bObject.call((Strand) (((Object[]) o)[0]), methodName, paramValues);
            Object resultFuture = scheduler.schedule(new Object[1], func, null, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    latch.countDown();
                    finalResult[0] = result;
                }

                @Override
                public void notifyFailure(BError error) {
                    latch.countDown();
                    finalResult[0] = error;
                }
            }, EVALUATOR_STRAND_NAME, null).result;

            scheduler.start();
            latch.await();
            return finalResult[0];
        } catch (Exception e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    /**
     * Invoke Ballerina functions in blocking manner.
     *
     * @param classLoader normal classLoader
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object invokeFunction(ClassLoader classLoader, String className, String methodName,
                                        Object... paramValues) {
        try {
            Scheduler scheduler = new Scheduler(1, false);
            Class<?> clazz = classLoader.loadClass(className);
            Method method = getMethod(methodName, clazz);

            Function<Object[], Object> func = args -> {
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BallerinaException(methodName + " function invocation failed: " + e.getMessage());
                }
            };

            final Object[] finalResult = new Object[1];
            CountDownLatch latch = new CountDownLatch(1);
            BFuture futureValue = scheduler.schedule(paramValues, func, null, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    latch.countDown();
                    finalResult[0] = result;
                }

                @Override
                public void notifyFailure(BError error) {
                    latch.countDown();
                    finalResult[0] = error;
                }
            }, new HashMap<>(), PredefinedTypes.TYPE_NULL, EVALUATOR_STRAND_NAME, null);

            scheduler.start();
            latch.await();
            return finalResult[0];
        } catch (Exception e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    /**
     * Creates and returns a new ballerina object instance.
     *
     * @param pkgOrg         org name of the module
     * @param pkgName        package name of the module
     * @param pkgVersion     package version of the module
     * @param objectTypeName type name of the class
     * @param fieldValues    field values
     * @return Ballerina object instance
     */
    public static Object createObjectValue(String pkgOrg, String pkgName, String pkgVersion, String objectTypeName,
                                           Object... fieldValues) {
        Module packageId = new Module(pkgOrg, pkgName, pkgVersion);
        return ValueCreator.createObjectValue(packageId, objectTypeName, fieldValues);
    }

    /**
     * Initializes and returns a Ballerina array instance with a given element type and the list of members.
     *
     * @param arrayElementType element type
     * @param values           member values
     * @return Ballerina array instance
     */
    public static Object getRestArgArray(Type arrayElementType, BValue... values) {
        ArrayType arrayType = TypeCreator.createArrayType(arrayElementType);
        if (values.length == 0) {
            return ValueCreator.createArrayValue(arrayType);
        } else if (values.length == 1) {
            if (values[0].getType().equals(arrayType)) {
                return values[0];
            }
        }
        return ValueCreator.createArrayValue(values, arrayType);
    }

    /**
     * Creates an error with given message, cause and details.
     *
     * @param message error message
     * @param cause   cause for the error
     * @param details error details
     * @return new error
     */
    public static Object createErrorValue(Object message, Object cause, Object... details) {
        if (!(message instanceof BString)) {
            return "incompatible types: expected 'string', found '" + getBTypeName(message) + "' for error message";
        } else if (cause != null && !(cause instanceof BError)) {
            return "incompatible types: expected 'error?', found '" + getBTypeName(cause) + "' for error cause";
        }

        List<BMapInitialValueEntry> errorDetailEntries = new ArrayList<>();
        for (int i = 0; i < details.length; i += 2) {
            errorDetailEntries.add(ValueCreator.createKeyFieldEntry(details[i], details[i + 1]));
        }

        ErrorType bErrorType = createErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage());
        BMap<BString, Object> errorDetailsMap = ValueCreator.createMapValue(PredefinedTypes.TYPE_ERROR_DETAIL,
                errorDetailEntries.toArray(errorDetailEntries.toArray(new BMapInitialValueEntry[0])));
        return ErrorCreator.createError(bErrorType, (StringValue) message, (ErrorValue) cause, errorDetailsMap);
    }

    private static Method getMethod(String functionName, Class<?> funcClass) throws NoSuchMethodException {
        Method declaredMethod = Arrays.stream(funcClass.getDeclaredMethods())
                .filter(method -> functionName.equals(method.getName()))
                .findAny()
                .orElse(null);

        if (declaredMethod != null) {
            return declaredMethod;
        } else {
            throw new NoSuchMethodException(functionName + " is not found");
        }
    }

    private static String getBTypeName(Object value) {
        if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Integer || value instanceof Long) {
            return "int";
        } else if (value instanceof Float || value instanceof Double) {
            return "float";
        } else if (value instanceof BValue) {
            return ((BValue) value).getType().getName();
        } else {
            return "unknown";
        }
    }

    private DebuggerRuntime() {
    }
}
