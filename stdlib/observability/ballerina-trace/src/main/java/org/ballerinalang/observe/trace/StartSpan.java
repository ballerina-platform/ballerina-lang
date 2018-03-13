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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This function which implements the startSpan method for tracing.
 */
@BallerinaFunction(
        packageName = "ballerina.observe",
        functionName = "init",
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class StartSpan extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        String serviceName = context.getStringArgument(0);
        String spanName = context.getStringArgument(1);
        BMap tags = (BMap) context.getRefArgument(0);
        String reference = context.getRefArgument(1).stringValue();
        String parentSpanId = ReferenceType.valueOf(reference) == ReferenceType.ROOT ?
                OpenTracerBallerinaWrapper.ROOT_CONTEXT : context.getStringArgument(2);

        long invocationId;
        if (context.getProperties().get(Constants.INVOCATION_ID_PROPERTY) != null) {
            invocationId = (Long) context.getProperties().get(Constants.INVOCATION_ID_PROPERTY);
        } else {
            invocationId = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
            context.setProperty(Constants.INVOCATION_ID_PROPERTY, invocationId);
        }

        String spanId = OpenTracerBallerinaWrapper.getInstance().startSpan(String.valueOf(invocationId), serviceName,
                spanName, Utils.toStringMap(tags), ReferenceType.valueOf(reference), parentSpanId);

        if (spanId != null) {
            context.setReturnValues(new BString(spanId));
        } else {
            throw new BallerinaException("Can not use tracing API when tracing is disabled. " +
                    "Check tracing configurations and dependencies.");
        }
    }
}
