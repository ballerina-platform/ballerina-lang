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

package org.ballerinalang.stdlib.io.socket.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;

/**
 * Extern function for Socket bind address.
 *
 * @since 0.971.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io", functionName = "bindAddress",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Socket",
                structPackage = SocketConstants.SOCKET_PACKAGE),
        args = {@Argument(name = "port", type = TypeKind.INT),
                @Argument(name = "interface", type = TypeKind.STRING)
        },
        isPublic = true
)
public class BindAddress extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(BindAddress.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> socketStruct;
        int port = (int) context.getIntArgument(0);
        try {
            socketStruct = (BMap<String, BValue>) context.getRefArgument(0);
            BValue networkInterface = context.getNullableRefArgument(1);
            SocketChannel socket = (SocketChannel) socketStruct.getNativeData(SocketConstants.SOCKET_KEY);
            if (networkInterface == null) {
                socket.bind(new InetSocketAddress(port));
            } else {
                socket.bind(new InetSocketAddress(networkInterface.stringValue(), port));
            }
        } catch (ConnectionPendingException e) {
            String message = "Socket initialization already in process. Unable to bind to the port.";
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, message);
            context.setReturnValues(errorStruct);
        } catch (AlreadyBoundException e) {
            String message = "Unable to bind to the port: " + port + ". Socket is already bound.";
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, message);
            context.setReturnValues(errorStruct);
        } catch (UnsupportedAddressTypeException e) {
            String message = "Socket address doesn't support for a TCP connection.";
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, message);
            context.setReturnValues(errorStruct);
        } catch (ClosedChannelException e) {
            String message = "Socket connection is already closed.";
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, message);
            context.setReturnValues(errorStruct);
        } catch (IOException e) {
            String message = "Error occurred while bind to the socket address: " + e.getMessage();
            log.error(message, e);
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, e.getMessage());
            context.setReturnValues(errorStruct);
        } catch (SecurityException e) {
            String message = "Unknown error occurred.";
            log.error(message, e);
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, e.getMessage());
            context.setReturnValues(errorStruct);
        } catch (Throwable e) {
            String message = "An error occurred.";
            log.error(message, e);
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, e.getMessage());
            context.setReturnValues(errorStruct);
        }
    }
}
