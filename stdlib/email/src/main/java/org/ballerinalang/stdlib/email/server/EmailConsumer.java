/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.email.server;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.stdlib.email.client.EmailAccessClient;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Provides the capability to read an email and forward it to a listener.
 *
 * @since 1.3.0
 */
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private EmailListener emailListener;
    private ObjectValue client;

    /**
     * Constructor for the EmailConsumer.
     *
     * @param emailProperties Map of property values
     * @param listener Forwards the received emails to Ballerina code
     */
    public EmailConsumer(Map<String, Object> emailProperties, EmailListener listener) {
        this.emailListener = listener;
        String host = (String) emailProperties.get(EmailConstants.PROPS_HOST);
        String username = (String) emailProperties.get(EmailConstants.PROPS_USERNAME);
        String password = (String) emailProperties.get(EmailConstants.PROPS_PASSWORD);
        String protocol = (String) emailProperties.get(EmailConstants.PROPS_PROTOCOL);
        MapValue protocolConfig = (MapValue) emailProperties.get(EmailConstants.PROTOCOL_CONFIG);
        if (protocol.equals(EmailConstants.IMAP)) {
            client = BallerinaValues.createObjectValue(EmailConstants.EMAIL_PACKAGE_ID, EmailConstants.IMAP_CLIENT,
                    host, username, password, (MapValue<Object, Object>) protocolConfig);
            EmailAccessClient.initImapClientEndpoint(client, host, username, password,
                    (MapValue<Object, Object>) protocolConfig);
        } else if (protocol.equals(EmailConstants.POP)) {
            client = BallerinaValues.createObjectValue(EmailConstants.EMAIL_PACKAGE_ID, EmailConstants.POP_CLIENT,
                    host, username, password, (MapValue<Object, Object>) protocolConfig);
            EmailAccessClient.initPopClientEndpoint(client, host, username, password,
                    (MapValue<Object, Object>) protocolConfig);
        } else {
            String errorMsg = "Protocol should either be 'IMAP' or 'POP'.";
            final EmailConnectorException e = new EmailConnectorException(errorMsg);
            emailListener.onError(BallerinaErrors.createError(e));
        }

    }

    /**
     * Read emails from the Email client and pass to the listener.
     */
    public void consume() {
        if (log.isDebugEnabled()) {
            log.debug("Consumer thread name: " + Thread.currentThread().getName());
            log.debug("Consumer hashcode: " + this.hashCode());
            log.debug("Polling for an email...");
        }
        Object message = EmailAccessClient.readMessage(client, EmailConstants.DEFAULT_STORE_LOCATION);
        if (message != null) {
            if (message instanceof MapValue) {
                emailListener.onMessage(new EmailEvent(message));
            } else if (message instanceof ErrorValue) {
                emailListener.onError(message);
            } else {
                emailListener.onError(BallerinaErrors.createError(
                        new EmailConnectorException("Received an undefined message from email server.")));
            }
        } else {
            log.debug("No emails found in the inbox.");
        }

    }

}
