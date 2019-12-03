/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.streamingclient;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.net.grpc.GrpcConstants.REQUEST_SENDER;

/**
 * Extern function to inform the server, client finished sending messages.
 *
 * @since 1.0.0
 */
public class Complete {
    private static final Logger LOG = LoggerFactory.getLogger(Complete.class);

    public static Object streamComplete(ObjectValue streamingConnection) {
        StreamObserver requestSender = (StreamObserver) streamingConnection.getNativeData(REQUEST_SENDER);
        if (requestSender == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while completing the " +
                            "message. endpoint does not exist")));
        } else {
            try {
                requestSender.onCompleted();
            } catch (Exception e) {
                LOG.error("Error while sending complete message to server.", e);
                return MessageUtils.getConnectorError(e);
            }
        }
        return null;
    }
}
