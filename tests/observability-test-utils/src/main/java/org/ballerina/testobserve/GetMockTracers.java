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
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.ballerina.testobserve.extension.BMockSpan;
import org.ballerina.testobserve.extension.BMockTracerProvider;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
public class GetMockTracers {
    public static Object getMockTracers(Strand strand, String serviceName) {
        InMemorySpanExporter spanExporter = BMockTracerProvider.getExporterMap().get(serviceName);

        List<BMockSpan> mockSpans;
        if (spanExporter == null) {
            mockSpans = Collections.emptyList();
        } else {
            List<SpanData> finishedSpanList = spanExporter.getFinishedSpanItems();
            mockSpans = finishedSpanList.stream()
                    .map(spanData -> new BMockSpan(spanData.getName(),
                            spanData.getTraceId(),
                            spanData.getSpanId(),
                            spanData.getParentSpanId(),
                            spanData.getAttributes(),
                            spanData.getEvents()))
                    .collect(Collectors.toList());
        }
        return JSONParser.parse(new Gson().toJson(mockSpans));
    }
}
