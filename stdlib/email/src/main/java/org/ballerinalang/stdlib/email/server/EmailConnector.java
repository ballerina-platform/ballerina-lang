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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contains the functionality to retrieve emails via consuming.
 *
 * @since 1.3.0
 */
public class EmailConnector {

    private static final Logger log = LoggerFactory.getLogger(EmailConnector.class);

    private EmailConsumer consumer;
    private AtomicBoolean isPollOperationOccupied = new AtomicBoolean(false);

    /**
     * Creates the email consumer.
     * @param properties Properties to connect to the server
     * @param emailListener Listener that polls emails from the server
     * @throws EmailConnectorException If the given protocol is invalid
     */
    public EmailConnector(Map<String, Object> properties, EmailListener emailListener) throws EmailConnectorException {
        log.debug("Email listener configurations: " + properties.keySet());
        consumer = new EmailConsumer(properties, emailListener);
    }

    /**
     * Polls to retrieve emails from the server.
     */
    public void poll() {
        if (isPollOperationOccupied.compareAndSet(false, true)) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Poll method invoked.");
                }
                consumer.consume();
            } catch (Exception e) {
                log.error("Error executing the polling cycle of RemoteFileSystemServer", e);
            } finally {
                isPollOperationOccupied.set(false);
            }
        } else {
            log.warn("A scheduled email polling job was skipped as the previous job was still processing.");
        }
    }

    protected EmailListener getEmailListener() {
        return consumer.getEmailListener();
    }
}
