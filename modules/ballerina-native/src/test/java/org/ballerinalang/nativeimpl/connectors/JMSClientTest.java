/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.connectors;

import org.ballerinalang.bre.SymScope;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class JMSClientTest {
    private SymScope globalScope;
    private BLangProgram bLangProgram;
    private boolean isNonBlockingEnabled;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/jmsClientConnectorTest.bal");
        globalScope = new SymScope(SymScope.Name.GLOBAL);
        BuiltInNativeConstructLoader.loadConstructs();
        isNonBlockingEnabled = ModeResolver.getInstance().isNonblockingEnabled();
        ModeResolver.getInstance().setNonblockingEnabled(false);
    }

    @Test(description = "Test for jms client connector without valid initial context factory",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = ".*Connector parameters not defined correctly..*")
    public void testJMSClientConnectorWithoutValidInitialContextFactory() throws BallerinaException {
        BLangFunctions.invoke(bLangProgram, "jmsClientConnectorTest");
    }

    @Test(description = "Test for jms client connector without valid message",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = ".*If the message type is TextMessage, a string payload must be set.*")
    public void testJMSClientConnectorWithoutValidMessage() throws BallerinaException {
        BLangFunctions.invoke(bLangProgram, "jmsSendNoMessageTest");
    }

    @Test(description = "Test for jms client connector map message without data",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp =
                    ".*If the message type is MapMessage, either set MapData property or pass a " +
                    "received jms map message*")
    public void testJMSClientConnectorMapMessageWithoutData() throws BallerinaException {
        BLangFunctions.invoke(bLangProgram, "jmsSendMapMessageWithoutData");
    }

    @AfterClass
    public void tearDown() {
        ModeResolver.getInstance().setNonblockingEnabled(isNonBlockingEnabled);
    }
}
