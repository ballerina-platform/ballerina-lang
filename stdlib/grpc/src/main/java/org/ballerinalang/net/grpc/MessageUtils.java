/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.CONTENT_TYPE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.Status.Code.INTERNAL;
import static org.ballerinalang.net.grpc.Status.Code.UNKNOWN;

/**
 * Util methods to generate protobuf message.
 *
 * @since 1.0.0
 */
public class MessageUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MessageUtils.class);
    private static final String UNKNOWN_ERROR = "Unknown Error";

    /** maximum buffer to be read is 16 KB. */
    private static final int MAX_BUFFER_LENGTH = 16384;
    private static final String GOOGLE_PROTOBUF_EMPTY = "google.protobuf.Empty";

    public static BMap<String, BValue> getHeaderStruct(ProgramFile programFile) {
        return BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_STRUCT_PACKAGE_GRPC, "Headers");
    }

    public static boolean headersRequired(Resource resource) {
        if (resource == null || resource.getParamDetails() == null) {
            throw new RuntimeException("Invalid resource input arguments");
        }
        boolean headersRequired = false;
        for (ParamDetail detail : resource.getParamDetails()) {
            BType paramType = detail.getVarType();
            if (paramType != null && PROTOCOL_STRUCT_PACKAGE_GRPC.equals(paramType.getPackagePath()) &&
                    "Headers".equals(paramType.getName())) {
                headersRequired = true;
                break;
            }
        }
        return headersRequired;
    }

    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[MAX_BUFFER_LENGTH];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    public static StreamObserver getResponseObserver(BRefType refType) {
        Object observerObject = null;
        if (refType instanceof BMap) {
            observerObject = ((BMap<String, BValue>) refType).getNativeData(GrpcConstants.RESPONSE_OBSERVER);
        }
        if (observerObject instanceof StreamObserver) {
            return ((StreamObserver) observerObject);
        }
        return null;
    }

    public static BError getConnectorError(Throwable throwable) {
        return getConnectorError(BTypes.typeError, throwable);
    }
    
    /**
     * Returns error struct of input type
     * Error type is generic ballerina error type. This utility method is used inside Observer onError
     * method to construct error struct from message.
     *
     * @param errorType this is ballerina generic error type.
     * @param error     this is StatusRuntimeException send by opposite party.
     * @return error value.
     */
    public static BError getConnectorError(BErrorType errorType, Throwable error) {
        BErrorType errType = Optional.ofNullable(errorType).orElse(BTypes.typeError);
        BMap<String, BValue> refData = new BMap<>(errType.detailsType);
        String reason = "{ballerina/grpc}";
        if (error instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) error;
            reason = reason + statusException.getStatus().getCode().name();
            String errorDescription = statusException.getStatus().getDescription();
            if (errorDescription != null) {
                refData.put("message", new BString(statusException.getStatus().getDescription()));
            } else if (statusException.getStatus().getCause() != null) {
                refData.put("message", new BString(statusException.getStatus().getCause().getMessage()));
            } else {
                refData.put("message", new BString(UNKNOWN_ERROR));
            }
        } else {
            if (error.getMessage() == null) {
                reason = reason + UNKNOWN.name();
                refData.put("message", new BString(UNKNOWN_ERROR));
            } else {
                reason = reason + INTERNAL.name();
                refData.put("message", new BString(error.getMessage()));
            }
        }
        return new BError(errorType, reason, refData);
    }
    
    /**
     * Handles failures in GRPC callable unit callback.
     *
     * @param streamObserver observer used the send the error back
     * @param error          error message struct
     */
    static void handleFailure(StreamObserver streamObserver, BError error) {
        String errorMsg = error.stringValue();
        ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        if (streamObserver != null) {
            streamObserver.onError(new Message(new StatusRuntimeException(Status.fromCodeValue(Status
                    .Code.INTERNAL.value()).withDescription(errorMsg))));
        }
    }
    
    /**
     * Returns wire type corresponding to the field descriptor type.
     * <p>
     * 0 -> int32, int64, uint32, uint64, sint32, sint64, bool, enum
     * 1 -> fixed64, sfixed64, double
     * 2 -> string, bytes, embedded messages, packed repeated fields
     * 5 -> fixed32, sfixed32, float
     *
     * @param fieldType field descriptor type
     * @return wire type
     */
    static int getFieldWireType(Descriptors.FieldDescriptor.Type fieldType) {
        if (fieldType == null) {
            return ServiceProtoConstants.INVALID_WIRE_TYPE;
        }
        Integer wireType = GrpcConstants.WIRE_TYPE_MAP.get(fieldType.toProto());
        if (wireType != null) {
            return wireType;
        } else {
            // Returns embedded messages, packed repeated fields message type, if field type doesn't map with the
            // predefined proto types.
            return ServiceProtoConstants.MESSAGE_WIRE_TYPE;
        }
    }

    static void setNestedMessages(Descriptors.Descriptor resMessage, MessageRegistry messageRegistry) {
        for (Descriptors.Descriptor nestedType : resMessage.getNestedTypes()) {
            messageRegistry.addMessageDescriptor(nestedType.getName(), nestedType);
        }
        for (Descriptors.FieldDescriptor msgField : resMessage.getFields()) {
            if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(msgField.getType())) {
                Descriptors.Descriptor msgType = msgField.getMessageType();
                messageRegistry.addMessageDescriptor(msgType.getName(), msgType);
            }
        }
    }

    /**
     * Util method to get method type.
     *
     * @param methodDescriptorProto method descriptor proto.
     * @return service method type.
     */
    public static MethodDescriptor.MethodType getMethodType(DescriptorProtos.MethodDescriptorProto
                                                                    methodDescriptorProto) {
        if (methodDescriptorProto.getClientStreaming() && methodDescriptorProto.getServerStreaming()) {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        } else if (!(methodDescriptorProto.getClientStreaming() || methodDescriptorProto.getServerStreaming())) {
            return MethodDescriptor.MethodType.UNARY;
        } else if (methodDescriptorProto.getServerStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else if (methodDescriptorProto.getClientStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else {
            return MethodDescriptor.MethodType.UNKNOWN;
        }
    }
    
    /**
     * Checks whether method has response message.
     *
     * @param messageDescriptor Message Descriptor
     * @return true if method response is empty, false otherwise
     */
    public static boolean isEmptyResponse(Descriptors.Descriptor messageDescriptor) {
        if (messageDescriptor == null) {
            return false;
        }
        return GOOGLE_PROTOBUF_EMPTY.equals(messageDescriptor.getFullName());
    }

    /** Closes an InputStream, ignoring IOExceptions. */
    static void closeQuietly(InputStream message) {
        try {
            message.close();
        } catch (IOException ignore) {
            // do nothing
        }
    }

    /**
     * Indicates whether or not the given value is a valid gRPC content-type.
     *
     * <p>
     * Referenced from grpc-java implementation.
     *
     * @param contentType gRPC content type
     * @return is valid content type
     */
    public static boolean isGrpcContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        if (CONTENT_TYPE_GRPC.length() > contentType.length()) {
            return false;
        }
        contentType = contentType.toLowerCase(Locale.ENGLISH);
        if (!contentType.startsWith(CONTENT_TYPE_GRPC)) {
            return false;
        }
        if (contentType.length() == CONTENT_TYPE_GRPC.length()) {
            // The strings match exactly.
            return true;
        }
        // The contentType matches, but is longer than the expected string.
        // We need to support variations on the content-type (e.g. +proto, +json) as defined by the gRPC wire spec.
        char nextChar = contentType.charAt(CONTENT_TYPE_GRPC.length());
        return nextChar == '+' || nextChar == ';';
    }

    public static HttpWsConnectorFactory createHttpWsConnectionFactory() {
        return new DefaultHttpWsConnectorFactory();
    }

    public static HttpCarbonMessage createHttpCarbonMessage(boolean isRequest) {
        HttpCarbonMessage httpCarbonMessage;
        if (isRequest) {
            httpCarbonMessage = new HttpCarbonMessage(
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
        } else {
            httpCarbonMessage = new HttpCarbonMessage(
                    new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        }
        return httpCarbonMessage;
    }

    public static Status httpStatusToGrpcStatus(int httpStatusCode) {
        return httpStatusToGrpcCode(httpStatusCode).toStatus()
                .withDescription("HTTP status code " + httpStatusCode);
    }

    private static Status.Code httpStatusToGrpcCode(int httpStatusCode) {
        if (httpStatusCode >= 100 && httpStatusCode < 200) {
            // 1xx. These headers should have been ignored.
            return Status.Code.INTERNAL;
        }
        switch (httpStatusCode) {
            case HttpURLConnection.HTTP_BAD_REQUEST:  // 400
            case 431:
                return Status.Code.INTERNAL;
            case HttpURLConnection.HTTP_UNAUTHORIZED:  // 401
                return Status.Code.UNAUTHENTICATED;
            case HttpURLConnection.HTTP_FORBIDDEN:  // 403
                return Status.Code.PERMISSION_DENIED;
            case HttpURLConnection.HTTP_NOT_FOUND:  // 404
                return Status.Code.UNIMPLEMENTED;
            case 429:
            case HttpURLConnection.HTTP_BAD_GATEWAY:  // 502
            case HttpURLConnection.HTTP_UNAVAILABLE:  // 503
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:  // 504
                return Status.Code.UNAVAILABLE;
            default:
                return UNKNOWN;
        }
    }

    /**
     * Reads an entire {@link HttpContent} to a new array. After calling this method, the buffer
     * will contain no readable bytes.
     */
    private static byte[] readArray(HttpContent httpContent) {
        if (httpContent == null || httpContent.content() == null) {
            throw new RuntimeException("Http content is null");
        }
        int length = httpContent.content().readableBytes();
        byte[] bytes = new byte[length];
        httpContent.content().readBytes(bytes, 0, length);
        return bytes;
    }

    /**
     * Reads the entire {@link HttpContent} to a new {@link String} with the given charset.
     */
    static String readAsString(HttpContent httpContent, Charset charset) {
        if (charset == null) {
            throw new RuntimeException("Charset cannot be null");
        }
        byte[] bytes = readArray(httpContent);
        return new String(bytes, charset);
    }

    private MessageUtils() {
    }
}
