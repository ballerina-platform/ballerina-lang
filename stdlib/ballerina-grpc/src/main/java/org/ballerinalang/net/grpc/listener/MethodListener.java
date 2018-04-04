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
package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.net.grpc.MessageUtils.getProgramFile;

/**
 * Abstract Method listener.
 * This provide method for all method listeners.
 *
 * @since 1.0.0
 */
abstract class MethodListener {
    
    private Descriptors.MethodDescriptor methodDescriptor;
    private static final Logger LOG = LoggerFactory.getLogger(MethodListener.class);
    
    MethodListener(Descriptors.MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }
    
    /**
     * Returns endpoint instance which is used to respond to the client.
     *
     * @param responseObserver client responder instance.
     * @return instance of endpoint type.
     */
    BValue getConnectionParameter(Resource resource, StreamObserver<Message> responseObserver) {
        ProgramFile programFile = getProgramFile(resource);
        // generate client responder struct on request message with response observer and response msg type.
        BStruct clientEndpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, MessageConstants.CLIENT_RESPONDER);
        clientEndpoint.setIntField(0, responseObserver.hashCode());
        clientEndpoint.addNativeData(MessageConstants.RESPONSE_OBSERVER, responseObserver);
        clientEndpoint.addNativeData(MessageConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());
        
        // create endpoint type instance on request.
        BStruct endpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, MessageConstants.SERVICE_ENDPOINT_TYPE);
        endpoint.setRefField(0, clientEndpoint);
        return endpoint;
    }
    
    /**
     * Returns BValue object corresponding to the protobuf request message.
     *
     * @param requestMessage protobuf request message.
     * @return b7a message.
     */
    BValue getRequestParameter(Resource resource, Message requestMessage) {
        if (resource == null || resource.getParamDetails() == null || resource.getParamDetails().size() > 2) {
            throw new RuntimeException("Invalid resource input arguments. arguments must not be greater than two");
        }
        
        if (resource.getParamDetails().size() == 2) {
            BType requestType = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_PARAM_INDEX)
                    .getVarType();
            String requestName = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_PARAM_INDEX)
                    .getVarName();
            return MessageUtils.generateRequestStruct(requestMessage, getProgramFile(resource), requestName,
                    requestType);
        } else {
            return null;
        }
    }
    
    /**
     * Checks whether service method has a response message.
     *
     * @return true if method response is empty, false otherwise
     */
    boolean isEmptyResponse() {
        return methodDescriptor != null && MessageUtils.isEmptyResponse(methodDescriptor.getOutputType());
    }

    void onErrorInvoke(Resource resource, StreamObserver<Message> responseObserver, Throwable t) {
        if (resource == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOG.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(resource, responseObserver);
        if (paramDetails.size() != 2) {
            String message = "Error in onError resource definition. It must have two input params, but have "
                    + paramDetails.size();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        BType errorType = paramDetails.get(1).getVarType();
        BStruct errorStruct = MessageUtils.getConnectorError((BStructType) errorType, t);
        signatureParams[1] = errorStruct;
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, Boolean.FALSE);
        Executor.submit(resource, callback, null, null, signatureParams);
    }

    void onMessageInvoke(Resource resource, Message request, StreamObserver<Message> responseObserver) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(resource, responseObserver);
        BValue requestParam = getRequestParameter(resource, request);
        if (requestParam != null) {
            signatureParams[1] = requestParam;
        }
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, isEmptyResponse());
        Executor.submit(resource, callback, null, null, signatureParams);
    }
}
