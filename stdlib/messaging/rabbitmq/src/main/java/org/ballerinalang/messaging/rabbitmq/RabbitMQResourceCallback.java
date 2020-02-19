/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq;

import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQMetricsUtil;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQObservabilityConstants;

import java.util.concurrent.CountDownLatch;

/**
 * The resource call back implementation for RabbitMQ async consumer.
 *
 * @since 0.995.0
 */
public class RabbitMQResourceCallback implements CallableUnitCallback {
    private CountDownLatch countDownLatch;
    private Channel channel;
    private String queueName;
    private int size;

    RabbitMQResourceCallback(CountDownLatch countDownLatch, Channel channel, String queueName, int size) {
        this.countDownLatch = countDownLatch;
        this.channel = channel;
        this.queueName = queueName;
        this.size = size;
    }

    @Override
    public void notifySuccess() {
        RabbitMQMetricsUtil.reportConsume(channel, queueName, size,
                                          RabbitMQObservabilityConstants.CONSUME_TYPE_SERVICE);
        countDownLatch.countDown();
    }

    @Override
    public void notifyFailure(ErrorValue error) {
        countDownLatch.countDown();
        RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_DISPATCH);
        ErrorHandlerUtils.printError("RabbitMQ Error: " + error.getPrintableStackTrace());
    }
}
