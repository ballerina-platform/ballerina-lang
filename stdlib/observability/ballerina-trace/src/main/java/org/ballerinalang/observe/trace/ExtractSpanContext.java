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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This function injects a span context and returns the spans Id.
 */
@BallerinaFunction(
        packageName = "ballerina.observe",
        functionName = "extractSpanContext",
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ExtractSpanContext extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {

        BStruct httpRequest = (BStruct) getRefArgument(context, 0);
        String group = getStringArgument(context, 0);

        HTTPCarbonMessage carbonMessage = HttpUtil.getCarbonMsg(httpRequest, null);
        Iterator<Map.Entry<String, String>> entryIterator = carbonMessage.getHeaders().iteratorAsString();
        Map<String, String> spanHeaders = new HashMap<>();

        while (entryIterator.hasNext()) {
            Map.Entry<String, String> headers = entryIterator.next();
            if (headers.getKey().startsWith(group)) {
                spanHeaders.put(headers.getKey().substring(group.length()), headers.getValue());
            } else {
                spanHeaders.put(headers.getKey(), headers.getValue());
            }
        }

        String spanId = OpenTracerBallerinaWrapper.getInstance().extract(spanHeaders);

        if (spanId != null) {
            return getBValues(new BString(spanId));
        } else {
            throw new BallerinaException("Can not use tracing API when tracing is disabled. " +
                    "Check tracing configurations and dependencies.");
        }
    }
}
