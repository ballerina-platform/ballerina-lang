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

package org.ballerinalang.util.tracer;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;

import java.util.HashMap;

import static org.ballerinalang.util.tracer.TraceConstants.KEY_TRACER;

/**
 * Utility call to perform trace related functions.
 *
 * @since 0.964.1
 */
public class TraceUtil {
    private TraceUtil() {
    }

    public static void finishTraceSpan(BTracer tracer) {
        tracer.finishSpan();
    }

    public static void setTracer(WorkerExecutionContext ctx, BTracer tracer) {
        if (ctx.localProps == null) {
            ctx.localProps = new HashMap<>();
        }
        ctx.localProps.put(KEY_TRACER, tracer);
    }

    public static BTracer getTracer(WorkerExecutionContext ctx) {
        if (ctx.localProps != null) {
            return (BTracer) ctx.localProps.get(KEY_TRACER);
        }
        return null;
    }
}
