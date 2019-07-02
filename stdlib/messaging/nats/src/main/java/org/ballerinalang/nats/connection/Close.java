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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Close a given connection in the NATS server.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "close",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Connection", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Close extends BlockingNativeCallableUnit {

    private static final Logger LOG = LoggerFactory.getLogger(Close.class);
    private static PrintStream console = System.out;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
    }

    public static Object close(Strand strand, ObjectValue connectionObject, Object forceful) {
        ArrayList connectedList = (ArrayList) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS);
        if (connectedList == null || connectedList.isEmpty() || TypeChecker.anyToBoolean(forceful)) {
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
        }
        if (!TypeChecker.anyToBoolean(forceful)) {
            String message = "Connection is still used by " + connectedList.size() + "client(s). Close them before " +
                    "closing the connection.";
            LOG.warn(message);
            console.println(message);
            connectionObject.addNativeData(Constants.CLOSING, true);
        }
        return null;
    }
}
