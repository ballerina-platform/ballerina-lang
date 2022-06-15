/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.function;

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;

import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FUNCTION_LANG_LIB;
import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.function:call(function func, any|error... args).
 *
 * @since 2201.2.0
 */
public class Call {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, FUNCTION_LANG_LIB,
                                                                      "1.0.0", "call");

    public static Object call(BFunctionPointer<Object, Object> func, Object... args) {
        BFunctionType functionType = (BFunctionType) func.getType();
        Parameter[] parameters = functionType.parameters;
        BArrayType restType = (BArrayType) functionType.restType;
        int numOfParams = parameters.length;
        int numOfArgs = args.length;
        int numOfRestArgs = Math.max(numOfArgs - numOfParams, 0);
        if (numOfRestArgs > 0 && restType == null) {
            throw createError(BallerinaErrorReasons.INVALID_INVOCATION,
                              BLangExceptionHelper.getErrorDetails(RuntimeErrors.TOO_MANY_ARGS_FUNC_CALL));
        }

        List<Object> argsList = new java.util.ArrayList<>(List.of(Scheduler.getStrand()));

        for (int i = 0; i < numOfParams; i++) {
            Parameter parameter = parameters[i];
            if (i >= numOfArgs) {
                if (parameter.isDefault) {
                    argsList.add(0);
                    argsList.add(false);
                    continue;
                }
                throw createError(BallerinaErrorReasons.INVALID_INVOCATION,
                        BLangExceptionHelper.getErrorDetails(RuntimeErrors.MISSING_REQUIRED_PARAMETER));
            }
            Object arg = args[i];
            Type paramType = parameter.type;
            if (!TypeChecker.checkIsType(arg, paramType)) {
                throw ErrorCreator.createError(
                        getModulePrefixedReason(FUNCTION_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        BLangExceptionHelper.getErrorDetails(RuntimeErrors.INCOMPATIBLE_TYPE, paramType,
                                                             TypeChecker.getType(arg)));
            }
            argsList.add(arg);
            argsList.add(true);
        }
        if (restType != null) {
            ListInitialValueEntry.ExpressionEntry[] initialValues =
                                           createInitialValuesOfRestArg(args, numOfRestArgs, restType.getElementType());
            argsList.add(new ArrayValueImpl(restType, -1L, initialValues));
            argsList.add(true);
        }

        return func.asyncCall(argsList.toArray(), METADATA);
    }

    private static ListInitialValueEntry.ExpressionEntry[] createInitialValuesOfRestArg(Object[] args, int size,
                                                                                        Type elementType) {
        ListInitialValueEntry.ExpressionEntry[] initialValues = new ListInitialValueEntry.ExpressionEntry[size];
        for (int i = 0; i < size; i++) {
            Object arg = args[args.length - size + i];
            if (!TypeChecker.checkIsType(arg, elementType)) {
                throw ErrorCreator.createError(
                        getModulePrefixedReason(FUNCTION_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        BLangExceptionHelper.getErrorDetails(RuntimeErrors.INCOMPATIBLE_TYPE, elementType,
                                                             TypeChecker.getType(arg)));
            }
            initialValues[i] = new ListInitialValueEntry.ExpressionEntry(arg);
        }
        return initialValues;
    }
}
