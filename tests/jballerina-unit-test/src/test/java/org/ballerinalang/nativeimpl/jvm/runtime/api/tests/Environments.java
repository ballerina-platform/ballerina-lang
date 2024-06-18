/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BString;

/**
 * This class contains a set of utility methods required for testing {@link Environment} API.
 *
 * @since 2201.5.0
 */
public final class Environments {

    private Environments() {
    }

    public static void callClientGetGreeting(Environment env, long p1, BString p2, boolean pn, BArray path, long t1,
                                             BString t2, boolean tn) {
        long returnVal = 1;
        Future balFuture = env.markAsync();
        String functionName = env.getFunctionName();
        Parameter[] pathParameters = env.getFunctionPathParameters();
        Parameter[] expectedPathParams = {
                new Parameter("p1", false, null, PredefinedTypes.TYPE_INT),
                new Parameter("p2", false, null, PredefinedTypes.TYPE_STRING),
                new Parameter("pn", false, null, PredefinedTypes.TYPE_BOOLEAN),
                new Parameter("path", false, null,
                        TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING))
        };
        String expectedFunctionNamePrefix = "$get$greeting$";
        assertFunctionNameAndPathParams(returnVal, balFuture, functionName, pathParameters, expectedPathParams,
                expectedFunctionNamePrefix);
    }

    private static void assertFunctionNameAndPathParams(long returnVal, Future balFuture, String functionName,
                                                        Parameter[] pathParameters, Parameter[] expectedPathParams,
                                                        String expectedFunctionNamePrefix) {
        if (functionName == null || !functionName.startsWith(expectedFunctionNamePrefix) || pathParameters == null
                || pathParameters.length != expectedPathParams.length) {
            balFuture.complete(-1);
            return;
        }

        for (int i = 0; i < expectedPathParams.length; i++) {
            if (!expectedPathParams[i].equals(pathParameters[i])) {
                returnVal = -1;
                break;
            }
        }
        balFuture.complete(returnVal);
    }

    public static void callClientPostGreeting(Environment env, long p1, BDecimal p2, float pn, long t1, BString t2,
                                              boolean tn) {
        long returnVal = 2;
        Future balFuture = env.markAsync();
        String functionName = env.getFunctionName();
        Parameter[] pathParameters = env.getFunctionPathParameters();
        Parameter[] expectedPathParams = {
                new Parameter("p1", false, null, PredefinedTypes.TYPE_INT),
                new Parameter("p2", false, null, PredefinedTypes.TYPE_DECIMAL),
                new Parameter("pn", false, null, PredefinedTypes.TYPE_FLOAT),
        };
        String expectedFunctionNamePrefix = "$post$greeting$";
        assertFunctionNameAndPathParams(returnVal, balFuture, functionName, pathParameters, expectedPathParams,
                expectedFunctionNamePrefix);
    }
}
