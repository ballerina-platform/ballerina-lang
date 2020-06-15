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
import com.icegreen.greenmail.util.ServerSetup;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test class for email receipt using the listener.
 *
 * @since 1.3.0
 */
public class ListenerPopReceiveTest {

    private GreenMailUser user;
    private CompileResult compiledResult;
    private static final int PORT_NUMBER = 3995;
    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost";
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String SSL_SOCKET_FACTORY_PROVIDER = "ssl.SocketFactory.provider";
    private static final int SERVER_TIMEOUT = 5000;
    private GreenMail mailServer;

    @BeforeClass
    public void setup() {
        startServer();
    }

    @Test(description = "Test for receiving an email with simple parameters")
    public void testReceiveSimpleEmail() throws MessagingException, InterruptedException {
        compileBallerinaScript();
        sendEmail();
        Thread.sleep(10000);
        readEmail();
    }

    @Test(
            description = "Test for receiving an email with simple parameters",
            dependsOnMethods = "testReceiveSimpleEmail"
    )
    public void testReceiveError() throws InterruptedException {
        compileBallerinaScript();
        mailServer.stop();
        Thread.sleep(10000);
        readError();
    }

    private void readEmail() {
        BValue[] isOnMessageInvokedReturns = BRunUtil.invoke(compiledResult, "isOnMessageInvoked");
        assertTrue(isOnMessageInvokedReturns[0] instanceof BBoolean);
        assertTrue(((BBoolean) isOnMessageInvokedReturns[0]).booleanValue());

        BValue[] receivedMessageReturns = BRunUtil.invoke(compiledResult, "getReceivedMessage");
        assertTrue(receivedMessageReturns[0] instanceof BString);
        assertEquals(receivedMessageReturns[0].stringValue(), "Test E-Mail");

        BValue[] isOnErrorInvokedReturns = BRunUtil.invoke(compiledResult, "isOnErrorInvoked");
        assertTrue(isOnErrorInvokedReturns[0] instanceof BBoolean);
        assertFalse(((BBoolean) isOnErrorInvokedReturns[0]).booleanValue());

        BValue[] receivedErrorReturns = BRunUtil.invoke(compiledResult, "getReceivedError");
        assertTrue(receivedErrorReturns[0] instanceof BString);
        assertEquals((receivedErrorReturns[0]).stringValue(), "");
    }

    private void readError() {
        BValue[] isOnErrorInvokedReturns = BRunUtil.invoke(compiledResult, "isOnErrorInvoked");
        assertTrue(isOnErrorInvokedReturns[0] instanceof BBoolean);
        assertTrue(((BBoolean) isOnErrorInvokedReturns[0]).booleanValue());

        BValue[] receivedErrorReturns = BRunUtil.invoke(compiledResult, "getReceivedError");
        assertTrue(receivedErrorReturns[0] instanceof BString);
        assertEquals((receivedErrorReturns[0]).stringValue(), "Couldn't connect to host, port: 127.0.0.1," +
                " 3995; timeout -1");
    }

    private void startServer() {
        Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummySSLSocketFactory.class.getName());
        ServerSetup setup = new ServerSetup(PORT_NUMBER, null, ServerSetup.PROTOCOL_POP3S);
        setup.setServerStartupTimeout(SERVER_TIMEOUT);
        mailServer = new GreenMail(setup);
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
                "ListenerPopReceive.bal");
        compiledResult = BCompileUtil.compileOffline(true, sourceFilePath.toAbsolutePath().toString());
    }

}
