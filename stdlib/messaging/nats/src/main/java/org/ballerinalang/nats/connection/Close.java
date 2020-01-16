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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Close a given connection in the NATS server.
 *
 * @since 0.995
 */
public class Close {

    private static final Logger LOG = LoggerFactory.getLogger(Close.class);
    private static PrintStream console = System.out;

    public static Object externClose(ObjectValue connectionObject, Object forceful) {
        int clientCount = ((AtomicInteger) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS)).get();
        if (clientCount == 0 || isForceShutdown(forceful)) {
            Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);
            try {
                if (natsConnection != null) {
                    natsConnection.close();
                }
                connectionObject.addNativeData(Constants.NATS_CONNECTION, null);
                connectionObject.addNativeData(Constants.CONNECTED_CLIENTS, null);
                return null;
            } catch (InterruptedException e) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while closing the connection " +
                        "with nats server. " + e.getMessage());
            }
        } else {
            String message = "Connection is still used by " + clientCount + " client(s). Close them before " +
                    "closing the connection.";
            LOG.warn(message);
            console.println(message);
            connectionObject.addNativeData(Constants.CLOSING, true);
        }
        return null;
    }

    private static boolean isForceShutdown(Object forceful) {
        return (TypeChecker.getType(forceful).getTag() == TypeTags.BOOLEAN_TAG && (Boolean) forceful);
    }
}
