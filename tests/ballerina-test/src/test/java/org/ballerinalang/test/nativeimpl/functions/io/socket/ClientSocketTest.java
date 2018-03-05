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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ThreadLocalRandom;

public class ClientSocketTest {

    private CompileResult socketClient;
    private MockSocketServer server;
    private int port = ThreadLocalRandom.current().nextInt(49152, 65535);

    @BeforeClass
    public void setup() {
        socketClient = BCompileUtil.compile("test-src/io/clientsocketio.bal");
        server = new MockSocketServer(port);
        server.start();
    }

    @AfterClass
    public void cleanup() {
        server.stop();
    }

    @Test(description = "Open client socket connection to the remote server that started in 9999")
    public void testOpenClientSocket() {
        BValue[] args = { new BString("localhost"), new BInteger(port) };
        BRunUtil.invoke(socketClient, "openSocketConnection", args);
    }

    @Test(dependsOnMethods = "testOpenClientSocket",
          description = "Test content read/write")
    public void testWriteReadContent() {
        String content = "Hello World\n";
        BValue[] args = { new BBlob(content.getBytes()) };
        final BValue[] writeReturns = BRunUtil.invoke(socketClient, "write", args);
        BInteger returnedSize = (BInteger) writeReturns[0];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Write content size is not match.");
        args = new BValue[] { new BInteger(content.length()) };
        final BValue[] readReturns = BRunUtil.invoke(socketClient, "read", args);
        final BBlob readContent = (BBlob) readReturns[0];
        returnedSize = (BInteger) readReturns[1];

        Assert.assertEquals(readContent.stringValue(), content, "Return content are not match with written content.");
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Read size not match with the request size");
    }

    @Test(dependsOnMethods = "testWriteReadContent",
          description = "Test the connection closure")
    public void testClosure() {
        BRunUtil.invoke(socketClient, "closeSocket");
    }

    /*@Test(dependsOnMethods = "testClosure",
          description = "Test connection open with properties")*/
    public void testOpenWithProperties() {
        int port = ThreadLocalRandom.current().nextInt(33000, 46000);
        PackageInfo ioPackageInfo = socketClient.getProgFile().getPackageInfo("ballerina.io");
        StructInfo socketProperties = ioPackageInfo.getStructInfo("SocketProperties");
        BStruct propertyStruct = BLangVMStructs.createBStruct(socketProperties, port);
        BValue[] args = { new BString("localhost"), new BInteger(port), propertyStruct };
        final BValue[] returns = BRunUtil.invoke(socketClient, "openSocketConnectionWithProps", args);
        final BStruct socket = (BStruct) returns[0];
        Assert.assertEquals(socket.getIntField(1), port, "Client port didn't bind to assign port.");
        args = new BValue[] { socket };
        BRunUtil.invoke(socketClient, "close", args);
    }
}
