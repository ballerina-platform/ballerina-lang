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

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.observe.nativeimpl.OpenTracerBallerinaWrapper.ROOT_SPAN_INDICATOR;

/**
 * This function which implements the startSpan method for observe.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe", version = "0.8.0",
        functionName = "startRootSpan",
        args = {
                @Argument(name = "spanName", type = TypeKind.STRING),
                @Argument(name = "tags", type = TypeKind.MAP),
        },
        returnType = @ReturnType(type = TypeKind.INT),
        isPublic = true
)
public class StartRootSpan {
    public static long startRootSpan(Strand strand, BString spanName, Object tags) {
        return OpenTracerBallerinaWrapper.getInstance().startSpan(
                (String) strand.getProperty(ObservabilityConstants.SERVICE_NAME),
                spanName.getValue(), Utils.toStringMap((MapValue<BString, ?>) tags), ROOT_SPAN_INDICATOR, strand);
    }
}
