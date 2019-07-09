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
 *
 */

package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * This function which implements the startSpan method for observe.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "startSpan",
        args = {
                @Argument(name = "spanName", type = TypeKind.STRING),
                @Argument(name = "tags", type = TypeKind.MAP),
                @Argument(name = "parentSpanId", type = TypeKind.INT),
        },
        returnType = @ReturnType(type = TypeKind.INT),
        isPublic = true
)
public class StartSpan extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
//        String spanName = context.getStringArgument(0);
//        BMap tags = (BMap) context.getNullableRefArgument(0);
//        int parentSpanId = (int) context.getIntArgument(0);
//        if (parentSpanId < -1) {
//            context.setReturnValues(Utils
//                    .createError(context, "The given parent span ID " + parentSpanId + " is invalid."));
//        } else {
//            int spanId = OpenTracerBallerinaWrapper.getInstance()
//                    .startSpan(spanName, Utils.toStringMap(tags), parentSpanId, context);
//            if (spanId == -1) {
//                context.setReturnValues(Utils.createError(context,
//                        "No parent span for ID " + parentSpanId + " found. Please recheck the parent span Id"));
//            }
//            context.setReturnValues(new BInteger(spanId));
//        }
    }

    public static Object startSpan(Strand strand, String spanName, MapValue tags, long parentSpanId) {
        if (parentSpanId < -1) {
            return BallerinaErrors.createError("The given parent span ID " + parentSpanId + " is invalid.");
        } else {
            int spanId = OpenTracerBallerinaWrapper.getInstance().startSpan(spanName, Utils.toStringMap(tags),
                    (int) parentSpanId, strand);
            if (spanId == -1) {
                return BallerinaErrors.createError(
                        "No parent span for ID " + parentSpanId + " found. Please recheck the parent span Id");
            }

            return spanId;
        }
    }
}
