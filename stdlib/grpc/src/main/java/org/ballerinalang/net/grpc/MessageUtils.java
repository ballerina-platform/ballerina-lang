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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Locale;

import static org.ballerinalang.net.grpc.GrpcConstants.CONTENT_TYPE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_GRPC_PKG_ID;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.Status.Code.UNKNOWN;

/**
 * Util methods to generate protobuf message.
 *
 * @since 1.0.0
 */
public class MessageUtils {

    private static final String UNKNOWN_ERROR_DETAIL = "Unknown error occurred";

    /** maximum buffer to be read is 16 KB. */
    private static final int MAX_BUFFER_LENGTH = 16384;
    private static final String GOOGLE_PROTOBUF_EMPTY = "google.protobuf.Empty";

    public static ObjectValue getHeaderObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_GRPC_PKG_ID, "Headers");
    }

    static boolean headersRequired(AttachedFunction function) {
        if (function == null || function.getParameterType() == null) {
            throw new RuntimeException("Invalid resource input arguments");
        }
        boolean headersRequired = false;
        for (BType paramType : function.getParameterType()) {
            if (paramType != null && "Headers".equals(paramType.getName()) &&
                    paramType.getPackage() != null && PROTOCOL_PACKAGE_GRPC.equals(paramType.getPackage().getName())) {
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

    public static StreamObserver getResponseObserver(ObjectValue refType) {
        Object observerObject = refType.getNativeData(GrpcConstants.RESPONSE_OBSERVER);
        if (observerObject instanceof StreamObserver) {
            return ((StreamObserver) observerObject);
        }
        return null;
    }
    
    /**
     * Returns error struct of input type
     * Error type is generic ballerina error type. This utility method is used inside Observer onError
     * method to construct error struct from message.
     *
     * @param error     this is StatusRuntimeException send by opposite party.
     * @return error value.
     */
    public static ErrorValue getConnectorError(Throwable error) {
        String reason;
        String message;
        if (error instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) error;
            reason = statusException.getStatus().getReason();
            String errorDescription = statusException.getStatus().getDescription();
            if (errorDescription != null) {
                message =  statusException.getStatus().getDescription();
            } else if (statusException.getStatus().getCause() != null) {
                message = statusException.getStatus().getCause().getMessage();
            } else {
                message = UNKNOWN_ERROR_DETAIL;
            }
        } else {
            if (error.getMessage() == null) {
                reason = GrpcConstants.UNKNOWN_ERROR;
                message = UNKNOWN_ERROR_DETAIL;
            } else {
                reason = GrpcConstants.INTERNAL_ERROR;
                message = error.getMessage();
            }
        }
        return BallerinaErrors.createError(reason, message);
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
        } else {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
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
    static boolean isGrpcContentType(String contentType) {
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

    static Status httpStatusToGrpcStatus(int httpStatusCode) {
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

    static int statusCodeToHttpCode(Status.Code code) {
        switch (code) {
            case CANCELLED:
                return 499; // Client Closed Request
            case INVALID_ARGUMENT:
            case FAILED_PRECONDITION:
            case OUT_OF_RANGE:
                return 400; // Bad Request
            case DEADLINE_EXCEEDED:
                return 504; // Gateway timeout
            case NOT_FOUND:
                return 404; // Not Found
            case ALREADY_EXISTS:
            case ABORTED:
                return 409; // Conflicts
            case PERMISSION_DENIED:
                return 403; // Forbidden
            case UNAUTHENTICATED:
                return 401; // Unauthorized
            case UNIMPLEMENTED:
                return 501; // Not Implemented
            case UNAVAILABLE:
                return 503; // Service Unavailable
            default:
                return 500; // Internal Server Error
        }
    }

    /**
     * Return Mapping Http Status code to gRPC status code.
     * @see <a href="https://github.com/grpc/grpc/blob/master/doc/statuscodes.md">gRPC status code</a>
     *
     * @param code gRPC Status code
     * @return Http status code
     */
    public static int getMappingHttpStatusCode(int code) {
        switch(code) {
            case 0:
                return HttpResponseStatus.OK.code();
            case 1:
                // The operation was cancelled
                return 499;
            case 3:
            case 9:
            case 11:
                return HttpResponseStatus.BAD_REQUEST.code();
            case 4:
                return HttpResponseStatus.GATEWAY_TIMEOUT.code();
            case 5:
                return HttpResponseStatus.NOT_FOUND.code();
            case 6:
            case 10:
                return HttpResponseStatus.CONFLICT.code();
            case 7:
                return HttpResponseStatus.FORBIDDEN.code();
            case 8:
                return HttpResponseStatus.TOO_MANY_REQUESTS.code();
            case 12:
                return HttpResponseStatus.NOT_IMPLEMENTED.code();
            case 14:
                return HttpResponseStatus.SERVICE_UNAVAILABLE.code();
            case 15:
                return HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
            case 16:
                return HttpResponseStatus.UNAUTHORIZED.code();
            case 2:
            case 13:
            default:
                return HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
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
