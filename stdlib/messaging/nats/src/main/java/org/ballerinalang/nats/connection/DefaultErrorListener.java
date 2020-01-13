/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.connection;

import io.nats.client.Connection;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import org.ballerinalang.nats.observability.NatsMetricsUtil;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ErrorListener to track the status of a {@link Connection Connection}.
 *
 * @since 1.0.0
 */
public class DefaultErrorListener implements ErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultErrorListener.class);

    DefaultErrorListener() {
    }

    @Override
    public void errorOccurred(Connection conn, String error) {
        String message = "Error in server " + conn.getConnectedUrl() + ". " + error;
        NatsMetricsUtil.reportConnectionError(conn.getConnectedUrl(), NatsObservabilityConstants.UNKNOWN);
        LOG.error(message);
    }

    @Override
    public void exceptionOccurred(Connection conn, Exception exp) {
        LOG.error("Exception in server " + conn.getConnectedUrl() + exp.getMessage());
        NatsMetricsUtil.reportConnectionError(conn.getConnectedUrl(), NatsObservabilityConstants.ERROR_TYPE_EXCEPTION);
    }

    @Override
    public void slowConsumerDetected(Connection conn, Consumer consumer) {
        LOG.error("slow consumer detected in server " + conn.getConnectedUrl() + " | " + consumer.toString());
        NatsMetricsUtil.reportConnectionError(conn.getConnectedUrl(),
                                              NatsObservabilityConstants.ERROR_TYPE_SLOW_CONSUMER);
    }
}
