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

package org.ballerinalang.nativeimpl.observe;

import org.ballerinalang.bre.Context;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.log.AbstractLogFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.tracer.OpenTracerBallerinaWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * This function adds logs to a span.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "log",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Span", structPackage = "ballerina.observe"),
        args = {
                @Argument(name = "event", type = TypeKind.STRING),
                @Argument(name = "message", type = TypeKind.STRING)
        },
        returnType = @ReturnType(type = TypeKind.VOID),
        isPublic = true
)
public class Log extends AbstractLogFunction {
    @Override
    public void execute(Context context) {
        BStruct span = (BStruct) context.getRefArgument(0);
        String spanId = span.getStringField(2);
        String event = context.getStringArgument(0);
        String message = context.getStringArgument(1);

        String pkg = getPackagePath(context);

        if (LOG_MANAGER.getPackageLogLevel(pkg).value() <= BLogLevel.INFO.value()) {
            String logMessage = String.format("[Tracing] Service: %s, Span: %s, Event: %s, Message: %s",
                    span.getStringField(0), span.getStringField(1), event, message);
            getLogger(pkg).info(logMessage);
        }

        Map<String, String> logMap = new HashMap<>();
        logMap.put("event", event);
        logMap.put("message", message);
        OpenTracerBallerinaWrapper.getInstance().log(spanId, logMap);
    }
}
