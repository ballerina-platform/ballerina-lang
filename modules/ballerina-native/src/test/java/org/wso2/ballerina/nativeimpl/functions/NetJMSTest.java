/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.ballerina.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.nativeimpl.connectors.jms.utils.JMSConstants;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;
import org.wso2.ballerina.nativeimpl.util.TestCallback;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

/**
 * Test cases for ballerina.net.jms native functions.
 */
public class NetJMSTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/netJMS.bal");
    }

    @Test
    public void testGetMessageType() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setProperty(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.TEXT_MESSAGE_TYPE);
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArgs = { msg };
        BValue[] returnVals = Functions.invoke(bFile, "testGetMessageType", inputArgs, ctx);
        Assert.assertEquals(returnVals.length, 1);
        BString sc = (BString) returnVals[0];
        Assert.assertEquals(sc.stringValue(), JMSConstants.TEXT_MESSAGE_TYPE);
        cMsg.setProperty(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.MAP_MESSAGE_TYPE);
        msg = new BMessage();
        msg.setValue(cMsg);
        ctx = new Context(cMsg);
        BValue[] inputArgs1 = { msg };
        returnVals = Functions.invoke(bFile, "testGetMessageType", inputArgs1, ctx);
        Assert.assertEquals(returnVals.length, 1);
        sc = (BString) returnVals[0];
        Assert.assertEquals(sc.stringValue(), JMSConstants.MAP_MESSAGE_TYPE);
    }

    @Test
    public void testAcknowledge() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        ctx.setBalCallback(new TestCallback());
        BValue[] inputArgs = { msg, new BString("SUCCESS") };
        Functions.invoke(bFile, "testAcknowledge", inputArgs, ctx);
        String sc = (String) msg.value().getProperty(JMSConstants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, JMSConstants.JMS_MESSAGE_DELIVERY_SUCCESS);
        BValue[] inputArgs1 = { msg, new BString("ERROR") };
        Functions.invoke(bFile, "testAcknowledge", inputArgs1, ctx);
        sc = (String) msg.value().getProperty(JMSConstants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, JMSConstants.JMS_MESSAGE_DELIVERY_ERROR);
    }

    @Test
    public void testCommit() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        ctx.setBalCallback(new TestCallback());
        BValue[] inputArgs = { msg };
        Functions.invoke(bFile, "testCommit", inputArgs, ctx);
        String sc = (String) msg.value().getProperty(JMSConstants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, JMSConstants.JMS_MESSAGE_DELIVERY_SUCCESS);
    }

    @Test
    public void testRollback() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        ctx.setBalCallback(new TestCallback());
        BValue[] inputArgs = { msg };
        Functions.invoke(bFile, "testRollback", inputArgs, ctx);
        String sc = (String) msg.value().getProperty(JMSConstants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, JMSConstants.JMS_MESSAGE_DELIVERY_ERROR);
    }
}
