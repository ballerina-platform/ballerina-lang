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

package org.wso2.ballerina.nativeimpl.connectors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.interpreter.nonblocking.ModeResolver;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;


public class JMSClientTest {
    private BallerinaFile bFile;
    private SymScope globalScope;
    private boolean isNonBlockingEnabled;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/jmsClientConnectorTest.bal");
        globalScope = new SymScope(SymScope.Name.GLOBAL);
        BuiltInNativeConstructLoader.loadConstructs();
        isNonBlockingEnabled = ModeResolver.getInstance().isNonblockingEnabled();
        ModeResolver.getInstance().setNonblockingEnabled(false);
    }

    @Test(description = "Test for jms client connector without valid initial context factory",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = ".*Connector parameters not defined correctly..*")
    public void testJMSClientConnectorWithoutValidInitialContextFactory() throws BallerinaException {
        Functions.invoke(bFile, "jmsClientConnectorTest");
    }

    @Test(description = "Test for jms client connector without valid message",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = ".*If the message type is TextMessage, a string payload must be set.*")
    public void testJMSClientConnectorWithoutValidMessage() throws BallerinaException {
        Functions.invoke(bFile, "jmsSendNoMessageTest");
    }

    @Test(description = "Test for jms client connector map message without data",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp =
                    ".*If the message type is MapMessage, either set MapData property or pass a " +
                    "received jms map message*")
    public void testJMSClientConnectorMapMessageWithoutData() throws BallerinaException {
        Functions.invoke(bFile, "jmsSendMapMessageWithoutData");
    }

    @AfterClass
    public void tearDown() {
        ModeResolver.getInstance().setNonblockingEnabled(isNonBlockingEnabled);
    }
}
