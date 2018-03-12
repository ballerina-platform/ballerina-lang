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
package org.ballerinalang.net.grpc.actions.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.net.grpc.ClientConnectorFactory;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.ssl.SSLConfig;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.ballerinalang.net.grpc.stubs.ProtoFileDefinition;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLException;

import static org.ballerinalang.net.grpc.MessageConstants.MAX_MESSAGE_SIZE;
import static org.ballerinalang.net.grpc.MessageConstants.PATH;
import static org.ballerinalang.net.grpc.SSLCertificateUtils.loadX509Cert;
import static org.ballerinalang.net.grpc.SSLCertificateUtils.preferredTestCiphers;
import static org.ballerinalang.net.grpc.SSLCertificateUtils.testServerAddress;
import static org.ballerinalang.net.grpc.actions.client.ActionUtils.notifyErrorReply;

/**
 * {@code Init} is the Init action implementation of the gRPC Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "<init>",
        connectorName = "GRPCConnector",
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR)},
        connectorArgs = {
                @Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT),
                @Argument(name = "type", type = TypeKind.STRING),
                @Argument(name = "descriptorKey", type = TypeKind.STRING),
                @Argument(name = "descriptorMap", type = TypeKind.MAP)
        })
public class Init extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        BConnector bConnector = (BConnector) context.getRefArgument(0);
        int port = (int) bConnector.getIntField(0);
        String host = bConnector.getStringField(0);
        String subtype = bConnector.getStringField(1);
        //EnumNode.Enumerator enumerator = (EnumNode.Enumerator) bConnector.getRefField(1);
        String descriptorKey = bConnector.getStringField(2);
        BMap<String, BValue> descriptorMap = (BMap<String, BValue>) bConnector.getRefField(0);
        try {
            // If there are more than one descriptors exist, other descriptors are considered as dependent
            // descriptors.  client supported only one depth descriptor dependency.
            List<byte[]> depDescriptorData = new ArrayList<>();
            byte[] descriptorValue = null;
            for (String key : descriptorMap.keySet()) {
                if (descriptorMap.get(key) == null) {
                    continue;
                }
                if (descriptorKey.equals(key)) {
                    descriptorValue = hexStringToByteArray(descriptorMap.get(key).stringValue());
                } else {
                    depDescriptorData.add(hexStringToByteArray(descriptorMap.get(key).stringValue()));
                }
            }
            
            if (descriptorValue == null) {
                throw new RuntimeException("Error while establishing the connection. service descriptor is null.");
            }
            ProtoFileDefinition protoFileDefinition = new ProtoFileDefinition(depDescriptorData);
            protoFileDefinition.setRootDescriptorData(descriptorValue);
            BStruct options = (BStruct) bConnector.getRefField(1);
            SSLConfig clientSslConfigs;
            ManagedChannel channel;
            if (options != null) {
                // TODO: 3/11/18 keystore file should get from this
                clientSslConfigs = populateClientConfigurationOptions(options);
                SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
                SslContext sslContext = GrpcSslContexts.forClient()
                        .trustManager(loadX509Cert(PATH + "ca.pem"))
                        .ciphers(preferredTestCiphers(), SupportedCipherSuiteFilter.INSTANCE)
                        .sslProvider(provider)
                        .build();
                channel = NettyChannelBuilder
                        .forAddress(testServerAddress(port))
                        .flowControlWindow(65 * 1024)
                        .maxInboundMessageSize(MAX_MESSAGE_SIZE)
                        .sslContext(sslContext).build();
            } else {
                channel = ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext(true)
                        .build();
            }
            ClientConnectorFactory clientConnectorFactory = new ClientConnectorFactory(protoFileDefinition);
            if ("blocking".equalsIgnoreCase(subtype)) {
                GrpcBlockingStub grpcBlockingStub = clientConnectorFactory.newBlockingStub(channel);
                bConnector.setNativeData("stub", grpcBlockingStub);
                // future.notifySuccess();
            } else if ("non-blocking".equalsIgnoreCase(subtype)) {
                GrpcNonBlockingStub nonBlockingStub = clientConnectorFactory.newNonBlockingStub(channel);
                bConnector.setNativeData("stub", nonBlockingStub);
                // future.notifySuccess();
            } else {
                notifyErrorReply(context, "gRPC Connector Error : Invalid stub type");
            }
        } catch (RuntimeException | GrpcClientException e) {
            notifyErrorReply(context, "gRPC Connector Error.");
        } catch (SSLException e) {
            notifyErrorReply(context, "gRPC Connector Error when generating SSL " +
                    "configuration.");
        } catch (CertificateException e) {
            notifyErrorReply(context, "gRPC Connector SSL Certificate Error.");
        } catch (IOException e) {
            notifyErrorReply(context, "gRPC Connector I/O Error.");
        }
    }
    
    private SSLConfig populateClientConfigurationOptions(BStruct options) {
        if (options.getRefField(0) != null) {
            SSLConfig clientSslConfigs = new SSLConfig(null, null);
            BStruct sslConfigStructs = (BStruct) options.getRefField(0);
            clientSslConfigs.setTrustStore(sslConfigStructs.getStringField(0));
            clientSslConfigs.setTrustStorePass(sslConfigStructs.getStringField(1));
            return clientSslConfigs;
        }
        return new SSLConfig();
    }
    
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
