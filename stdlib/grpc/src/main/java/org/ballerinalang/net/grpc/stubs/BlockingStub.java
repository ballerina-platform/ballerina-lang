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
package org.ballerinalang.net.grpc.stubs;

import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.ClientCall;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.http.DataContext;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import java.util.Arrays;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_BOOL_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_BYTES_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_DOUBLE_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_FLOAT_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_INT32_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_INT64_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_STRING_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_UINT32_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_UINT64_MESSAGE;
import static org.ballerinalang.net.http.HttpConstants.PACKAGE_BALLERINA_BUILTIN;
import static org.ballerinalang.net.http.HttpConstants.STRUCT_GENERIC_ERROR;

/**
 * This class handles Blocking client connection.
 *
 * @since 0.980.0
 */
public class BlockingStub extends AbstractStub {

    private static final BTupleType RESP_TUPLE_TYPE = new BTupleType(Arrays.asList(BTypes.typeAny, BTypes.typeAny));

    public BlockingStub(HttpClientConnector clientConnector, Struct endpointConfig) {
        super(clientConnector, endpointConfig);
    }

    /**
     * Executes a unary call and blocks on the response.
     *
     * @param request          request message.
     * @param methodDescriptor method descriptor
     */
    public void executeUnary(Message request, MethodDescriptor methodDescriptor,
                                           DataContext dataContext) {
        ClientCall call = new ClientCall(getConnector(), createOutboundRequest(request
                .getHeaders()), methodDescriptor);
        call.start(new CallBlockingListener(dataContext, methodDescriptor.getSchemaDescriptor()
                .getOutputType()));
        try {
            call.sendMessage(request);
            call.halfClose();
        } catch (Exception e) {
            throw cancelThrow(call, e);
        }
    }

    /**
     *  Callbacks for receiving headers, response messages and completion status in blocking calls.
     */
    private static final class CallBlockingListener implements Listener {

        private final DataContext dataContext;
        private final Descriptors.Descriptor outputDescriptor;
        private Message value;

        // Non private to avoid synthetic class
        private CallBlockingListener(DataContext dataContext, Descriptors.Descriptor outputDescriptor) {
            this.dataContext = dataContext;
            this.outputDescriptor = outputDescriptor;
        }

        @Override
        public void onHeaders(HttpHeaders headers) {
            // Headers are processed at client connector listener. Do not need to further process.
        }

        @Override
        public void onMessage(Message value) {
            if (this.value != null) {
                throw Status.Code.INTERNAL.toStatus().withDescription("More than one value received for unary call")
                        .asRuntimeException();
            }
            this.value = value;
        }

        @Override
        public void onClose(Status status, HttpHeaders trailers) {
            BMap<String, BValue> httpConnectorError = null;
            BRefValueArray inboundResponse = null;
            if (status.isOk()) {
                if (value == null) {
                    // No value received so mark the future as an error
                    httpConnectorError = MessageUtils.getConnectorError(dataContext.context,
                            Status.Code.INTERNAL.toStatus()
                                    .withDescription("No value received for unary call").asRuntimeException());
                } else {
                    BValue responseBValue = MessageUtils.generateRequestStruct(value, dataContext.context
                            .getProgramFile(), outputDescriptor.getName(), getBalType(outputDescriptor.getName(),
                            dataContext.context));
                    // Set response headers, when response headers exists in the message context.
                    BMap<String, BValue> headerStruct = BLangConnectorSPIUtil.createBStruct(dataContext.context
                            .getProgramFile(), PROTOCOL_STRUCT_PACKAGE_GRPC, "Headers");
                    headerStruct.addNativeData(MESSAGE_HEADERS, value.getHeaders());
                    BRefValueArray contentTuple = new BRefValueArray(RESP_TUPLE_TYPE);
                    contentTuple.add(0, (BRefType) responseBValue);
                    contentTuple.add(1, headerStruct);
                    inboundResponse = contentTuple;
                }
            } else {
                httpConnectorError = MessageUtils.getConnectorError(dataContext.context, status.asRuntimeException());
            }
            if (inboundResponse != null) {
                dataContext.context.setReturnValues(inboundResponse);
            } else if (httpConnectorError != null) {
                dataContext.context.setReturnValues(httpConnectorError);
            } else {
                BMap<String, BValue> err = BLangConnectorSPIUtil.createBStruct(dataContext.context,
                        PACKAGE_BALLERINA_BUILTIN, STRUCT_GENERIC_ERROR, "HttpClient failed");
                dataContext.context.setReturnValues(err);
            }
            dataContext.callback.notifySuccess();
        }

        /**
         * Returns corresponding Ballerina type for the proto buffer type.
         *
         * @param protoType Protocol buffer type
         * @param context   Ballerina Context
         * @return .
         */
        private static BType getBalType(String protoType, Context context) {
            if (protoType.equalsIgnoreCase(WRAPPER_DOUBLE_MESSAGE) || protoType
                    .equalsIgnoreCase(WRAPPER_FLOAT_MESSAGE)) {
                return BTypes.typeFloat;
            } else if (protoType.equalsIgnoreCase(WRAPPER_INT32_MESSAGE) || protoType
                    .equalsIgnoreCase(WRAPPER_INT64_MESSAGE) || protoType
                    .equalsIgnoreCase(WRAPPER_UINT32_MESSAGE) || protoType
                    .equalsIgnoreCase(WRAPPER_UINT64_MESSAGE)) {
                return BTypes.typeInt;
            } else if (protoType.equalsIgnoreCase(WRAPPER_BOOL_MESSAGE)) {
                return BTypes.typeBoolean;
            } else if (protoType.equalsIgnoreCase(WRAPPER_STRING_MESSAGE)) {
                return BTypes.typeString;
            } else if (protoType.equalsIgnoreCase(WRAPPER_BYTES_MESSAGE)) {
                return BTypes.typeBlob;
            } else {
                return context.getProgramFile().getEntryPackage().getStructInfo(protoType).getType();
            }
        }
    }
}
