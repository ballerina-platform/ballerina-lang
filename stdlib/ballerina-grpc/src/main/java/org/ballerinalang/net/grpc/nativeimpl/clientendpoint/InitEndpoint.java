/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.clientendpoint;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.EndpointConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
import org.ballerinalang.net.grpc.interceptor.ClientHeaderInterceptor;
import org.ballerinalang.net.grpc.nativeimpl.EndpointUtils;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.ballerinalang.net.grpc.MessageConstants.CHANNEL_KEY;
import static org.ballerinalang.net.grpc.MessageConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.MessageConstants.DEFAULT_HOSTNAME;
import static org.ballerinalang.net.grpc.MessageConstants.MAX_MESSAGE_SIZE;
import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;


/**
 * Native function for initializing gRPC client endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = CLIENT_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ClientEndpointConfiguration")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            // Creating client endpoint with channel as native data.
            Struct endpointConfig = clientEndpoint.getStructField(EndpointConstants.ENDPOINT_CONFIG);
            EndpointConfiguration configuration = EndpointUtils.getEndpointConfiguration(endpointConfig);
            ManagedChannel channel;
            if (configuration.getSslConfig() == null) {
                channel = ManagedChannelBuilder.forAddress(configuration.getHost(), configuration.getPort())
                        .usePlaintext(true)
                        .build();
            } else {
                SslContext sslContext = new SSLHandlerFactory(configuration.getSslConfig())
                        .createHttp2TLSContextForClient();
                channel = NettyChannelBuilder
                        .forAddress(generateSocketAddress(configuration.getHost(), configuration.getPort()))
                        .flowControlWindow(65 * 1024)
                        .maxInboundMessageSize(MAX_MESSAGE_SIZE)
                        .sslContext(sslContext).build();
            }
            // attach header interceptor to read/write headers.
            ClientHeaderInterceptor headerInterceptor = new ClientHeaderInterceptor();
            Channel channelWithHeader = ClientInterceptors.intercept(channel, headerInterceptor);
            clientEndpoint.addNativeData(CHANNEL_KEY, channelWithHeader);

        } catch (Throwable throwable) {
            BStruct errorStruct = MessageUtils.getConnectorError(context, throwable);
            context.setError(errorStruct);
        }
        
    }
    
    /**
     * Creates a new {@link InetSocketAddress} on localhost that overrides the host with.
     */
    private static InetSocketAddress generateSocketAddress(String host, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(DEFAULT_HOSTNAME);
            inetAddress = InetAddress.getByAddress(host, inetAddress.getAddress());
            return new InetSocketAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
