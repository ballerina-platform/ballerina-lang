/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.util.tracer.TraceConstant.ERROR_KIND_EXCEPTION;
import static org.ballerinalang.util.tracer.TraceConstant.KEY_ERROR_KIND;
import static org.ballerinalang.util.tracer.TraceConstant.KEY_ERROR_OBJECT;
import static org.ballerinalang.util.tracer.TraceConstant.KEY_MESSAGE;

/**
 * ConnectorFutureListener to be use with tracing.
 *
 * @since 0.963.1
 */
public class TraceFutureListener implements ConnectorFutureListener {
    private BTracer bTracer;

    public TraceFutureListener(BTracer bTracer) {
        this.bTracer = bTracer;
        this.bTracer.startSpan();
    }

    @Override
    public void notifySuccess() {
        finishSpan();
    }

    @Override
    public void notifyReply(BValue... response) {
        finishSpan();
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {

        Map<String, Object> fields = new HashMap<>();
        fields.put(KEY_ERROR_KIND, ERROR_KIND_EXCEPTION);
        fields.put(KEY_ERROR_OBJECT, ex);
        fields.put(KEY_MESSAGE, ex.getMessage());
        bTracer.logError(fields);

        finishSpan();
    }

    private void finishSpan() {
        bTracer.finishSpan();
    }
}
