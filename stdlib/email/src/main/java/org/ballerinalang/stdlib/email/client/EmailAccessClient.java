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
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.stdlib.email.util.EmailAccessUtil;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.ballerinalang.stdlib.email.util.SmtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

/**
 * Contains the functionality of email reading with POP and IMAP clients.
 *
 * @since 1.2.0
 */
public class EmailAccessClient {

    private static final Logger log = LoggerFactory.getLogger(EmailAccessClient.class);
    private static final FlagTerm UNSEEN_FLAG = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

    private EmailAccessClient() {
        // A Singleton class.
    }

    /**
     * Initializes the ObjectValue object with the POP properties.
     * @param clientEndpoint Represents the POP Client class
     * @param host Represents the host address of POP server
     * @param username Represents the username of the POP server
     * @param password Represents the password of the POP server
     * @param config Properties required to configure the POP session
     * @return If an error occurs in the POP client, returns an error
     */
    public static Object initPopClientEndpoint(ObjectValue clientEndpoint, BString host, BString username,
                                               BString password, MapValue<BString, Object> config) {
        Properties properties = EmailAccessUtil.getPopProperties(config, host.getValue());
        Session session = Session.getInstance(properties, null);
        try {
            Store store = session.getStore(EmailConstants.POP_PROTOCOL);
            clientEndpoint.addNativeData(EmailConstants.PROPS_STORE, store);
            clientEndpoint.addNativeData(EmailConstants.PROPS_HOST.getValue(), host.getValue());
            clientEndpoint.addNativeData(EmailConstants.PROPS_USERNAME.getValue(), username.getValue());
            clientEndpoint.addNativeData(EmailConstants.PROPS_PASSWORD.getValue(), password.getValue());
            return null;
        } catch (NoSuchProviderException e) {
            log.error("Failed initialize client properties : ", e);
            return SmtpUtil.getBallerinaError(EmailConstants.READ_CLIENT_INIT_ERROR, e.getMessage());
        }
    }

    /**
     * Initializes the ObjectValue object with the IMAP properties.
     * @param clientEndpoint Represents the IMAP Client class
     * @param host Represents the host address of IMAP server
     * @param username Represents the username of the IMAP server
     * @param password Represents the password of the IMAP server
     * @param config Properties required to configure the IMAP session
     * @return If an error occurs in the IMAP client, returns an error
     */
    public static Object initImapClientEndpoint(ObjectValue clientEndpoint, BString host, BString username,
                                                BString password, MapValue<BString, Object> config) {
        Properties properties = EmailAccessUtil.getImapProperties(config, host.getValue());
        Session session = Session.getInstance(properties, null);
        try {
            Store store = session.getStore(EmailConstants.IMAP_PROTOCOL);
            clientEndpoint.addNativeData(EmailConstants.PROPS_STORE, store);
            clientEndpoint.addNativeData(EmailConstants.PROPS_HOST.getValue(), host.getValue());
            clientEndpoint.addNativeData(EmailConstants.PROPS_USERNAME.getValue(), username.getValue());
            clientEndpoint.addNativeData(EmailConstants.PROPS_PASSWORD.getValue(), password.getValue());
            return null;
        } catch (NoSuchProviderException e) {
            log.error("Failed initialize client properties : ", e);
            return SmtpUtil.getBallerinaError(EmailConstants.READ_CLIENT_INIT_ERROR, e.getMessage());
        }
    }

    /**
     * Read emails from the server.
     * @param clientConnector Represents the POP or IMAP client class
     * @param folder Name of the folder to read emails
     * @return If successful return the received email, otherwise an error
     */
    public static Object readMessage(ObjectValue clientConnector, BString folder) {
        String host = (String) clientConnector.getNativeData(EmailConstants.PROPS_HOST.getValue());
        String username = (String) clientConnector.getNativeData(EmailConstants.PROPS_USERNAME.getValue());
        String password = (String) clientConnector.getNativeData(EmailConstants.PROPS_PASSWORD.getValue());
        try (Store store = (Store) clientConnector.getNativeData(EmailConstants.PROPS_STORE)) {
            log.debug("Access email server with properties, host: " + host + " username: " + username
                    + " folder: " + folder.getValue());
            store.connect(host, username, password);
            Folder emailFolder = store.getFolder(folder.getValue());
            MapValue<BString, Object> mapValue = null;
            if (emailFolder == null) {
                log.error("Email store folder, " + folder.getValue() + " is not found.");
            } else {
                emailFolder.open(Folder.READ_WRITE);
                Message[] messages = emailFolder.search(UNSEEN_FLAG);
                if (messages.length > 0) {
                    Flags flags = new Flags();
                    flags.add(Flags.Flag.SEEN);
                    emailFolder.setFlags(new int[] {messages[0].getMessageNumber()}, flags, true);
                    mapValue = EmailAccessUtil.getMapValue(messages[0]);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Got the messages. Email count = " + messages.length);
                }
                emailFolder.close(false);
            }
            return mapValue;
        } catch (MessagingException | IOException e) {
            log.error("Failed to read message : ", e);
            return SmtpUtil.getBallerinaError(EmailConstants.READ_ERROR, e.getMessage());
        }
    }

}
