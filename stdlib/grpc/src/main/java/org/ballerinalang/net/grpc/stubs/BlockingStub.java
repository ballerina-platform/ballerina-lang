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

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.net.grpc.ClientCall;
import org.ballerinalang.net.grpc.DataContext;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.Status;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import java.util.Arrays;

import static org.ballerinalang.net.grpc.GrpcConstants.HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_GRPC_PKG_ID;

/**
 * This class handles Blocking client connection.
 *
 * @since 0.980.0
 */
public class BlockingStub extends AbstractStub {

    public BlockingStub(HttpClientConnector clientConnector, String url) {
        super(clientConnector, url);
    }

    /**
     * Executes a unary call and blocks on the response.
     *
     * @param request          request message.
     * @param methodDescriptor method descriptor
     * @param dataContext data context
     * @throws Exception if an error occur while processing client call.
     */
    public void executeUnary(Message request, MethodDescriptor methodDescriptor,
                                           DataContext dataContext) throws Exception {
        ClientCall call = new ClientCall(getConnector(), createOutboundRequest(request
                .getHeaders()), methodDescriptor, dataContext);
        call.start(new CallBlockingListener(dataContext));
        try {
            call.sendMessage(request);
            call.halfClose();
        } catch (Exception e) {
            cancelThrow(call, e);
        }
    }

    /**
     *  Callbacks for receiving headers, response messages and completion status in blocking calls.
     */
    private static final class CallBlockingListener implements Listener {

        private final DataContext dataContext;
        private Message value;

        // Non private to avoid synthetic class
        private CallBlockingListener(DataContext dataContext) {
            this.dataContext = dataContext;
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
            ErrorValue httpConnectorError = null;
            TupleValueImpl inboundResponse = null;
            if (status.isOk()) {
                if (value == null) {
                    // No value received so mark the future as an error
                    httpConnectorError = MessageUtils.getConnectorError(Status.Code.INTERNAL.toStatus()
                                    .withDescription("No value received for unary call").asRuntimeException());
                } else {
                    Object responseBValue = value.getbMessage();
                    // Set response headers, when response headers exists in the message context.
                    ObjectValue headerObject = BallerinaValues.createObjectValue(PROTOCOL_GRPC_PKG_ID, HEADERS);
                    headerObject.addNativeData(MESSAGE_HEADERS, value.getHeaders());
                    TupleValueImpl contentTuple = new TupleValueImpl(
                            new BTupleType(Arrays.asList(BTypes.typeAnydata, headerObject.getType())));
                    contentTuple.add(0, responseBValue);
                    contentTuple.add(1, headerObject);
                    inboundResponse = contentTuple;
                }
            } else {
                httpConnectorError = MessageUtils.getConnectorError(status.asRuntimeException());
            }
            if (inboundResponse != null) {
                dataContext.getCallback().setReturnValues(inboundResponse);
            } else {
                dataContext.getCallback().setReturnValues(httpConnectorError);
            }
            dataContext.getCallback().notifySuccess();
        }
    }
}
