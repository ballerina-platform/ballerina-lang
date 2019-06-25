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

package org.ballerinalang.nats.nativeimpl.consumer;

import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Creates a subscription with the NATS server.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "nats",
        functionName = "create",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Create implements NativeCallableUnit {

    private static final String START_SEQ = "startSeq";
    private static final String SUBJECT_FIELD = "subject";
    private static final String MANUAL_ACK = "manualAck";
    private static final String ACK_WAIT = "ackWait";
    private static final String DURABLE_NAME = "durableName";
    private static final int ZERO = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        try {
            BMap<String, BValue> consumer = Utils.getReceiverObject(context);
            StreamingConnection connection = (StreamingConnection) ((BMap) consumer.get(Constants.CONNECTION_OBJ)).
                    getNativeData(Constants.NATS_CONNECTION);
            Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
            List<Annotation> annotationList = service.getAnnotationList(Constants.NATS_PACKAGE,
                    Constants.NATS_SERVICE_CONFIG);
            Annotation annotation = annotationList.get(ZERO);
            Resource resources = Utils.extractNATSResource(service);
            Struct value = annotation.getValue();
            String subject = value.getStringField(SUBJECT_FIELD);
            SubscriptionOptions.Builder builder = new SubscriptionOptions.Builder();
            if (value.getBooleanField(MANUAL_ACK)) {
                builder.manualAcks();
            }
            long ackWait = value.getRefField(ACK_WAIT) != null ? value.getIntField(ACK_WAIT) : ZERO;
            if (ackWait > ZERO) {
                builder.ackWait(ackWait, TimeUnit.MILLISECONDS);
            }
            long startSeq = value.getRefField(START_SEQ) != null ? value.getIntField(START_SEQ) : ZERO;
            if (startSeq > ZERO) {
                builder.startAtSequence(startSeq);
            }
            String durableName = value.getRefField(DURABLE_NAME) != null ? value.getStringField(DURABLE_NAME) : null;
            if (null != durableName) {
                builder.durableName(durableName);
            }
            Listener listener = new Listener(resources);
            connection.subscribe(subject, listener, builder.build());
        } catch (IOException | TimeoutException e) {
            // Both the error messages returned would not give java specific information
            // Hence the error message will not be overridden
            context.setReturnValues(Utils.createError(context, Constants.NATS_ERROR_CODE, e.getMessage()));
        } catch (InterruptedException ignore) {
            // ignore
            Thread.currentThread().interrupt();
        } finally {
            callableUnitCallback.notifySuccess();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlocking() {
        return false;
    }
}
