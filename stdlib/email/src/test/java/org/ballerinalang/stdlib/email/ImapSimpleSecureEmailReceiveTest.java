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
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

/**
 * Test class for email receipt using IMAPS with least number of parameters.
 *
 * @since 1.2.0
 */
public class ImapSimpleSecureEmailReceiveTest {

    private GreenMailUser user;
    private CompileResult compiledResult;
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
        startServer();
        compileBallerinaScript();
    }

    @Test(description = "Test for receiving zero emails with simple parameters")
    public void testReceiveZeroEmails() {
        readZeroEmails();
    }

    @Test(
            description = "Test for receiving an email with simple parameters",
            dependsOnMethods = "testReceiveZeroEmails"
    )
    public void testReceiveSimpleEmail() throws MessagingException {
        sendEmail();
        readEmails();
    }

    @Test(
            description = "Test for receiving an email with simple parameters",
            dependsOnMethods = "testReceiveZeroEmails"
    )
    public void testReceiveErrorWhileReadingEmail() throws MessagingException {
        sendEmail();
        readEmails();
    }

    @AfterClass
    public void terminate() {
        mailServer.stop();
    }

    private void readEmails() {
        BValue[] args = {new BString(HOST_NAME), new BString(USER_NAME), new BString(USER_PASSWORD)};
        BValue[] returns = BRunUtil.invoke(compiledResult, "testReceiveSimpleEmail", args);
        assertNotNull("No response received with simple IMAP configuration.", returns);
        if (returns[0] instanceof BError) {
            fail("Error occurred while reading from IMAP server.");
        } else if (returns[0] == null) {
            fail("IMAP Client could read zero emails.");
        } else {
            BMap<String, BValue> email = (BMap<String, BValue>) returns[0];
            String subject = email.get(EmailConstants.MESSAGE_SUBJECT).stringValue();
            assertEquals(EMAIL_SUBJECT, subject);
        }
    }

    private void readZeroEmails() {
        BValue[] args = {new BString(HOST_NAME), new BString(USER_NAME), new BString(USER_PASSWORD)};
        BValue[] returns = BRunUtil.invoke(compiledResult, "testReceiveSimpleEmail", args);
        assertNotNull("No response received with simple IMAP configuration.", returns);
        if (returns[0] instanceof BError) {
            fail("Error occurred while reading from IMAP server.");
        } else {
            assertNull("Returned non-null result for zero emails.", returns[0]);
        }
    }

    private void startServer() {
        Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummySSLSocketFactory.class.getName());
        mailServer = new GreenMail(ServerSetupTest.IMAPS);
        mailServer.start();
        user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
    }

    private void sendEmail() throws MessagingException {
        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS));
        message.setSubject(EMAIL_SUBJECT);
        message.setText(EMAIL_TEXT);
        user.deliver(message);
    }

    private void compileBallerinaScript() {
        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src",
                "ImapSimpleSecureEmailReceive.bal");
        compiledResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

}
