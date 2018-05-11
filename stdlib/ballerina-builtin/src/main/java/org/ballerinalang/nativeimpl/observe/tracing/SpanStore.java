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

package org.ballerinalang.nativeimpl.observe.tracing;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold the spans of a trace when using open tracing ballerina api.
 */
public class SpanStore {

    private Map<String, BSpan> bSpans;
    //Invocation Id, SpanId
    private Map<String, String> activeSpanMap;

    public SpanStore() {
        bSpans = new HashMap<>();
        activeSpanMap = new HashMap<>();
    }

    BSpan getSpan(String spanId) {
        return bSpans.get(spanId);
    }

    void addSpan(BSpan span) {
        bSpans.put(span.getSpanId(), span);
        activeSpanMap.put(span.getInvocationId(), span.getSpanId());
    }

    BSpan finishAndRemoveSpan(String invocationId, String spanId) {
        if (spanId.equals(activeSpanMap.get(invocationId))) {
            activeSpanMap.remove(invocationId);
            BSpan parentBSpan = bSpans.get(spanId).getParentSpan();
            if (parentBSpan != null) {
                activeSpanMap.put(parentBSpan.getInvocationId(), parentBSpan.getSpanId());
            }
        }
        return bSpans.remove(spanId);
    }

    public BSpan getActiveBSpan(String invocationId) {
        return bSpans.get(activeSpanMap.get(invocationId));
    }
}
