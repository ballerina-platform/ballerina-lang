/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native implementation for get current call stack.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "runtime",
        functionName = "getErrorCallStackFrame"
)
public class GetErrorCallStackFrame extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        final BError errorStruct = (BError) context.getRefArgument(0);
        BValueArray errorStack = new BValueArray(errorStruct.getType());
        for (int i = 0; i < errorStruct.callStack.size(); i++) {
            errorStack.add(i, errorStruct.callStack.get(i));
        }
        context.setReturnValues(errorStack);
    }

    public static ArrayValue getErrorCallStackFrame(Strand strand, ErrorValue errorRecord) {
        ArrayValue errorStack = new ArrayValue(TypeChecker.getType(errorRecord));
        //TODO verify logic as the StackTraceElement of Throwable is used
        StackTraceElement[] stackTraceElements = errorRecord.getStackTrace();
        int index = 0;
        for (StackTraceElement stackTrace : stackTraceElements) {
            errorStack.add(index++, stackTrace);
        }
        return errorStack;
    }
}
