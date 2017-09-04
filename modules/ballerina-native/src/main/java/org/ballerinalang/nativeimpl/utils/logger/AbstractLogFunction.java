/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.utils.logger;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.logging.LogContext;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

/**
 * {@code AbstractHTTPAction} is the base class for all the log functions
 *
 * @since 0.94
 */
public abstract class AbstractLogFunction extends AbstractNativeFunction {

    protected LogContext prepareLogContext(Context ctx) {
        long timestamp = System.currentTimeMillis();
        StackFrame stackFrame = ctx.getControlStackNew().getStack()[ctx.getControlStackNew().fp - 1];
        CallableUnitInfo callableUnitInfo = stackFrame.getCallableUnitInfo();

        String packageName = callableUnitInfo.getPackageInfo().getPkgPath();

        String location = "";
        if (callableUnitInfo instanceof FunctionInfo || callableUnitInfo instanceof ActionInfo) {
            location = callableUnitInfo.getName();
        } else if (callableUnitInfo instanceof ResourceInfo) {
            location = ((ResourceInfo) callableUnitInfo).getServiceInfo().getName() + "." + callableUnitInfo.getName();
        }

        WorkerInfo workerInfo = ctx.getWorkerInfo();
        String workerName = workerInfo != null ? workerInfo.getWorkerName() : "default";

        StringBuilder lineNumber = new StringBuilder();
        LineNumberInfo lineNumberInfo = callableUnitInfo.getPackageInfo().getLineNumberInfo(
                ctx.getBLangVM().getCurrentIP() - 1);
        lineNumber.append(lineNumberInfo.getFileName()).append(':').append(lineNumberInfo.getLineNumber());

        LogContext logCtx = new LogContext();
        logCtx.setLocation(location);
        logCtx.setPackageName(packageName);
        logCtx.setWorkerName(workerName);
        logCtx.setLineNumber(lineNumber.toString());
        logCtx.setTimestamp(timestamp);

        return logCtx;
    }
}
