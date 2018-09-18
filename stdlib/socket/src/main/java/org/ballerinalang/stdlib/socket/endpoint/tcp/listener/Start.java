/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.endpoint.tcp.listener;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.SERVER_SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * Start server connector.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Start extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        try {
            Struct listenerEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            ServerSocketChannel channel = (ServerSocketChannel) listenerEndpoint.getNativeData(SERVER_SOCKET_KEY);
            BMap<String, BValue> config = (BMap<String, BValue>) listenerEndpoint.getNativeData(LISTENER_CONFIG);
            BInteger port = (BInteger) config.get(SocketConstants.CONFIG_FIELD_PORT);
            BString networkInterface = (BString) config.get(SocketConstants.CONFIG_FIELD_INTERFACE);
            if (networkInterface != null) {
                channel.bind(new InetSocketAddress(networkInterface.stringValue(), (int) port.intValue()));
            } else {
                channel.bind(new InetSocketAddress((int) port.intValue()));
            }
            final SelectorManager selectorManager = SelectorManager.getInstance();
            selectorManager.start();
            context.setReturnValues();
        } catch (Throwable e) {
            throw new BallerinaException(e);
        }
    }
}
