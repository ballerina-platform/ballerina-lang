/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.test.connector.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ballerinalang.core.interpreter.Context;
import org.ballerinalang.core.model.Connector;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.natives.connectors.http.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@code AbstractHTTPAction} is the base class for all HTTP Connector Actions
 */
public abstract class AbstractHTTPAction extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHTTPAction.class);

    protected void prepareRequest(Connector connector, String path, CarbonMessage cMsg) {

        String uri = ((DummyHTTPConnector) connector).getServiceUri() + path;

        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            logger.error("Malformed url specified :  " + uri, e);
            return;
        }
        String host = url.getHost();
        int port = (url.getPort() == -1) ? 80 : url.getPort();

        cMsg.setProperty(Constants.HOST, host);
        cMsg.setProperty(Constants.PORT, port);
        cMsg.setProperty(Constants.TO, url.getPath());

        if (cMsg.getProperty(Constants.PROTOCOL) == null) {
            cMsg.setProperty(Constants.PROTOCOL, Constants.PROTOCOL_HTTP);
        }

        if (port != 80) {
            cMsg.getHeaders().set(Constants.HOST, host + ":" + port);
        } else {
            cMsg.getHeaders().set(Constants.HOST, host);
        }
    }

    protected BValue executeAction(Context context, CarbonMessage message) {

        BalConnectorCallback balConnectorCallback = new BalConnectorCallback(context);
        // Handle the message built scenario
        if (message.isAlreadyRead()) {
            MessageDataSource messageDataSource = message.getMessageDataSource();
            if (messageDataSource != null) {
                messageDataSource.serializeData();
                message.setEndOfMsgAdded(true);
                message.getHeaders().remove(Constants.HTTP_CONTENT_LENGTH);
                message.getHeaders().set(Constants.HTTP_CONTENT_LENGTH, String.valueOf(message.getFullMessageLength()));

            } else {
                logger.error("Message is already built but cannot find the MessageDataSource");
            }
        }
        balConnectorCallback.responseArrived = true;
        return balConnectorCallback.valueRef;
    }
}
