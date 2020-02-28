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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;


/**
 * Test class for email receipt using IMAP4 with least number of parameters.
 *
 * @since 1.1.3
 */
public class ImapSimpleEmailReceiveTest {

    private CompileResult compileResult;
    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost";
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private GreenMail mailServer;

    @BeforeClass
    public void setup() throws MessagingException {

        mailServer = new GreenMail(ServerSetupTest.IMAP);
        mailServer.start();
        GreenMailUser user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);

        // create an e-mail message using javax.mail ..
        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS));
        message.setSubject(EMAIL_SUBJECT);
        message.setText(EMAIL_TEXT);

        // use greenmail to store the message
        user.deliver(message);

        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src", "ImapSimpleEmailReceive.bal");
        compileResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test for receiving an email with simple parameters")
    public void testReceiveSimpleEmail() throws MessagingException, IOException {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReceiveSimpleEmail");
        assertNotNull("No messages received with simple IMAP configuration.", returns);
        if (returns[0] instanceof BError) {
            fail("IMAP Client could not read from server.");
        } else if (returns[0] == null) {
            fail("IMAP Client could read zero emails.");
        } else {
            BMap<String, BValue> email = (BMap<String, BValue>) returns[0];
            String subject = email.get("subject").stringValue();
            assertEquals(EMAIL_SUBJECT, subject);
        }
    }

    @AfterClass
    public void terminate() {
        mailServer.stop();
    }

}
