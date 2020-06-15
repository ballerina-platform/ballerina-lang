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

package org.ballerinalang.nats.basic.producer;

import io.nats.client.Connection;
import io.nats.client.Message;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.observability.NatsMetricsReporter;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;
import org.ballerinalang.nats.observability.NatsTracingUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.nats.Utils.convertDataIntoByteArray;

/**
 * Extern function to publish message to a given subject.
 *
 * @since 0.995
 */
public class Request {

    @SuppressWarnings("unused")
    public static Object externRequest(ObjectValue producerObject, BString subject, Object data, Object duration) {
        NatsTracingUtil.traceResourceInvocation(Scheduler.getStrand(), producerObject, subject.getValue());
        Object connection = producerObject.get(Constants.CONNECTION_OBJ);
        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);

            if (natsConnection == null) {
                NatsMetricsReporter.reportProducerError(NatsObservabilityConstants.ERROR_TYPE_REQUEST);
                return Utils.createNatsError(Constants.PRODUCER_ERROR +
                        subject.getValue() + ". NATS connection doesn't exist.");
            }
            NatsMetricsReporter natsMetricsReporter =
                    (NatsMetricsReporter) connectionObject.getNativeData(Constants.NATS_METRIC_UTIL);
            byte[] byteContent = convertDataIntoByteArray(data);
            try {
                Message reply;
                Future<Message> incoming = natsConnection.request(subject.getValue(), byteContent);
                natsMetricsReporter.reportRequest(subject.getValue(), byteContent.length);
                if (TypeChecker.getType(duration).getTag() == TypeTags.INT_TAG) {
                    reply = incoming.get((Long) duration, TimeUnit.MILLISECONDS);
                } else {
                    reply = incoming.get();
                }
                ArrayValue msgData = new ArrayValueImpl(reply.getData());
                natsMetricsReporter.reportResponse(subject.getValue());
                ObjectValue msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE_ID,
                        Constants.NATS_MESSAGE_OBJ_NAME, reply.getSubject(), msgData, reply.getReplyTo());
                msgObj.addNativeData(Constants.NATS_MSG, reply);
                return msgObj;
            } catch (TimeoutException ex) {
                natsMetricsReporter.reportProducerError(subject.getValue(),
                                                        NatsObservabilityConstants.ERROR_TYPE_REQUEST);
                return Utils.createNatsError("Request to subject " + subject.getValue() +
                                                     " timed out while waiting for a reply");
            } catch (IllegalArgumentException | IllegalStateException | ExecutionException ex) {
                natsMetricsReporter.reportProducerError(subject.getValue(),
                                                        NatsObservabilityConstants.ERROR_TYPE_REQUEST);
                return Utils.createNatsError("Error while requesting message to " +
                        "subject " + subject.getValue() + ". " + ex.getMessage());
            } catch (InterruptedException ex) {
                natsMetricsReporter.reportProducerError(subject.getValue(),
                                                        NatsObservabilityConstants.ERROR_TYPE_REQUEST);
                Thread.currentThread().interrupt();
                return Utils.createNatsError("Error while requesting message to " +
                        "subject " + subject.getValue() + ". " + ex.getMessage());
            }
        } else {
            NatsMetricsReporter.reportProducerError(NatsObservabilityConstants.ERROR_TYPE_REQUEST);
            return Utils.createNatsError(Constants.PRODUCER_ERROR +
                    subject.getValue() + ". Producer is logically disconnected.");
        }
    }
}
