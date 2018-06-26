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
package org.ballerinalang.net.grpc;

import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.io.InputStream;

/**
 * Descriptor for a service method.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 *
 * @param <ReqT> InboundMessage Message Type
 * @param <RespT> OutboundMessage Message Type
 *
 * @since 0.980.0
 */
public final class MethodDescriptor<ReqT, RespT> {

    private final MethodType type;
    private final String fullMethodName;
    private final Marshaller<ReqT> requestMarshaller;
    private final Marshaller<RespT> responseMarshaller;
    private final com.google.protobuf.Descriptors.MethodDescriptor schemaDescriptor;

    /**
     * The call type of a method.
     */
    public enum MethodType {
        /**
         * One request message followed by one response message.
         */
        UNARY,

        /**
         * Zero or more request messages followed by one response message.
         */
        CLIENT_STREAMING,

        /**
         * One request message followed by zero or more response messages.
         */
        SERVER_STREAMING,

        /**
         * Zero or more request and response messages arbitrarily interleaved in time.
         */
        BIDI_STREAMING,

        /**
         * Unknown. something wrong in service definition.
         */
        UNKNOWN;

        /**
         * Returns where the client send one request message to the server.
         *
         * @return true, if client send one message. false otherwise.
         */
        public final boolean clientSendsOneMessage() {
            return this == UNARY || this == SERVER_STREAMING;
        }

        /**
         * Returns whether the server send one response message to the client.
         *
         * @return true, if server send one message. false otherwise.
         */
        public final boolean serverSendsOneMessage() {
            return this == UNARY || this == CLIENT_STREAMING;
        }
    }

    /**
     * A typed abstraction over message serialization and deserialization, a.k.a. marshalling and
     * unmarshalling.
     * <p>
     * Referenced from grpc-java implementation.
     * <p>
     *
     * @param <Message> type of serializable message
     */
    public interface Marshaller<Message> {

        /**
         * Produces an {@link InputStream} for given messages.
         *
         * @param value to serialize.
         * @return serialized value as stream of bytes.
         */
        public InputStream stream(Message value);

        /**
         * Produces an {@link Message} instance for given {@link InputStream}.
         *
         * @param stream of bytes for serialized value
         * @return parsed value
         */
        public Message parse(InputStream stream);
    }

    private MethodDescriptor(
            MethodType type,
            String fullMethodName,
            Marshaller<ReqT> requestMarshaller,
            Marshaller<RespT> responseMarshaller,
            com.google.protobuf.Descriptors.MethodDescriptor schemaDescriptor) {

        this.type = type;
        this.fullMethodName = fullMethodName;
        this.requestMarshaller = requestMarshaller;
        this.responseMarshaller = responseMarshaller;
        this.schemaDescriptor = schemaDescriptor;
    }

    /**
     * Returns type of the method.
     *
     * @return method type.
     */
    public MethodType getType() {
        return type;
    }

    /**
     * Returns fully qualified name of the method.
     *
     * @return method full name
     */
    public String getFullMethodName() {
        return fullMethodName;
    }

    /**
     * Parse a response payload from the given {@link InputStream}.
     *
     * @param input stream containing response message to parse.
     * @return parsed response message object.
     */
    public RespT parseResponse(InputStream input) {
        return responseMarshaller.parse(input);
    }

    /**
     * Convert a request message to an {@link InputStream}.
     *
     * @param requestMessage to serialize using the request {@link Marshaller}.
     * @return serialized request message.
     */
    public InputStream streamRequest(ReqT requestMessage) {
        return requestMarshaller.stream(requestMessage);
    }

    /**
     * Parse an incoming request message.
     *
     * @param input the serialized message as a byte stream.
     * @return a parsed instance of the message.
     */
    public ReqT parseRequest(InputStream input) {
        return requestMarshaller.parse(input);
    }

    /**
     * Serialize an outgoing response message.
     *
     * @param response the response message to serialize.
     * @return the serialized message as a byte stream.
     * @since 1.0.0
     */
    public InputStream streamResponse(RespT response) {
        return responseMarshaller.stream(response);
    }

    /**
     * Returns the marshaller for the request type.
     *
     * @return request marshaller instance.
     */
    public Marshaller<ReqT> getRequestMarshaller() {
        return requestMarshaller;
    }

    /**
     * Returns the marshaller for the response type.
     *
     * @return response marshaller instance.
     */
    public Marshaller<RespT> getResponseMarshaller() {
        return responseMarshaller;
    }

    /**
     * Returns the schema descriptor for this method.
     *
     * @return method schema descriptor.
     */
    public com.google.protobuf.Descriptors.MethodDescriptor getSchemaDescriptor() {
        return schemaDescriptor;
    }

    /**
     * Generate the fully qualified method name.
     *
     * @param fullServiceName the fully qualified service name that is prefixed with the package name
     * @param methodName      the short method name
     * @return fully qualified method name.
     */
    public static String generateFullMethodName(String fullServiceName, String methodName) {
        if (fullServiceName == null) {
            throw new RuntimeException("Full service name cannot be null");
        }
        return fullServiceName + "/" + methodName;
    }

    /**
     * Extract the fully qualified service name out of a fully qualified method name.
     *
     * @param fullMethodName fully qualified method name.
     * @return fully qualified service name.
     * @throws GrpcServerException if failed.
     */
    public static String extractFullServiceName(String fullMethodName) throws GrpcServerException {
        if (fullMethodName == null) {
            throw new GrpcServerException("Full method name cannot be null");
        }
        int index = fullMethodName.lastIndexOf('/');
        if (index == -1) {
            return null;
        }
        return fullMethodName.substring(0, index);
    }

    /**
     * Creates a new builder for a {@link MethodDescriptor}.
     *
     * @param <ReqT> Request Message type.
     * @param <RespT> Response Message type.
     * @return new builder instance.
     */
    public static <ReqT, RespT> Builder<ReqT, RespT> newBuilder() {
        return new Builder<>();
    }

    /**
     * A builder for a {@link MethodDescriptor}.
     *
     * @param <ReqT> InboundMessage Message Type
     * @param <RespT> OutboundMessage Message Type
     */
    public static final class Builder<ReqT, RespT> {

        private Marshaller<ReqT> requestMarshaller;
        private Marshaller<RespT> responseMarshaller;
        private MethodType type;
        private String fullMethodName;
        private com.google.protobuf.Descriptors.MethodDescriptor schemaDescriptor;

        private Builder() {
        }

        /**
         * Sets the request marshaller.
         *
         * @param requestMarshaller the marshaller to use.
         * @return builder instance.
         */
        public Builder<ReqT, RespT> setRequestMarshaller(Marshaller<ReqT> requestMarshaller) {
            this.requestMarshaller = requestMarshaller;
            return this;
        }

        /**
         * Sets the response marshaller.
         *
         * @param responseMarshaller the marshaller to use.
         * @return builder instance.
         */
        public Builder<ReqT, RespT> setResponseMarshaller(Marshaller<RespT> responseMarshaller) {
            this.responseMarshaller = responseMarshaller;
            return this;
        }

        /**
         * Sets the method type.
         *
         * @param type the type of the method.
         * @return builder instance.
         */
        public Builder<ReqT, RespT> setType(MethodType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the fully qualified (service and method) method name.
         *
         * @see MethodDescriptor#generateFullMethodName
         * @return builder instance.
         */
        public Builder<ReqT, RespT> setFullMethodName(String fullMethodName) {
            this.fullMethodName = fullMethodName;
            return this;
        }

        /**
         * Sets the schema descriptor for this builder.
         *
         * @param schemaDescriptor an object that describes the service structure.  Should be immutable.
         * @return builder instance.
         */
        public Builder<ReqT, RespT> setSchemaDescriptor(com.google.protobuf.Descriptors.MethodDescriptor
                                                                schemaDescriptor) {
            this.schemaDescriptor = schemaDescriptor;
            return this;
        }


        /**
         * Builds the method descriptor.
         *
         * @return new {@link MethodDescriptor} instance.
         */
        public MethodDescriptor<ReqT, RespT> build() {
            return new MethodDescriptor<>(
                    type,
                    fullMethodName,
                    requestMarshaller,
                    responseMarshaller,
                    schemaDescriptor);
        }
    }

}
