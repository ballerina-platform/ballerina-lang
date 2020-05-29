/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.ballerinalang.observe.trace.extension.choreo.gen.HandshakeGrpc;
import org.ballerinalang.observe.trace.extension.choreo.gen.NegotiatorOuterClass.PublishAstRequest;
import org.ballerinalang.observe.trace.extension.choreo.gen.NegotiatorOuterClass.RegisterRequest;
import org.ballerinalang.observe.trace.extension.choreo.gen.NegotiatorOuterClass.RegisterResponse;
import org.ballerinalang.observe.trace.extension.choreo.gen.TelemetryGrpc;
import org.ballerinalang.observe.trace.extension.choreo.gen.TelemetryOuterClass;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;
import org.ballerinalang.observe.trace.extension.choreo.model.ChoreoMetric;
import org.ballerinalang.observe.trace.extension.choreo.model.ChoreoTraceSpan;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Manages the communication with Choreo cloud.
 */
public class ChoreoClient implements AutoCloseable {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final int MESSAGE_SIZE_BUFFER_BYTES = 200 * 1024; // Buffer for the rest of the content
    private static final int SERVER_MAX_FRAME_SIZE_BYTES = 4 * 1024 * 1024 - MESSAGE_SIZE_BUFFER_BYTES;

    private String id;      // ID received from the handshake
    private String nodeId;

    // TODO: Remove this field from the class.
    private String appId;
    private ManagedChannel channel;
    private HandshakeGrpc.HandshakeBlockingStub registrationClient;
    private TelemetryGrpc.TelemetryBlockingStub telemetryClient;
    private Thread uploadingThread;

    public ChoreoClient(String hostname, int port, boolean useSSL) {
        LOGGER.info("initializing connection with observability backend " + hostname + ":" + port);

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(hostname, port);
        if (!useSSL) {
            channelBuilder.usePlaintext();
        }
        channel = channelBuilder.build();
        registrationClient = HandshakeGrpc.newBlockingStub(channel);
        telemetryClient = TelemetryGrpc.newBlockingStub(channel);
    }

    public String register(final MetadataReader metadataReader, String nodeId, String appSecret) {
        RegisterRequest handshakeRequest = RegisterRequest.newBuilder()
                .setAstHash(metadataReader.getAstHash())
                .setProjectSecret(nodeId)
                .setNodeId(nodeId)
                .build();
        RegisterResponse registerResponse = registrationClient.register(handshakeRequest);
        this.id = registerResponse.getObsId();
        this.appId = this.id;
        boolean sendProgramJson = registerResponse.getSendAst();

        if (sendProgramJson) {
            uploadingThread = new Thread(() -> {
                PublishAstRequest programRequest = PublishAstRequest.newBuilder()
                        .setAst(metadataReader.getAstData())
                        .setObsId(id)
                        .setProjectSecret(appSecret)
                        .build();
                registrationClient.withCompression("gzip").publishAst(programRequest);
                // TODO add debug log to indicate success
            }, "AST Uploading Thread");
            uploadingThread.start();
        }

        this.nodeId = nodeId;
        return registerResponse.getObsUrl();
    }

    public void publishMetrics(ChoreoMetric[] metrics) {
        int i = 0;
        while (i < metrics.length) {
            TelemetryOuterClass.MetricsPublishRequest.Builder requestBuilder =
                    TelemetryOuterClass.MetricsPublishRequest.newBuilder();
            int messageSize = 0;
            while (i < metrics.length && messageSize < SERVER_MAX_FRAME_SIZE_BYTES) {
                ChoreoMetric metric = metrics[i];
                TelemetryOuterClass.Metric metricMessage = TelemetryOuterClass.Metric.newBuilder()
                        .setTimestamp(metric.getTimestamp())
                        .setName(metric.getName())
                        .setValue(metric.getValue())
                        .putAllTags(metric.getTags())
                        .build();

                int currentMessageSize = metricMessage.getSerializedSize();
                if (currentMessageSize >= SERVER_MAX_FRAME_SIZE_BYTES) {
                    LOGGER.error("Dropping metric with size %d larger than gRPC frame limit %d",
                            currentMessageSize, SERVER_MAX_FRAME_SIZE_BYTES);
                    i++;
                    continue;
                }
                messageSize += currentMessageSize;
                if (messageSize < SERVER_MAX_FRAME_SIZE_BYTES) {
                    requestBuilder.addMetrics(metricMessage);
                    i++;
                }
            }
            telemetryClient.publishMetrics(requestBuilder.setObservabilityId(id)
                    .setNodeId(nodeId)
                    .setAppId(appId)
                    .build());
        }
    }

    public void publishTraceSpans(ChoreoTraceSpan[] traceSpans) {
        int i = 0;
        while (i < traceSpans.length) {
            TelemetryOuterClass.TracesPublishRequest.Builder requestBuilder =
                    TelemetryOuterClass.TracesPublishRequest.newBuilder();
            int messageSize = 0;
            while (i < traceSpans.length && messageSize < SERVER_MAX_FRAME_SIZE_BYTES) {
                ChoreoTraceSpan traceSpan = traceSpans[i];
                TelemetryOuterClass.TraceSpan.Builder traceSpanBuilder = TelemetryOuterClass.TraceSpan.newBuilder()
                        .setTraceId(traceSpan.getTraceId())
                        .setSpanId(traceSpan.getSpanId())
                        .setServiceName(traceSpan.getServiceName())
                        .setOperationName(traceSpan.getOperationName())
                        .setTimestamp(traceSpan.getTimestamp())
                        .setDuration(traceSpan.getDuration())
                        .putAllTags(traceSpan.getTags());
                for (ChoreoTraceSpan.Reference reference : traceSpan.getReferences()) {
                    traceSpanBuilder.addReferences(TelemetryOuterClass.TraceSpanReference.newBuilder()
                            .setTraceId(reference.getTraceId())
                            .setSpanId(reference.getSpanId())
                            .setRefType(reference.getRefType() == ChoreoTraceSpan.Reference.Type.CHILD_OF
                                    ? TelemetryOuterClass.TraceReferenceType.CHILD_OF
                                    : TelemetryOuterClass.TraceReferenceType.FOLLOWS_FROM));
                }

                TelemetryOuterClass.TraceSpan traceSpanMessage = traceSpanBuilder.build();
                int currentMessageSize = traceSpanMessage.getSerializedSize();
                if (currentMessageSize >= SERVER_MAX_FRAME_SIZE_BYTES) {
                    LOGGER.error("Dropping trace span with size %d larger than gRPC frame limit %d",
                            currentMessageSize, SERVER_MAX_FRAME_SIZE_BYTES);
                    i++;
                    continue;
                }
                messageSize += currentMessageSize;
                if (messageSize < SERVER_MAX_FRAME_SIZE_BYTES) {
                    requestBuilder.addSpans(traceSpanMessage);
                    i++;
                }
            }
            telemetryClient.publishTraces(requestBuilder.setObservabilityId(id)
                    .setNodeId(nodeId)
                    .setAppId(appId)
                    .build());
        }
    }

    @Override
    public void close() throws Exception {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        if (Objects.nonNull(uploadingThread)) {
            uploadingThread.join(5000);
        }
    }
}
