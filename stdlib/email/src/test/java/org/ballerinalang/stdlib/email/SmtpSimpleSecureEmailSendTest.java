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

import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test class for email send using SMTP with least number of parameters with SSL.
 *
 * @since 1.2.0
 */
public class SmtpSimpleSecureEmailSendTest {

    private CompileResult compileResult;

    private static final String HOST_NAME = "127.0.0.1";
    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost";
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String SSL_SOCKET_FACTORY_PROVIDER = "ssl.SocketFactory.provider";
    private GreenMail mailServer;

    @BeforeClass
    public void setup() {
        Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummySSLSocketFactory.class.getName());
        mailServer = new GreenMail(ServerSetupTest.SMTPS);
        mailServer.start();
        mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src",
                "SmtpSimpleSecureEmailSend.bal");
        compileResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test for sending an email with simple parameters")
    public void testSendSimpleEmail() throws MessagingException, IOException {
        BValue[] args = { new BString(HOST_NAME), new BString(USER_NAME), new BString(USER_PASSWORD),
                new BString(EMAIL_USER_ADDRESS), new BString(EMAIL_SUBJECT), new BString(EMAIL_TEXT),
                new BString(EMAIL_FROM)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSendSimpleEmail", args);
        assertNull(returns[0], "Error while sending email in simple use case.");
        // fetch messages from server
        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertEquals(EMAIL_SUBJECT, message.getSubject());
        assertTrue(String.valueOf(message.getContent()).contains(EMAIL_TEXT));
        assertEquals(EMAIL_FROM, message.getFrom()[0].toString());
    }

    @Test(description = "Test for sending an email with wrong password")
    public void testSendEmailWithWrongPassword() throws MessagingException, IOException {
        BValue[] args = { new BString(HOST_NAME), new BString(USER_NAME), new BString("wrongPassword"),
                new BString(EMAIL_USER_ADDRESS), new BString(EMAIL_SUBJECT), new BString(EMAIL_TEXT),
                new BString(EMAIL_FROM)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSendSimpleEmail", args);
        assertNotNull("No error returned when wrong password is used for sending an email.", returns[0]);
        BError error = (BError) returns[0];
        Assert.assertEquals(error.getReason(), EmailConstants.SEND_ERROR);
    }

    @AfterClass
    public void terminate() {
        mailServer.stop();
    }

}
