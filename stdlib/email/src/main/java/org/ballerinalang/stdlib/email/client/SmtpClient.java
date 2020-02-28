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
import org.ballerinalang.stdlib.email.util.BallerinaSmtpException;
import org.ballerinalang.stdlib.email.util.SmtpConstants;
import org.ballerinalang.stdlib.email.util.SmtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;


/**
 * Contains functionality of SMTP Client.
 */
public class SmtpClient {

    private static final Logger log = LoggerFactory.getLogger(SmtpClient.class);

    private SmtpClient() {
        // Singleton class
    }

    /**
     * Initializes the ObjectValue object with the SMTP Properties.
     * @param clientEndpoint Represents the SMTP Client class
     * @param config Properties required to configure the SMTP Session
     * @throws BallerinaSmtpException If an error occurs in SMTP client
     */
    public static void initClientEndpoint(ObjectValue clientEndpoint, MapValue<Object, Object> config)
            throws BallerinaSmtpException {
        log.debug("[SmtpClient][InitClient] Calling getProperties");
        Properties properties = SmtpUtil.getProperties(config);
        log.debug("[SmtpClient][Send] Creating session");
        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getProperty(SmtpConstants.PROPS_USERNAME),
                                properties.getProperty(SmtpConstants.PROPS_PASSWORD));
                    }
                });
        clientEndpoint.addNativeData(SmtpConstants.PROPS_SESSION, session);
        clientEndpoint.addNativeData(SmtpConstants.PROPS_USERNAME,
                properties.getProperty(SmtpConstants.PROPS_USERNAME));
    }

    /**
     * Send an email to an SMTP server.
     * @param clientConnector Represents the SMTP Client class
     * @param message Fields of an email
     * @throws BallerinaSmtpException If an error occurs in SMTP client
     */
    public static void sendMessage(ObjectValue clientConnector, MapValue<Object, Object> message)
            throws BallerinaSmtpException {
        try {
            log.debug("[SmtpClient][Send] Sending Email");
            Transport.send(SmtpUtil.generateMessage(
                    (Session) clientConnector.getNativeData(SmtpConstants.PROPS_SESSION),
                    (String) clientConnector.getNativeData(SmtpConstants.PROPS_USERNAME), message));
        } catch (MessagingException e) {
            log.error("Failed to send message : ", e);
            throw new BallerinaSmtpException("Error occurred while sending the email. ", e);
        }
    }

}
