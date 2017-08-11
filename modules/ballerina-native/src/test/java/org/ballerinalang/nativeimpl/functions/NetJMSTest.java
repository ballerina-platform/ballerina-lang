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

package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.jms.utils.Constants;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.TestCallback;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import javax.jms.Session;

/**
 * Test cases for ballerina.net.jms native functions.
 */
public class NetJMSTest {
    private ProgramFile bLangProgram;
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("samples/netJMS.bal");
    }

    @Test
    public void testAcknowledge() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, Session.CLIENT_ACKNOWLEDGE);
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context();
        ctx.setCarbonMessage(cMsg);
        ctx.setBalCallback(new TestCallback());
        BValue[] inputArgs = { msg, new BString("SUCCESS") };
        BLangFunctions.invokeNew(bLangProgram, "testAcknowledge", inputArgs, ctx);
        String sc = (String) msg.value().getProperty(Constants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, Constants.JMS_MESSAGE_DELIVERY_SUCCESS);
        BValue[] inputArgs1 = { msg, new BString("ERROR") };
        BLangFunctions.invokeNew(bLangProgram, "testAcknowledge", inputArgs1, ctx);
        sc = (String) msg.value().getProperty(Constants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, Constants.JMS_MESSAGE_DELIVERY_ERROR);
    }

    @Test
    public void testCommit() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, Session.SESSION_TRANSACTED);
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context();
        ctx.setCarbonMessage(cMsg);
        ctx.setBalCallback(new TestCallback());
        BValue[] inputArgs = { msg };
        BLangFunctions.invokeNew(bLangProgram, "testCommit", inputArgs, ctx);
        String sc = (String) msg.value().getProperty(Constants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, Constants.JMS_MESSAGE_DELIVERY_SUCCESS);
    }

    @Test
    public void testRollback() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, Session.SESSION_TRANSACTED);
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context();
        ctx.setCarbonMessage(cMsg);
        ctx.setBalCallback(new TestCallback());
        BValue[] inputArgs = { msg };
        BLangFunctions.invokeNew(bLangProgram, "testRollback", inputArgs, ctx);
        String sc = (String) msg.value().getProperty(Constants.JMS_MESSAGE_DELIVERY_STATUS);
        Assert.assertEquals(sc, Constants.JMS_MESSAGE_DELIVERY_ERROR);
    }
}
