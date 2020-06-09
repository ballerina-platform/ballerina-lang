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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.mime.nativeimpl.EntityHeaders;
import org.ballerinalang.mime.nativeimpl.MimeDataSourceBuilder;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import static org.ballerinalang.mime.util.MimeUtil.getContentTypeWithParameters;

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
    public static Properties getProperties(MapValue<BString, Object> smtpConfig, String host) {
        Properties properties = new Properties();
        properties.put(EmailConstants.PROPS_SMTP_HOST, host);
        properties.put(EmailConstants.PROPS_SMTP_PORT, Long.toString(
                smtpConfig.getIntValue(EmailConstants.PROPS_PORT)));
        properties.put(EmailConstants.PROPS_SMTP_AUTH, "true");
        properties.put(EmailConstants.PROPS_SMTP_STARTTLS, "true");
        properties.put(EmailConstants.PROPS_ENABLE_SSL, smtpConfig.getBooleanValue(EmailConstants.PROPS_SSL));
        CommonUtil.addCustomProperties(
                (MapValue<BString, Object>) smtpConfig.getMapValue(EmailConstants.PROPS_PROPERTIES), properties);
        if (log.isDebugEnabled()) {
            Set<String> propertySet = properties.stringPropertyNames();
            log.debug("SMTP Properties set are as follows.");
            for (Object propertyObj : propertySet) {
                log.debug("Property Name: " + propertyObj + ", Value: " + properties.get(propertyObj).toString()
                        + " ValueType: " + properties.get(propertyObj).getClass().getName());
            }
        }
        return properties;
    }

    /**
     * Generates a MIME message to be sent as an email.
     *
     * @param session Session to which the message is attached
     * @param username User who sends the email
     * @param message Ballerina-typed data object
     * @return MimeMessage Email message as a MIME message
     * @throws MessagingException If an error occurs related to messaging operations
     * @throws IOException If an error occurs related to I/O operations
     */
    public static MimeMessage generateMessage(Session session, String username, MapValue<BString, Object> message)
            throws MessagingException, IOException {
        Address[] toAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_TO);
        Address[] ccAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_CC);
        Address[] bccAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_BCC);
        Address[] replyToAddressArray = extractAddressLists(message, EmailConstants.MESSAGE_REPLY_TO);
        String subject = message.getStringValue(EmailConstants.MESSAGE_SUBJECT).getValue();
        String messageBody = message.getStringValue(EmailConstants.MESSAGE_MESSAGE_BODY).getValue();
        String bodyContentType = message.getStringValue(EmailConstants.MESSAGE_BODY_CONTENT_TYPE).getValue();
        String fromAddress = message.getStringValue(EmailConstants.MESSAGE_FROM).getValue();
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
        emailMessage.setFrom(new InternetAddress(fromAddress));
        if (!senderAddress.isEmpty()) {
            emailMessage.setSender(new InternetAddress(senderAddress));
        }
        ArrayValue attachments = message.getArrayValue(EmailConstants.MESSAGE_ATTACHMENTS);
        if (attachments == null) {
            emailMessage.setContent(messageBody, bodyContentType);
        } else {
            addBodyAndAttachments(emailMessage, messageBody, bodyContentType, attachments);
        }
        addMessageHeaders(emailMessage, message);
        return emailMessage;
    }

    private static void addMessageHeaders(MimeMessage emailMessage, MapValue<BString, Object> message)
            throws MessagingException {
        MapValue<BString, BString> headers =
                (MapValue<BString, BString>) message.getMapValue(EmailConstants.MESSAGE_HEADERS);
        if (headers != null) {
            BString[] headerNames = headers.getKeys();
            for (BString headerName : headerNames) {
                emailMessage.addHeader(headerName.getValue(), headers.getStringValue(headerName).getValue());
            }
        }
    }

    private static void addBodyAndAttachments(MimeMessage emailMessage, String messageBody, String bodyContentType,
                                              ArrayValue attachments)
            throws MessagingException, IOException {
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(messageBody, bodyContentType);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        for (int i = 0; i < attachments.size(); i++) {
            if (attachments.get(i) instanceof ObjectValue) {
                ObjectValue mimeEntity = (ObjectValue) attachments.get(i);
                String contentType = getContentTypeWithParameters(mimeEntity);
                if (contentType.startsWith(MimeConstants.MULTIPART_AS_PRIMARY_TYPE)) {
                    multipart.addBodyPart(populateMultipart(mimeEntity));
                } else {
                    multipart.addBodyPart(buildJavaMailBodyPart(mimeEntity, contentType));
                }
            }
        }
        emailMessage.setContent(multipart);
    }

    private static MimeBodyPart populateMultipart(ObjectValue mimeEntity) throws IOException, MessagingException {
        Multipart multipart = new MimeMultipart();
        ArrayValue multipartMimeEntityArrayValue = EntityBodyHandler.getBodyPartArray(mimeEntity);
        int entityCount = multipartMimeEntityArrayValue.size();
        for (int i = 0; i < entityCount; i++) {
            ObjectValue childMimeEntity = (ObjectValue) multipartMimeEntityArrayValue.get(i);
            String childContentType = getContentTypeWithParameters(childMimeEntity);
            if (childContentType.startsWith(MimeConstants.MULTIPART_AS_PRIMARY_TYPE)) {
                multipart.addBodyPart(populateMultipart(childMimeEntity));
            } else {
                multipart.addBodyPart(buildJavaMailBodyPart(childMimeEntity, childContentType));
            }
        }
        MimeBodyPart returnMimeBodyPart = new MimeBodyPart();
        returnMimeBodyPart.setContent(multipart);
        return returnMimeBodyPart;
    }

    private static MimeBodyPart buildJavaMailBodyPart(ObjectValue mimeEntity, String contentType)
            throws MessagingException, IOException {
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        Channel channel = EntityBodyHandler.getByteChannel(mimeEntity);
        if (channel != null) {
            InputStream inputStream = channel.getInputStream();
            ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, contentType);
            attachmentBodyPart.setDataHandler(new DataHandler(ds));
        } else {
            if (CommonUtil.isTextBased(contentType)) {
                attachmentBodyPart.setText(((BString) MimeDataSourceBuilder.getText(mimeEntity)).getValue());
            } else {
                ArrayValue binaryContent = (ArrayValue) MimeDataSourceBuilder.getByteArray(mimeEntity);
                attachmentBodyPart.setContent(binaryContent.getBytes(), MimeConstants.OCTET_STREAM);
            }
        }
        addHeadersToJavaMailBodyPart(mimeEntity, attachmentBodyPart);
        return attachmentBodyPart;
    }

    private static void addHeadersToJavaMailBodyPart(ObjectValue mimeEntity, MimeBodyPart attachmentBodyPart)
            throws MessagingException {
        ArrayValue headerNamesArrayValue = EntityHeaders.getHeaderNames(mimeEntity,
                StringUtils.fromString(MimeConstants.LEADING_HEADER));
        if (headerNamesArrayValue.size() > 0) {
            for (int i = 0; i < headerNamesArrayValue.size(); i++) {
                BString headerName = (BString) headerNamesArrayValue.get(i);
                BString headerValue = EntityHeaders.getHeader(mimeEntity, headerName,
                        StringUtils.fromString(MimeConstants.LEADING_HEADER));
                if (isNotEmpty(headerName.getValue())) {
                    log.debug("Added a MIME body part header " + headerName + " with value " + headerValue);
                    attachmentBodyPart.setHeader(headerName.getValue(), headerValue.getValue());
                }
            }
        }
    }

    private static Address[] extractAddressLists(MapValue<BString, Object> message, BString addressType)
            throws AddressException {
        String[] address =  getNullCheckedStringArray(message, addressType);
        int addressArrayLength = address.length;
        Address[] addressArray = new Address[addressArrayLength];
        for (int i = 0; i < addressArrayLength; i++) {
            addressArray[i] = new InternetAddress(address[i]);
        }
        return addressArray;
    }

    private static String[] getNullCheckedStringArray(MapValue<BString, Object> mapValue, BString parameter) {
        if (mapValue != null) {
            ArrayValue arrayValue = mapValue.getArrayValue(parameter);
            if (arrayValue != null) {
                return arrayValue.getStringArray();
            } else {
                return new String[0];
            }
        } else {
            return new String[0];
        }
    }

    private static String getNullCheckedString(BString string) {
        return string == null ? "" : string.getValue();
    }

    private static boolean isNotEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    public static ErrorValue getBallerinaError(String typeId, String message) {
        return BallerinaErrors.createDistinctError(typeId, EmailConstants.EMAIL_PACKAGE_ID, message);
    }
}
