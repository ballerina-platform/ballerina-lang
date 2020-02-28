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

package org.ballerinalang.stdlib.email.util;

import org.ballerinalang.jvm.values.MapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * Contains the utility functions related to the SMTP protocol.
 */
public class SmtpUtil {

    private static final Logger log = LoggerFactory.getLogger(SmtpUtil.class);
    private static final int MAX_PORT = 65535;

    /**
     * Generates the Properties object using the passed MapValue.
     *
     * @param smtpConfig MapValue with the configuration values
     * @return Properties object
     */
    public static Properties getProperties(MapValue smtpConfig) {
        Properties properties = new Properties();
        properties.put(SmtpConstants.PROPS_SMTP_HOST, smtpConfig.getStringValue(SmtpConstants.PROPS_HOST));
        properties.put(SmtpConstants.PROPS_SMTP_PORT,
                extractPortValue(smtpConfig.getIntValue(SmtpConstants.PROPS_PORT)));
        properties.put(SmtpConstants.PROPS_SMTP_AUTH, "true");
        properties.put(SmtpConstants.PROPS_SMTP_STARTTLS, "true"); //TLS
        properties.put(SmtpConstants.PROPS_USERNAME, smtpConfig.getStringValue(SmtpConstants.PROPS_USERNAME));
        properties.put(SmtpConstants.PROPS_PASSWORD, smtpConfig.getStringValue(SmtpConstants.PROPS_PASSWORD));
        log.debug("[SmtpUtil][getProperties] Property object created. Returning Object to SMTPClient");
        return properties;
    }


    /**
     * Extracts port value and checks validity.
     *
     * @param longValue Port value extracted from the Config
     * @return Port value as a string
     */
    private static String extractPortValue(long longValue) {
        log.debug("[SmtpUtil][extractPortValue] Extracting Port value");
        if (longValue <= 0 || longValue > MAX_PORT) {
            log.error("Invalid port number given in configuration. Setting default port "
                    + SmtpConstants.DEFAULT_PORT_NUMBER);
            return SmtpConstants.DEFAULT_PORT_NUMBER;
        } else {
            return Long.toString(longValue);
        }
    }

    /**
     * Generates a MIME message to be sent as an email.
     *
     * @param session Session the message is attached
     * @param username User who sens the email
     * @param message Ballerina typed data object
     * @return Email message as a MIME message
     * @throws AddressException If an error occurs related to Internet Address operations
     */

    public static MimeMessage generateMessage(Session session, String username, MapValue message)
            throws AddressException {
        MimeMessage emailMessage = new MimeMessage(session);
        String[] toAddress = message.getArrayValue(EmailConstants.MESSAGE_TO).getStringArray();
        String[] ccAddress = message.getArrayValue(EmailConstants.MESSAGE_CC).getStringArray();
        String[] bccAddress = message.getArrayValue(EmailConstants.MESSAGE_BCC).getStringArray();
        String[] replyToAddress = message.getArrayValue(EmailConstants.MESSAGE_REPLY_TO).getStringArray();
        int toAddressArrayLength = getNullArrayLengthChecked(toAddress);
        int ccAddressArrayLength = getNullArrayLengthChecked(ccAddress);
        int bccAddressArrayLength = getNullArrayLengthChecked(bccAddress);
        int replyToAddressArrayLength = getNullArrayLengthChecked(replyToAddress);
        Address[] toAddressArray = new Address[toAddressArrayLength];
        Address[] ccAddressArray = new Address[ccAddressArrayLength];
        Address[] bccAddressArray = new Address[bccAddressArrayLength];
        Address[] replyToAddressArray = new Address[replyToAddressArrayLength];
        for (int i = 0; i < toAddressArrayLength; i++) {
            toAddressArray[i] = new InternetAddress(toAddress[i]);
        }
        for (int i = 0; i < ccAddressArrayLength; i++) {
            ccAddressArray[i] = new InternetAddress(ccAddress[i]);
        }
        for (int i = 0; i < bccAddressArrayLength; i++) {
            bccAddressArray[i] = new InternetAddress(bccAddress[i]);
        }
        for (int i = 0; i < replyToAddressArrayLength; i++) {
            replyToAddressArray[i] = new InternetAddress(replyToAddress[i]);
        }
        log.debug("[SmtpUtil][generateMessage] Generated TO, CC and BCC Address Arrays.");
        String subject = message.getStringValue(EmailConstants.MESSAGE_SUBJECT);
        String messageBody = message.getStringValue(EmailConstants.MESSAGE_MESSAGE_BODY);
        String fromAddress = message.getStringValue(EmailConstants.MESSAGE_FROM);
        if (fromAddress == null || fromAddress.isEmpty()) {
            fromAddress = username;
        }
        String senderAddress = getNullCheckedString(message.getStringValue(EmailConstants.MESSAGE_SENDER));
        log.debug("[SmtpUtil][generateMessage] Generating MimeMessage");

        try {
            emailMessage.setRecipients(Message.RecipientType.TO, toAddressArray);
            if (ccAddressArrayLength > 0) {
                emailMessage.setRecipients(Message.RecipientType.CC, ccAddressArray);
            }
            if (bccAddressArrayLength > 0) {
                emailMessage.setRecipients(Message.RecipientType.BCC, bccAddressArray);
            }
            if (replyToAddressArrayLength > 0) {
                emailMessage.setReplyTo(replyToAddressArray);
            }
            emailMessage.setSubject(subject);
            emailMessage.setText(messageBody);
            emailMessage.setFrom(new InternetAddress(fromAddress));
            if (!senderAddress.isEmpty()) {
                emailMessage.setSender(new InternetAddress(senderAddress));
            }
        } catch (MessagingException e) {
            log.error("Failed to send message : ", e);
        }
        return emailMessage;
    }

    private static int getNullArrayLengthChecked(String[] addresses) {
        if (addresses == null) {
            return 0;
        } else {
            return addresses.length;
        }
    }

    private static String getNullCheckedString(String string) {
        if (string != null) {
            return string;
        } else {
            return "";
        }
    }

}
