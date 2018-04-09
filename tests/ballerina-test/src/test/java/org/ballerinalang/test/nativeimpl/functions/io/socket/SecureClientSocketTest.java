/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.nativeimpl.functions.io.socket;

import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test class for secure client socket related actions.
 */
@Test
public class SecureClientSocketTest {

    private static final Logger log = LoggerFactory.getLogger(SecureClientSocketTest.class);

    private CompileResult socketClient;
    private Process server;
    private int port;

    @BeforeClass
    public void setup() {
        port = ThreadLocalRandom.current().nextInt(47000, 55000);
        socketClient = BCompileUtil.compileAndSetup("test-src/io/secure_client_socket_io.bal");
        boolean connectionStatus;
        int numberOfRetryAttempts = 10;
        try {
            server = MockSecureSocketServer.start(String.valueOf(port));
            connectionStatus = isConnected(port, numberOfRetryAttempts);
            if (!connectionStatus) {
                Assert.fail("Unable to open connection with the test TCP server");
            }
        } catch (Exception e) {
            log.error("Unable to open Socket Server: " + e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Closes a provided socket connection.
     *
     * @param socket socket which should be closed.
     */
    private void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error occurred while closing the Socket connection", e);
        }
    }

    /**
     * Will enforce to sleep the thread for the provided time.
     *
     * @param retryInterval the time in milliseconds the thread should sleep.
     */
    private void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Attempts to establish a connection with the test server.
     *
     * @param port            port of the server.
     * @param numberOfRetries number of retry attempts.
     * @return true if the connection is established successfully.
     */
    private boolean isConnected(int port, int numberOfRetries) {
        Socket temporarySocketConnection = null;
        boolean isConnected = false;
        final int retryInterval = 1000;
        final int initialRetryCount = 0;
        for (int retryCount = initialRetryCount; retryCount < numberOfRetries && !isConnected; retryCount++) {
            try {
                //Attempts to establish a connection with the server
                temporarySocketConnection = new Socket("localhost", port);
                isConnected = true;
            } catch (IOException e) {
                log.error("Error occurred while establishing a connection with test server", e);
                sleep(retryInterval);
            } finally {
                if (null != temporarySocketConnection) {
                    //We close the connection once completed.
                    close(temporarySocketConnection);
                }
            }
        }
        return isConnected;
    }

    @AfterClass
    public void cleanup() {
        server.destroy();
    }

    @Test(description = "Test connection open with properties")
    public void testOpenSecureClientSocket() throws URISyntaxException {
        PackageInfo ioPackageInfo = socketClient.getProgFile().getPackageInfo("ballerina.io");
        StructInfo socketProperties = ioPackageInfo.getStructInfo("SocketProperties");
        BStruct propertyStruct = BLangVMStructs.createBStruct(socketProperties);
        URL resource = getClass().getClassLoader().
                getResource("datafiles/security/keyStore/ballerinaTruststore.p12");
        Assert.assertNotNull(resource, "Unable to find TrustStore.");
        propertyStruct.setStringField(2, Paths.get(resource.toURI()).toFile().getAbsolutePath());
        propertyStruct.setStringField(3, "ballerina");
        BValue[] args = { new BString("localhost"), new BInteger(port), propertyStruct };
        BRunUtil.invokeStateful(socketClient, "openSocketConnection", args);
    }

    @Test(dependsOnMethods = "testOpenSecureClientSocket", description = "Test content read/write")
    public void testWriteReadContent() {
        final String newline = System.lineSeparator();
        String content = "Hello World" + newline;
        final byte[] contentBytes = content.getBytes();
        BValue[] args = { new BBlob(contentBytes)};
        final BValue[] writeReturns = BRunUtil.invokeStateful(socketClient, "write", args);
        BInteger returnedSize = (BInteger) writeReturns[0];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Write content size is not match.");
        args = new BValue[] { new BInteger(content.length()) };
        final BValue[] readReturns = BRunUtil.invokeStateful(socketClient, "read", args);
        final BBlob readContent = (BBlob) readReturns[0];
        returnedSize = (BInteger) readReturns[1];
        Assert.assertEquals(readContent.stringValue(), content, "Return content are not match with written content.");
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Read size not match with the request size");
    }

    @Test(dependsOnMethods = "testWriteReadContent",
          description = "Test the connection closure")
    public void testClosure() {
        BRunUtil.invokeStateful(socketClient, "closeSocket");
    }
}
