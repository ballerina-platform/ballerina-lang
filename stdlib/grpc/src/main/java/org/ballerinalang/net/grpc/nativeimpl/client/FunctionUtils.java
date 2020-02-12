/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.grpc.nativeimpl.client;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.grpc.DataContext;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.ServiceDefinition;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.stubs.BlockingStub;
import org.ballerinalang.net.grpc.stubs.DefaultStreamObserver;
import org.ballerinalang.net.grpc.stubs.NonBlockingStub;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.ENDPOINT_URL;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.GrpcConstants.NON_BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_GRPC_PKG_ID;
import static org.ballerinalang.net.grpc.GrpcConstants.REQUEST_MESSAGE_DEFINITION;
import static org.ballerinalang.net.grpc.GrpcConstants.REQUEST_SENDER;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.GrpcConstants.STREAMING_CLIENT;
import static org.ballerinalang.net.grpc.GrpcConstants.TAG_KEY_GRPC_MESSAGE_CONTENT;
import static org.ballerinalang.net.grpc.GrpcUtil.getConnectionManager;
import static org.ballerinalang.net.grpc.GrpcUtil.populatePoolingConfig;
import static org.ballerinalang.net.grpc.GrpcUtil.populateSenderConfigurations;
import static org.ballerinalang.net.grpc.Status.Code.INTERNAL;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;

/**
 * Utility methods represents actions for the client.
 *
 * @since 1.0.0
 */
public class FunctionUtils extends AbstractExecute {

    /**
     * Extern function to initialize global connection pool.
     *
     * @param endpointObject client endpoint instance.
     * @param globalPoolConfig global pool configuration.
     */
    public static void externInitGlobalPool(ObjectValue endpointObject, MapValue<String, Long> globalPoolConfig) {
        PoolConfiguration globalPool = new PoolConfiguration();
        populatePoolingConfig(globalPoolConfig, globalPool);
        ConnectionManager connectionManager = new ConnectionManager(globalPool);
        globalPoolConfig.addNativeData(CONNECTION_MANAGER, connectionManager);
    }

    /**
     * Extern function to initialize client endpoint.
     *
     * @param clientEndpoint client endpoint instance.
     * @param urlString service Url.
     * @param clientEndpointConfig endpoint configuration.
     * @param globalPoolConfig global pool configuration.
     * @return Error if there is an error while initializing the client endpoint, else returns nil
     */
    @SuppressWarnings("unchecked")
    public static Object externInit(ObjectValue clientEndpoint, String urlString,
                                    MapValue clientEndpointConfig, MapValue globalPoolConfig) {
        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Malformed URL: "
                            + urlString)));
        }

        String scheme = url.getProtocol();
        Map<String, Object> properties =
                HttpConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HttpConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        try {
            populateSenderConfigurations(senderConfiguration, clientEndpointConfig, scheme);
            MapValue userDefinedPoolConfig = (MapValue) clientEndpointConfig.get(
                    HttpConstants.USER_DEFINED_POOL_CONFIG);
            ConnectionManager poolManager = userDefinedPoolConfig == null ? getConnectionManager(globalPoolConfig) :
                    getConnectionManager(userDefinedPoolConfig);
            senderConfiguration.setHttpVersion(Constants.HTTP_2_0);
            senderConfiguration.setForceHttp2(true);
            HttpClientConnector clientConnector = HttpUtil.createHttpWsConnectionFactory()
                    .createHttpClientConnector(properties, senderConfiguration, poolManager);

            clientEndpoint.addNativeData(CLIENT_CONNECTOR, clientConnector);
            clientEndpoint.addNativeData(ENDPOINT_URL, urlString);
        } catch (ErrorValue ex) {
            return ex;
        } catch (RuntimeException ex) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withCause(ex)));
        }
        return null;
    }

    /**
     * Extern function to initialize client stub.
     *
     * @param genericEndpoint generic client endpoint instance.
     * @param clientEndpoint generated client endpoint instance.
     * @param stubType stub type (blocking or non-blocking).
     * @param rootDescriptor service descriptor.
     * @param descriptorMap dependent descriptor map.
     * @return Error if there is an error while initializing the stub, else returns nil
     */
    public static Object externInitStub(ObjectValue genericEndpoint, ObjectValue clientEndpoint, String stubType,
                                        String rootDescriptor, MapValue<String, Object> descriptorMap) {
        HttpClientConnector clientConnector = (HttpClientConnector) genericEndpoint.getNativeData(CLIENT_CONNECTOR);
        String urlString = (String) genericEndpoint.getNativeData(ENDPOINT_URL);

        if (stubType == null || rootDescriptor == null || descriptorMap == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while initializing " +
                            "connector. message descriptor keys not exist. Please check the generated sub file")));
        }

        try {
            ServiceDefinition serviceDefinition = new ServiceDefinition(rootDescriptor, descriptorMap);
            Map<String, MethodDescriptor> methodDescriptorMap =
                    serviceDefinition.getMethodDescriptors(clientEndpoint.getType());

            genericEndpoint.addNativeData(METHOD_DESCRIPTORS, methodDescriptorMap);
            if (BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                BlockingStub blockingStub = new BlockingStub(clientConnector, urlString);
                genericEndpoint.addNativeData(SERVICE_STUB, blockingStub);
            } else if (NON_BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                NonBlockingStub nonBlockingStub = new NonBlockingStub(clientConnector, urlString);
                genericEndpoint.addNativeData(SERVICE_STUB, nonBlockingStub);
            } else {
                return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while " +
                                "initializing connector. invalid connector type")));
            }
        } catch (RuntimeException | GrpcClientException e) {
            return MessageUtils.getConnectorError(e);
        }
        return null;
    }

    /**
     * Extern function to perform blocking call for the gRPC client.
     *
     * @param clientEndpoint client endpoint instance.
     * @param methodName remote method name.
     * @param payloadBValue request payload.
     * @param headerValues custom metadata to send with the request.
     * @return Error if there is an error while calling remote method, else returns response message.
     */
    @SuppressWarnings("unchecked")
    public static Object externBlockingExecute(ObjectValue clientEndpoint, String methodName,
                                               Object payloadBValue, Object headerValues) {
        if (clientEndpoint == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connector. gRPC client connector " +
                    "is not initialized properly");
        }

        Object connectionStub = clientEndpoint.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connection stub. gRPC Client " +
                    "connector is not initialized properly");
        }

        if (methodName == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. RPC endpoint " +
                    "doesn't set properly");
        }
        Map<String, MethodDescriptor> methodDescriptors = (Map<String, MethodDescriptor>) clientEndpoint.getNativeData
                (METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. method descriptors " +
                    "doesn't set properly");
        }

        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = methodDescriptors.get(methodName) != null
                ? methodDescriptors.get(methodName).getSchemaDescriptor() : null;
        if (methodDescriptor == null) {
            return notifyErrorReply(INTERNAL, "No registered method descriptor for '" + methodName + "'");
        }

        if (connectionStub instanceof BlockingStub) {
            Optional<ObserverContext> observerContext =
                    ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());
            observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_GRPC_MESSAGE_CONTENT, payloadBValue.toString()));
            Message requestMsg = new Message(methodDescriptor.getInputType().getName(), payloadBValue);
            // Update request headers when request headers exists in the context.
            HttpHeaders headers = null;
            if (headerValues != null && (TypeChecker.getType(headerValues).getTag() == TypeTags.OBJECT_TYPE_TAG)) {
                headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
            }
            if (headers != null) {
                requestMsg.setHeaders(headers);
            }
            BlockingStub blockingStub = (BlockingStub) connectionStub;
            DataContext dataContext = null;
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {

                    dataContext = new DataContext(Scheduler.getStrand(),
                            new NonBlockingCallback(Scheduler.getStrand()));
                    blockingStub.executeUnary(requestMsg, methodDescriptors.get(methodName), dataContext);
                } else {
                    return notifyErrorReply(INTERNAL, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                }
            } catch (Exception e) {
                if (dataContext != null) {
                    dataContext.getCallback().notifyFailure(MessageUtils.getConnectorError(e));
                }
                return notifyErrorReply(INTERNAL, "gRPC Client Connector Error :" + e.getMessage());
            }
        } else {
            return notifyErrorReply(INTERNAL, "Error while processing the request message. Connection Sub " +
                    "type not supported");
        }
        return null;
    }

    /**
     * Extern function to perform non blocking call for the gRPC client.
     *
     * @param clientEndpoint client endpoint instance.
     * @param methodName remote method name.
     * @param payload request payload.
     * @param callbackService response callback listener service.
     * @param headerValues custom metadata to send with the request.
     * @return Error if there is an error while initializing the stub, else returns nil
     */
    @SuppressWarnings("unchecked")
    public static Object externNonBlockingExecute(ObjectValue clientEndpoint, String methodName,
                                                  Object payload, ObjectValue callbackService, Object headerValues) {
        if (clientEndpoint == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connector. gRPC Client connector is " +
                    "not initialized properly");
        }

        Object connectionStub = clientEndpoint.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connection stub. gRPC Client connector " +
                    "is not initialized properly");
        }

        if (methodName == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. RPC endpoint doesn't " +
                    "set properly");
        }

        Map<String, MethodDescriptor> methodDescriptors = (Map<String, MethodDescriptor>) clientEndpoint.getNativeData
                (METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. method descriptors " +
                    "doesn't set properly");
        }

        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = methodDescriptors.get(methodName) != null
                ? methodDescriptors.get(methodName).getSchemaDescriptor() : null;
        if (methodDescriptor == null) {
            return notifyErrorReply(INTERNAL, "No registered method descriptor for '" + methodName + "'");
        }

        if (connectionStub instanceof NonBlockingStub) {
            Message requestMsg = new Message(methodDescriptor.getInputType().getName(), payload);
            Optional<ObserverContext> observerContext =
                    ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());
            observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_GRPC_MESSAGE_CONTENT, payload.toString()));
            // Update request headers when request headers exists in the context.
            HttpHeaders headers = null;
            if (headerValues != null && (TypeChecker.getType(headerValues).getTag() == TypeTags.OBJECT_TYPE_TAG)) {
                headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
            }
            if (headers != null) {
                requestMsg.setHeaders(headers);
            }

            NonBlockingStub nonBlockingStub = (NonBlockingStub) connectionStub;
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                DataContext context = new DataContext(Scheduler.getStrand(), null);
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                    nonBlockingStub.executeUnary(requestMsg, new DefaultStreamObserver(BRuntime.getCurrentRuntime(),
                            callbackService), methodDescriptors.get(methodName), context);
                } else if (methodType.equals(MethodDescriptor.MethodType.SERVER_STREAMING)) {
                    nonBlockingStub.executeServerStreaming(requestMsg,
                            new DefaultStreamObserver(BRuntime.getCurrentRuntime(), callbackService),
                            methodDescriptors.get(methodName), context);
                } else {
                    return notifyErrorReply(INTERNAL, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                }
                return null;
            } catch (Exception e) {
                return notifyErrorReply(INTERNAL, "gRPC Client Connector Error :" + e.getMessage());
            }
        } else {
            return notifyErrorReply(INTERNAL, "Error while processing the request message. Connection Sub " +
                    "type not supported");
        }
    }

    /**
     * Extern function to perform streaming call for the gRPC client.
     *
     * @param clientEndpoint client endpoint instance.
     * @param methodName remote method name.
     * @param callbackService response callback listener service.
     * @param headerValues custom metadata to send with the request.
     * @return Error if there is an error while initializing the stub, else returns nil
     */
    @SuppressWarnings("unchecked")
    public static Object externStreamingExecute(ObjectValue clientEndpoint, String methodName,
                                                ObjectValue callbackService, Object headerValues) {
        if (clientEndpoint == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connector. gRPC Client connector " +
                    "is not initialized properly");
        }

        Object connectionStub = clientEndpoint.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connection stub. gRPC Client connector is " +
                    "not initialized properly");
        }

        if (methodName == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. RPC endpoint doesn't " +
                    "set properly");
        }

        Map<String, MethodDescriptor> methodDescriptors = (Map<String, MethodDescriptor>) clientEndpoint.getNativeData
                (METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. method descriptors " +
                    "doesn't set properly");
        }

        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = methodDescriptors.get(methodName) != null
                ? methodDescriptors.get(methodName).getSchemaDescriptor() : null;
        if (methodDescriptor == null) {
            return notifyErrorReply(INTERNAL, "No registered method descriptor for '" + methodName + "'");
        }

        if (connectionStub instanceof NonBlockingStub) {
            NonBlockingStub nonBlockingStub = (NonBlockingStub) connectionStub;
            HttpHeaders headers = null;
            if (headerValues != null && (TypeChecker.getType(headerValues).getTag() == TypeTags.OBJECT_TYPE_TAG)) {
                headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
            }

            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                DefaultStreamObserver responseObserver = new DefaultStreamObserver(BRuntime.getCurrentRuntime(),
                        callbackService);
                StreamObserver requestSender;
                DataContext context = new DataContext(Scheduler.getStrand(), null);
                if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING)) {
                    requestSender = nonBlockingStub.executeClientStreaming(headers, responseObserver,
                            methodDescriptors.get(methodName), context);
                } else if (methodType.equals(MethodDescriptor.MethodType.BIDI_STREAMING)) {
                    requestSender = nonBlockingStub.executeBidiStreaming(headers, responseObserver, methodDescriptors
                            .get(methodName), context);
                } else {
                    return notifyErrorReply(INTERNAL, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                }
                ObjectValue streamingConnection = BallerinaValues.createObjectValue(PROTOCOL_GRPC_PKG_ID,
                        STREAMING_CLIENT);
                streamingConnection.addNativeData(REQUEST_SENDER, requestSender);
                streamingConnection.addNativeData(REQUEST_MESSAGE_DEFINITION, methodDescriptor
                        .getInputType());
                return streamingConnection;
            } catch (RuntimeException | GrpcClientException e) {
                return notifyErrorReply(INTERNAL, "gRPC Client Connector Error :" + e.getMessage());
            }
        } else {
            return notifyErrorReply(INTERNAL, "Error while processing the request message. Connection Sub " +
                    "type not supported");
        }
    }

}
