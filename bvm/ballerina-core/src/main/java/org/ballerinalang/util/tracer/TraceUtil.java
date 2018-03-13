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

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BStruct;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.util.tracer.TraceConstant.LOG_ERROR_KIND_EXCEPTION;
import static org.ballerinalang.util.tracer.TraceConstant.LOG_EVENT_TYPE_ERROR;
import static org.ballerinalang.util.tracer.TraceConstant.LOG_KEY_ERROR_KIND;
import static org.ballerinalang.util.tracer.TraceConstant.LOG_KEY_EVENT_TYPE;
import static org.ballerinalang.util.tracer.TraceConstant.LOG_KEY_MESSAGE;

/**
 * Utility call to perform trace related functions.
 */
public class TraceUtil {
    private TraceUtil() {

    }

    public static void finishTraceSpan(BTracer tracer) {
        if (tracer != null) {
            tracer.finishSpan();
        }
    }

    public static void finishTraceSpan(BTracer tracer, BStruct error) {
        if (tracer != null) {
            Map<String, Object> logProps = new HashMap<>();
            logProps.put(LOG_KEY_ERROR_KIND, LOG_ERROR_KIND_EXCEPTION);
            logProps.put(LOG_KEY_MESSAGE, BLangVMErrors.getPrintableStackTrace(error));
            logProps.put(LOG_KEY_EVENT_TYPE, LOG_EVENT_TYPE_ERROR);
            tracer.logError(logProps);
            finishTraceSpan(tracer);
        }
    }

    public static BTracer getParentTracer(WorkerExecutionContext ctx) {
        WorkerExecutionContext parent = ctx;
        while (parent.parent != null) {
            parent = parent.parent;
            if (parent.getTracer() != null) {
                return parent.getTracer();
            }
        }
        return null;
    }
}
