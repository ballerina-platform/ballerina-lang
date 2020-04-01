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
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;

/**
 * Contains utility functions related to the POP and IMAP protocols.
 *
 * @since 1.2.0
 */
public class EmailAccessUtil {

    private static final Logger log = LoggerFactory.getLogger(EmailAccessUtil.class);

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
        BArray attachments = extractAttachmentsFromMessage(message);
        valueMap.put(EmailConstants.MESSAGE_TO, toAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_CC, ccAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_BCC, bccAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_REPLY_TO, replyToAddressArrayValue);
        valueMap.put(EmailConstants.MESSAGE_SUBJECT, subject);
        valueMap.put(EmailConstants.MESSAGE_MESSAGE_BODY, messageBody);
        valueMap.put(EmailConstants.MESSAGE_FROM, fromAddress);
        valueMap.put(EmailConstants.MESSAGE_SENDER, senderAddress);
        if (attachments != null && attachments.size() > 0) {
            valueMap.put(EmailConstants.MESSAGE_ATTACHMENTS, attachments);
        }
        return BallerinaValues.createRecordValue(EmailConstants.EMAIL_PACKAGE_ID, EmailConstants.EMAIL, valueMap);
    }

    private static String extractBodyFromMessage(Message message) throws MessagingException, IOException {
        String messageBody = "";
        if (CommonUtil.isTextBased(message.getContentType())) {
            if (message.getContent() != null) {
                messageBody = message.getContent().toString();
            }
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            if (mimeMultipart != null && mimeMultipart.getCount() > 0 && mimeMultipart.getBodyPart(0) != null
                    && mimeMultipart.getBodyPart(0).getContent() != null) {
                messageBody = (String) mimeMultipart.getBodyPart(0).getContent();
            }
        }
        return messageBody;
    }

    private static BArray extractAttachmentsFromMessage(Message message) throws MessagingException, IOException {
        ArrayList<ObjectValue> attachmentArray = new ArrayList<>();
        if (!message.isMimeType("multipart/*")) {
            return null;
        } else {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            int numberOfAttachments = mimeMultipart.getCount();
            if (numberOfAttachments > 1) {
                for (int i = 1; i < numberOfAttachments; i++) {
                    BodyPart attachment = mimeMultipart.getBodyPart(i);
                    String contentType = attachment.getContentType();
                    if (CommonUtil.isTextBased(contentType)) {
                        if (CommonUtil.isJsonBased(contentType)) {
                            attachJsonBodyPart(attachment, attachmentArray);
                        } else if (CommonUtil.isXmlBased(contentType)) {
                            attachXmlBodyPart(attachment, attachmentArray);
                        } else {
                            attachTextBodyPart(attachment, attachmentArray);
                        }
                    } else {
                        attachBinaryBodyPart(attachment, attachmentArray);
                    }
                }
                return getArrayOfBodyParts(attachmentArray);
            } else {
                log.debug("Received a Multipart email message without any attachments.");
                return null;
            }
        }
    }

    private static void attachJsonBodyPart(BodyPart attachment, ArrayList<ObjectValue> attachmentArray)
            throws IOException, MessagingException {
        String jsonContent = (String) attachment.getContent();
        ObjectValue bodyPart = createEntityObject();
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper(jsonContent);
        bodyPart.addNativeData(MimeConstants.ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, MimeConstants.APPLICATION_JSON);
        attachmentArray.add(bodyPart);
    }

    private static void attachXmlBodyPart(BodyPart attachment, ArrayList<ObjectValue> attachmentArray)
            throws IOException, MessagingException {
        String xmlContent = (String) attachment.getContent();
        XMLValue xmlNode = XMLFactory.parse(xmlContent);
        ObjectValue bodyPart = createEntityObject();
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                xmlNode.stringValue().getBytes(StandardCharsets.UTF_8)));
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(byteChannel));
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, MimeConstants.APPLICATION_XML);
        attachmentArray.add(bodyPart);
    }

    private static void attachTextBodyPart(BodyPart attachment, ArrayList<ObjectValue> attachmentArray)
            throws IOException, MessagingException {
        String textPayload = (String) attachment.getContent();
        ObjectValue bodyPart = BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(textPayload));
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, MimeConstants.TEXT_PLAIN);
        attachmentArray.add(bodyPart);
    }

    private static void attachBinaryBodyPart(BodyPart attachment, ArrayList<ObjectValue> attachmentArray)
            throws IOException, MessagingException {
        byte[] binaryContent = CommonUtil.convertInputStreamToByteArray(attachment.getInputStream());
        EntityWrapper byteChannel = new EntityWrapper(new EntityBodyChannel(new ByteArrayInputStream(binaryContent)));
        ObjectValue bodyPart = createEntityObject();
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, OCTET_STREAM);
        attachmentArray.add(bodyPart);
    }

    private static ArrayValue getArrayOfBodyParts(ArrayList<ObjectValue> bodyParts) {
        BType typeOfBodyPart = bodyParts.get(0).getType();
        ObjectValue[] result = bodyParts.toArray(new ObjectValue[bodyParts.size()]);
        return new ArrayValueImpl(result, new org.ballerinalang.jvm.types.BArrayType(typeOfBodyPart));
    }

    private static ObjectValue createMediaTypeObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
    }

    public static ObjectValue createEntityObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY);
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

    private static String getStringNullChecked(String string) {
        return string == null ? "" : string;
    }

}
