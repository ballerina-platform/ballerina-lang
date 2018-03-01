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

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;

/**
 * ConnectorFutureListener to be use with tracing.
 */
public class TraceConnectorFutureListener implements ConnectorFutureListener {
    private Context context;
    private String traceSpanId;
    private TraceContext traceContext;

    public TraceConnectorFutureListener(Context context, String traceSpanId) {
        this.context = context;
        this.traceSpanId = traceSpanId;
        this.traceContext = context.getActiveTraceContext();
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
        finishSpan();
    }

    private void finishSpan() {
        BallerinaTracerManager.getInstance().finishSpan(traceContext, traceSpanId);
    }
}
