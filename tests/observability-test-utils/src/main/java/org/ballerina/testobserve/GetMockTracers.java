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

package org.ballerina.testobserve;

import com.google.gson.Gson;
import io.opentracing.mock.MockTracer;
import org.ballerina.testobserve.extension.BMockTracer;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.List;

/**
 * This function returns the span context of a given span.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "testobserve",
        functionName = "getMockTracers",
        returnType = {@ReturnType(type = TypeKind.JSON)},
        isPublic = true
)
public class GetMockTracers extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        List<MockTracer> mockTracers = BMockTracer.getTracerMap();
        List<BMockSpan> mockSpans = new ArrayList<>();
        mockTracers
                .forEach(mockTracer -> mockTracer.finishedSpans()
                        .forEach(mockSpan -> mockSpans
                                .add(new BMockSpan(mockSpan.operationName(), mockSpan.context().traceId(),
                                        mockSpan.context().spanId(), mockSpan.parentId(), mockSpan.tags()))));
        context.setReturnValues(new BJSON(new Gson().toJson(mockSpans)));
    }
}
