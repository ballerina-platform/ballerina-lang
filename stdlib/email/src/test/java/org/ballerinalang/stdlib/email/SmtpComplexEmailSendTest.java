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

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test class for email send using SMTP with all the parameters.
 *
 * @since 1.2.0
 */
public class SmtpComplexEmailSendTest {

    private CompileResult compileResult;

    private static final String HOST_NAME = "127.0.0.1";
    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String USER_NAME_2 = "hascode2";
    private static final String USER_NAME_3 = "hascode3";
    private static final String USER_NAME_4 = "hascode4";
    private static final String USER_NAME_5 = "hascode5";
    private static final String USER_NAME_6 = "hascode6";
    private static final String EMAIL_USER_ADDRESS_1 = "hascode1@localhost";
    private static final String EMAIL_USER_ADDRESS_2 = "hascode2@localhost";
    private static final String EMAIL_USER_ADDRESS_3 = "hascode3@localhost";
    private static final String EMAIL_USER_ADDRESS_4 = "hascode4@localhost";
    private static final String EMAIL_USER_ADDRESS_5 = "hascode5@localhost";
    private static final String EMAIL_USER_ADDRESS_6 = "hascode6@localhost";
    private static final String EMAIL_FROM = "someone1@localhost.com";
    private static final String EMAIL_SENDER = "someone2@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String[] EMAIL_TO_ADDRESSES = {"hascode1@localhost", "hascode2@localhost"};
    private static final String[] EMAIL_CC_ADDRESSES = {"hascode3@localhost", "hascode4@localhost"};
    private static final String[] EMAIL_BCC_ADDRESSES = {"hascode5@localhost", "hascode6@localhost"};
    private static final String[] EMAIL_REPLY_TO_ADDRESSES = {"reply1@abc.com", "reply2@abc.com"};
    private GreenMail mailServer;

    @BeforeClass
    public void setup() {
        mailServer = new GreenMail(ServerSetupTest.SMTP);
        mailServer.start();
        mailServer.setUser(EMAIL_USER_ADDRESS_1, USER_NAME, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS_2, USER_NAME_2, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS_3, USER_NAME_3, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS_4, USER_NAME_4, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS_5, USER_NAME_5, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS_6, USER_NAME_6, USER_PASSWORD);
        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src", "SmtpComplexEmailSend.bal");
        compileResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test for sending an email with all the parameters")
    public void testSendComplexEmail() throws MessagingException, IOException {
        BValue[] args = { new BString(HOST_NAME), new BString(USER_NAME), new BString(USER_PASSWORD),
                new BString(EMAIL_SUBJECT), new BString(EMAIL_TEXT), new BString(EMAIL_FROM), new BString(EMAIL_SENDER),
                new BValueArray(EMAIL_TO_ADDRESSES), new BValueArray(EMAIL_CC_ADDRESSES),
                new BValueArray(EMAIL_BCC_ADDRESSES), new BValueArray(EMAIL_REPLY_TO_ADDRESSES)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSendComplexEmail", args);
        assertNull(returns[0], "Error while sending email in complex use case.");
        // fetch messages from server
        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(6, messages.length);
        for (MimeMessage message : messages) {
            assertEquals(EMAIL_SUBJECT, message.getSubject());
            assertTrue(String.valueOf(message.getContent()).contains(EMAIL_TEXT));
            assertEquals(EMAIL_FROM, message.getFrom()[0].toString());
            assertEquals(EMAIL_SENDER, message.getSender().toString());
            assertTrue(containAddresses(message.getRecipients(Message.RecipientType.TO), EMAIL_TO_ADDRESSES));
            assertTrue(containAddresses(message.getRecipients(Message.RecipientType.CC), EMAIL_CC_ADDRESSES));
            assertTrue(containAddresses(message.getReplyTo(), EMAIL_REPLY_TO_ADDRESSES));
        }
    }

    private boolean containAddresses(Address[] receivedList, String[] realList) {
        if (receivedList != null && receivedList.length == 2) {
            String[] stringReceivedList = {receivedList[0].toString(), receivedList[1].toString()};
            Arrays.sort(stringReceivedList);
            Arrays.sort(realList);
            return Arrays.equals(stringReceivedList, realList);
        } else {
            return false;
        }
    }

    @AfterClass
    public void terminate() {
        mailServer.stop();
    }

}
