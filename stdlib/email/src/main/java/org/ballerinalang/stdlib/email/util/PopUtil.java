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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Contains Utility functions related to the POP3 protocol.
 *
 * @since 1.1.5
 */
public class PopUtil {

    private static final Logger log = LoggerFactory.getLogger(PopUtil.class);
    private static final int MAX_PORT = 65535;

    /**
     * Generates Properties object using the passed MapValue.
     *
     * @param popConfig MapValue with the configuration values
     * @param isPop True if the protocol is POP3 and false otherwise (if protocol is IMAP)
     * @return Properties object
     */
    public static Properties getProperties(MapValue popConfig, boolean isPop) {
        if (log.isDebugEnabled()) {
            log.debug("[PopUtil][getProperties] Extracting Property values from Config");
        }
        Properties properties = new Properties();
        if (isPop) {
            properties.put(PopConstants.PROPS_POP_HOST, popConfig.getStringValue(PopConstants.PROPS_HOST));
            properties.put(PopConstants.PROPS_POP_PORT,
                    extractPortValue(popConfig.getIntValue(PopConstants.PROPS_PORT), isPop));
            properties.put(PopConstants.PROPS_POP_AUTH, "true");
            properties.put(PopConstants.PROPS_POP_STARTTLS, "true");
            properties.put(PopConstants.PROPS_POP_SSL_ENABLE, popConfig.getBooleanValue(PopConstants.PROPS_SSL));
            properties.put(EmailConstants.MAIL_STORE_PROTOCOL, PopConstants.POP_PROTOCOL);
        } else {
            properties.put(ImapConstants.PROPS_IMAP_HOST, popConfig.getStringValue(PopConstants.PROPS_HOST));
            properties.put(ImapConstants.PROPS_IMAP_PORT,
                    extractPortValue(popConfig.getIntValue(PopConstants.PROPS_PORT), isPop));
            properties.put(ImapConstants.PROPS_IMAP_AUTH, "true");
            properties.put(ImapConstants.PROPS_IMAP_STARTTLS, "true");
            properties.put(ImapConstants.PROPS_IMAP_SSL_ENABLE, popConfig.getBooleanValue(PopConstants.PROPS_SSL));
            properties.put(EmailConstants.MAIL_STORE_PROTOCOL, ImapConstants.IMAP_PROTOCOL);
        }

        properties.put(PopConstants.PROPS_USERNAME, popConfig.getStringValue(PopConstants.PROPS_USERNAME));
        properties.put(PopConstants.PROPS_PASSWORD, popConfig.getStringValue(PopConstants.PROPS_PASSWORD));

        if (log.isDebugEnabled()) {
            log.debug("[PopUtil][getProperties] Property object created. Returning Object to PopClient");
        }
        return properties;
    }

    /**
     * Generates MapValue object using the passed message.
     *
     * @param message received
     * @return MapValue object
     * @throws MessagingException If an error occurs related to messaging
     * @throws IOException If an error occurs related to I/O
     */
    public static MapValue getMapValue(Message message) throws MessagingException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("[PopUtil][getMapValue] Extracting MapValue values from Config");
        }
        Map<String, Object> valueMap = new HashMap<>();
        Address[] toAddress = message.getRecipients(Message.RecipientType.TO);
        Address[] ccAddress = message.getRecipients(Message.RecipientType.CC);
        Address[] bccAddress = message.getRecipients(Message.RecipientType.BCC);
        Address[] replyToAddress = message.getReplyTo();
        ArrayValue toAddressArrayValue = new ArrayValueImpl(new BArrayType(BTypes.typeString));
        ArrayValue ccAddressArrayValue = new ArrayValueImpl(new BArrayType(BTypes.typeString));
        ArrayValue bccAddressArrayValue = new ArrayValueImpl(new BArrayType(BTypes.typeString));
        ArrayValue replyToAddressArrayValue = new ArrayValueImpl(new BArrayType(BTypes.typeString));
        if (toAddress != null) {
            for (Address address: toAddress) {
                toAddressArrayValue.append(address.toString());
            }
        }
        if (ccAddress != null) {
            for (Address address: ccAddress) {
                ccAddressArrayValue.append(address.toString());
            }
        }
        if (bccAddress != null) {
            for (Address address: bccAddress) {
                bccAddressArrayValue.append(address.toString());
            }
        }
        if (replyToAddress != null) {
            for (Address address: replyToAddress) {
                replyToAddressArrayValue.append(address.toString());
            }
        }
        String subject = getStringNullChecked(message.getSubject());
        String messageBody = "";
        if (message.isMimeType("text/plain")) {
            if (message.getContent() != null) {
                messageBody = message.getContent().toString();
            } else {
                messageBody = "";
            }
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            messageBody = getTextFromMimeMultipart(mimeMultipart);
        }
        String fromAddress;
        if (message.getFrom() != null) {
            fromAddress = message.getFrom()[0].toString();
        } else {
            fromAddress = "";
        }
        String senderAddress = "";
        valueMap.put(EmailConstants.MESSAGE_TO, toAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_CC, ccAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_BCC, bccAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_REPLY_TO, replyToAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_SUBJECT, subject);
        valueMap.put(EmailConstants.MESSAGE_MESSAGE_BODY, messageBody);
        valueMap.put(EmailConstants.MESSAGE_FROM, fromAddress);
        valueMap.put(EmailConstants.MESSAGE_SENDER, senderAddress);
        if (log.isDebugEnabled()) {
            log.debug("[PopUtil][getMapValue] MapValue object created. Returning Object to PopClient");
        }
        return BallerinaValues.createRecordValue(EmailConstants.EMAIL_PACKAGE_ID, PopConstants.EMAIL, valueMap);
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                // result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
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

    /**
     * Extracts port value and checks validity.
     *
     * @param longValue Port value extracted from the Config
     * @return Port value as a string
     */
    private static String extractPortValue(long longValue, boolean isPop) {
        if (log.isDebugEnabled()) {
            log.debug("[PopUtil][extractPortValue] Extracting Port value");
        }
        if (longValue <= 0 || longValue > MAX_PORT) {
            if (isPop) {
                log.error("Invalid port number given in configuration. Setting default port "
                        + PopConstants.DEFAULT_PORT_NUMBER);
                return PopConstants.DEFAULT_PORT_NUMBER;
            } else {
                log.error("Invalid port number given in configuration. Setting default port "
                        + ImapConstants.DEFAULT_PORT_NUMBER);
                return PopConstants.DEFAULT_PORT_NUMBER;
            }
        } else {
            return Long.toString(longValue);
        }
    }

}
