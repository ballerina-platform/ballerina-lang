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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Test class for email receive using IMAP4 with all the parameters.
 *
 * @since 1.2.0
 */
public class ImapComplexEmailReceiveTest {

    private CompileResult compileResult;
    private static final String HOST_NAME = "127.0.0.1";
    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost";
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_SENDER = "someone2@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String[] EMAIL_TO_ADDRESSES = {"hascode1@localhost", "hascode2@localhost"};
    private static final String[] EMAIL_CC_ADDRESSES = {"hascode3@localhost", "hascode4@localhost"};
    private static final String[] EMAIL_BCC_ADDRESSES = {"hascode5@localhost", "hascode6@localhost"};
    private static final String[] EMAIL_REPLY_TO_ADDRESSES = {"reply1@abc.com", "reply2@abc.com"};
    private GreenMail mailServer;

    @BeforeClass
    public void setup() throws MessagingException {

        mailServer = new GreenMail(ServerSetupTest.IMAP);
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
        message.setText(EMAIL_TEXT);

        // Use greenmail to store the message
        user.deliver(message);

        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src",
                "ImapComplexEmailReceive.bal");
        compileResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test for receiving an email with all the parameters")
    public void testReceiveComplexEmail() {
        BValue[] args = {new BString(HOST_NAME), new BString(USER_NAME), new BString(USER_PASSWORD)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReceiveComplexEmail", args);
        assertNotNull("No messages received with complex IMAP configuration.", returns);
        if (returns[0] instanceof BError) {
            fail("IMAP Client could not read from server.");
        } else if (returns[0] == null) {
            fail("IMAP Client could read zero emails.");
        } else {
            BMap<String, BValue> email = (BMap<String, BValue>) returns[0];
            String subject = email.get(EmailConstants.MESSAGE_SUBJECT).stringValue();
            String messageBody = email.get(EmailConstants.MESSAGE_MESSAGE_BODY).stringValue();
            String fromAddress = email.get(EmailConstants.MESSAGE_FROM).stringValue();
            String senderAddress = email.get(EmailConstants.MESSAGE_SENDER).stringValue();
            String[] toAddresses = ((BValueArray) email.get(EmailConstants.MESSAGE_TO)).getStringArray();
            String[] ccAddresses = ((BValueArray) email.get(EmailConstants.MESSAGE_CC)).getStringArray();
            String[] replyToAddresses = ((BValueArray) email.get(EmailConstants.MESSAGE_REPLY_TO)).getStringArray();
            assertEquals(EMAIL_SUBJECT, subject);
            assertEquals(EMAIL_TEXT.trim(), messageBody.trim());
            assertEquals(EMAIL_FROM, fromAddress);
            assertEquals(EMAIL_SENDER, senderAddress);
            assertTrue(arraysMatchInContent(toAddresses, EMAIL_TO_ADDRESSES));
            assertTrue(arraysMatchInContent(ccAddresses, EMAIL_CC_ADDRESSES));
            assertTrue(arraysMatchInContent(replyToAddresses, EMAIL_REPLY_TO_ADDRESSES));
        }
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

    private boolean arraysMatchInContent(String[] receivedList, String[] realList) {
        Arrays.sort(receivedList);
        Arrays.sort(realList);
        String[] newReceivedList = {receivedList[receivedList.length - 2], receivedList[receivedList.length - 1]};
        return Arrays.equals(newReceivedList, realList);
    }

}
