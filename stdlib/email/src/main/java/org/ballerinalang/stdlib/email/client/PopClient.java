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

package org.ballerinalang.stdlib.email.client;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.stdlib.email.util.BallerinaPopException;
import org.ballerinalang.stdlib.email.util.ImapConstants;
import org.ballerinalang.stdlib.email.util.PopConstants;
import org.ballerinalang.stdlib.email.util.PopUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Contains functionality of POP Client.
 */
public class PopClient {

    private static final Logger log = LoggerFactory.getLogger(PopClient.class);

    private PopClient() {
        // Singleton class
    }

    /**
     * Initializes the ObjectValue object with the POP Properties.
     * @param clientEndpoint Represents the POP Client class
     * @param config Properties required to configure the POP Session
     * @param isPop True if the protocol is POP3 and false otherwise (if protocol is IMAP)
     * @throws BallerinaPopException If an error occurs in POP client
     */
    public static void initClientEndpoint(ObjectValue clientEndpoint, MapValue<Object, Object> config, boolean isPop)
            throws BallerinaPopException {
        log.debug("[PopClient][InitClient] Calling getProperties");
        Properties properties = PopUtil.getProperties(config, isPop);
        Session session = Session.getInstance(properties, null);
        try {
            Store store;

            if (isPop) {
                store = session.getStore(PopConstants.POP_PROTOCOL);
                clientEndpoint.addNativeData(PopConstants.PROPS_HOST,
                        properties.getProperty(PopConstants.PROPS_POP_HOST));
            } else {
                store = session.getStore(ImapConstants.IMAP_PROTOCOL);
                clientEndpoint.addNativeData(PopConstants.PROPS_HOST,
                        properties.getProperty(ImapConstants.PROPS_IMAP_HOST));
            }
            clientEndpoint.addNativeData(PopConstants.PROPS_STORE, store);
            clientEndpoint.addNativeData(PopConstants.PROPS_USERNAME,
                    properties.getProperty(PopConstants.PROPS_USERNAME));
            clientEndpoint.addNativeData(PopConstants.PROPS_PASSWORD,
                    properties.getProperty(PopConstants.PROPS_PASSWORD));
        } catch (NoSuchProviderException e) {
            log.error("Failed to read message : ", e);
            throw new BallerinaPopException("Error occurred while accessing POP server", e);
        }
    }

    /**
     * Read emails from the server.
     * @param clientConnector Represents the POP Client class
     * @param filter Criteria which are used to read emails
     * @param isPop True if the protocol is POP3 and false otherwise (if protocol is IMAP)
     * @return MapValue Returns the type supported to Ballerina
     * @throws BallerinaPopException If an error occurs in POP client
     */
    public static MapValue readMessage(ObjectValue clientConnector, MapValue<Object, Object> filter, boolean isPop)
            throws BallerinaPopException {
        try {
            Store store = (Store) clientConnector.getNativeData(PopConstants.PROPS_STORE);
            String host = (String) clientConnector.getNativeData(PopConstants.PROPS_HOST);
            String username = (String) clientConnector.getNativeData(PopConstants.PROPS_USERNAME);
            String password = (String) clientConnector.getNativeData(PopConstants.PROPS_PASSWORD);
            store.connect(host, username, password);
            String folderName = filter.getStringValue(PopConstants.PROPS_FILTER_FOLDER);
            Folder emailFolder = store.getFolder(folderName);
            log.debug("[PopClient][Read] Got the folder.");
            emailFolder.open(Folder.READ_ONLY);
            Message[] messages = emailFolder.getMessages();
            log.debug("[PopClient][Read] Got the messages.");
            log.debug("[PopClient][Read] Email count = " + messages.length);
            MapValue mapValue = null;
            if (messages.length > 0) {
                mapValue = PopUtil.getMapValue(messages[0]);
            }
            log.debug("[PopClient][Read] Closing the folder and the store.");
            emailFolder.close(false);
            store.close();
            return mapValue;
        } catch (MessagingException | IOException e) {
            log.error("Failed to read message : ", e);
            throw new BallerinaPopException("Error occurred while accessing POP server", e);
        }
    }

}
