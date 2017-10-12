/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.net.jms;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.jms.callback.JMSCallback;

/**
 * {@code JMSConnectorFutureListener} is the responsible for acting on notifications received from Ballerina side.
 *
 * @since 0.94
 */
public class JMSConnectorFutureListener implements ConnectorFutureListener {
    private static final Logger log = LoggerFactory.getLogger(JMSConnectorFutureListener.class);
    private JMSCallback jmsCallback;
    /** future will get notified by the jms native methods and from the Ballerina engine when the Resource invocation
     * is over or when an error occurred. This parameter will maintain the state of the JMSConnectorFutureListener
     * and Callback will notified only if its not already notified
     * */
    private boolean isInformedCallback;

    public JMSConnectorFutureListener(JMSCallback jmsCallback) {
        this.jmsCallback = jmsCallback;
        this.isInformedCallback = Boolean.FALSE;
    }

    @Override
    public void notifySuccess() {
        informCallback(Boolean.TRUE);
    }

    @Override
    public void notifyReply(BValue response) {
        informCallback(Boolean.parseBoolean(response.stringValue()));
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {
        informCallback(Boolean.FALSE);
    }

    private void informCallback(boolean status) {
        if (!isInformedCallback) {
            isInformedCallback = Boolean.TRUE;
            jmsCallback.done(status);
        }
    }
}
