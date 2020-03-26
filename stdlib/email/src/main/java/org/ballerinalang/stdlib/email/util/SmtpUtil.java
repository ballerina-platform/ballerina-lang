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

import org.ballerinalang.jvm.values.ArrayValue;
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
 *
 * @since 1.2.0
 */
public class SmtpUtil {

    private static final Logger log = LoggerFactory.getLogger(SmtpUtil.class);

    /**
     * Generates the Properties object using the passed MapValue.
     *
     * @param smtpConfig MapValue with the configuration values
     * @param host Host address of the SMTP server
     * @return Properties Set of properties required to connect to an SMTP server
     */
    public static Properties getProperties(MapValue smtpConfig, String host) {
        Properties properties = new Properties();
        properties.put(EmailConstants.PROPS_SMTP_HOST, host);
        properties.put(EmailConstants.PROPS_SMTP_PORT, Long.toString(
                smtpConfig.getIntValue(EmailConstants.PROPS_PORT)));
        properties.put(EmailConstants.PROPS_SMTP_AUTH, "true");
        properties.put(EmailConstants.PROPS_SMTP_STARTTLS, "true");
        properties.put(EmailConstants.PROPS_ENABLE_SSL, smtpConfig.getBooleanValue(EmailConstants.PROPS_SSL));
        return properties;
    }

    /**
     * Generates a MIME message to be sent as an email.
     *
     * @param session Session to which the message is attached
     * @param username User who sends the email
     * @param message Ballerina-typed data object
     * @return MimeMessage Email message as a MIME message
     * @throws AddressException If an error occurs related to Internet Address operations
     */
    public static MimeMessage generateMessage(Session session, String username, MapValue message)
            throws MessagingException {
        Address[] toAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_TO);
        Address[] ccAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_CC);
        Address[] bccAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_BCC);
        Address[] replyToAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_REPLY_TO);
        String subject = message.getStringValue(EmailConstants.MESSAGE_SUBJECT);
        String messageBody = message.getStringValue(EmailConstants.MESSAGE_MESSAGE_BODY);
        String fromAddress = message.getStringValue(EmailConstants.MESSAGE_FROM);
        if (fromAddress == null || fromAddress.isEmpty()) {
            fromAddress = username;
        }
        String senderAddress = getNullCheckedString(message.getStringValue(EmailConstants.MESSAGE_SENDER));
        MimeMessage emailMessage = new MimeMessage(session);
        emailMessage.setRecipients(Message.RecipientType.TO, toAddressArray);
        if (ccAddressArray.length > 0) {
            emailMessage.setRecipients(Message.RecipientType.CC, ccAddressArray);
        }
        if (bccAddressArray.length > 0) {
            emailMessage.setRecipients(Message.RecipientType.BCC, bccAddressArray);
        }
        if (replyToAddressArray.length > 0) {
            emailMessage.setReplyTo(replyToAddressArray);
        }
        emailMessage.setSubject(subject);
        emailMessage.setText(messageBody);
        emailMessage.setFrom(new InternetAddress(fromAddress));
        if (!senderAddress.isEmpty()) {
            emailMessage.setSender(new InternetAddress(senderAddress));
        }
        return emailMessage;
    }

    private static Address[] extractAddressLists(MapValue message, String addressType) throws AddressException {
        String[] address =  getNullCheckedStringArray(message, addressType);
        int addressArrayLength = address.length;
        Address[] addressArray = new Address[addressArrayLength];
        for (int i = 0; i < addressArrayLength; i++) {
            addressArray[i] = new InternetAddress(address[i]);
        }
        return addressArray;
    }

    private static String[] getNullCheckedStringArray(MapValue mapValue, String parameter) {
        ArrayValue arrayValue = mapValue.getArrayValue(parameter);
        if (arrayValue != null) {
            return arrayValue.getStringArray();
        } else {
            return new String[0];
        }
    }

    private static String getNullCheckedString(String string) {
        return string == null ? "" : string;
    }

}
