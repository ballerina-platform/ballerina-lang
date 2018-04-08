/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.net.jms;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * JMS Message listener implementation.
 */
public class JmsMessageListenerImpl implements MessageListener {

    private Resource resource;

    private ResponseCallback callback;

    public JmsMessageListenerImpl(Resource resource) {
        this.resource = resource;
        this.callback = new ResponseCallback();
    }

    @Override
    public void onMessage(Message message) {
        Map<String, Object> properties = new HashMap<>();
        Executor.submit(resource, callback, properties, null, getSignatureParameters(message));
    }

    private BValue[] getSignatureParameters(Message jmsMessage) {
        ProgramFile programFile = resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        BStruct consumerEndpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                                                                       Constants.BALLERINA_PACKAGE_JMS,
                                                                       Constants.QUEUE_CONSUMER_ENDPOINT);
        BStruct message = BLangConnectorSPIUtil.createBStruct(programFile,
                                                              Constants.BALLERINA_PACKAGE_JMS,
                                                              Constants.JMS_MESSAGE_STRUCT_NAME);
        message.addNativeData(Constants.JMS_MESSAGE_OBJECT, jmsMessage);

        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];

        bValues[0] = consumerEndpoint;
        bValues[1] = message;

        return bValues;
    }


    private static class ResponseCallback implements CallableUnitCallback {

        @Override
        public void notifySuccess() {

        }

        @Override
        public void notifyFailure(BStruct bStruct) {

        }
    }
}
