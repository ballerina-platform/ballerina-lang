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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.EndpointConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
import org.ballerinalang.net.grpc.nativeimpl.EndpointUtils;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_STUB;
import static org.ballerinalang.net.grpc.MessageConstants.MAX_MESSAGE_SIZE;
import static org.ballerinalang.net.grpc.SSLCertificateUtils.preferredTestCiphers;


/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Client",
                structPackage = "ballerina.net.grpc"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ClientEndpointConfiguration")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            // Creating server connector
            Struct endpointConfig = clientEndpoint.getStructField(EndpointConstants.ENDPOINT_CONFIG);
            EndpointConfiguration configuration = EndpointUtils.getEndpointConfiguration(endpointConfig);
            ManagedChannel channel;
            if (configuration.getSslConfig() == null) {
                channel = ManagedChannelBuilder.forAddress(configuration.getHost(), configuration.getPort())
                        .usePlaintext(true)
                        .build();
            } else {
                SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
                SslContext sslContext = GrpcSslContexts.forClient()
                        .trustManager(SSLHandlerFactory.generateTrustManagerFactory(configuration.getSslConfig()))
                        .ciphers(preferredTestCiphers(), SupportedCipherSuiteFilter.INSTANCE)
                        .sslProvider(provider)
                        .build();
                channel = NettyChannelBuilder
                        .forAddress(generateSocketAddress(configuration.getPort(), configuration.getHost()))
                        .flowControlWindow(65 * 1024)
                        .maxInboundMessageSize(MAX_MESSAGE_SIZE)
                        .sslContext(sslContext).build();
            }
            clientEndpoint.addNativeData(EndpointConstants.CHANNEL_KEY, channel);
            BStruct clientStub = generateClientStub(context, endpointConfig);
            if (clientStub != null) {
                clientEndpoint.addNativeData(CLIENT_STUB, clientStub);
                context.setReturnValues();
            }
        } catch (Throwable throwable) {
            BStruct errorStruct = MessageUtils.getConnectorError(context, throwable);
            context.setError(errorStruct);
        }
        
    }
    
    /**
     * Creates a new {@link InetSocketAddress} on localhost that overrides the host with.
     */
    private static InetSocketAddress generateSocketAddress(int port, String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName("localhost");
            inetAddress = InetAddress.getByAddress(host, inetAddress.getAddress());
            return new InetSocketAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    private BStruct generateClientStub(Context context, Struct endpointConfig) {
        Value stubValue = endpointConfig.getTypeField("stub");
        if (stubValue == null) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "client stub type not specified."))));
            return null;
        }
        
        if (!(stubValue.getVMValue() instanceof BTypeValue)) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "client stub type not a Ballerina Type."))));
            return null;
        }
        
        BType stubType = ((BTypeValue) stubValue.getVMValue()).value();
        if (stubType == null) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "client stub type is null."))));
            return null;
        }
        return BLangConnectorSPIUtil.createBStruct(context, stubType.getPackagePath(), stubType
                .getName());
    }
}
