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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;


/**
 * Test class for email send using SMTP with all the parameters.
 *
 * @since 1.1.3
 */
public class SmtpComplexEmailSendTest {


    private CompileResult compileResult;

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
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testSendComplexEmail");
        assertNull(returns[0], "Error while sending email in complex use case.");
        // fetch messages from server
        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(6, messages.length);
        MimeMessage m = messages[0];
        assertEquals(EMAIL_SUBJECT, m.getSubject());
        assertTrue(String.valueOf(m.getContent()).contains(EMAIL_TEXT));
        assertEquals(EMAIL_FROM, m.getFrom()[0].toString());
    }

    @AfterClass
    public void terminate() {
        mailServer.stop();
    }

}
