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

import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.pop3.POP3Message;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

/**
 * Contains utility functions related to the POP and IMAP protocols.
 *
 * @since 1.2.0
 */
public class EmailAccessUtil {

    /**
     * Generates Properties object using the passed MapValue.
     *
     * @param emailAccessConfig MapValue with the configuration values
     * @param host Host address of email server
     * @return Properties Email server access properties
     */
    public static Properties getPopProperties(MapValue emailAccessConfig, String host) {
        Properties properties = new Properties();
        properties.put(EmailConstants.PROPS_POP_HOST, host);
        properties.put(EmailConstants.PROPS_POP_PORT,
                Long.toString(emailAccessConfig.getIntValue(EmailConstants.PROPS_PORT)));
        properties.put(EmailConstants.PROPS_POP_AUTH, "true");
        properties.put(EmailConstants.PROPS_POP_STARTTLS, "true");
        properties.put(EmailConstants.PROPS_POP_SSL_ENABLE,
                emailAccessConfig.getBooleanValue(EmailConstants.PROPS_SSL));
        properties.put(EmailConstants.MAIL_STORE_PROTOCOL, EmailConstants.POP_PROTOCOL);
        return properties;
    }

    /**
     * Generates Properties object using the passed MapValue.
     *
     * @param emailAccessConfig MapValue with the configuration values
     * @param host Host address of email server
     * @return Properties Email server access properties
     */
    public static Properties getImapProperties(MapValue emailAccessConfig, String host) {
        Properties properties = new Properties();
        properties.put(EmailConstants.PROPS_IMAP_HOST, host);
        properties.put(EmailConstants.PROPS_IMAP_PORT,
                Long.toString(emailAccessConfig.getIntValue(EmailConstants.PROPS_PORT)));
        properties.put(EmailConstants.PROPS_IMAP_AUTH, "true");
        properties.put(EmailConstants.PROPS_IMAP_STARTTLS, "true");
        properties.put(EmailConstants.PROPS_IMAP_SSL_ENABLE,
                emailAccessConfig.getBooleanValue(EmailConstants.PROPS_SSL));
        properties.put(EmailConstants.MAIL_STORE_PROTOCOL, EmailConstants.IMAP_PROTOCOL);
        return properties;
    }

    /**
     * Generates MapValue object using the passed message.
     *
     * @param message Email message received
     * @return MapValue Ballerina compatible map object
     * @throws MessagingException If an error occurs related to messaging
     * @throws IOException If an error occurs related to I/O
     */
    public static MapValue getMapValue(Message message) throws MessagingException, IOException {
        Map<String, Object> valueMap = new HashMap<>();
        BArray toAddressArrayValue = getAddressBArrayList(message.getRecipients(Message.RecipientType.TO));
        BArray ccAddressArrayValue = getAddressBArrayList(message.getRecipients(Message.RecipientType.CC));
        BArray bccAddressArrayValue = getAddressBArrayList(message.getRecipients(Message.RecipientType.BCC));
        BArray replyToAddressArrayValue = getAddressBArrayList(message.getReplyTo());
        String subject = getStringNullChecked(message.getSubject());
        String messageBody = extractBodyFromMessage(message);
        String fromAddress = extractFromAddressFromMessage(message);
        String senderAddress = getSenderAddress(message);
        valueMap.put(EmailConstants.MESSAGE_TO, toAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_CC, ccAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_BCC, bccAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_REPLY_TO, replyToAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_SUBJECT, subject);
        valueMap.put(EmailConstants.MESSAGE_MESSAGE_BODY, messageBody);
        valueMap.put(EmailConstants.MESSAGE_FROM, fromAddress);
        valueMap.put(EmailConstants.MESSAGE_SENDER, senderAddress);
        return BallerinaValues.createRecordValue(EmailConstants.EMAIL_PACKAGE_ID, EmailConstants.EMAIL, valueMap);
    }

    private static String extractBodyFromMessage(Message message) throws MessagingException, IOException {
        String messageBody = "";
        if (message.isMimeType("text/plain")) {
            if (message.getContent() != null) {
                messageBody = message.getContent().toString();
            }
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            messageBody = getTextFromMimeMultipart(mimeMultipart);
        }
        return messageBody;
    }

    private static String extractFromAddressFromMessage(Message message) throws MessagingException {
        String fromAddress = "";
        if (message.getFrom() != null) {
            fromAddress = message.getFrom()[0].toString();
        }
        return fromAddress;
    }

    private static String getSenderAddress(Message message) throws MessagingException {
        String senderAddress = "";
        if (message instanceof POP3Message) {
            if (((POP3Message) message).getSender() != null) {
                senderAddress = ((POP3Message) message).getSender().toString();
            }
        } else if (message instanceof IMAPMessage) {
            if (((IMAPMessage) message).getSender() != null) {
                senderAddress = ((IMAPMessage) message).getSender().toString();
            }
        }
        return senderAddress;
    }

    private static BArray getAddressBArrayList(Address[] addresses) {
        BArray addressArrayValue = BValueCreator.createArrayValue(new BArrayType(BTypes.typeString));
        if (addresses != null) {
            for (Address address: addresses) {
                addressArrayValue.append(address.toString());
            }
        }
        return addressArrayValue;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart)
            throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append("\n").append(html);
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private static String getStringNullChecked(String string) {
        return string == null ? "" : string;
    }

}
