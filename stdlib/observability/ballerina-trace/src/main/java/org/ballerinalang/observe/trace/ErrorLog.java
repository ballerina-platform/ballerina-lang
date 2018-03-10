/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.observe.trace;

import org.ballerinalang.bre.Context;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.log.AbstractLogFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.util.tracer.TraceConstant.EVENT_TYPE_ERROR;
import static org.ballerinalang.util.tracer.TraceConstant.KEY_ERROR_KIND;
import static org.ballerinalang.util.tracer.TraceConstant.KEY_EVENT_TYPE;
import static org.ballerinalang.util.tracer.TraceConstant.KEY_MESSAGE;
import static org.ballerinalang.util.tracer.TraceConstant.STR_TRUE;

/**
 * This function adds logs to a span.
 */
@BallerinaFunction(
        packageName = "ballerina.observe",
        functionName = "logError",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Span", structPackage = "ballerina.observe"),
        args = {@Argument(name = "errorKind", type = TypeKind.STRING),
                @Argument(name = "message", type = TypeKind.STRING)},
        isPublic = true
)
public class ErrorLog extends AbstractLogFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct span = (BStruct) getRefArgument(context, 0);
        String spanId = span.getStringField(0);
        String errorKind = getStringArgument(context, 0);
        String message = getStringArgument(context, 1);

        String pkg = context.getControlStack().currentFrame.prevStackFrame
                .getCallableUnitInfo().getPackageInfo().getPkgPath();

        if (LOG_MANAGER.getPackageLogLevel(pkg).value() <= BLogLevel.ERROR.value()) {
            String logMessage = String.format("[Tracing][Service: %s, Span: %s] ErrorKind: %s, Message: %s",
                    span.getStringField(1), span.getStringField(2), errorKind, message);
            getLogger(pkg).error(logMessage);
        }

        Map<String, String> logMap = new HashMap<>();
        logMap.put(KEY_EVENT_TYPE, EVENT_TYPE_ERROR);
        logMap.put(KEY_ERROR_KIND, errorKind);
        logMap.put(KEY_MESSAGE, message);
        OpenTracerBallerinaWrapper.getInstance().addTags(spanId, EVENT_TYPE_ERROR, STR_TRUE);
        OpenTracerBallerinaWrapper.getInstance().log(spanId, logMap);
        return VOID_RETURN;
    }
}
