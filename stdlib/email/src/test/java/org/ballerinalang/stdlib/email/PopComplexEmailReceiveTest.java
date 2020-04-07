/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.email;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for email receive using POP3 with all the parameters.
 *
 * @since 1.2.0
 */
public class PopComplexEmailReceiveTest {

    private CompileResult compileResult;
    private static final String HOST_NAME = "127.0.0.1";
    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost";
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_SENDER = "someone2@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String ATTACHMENT1_TEXT = "Sample attachment text";
    private static final String ATTACHMENT2_TEXT = "{\"bodyPart\":\"jsonPart\"}";
    private static final String ATTACHMENT3_TEXT = "<name>Ballerina xml file part</name>";
    private static final byte[] ATTACHMENT4_BINARY = "This is a sample source of bytes.".getBytes();
    private static final String ATTACHMENT1_HEADER1_NAME_TEXT = "H1";
    private static final String ATTACHMENT1_HEADER1_VALUE_TEXT = "V1";
    private static final String[] EMAIL_TO_ADDRESSES = {"hascode1@localhost", "hascode2@localhost"};
    private static final String[] EMAIL_CC_ADDRESSES = {"hascode3@localhost", "hascode4@localhost"};
    private static final String[] EMAIL_BCC_ADDRESSES = {"hascode5@localhost", "hascode6@localhost"};
    private static final String[] EMAIL_REPLY_TO_ADDRESSES = {"reply1@abc.com", "reply2@abc.com"};
    private GreenMail mailServer;

    @BeforeClass
    public void setup() throws MessagingException {

        mailServer = new GreenMail(ServerSetupTest.POP3);
        mailServer.start();
        GreenMailUser user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);

        // Create an e-mail message using javax.mail ..
        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setSender(new InternetAddress(EMAIL_SENDER));
        message.addRecipients(Message.RecipientType.TO, convertToAddressArray(EMAIL_TO_ADDRESSES));
        message.addRecipients(Message.RecipientType.CC, convertToAddressArray(EMAIL_CC_ADDRESSES));
        message.addRecipients(Message.RecipientType.BCC, convertToAddressArray(EMAIL_BCC_ADDRESSES));
        message.setReplyTo(convertToAddressArray(EMAIL_REPLY_TO_ADDRESSES));
        message.setSubject(EMAIL_SUBJECT);

        Multipart multipartMessage = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(EMAIL_TEXT, MimeConstants.TEXT_PLAIN);
        MimeBodyPart attachment1 = new MimeBodyPart();
        MimeBodyPart attachment2 = new MimeBodyPart();
        MimeBodyPart attachment3 = new MimeBodyPart();
        MimeBodyPart attachment4 = new MimeBodyPart();
        attachment1.setContent(ATTACHMENT1_TEXT, MimeConstants.TEXT_PLAIN);
        attachment1.addHeader(ATTACHMENT1_HEADER1_NAME_TEXT, ATTACHMENT1_HEADER1_VALUE_TEXT);
        attachment2.setContent(ATTACHMENT2_TEXT, MimeConstants.APPLICATION_JSON);
        attachment3.setContent(ATTACHMENT3_TEXT, MimeConstants.APPLICATION_XML);
        attachment4.setContent(ATTACHMENT4_BINARY, MimeConstants.OCTET_STREAM);
        multipartMessage.addBodyPart(messageBodyPart);
        multipartMessage.addBodyPart(attachment1);
        multipartMessage.addBodyPart(attachment2);
        multipartMessage.addBodyPart(attachment3);
        multipartMessage.addBodyPart(attachment4);
        message.setContent(multipartMessage);

        // Use greenmail to store the message
        user.deliver(message);

        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src",
                "PopComplexEmailReceive.bal");
        compileResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test for receiving an email with all the parameters")
    public void testReceiveComplexEmail() {
        BValue[] args = {new BString(HOST_NAME), new BString(USER_NAME), new BString(USER_PASSWORD)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReceiveComplexEmail", args);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        String[] result = ((BValueArray) returns[0]).getStringArray();
        assertEquals(EMAIL_SUBJECT, result[0]);
        assertEquals(EMAIL_TEXT.trim(), result[1].trim());
        assertEquals(EMAIL_FROM, result[2]);
        assertEquals(EMAIL_SENDER, result[3]);
        assertEquals(concatAddresses(EMAIL_TO_ADDRESSES), result[4]);
        assertEquals(concatAddresses(EMAIL_CC_ADDRESSES), result[5]);
        assertEquals(concatAddresses(EMAIL_REPLY_TO_ADDRESSES), result[6]);
        assertEquals(ATTACHMENT1_TEXT, result[7]);
        assertEquals(ATTACHMENT2_TEXT, result[8]);
        assertEquals(ATTACHMENT3_TEXT, result[9]);
        assertEquals(new String(ATTACHMENT4_BINARY), result[10]);
        assertEquals(ATTACHMENT1_HEADER1_VALUE_TEXT, result[11]);
        Assert.assertTrue(result[12].startsWith(MimeConstants.TEXT_PLAIN));
    }

    private String concatAddresses(String[] addresses) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String address : addresses) {
            stringBuilder.append(address);
        }
        return stringBuilder.toString();
    }

    @AfterClass
    public void terminate() {
        mailServer.stop();
    }

    private Address[] convertToAddressArray(String[] stringAddresses) throws AddressException {
        if (stringAddresses != null && stringAddresses.length > 0) {
            Address[] addresses = new Address[stringAddresses.length];
            for (int i = 0; i < stringAddresses.length; i++) {
                addresses[i] = new InternetAddress(stringAddresses[i]);
            }
            return addresses;

        } else {
            return null;
        }
    }

}
