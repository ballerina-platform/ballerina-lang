/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.jms;

import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.jms.actions.utils.Constants;
import org.wso2.carbon.transport.jms.callback.JMSCallback;
import org.wso2.carbon.transport.jms.contract.JMSListener;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Message;

/**
 * JMS Connector listener for Ballerina
 */
public class JMSListenerImpl implements JMSListener {

    private Resource resource;

    public JMSListenerImpl (Resource resource) {
        this.resource = resource;
    }

    @Override
    public void onMessage(Message carbonMessage, JMSCallback jmsCallback) {
        if (jmsCallback != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, jmsCallback.getAcknowledgementMode());

            ConnectorFuture future = Executor
                    .submit(resource, properties, JMSDispatcher.getSignatureParameters(resource, carbonMessage));

            ConnectorFutureListener futureListener = new JMSConnectorFutureListener(jmsCallback);
            future.setConnectorFutureListener(futureListener);
        } else {
            Executor.submit(resource, null, JMSDispatcher.getSignatureParameters(resource, carbonMessage));
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

}
