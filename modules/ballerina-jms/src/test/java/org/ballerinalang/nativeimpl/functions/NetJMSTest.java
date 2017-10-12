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
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.connector.impl.BServerConnectorFuture;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.CompileResult;
import org.ballerinalang.nativeimpl.util.TestAcknowledgementCallback;
import org.ballerinalang.nativeimpl.util.TestTransactionCallback;
import org.ballerinalang.net.jms.JMSConnectorFutureListener;
import org.ballerinalang.net.jms.actions.utils.Constants;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.Session;

/**
 * Test cases for ballerina.net.jms native functions.
 */
public class NetJMSTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("samples/netJMS.bal");
    }

    @Test (description = "Test Ballerina native JMS Acknowledgement method for success scenario ")
    public void testAcknowledge() {
        Context ctx = new Context(result.getProgFile());

        TestAcknowledgementCallback jmsCallback = new TestAcknowledgementCallback(null, null);
        BServerConnectorFuture connectorFuture = new BServerConnectorFuture();
        ConnectorFutureListener futureListener = new JMSConnectorFutureListener(jmsCallback);
        connectorFuture.setConnectorFutureListener(futureListener);

        ctx.setConnectorFuture(connectorFuture);
        ctx.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, javax.jms.Session.CLIENT_ACKNOWLEDGE);

        BValue[] inputArgs = { null, new BString("SUCCESS") };
        BTestUtils.invoke(result, "testAcknowledge", inputArgs, ctx);

        Assert.assertTrue(jmsCallback.isAcknowledged(), "JMS message is not acknowledged properly");
    }

    @Test (description = "Test Ballerina native JMS Acknowledgement method for failure scenario ")
    public void testAcknowledgeReset() {
        Context ctx = new Context(result.getProgFile());

        TestAcknowledgementCallback jmsCallback = new TestAcknowledgementCallback(null, null);
        BServerConnectorFuture connectorFuture = new BServerConnectorFuture();
        ConnectorFutureListener futureListener = new JMSConnectorFutureListener(jmsCallback);
        connectorFuture.setConnectorFutureListener(futureListener);

        ctx.setConnectorFuture(connectorFuture);
        ctx.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, javax.jms.Session.CLIENT_ACKNOWLEDGE);

        BValue[] inputArgs = { null, new BString("ERROR") };
        BTestUtils.invoke(result, "testAcknowledge", inputArgs, ctx);

        Assert.assertTrue(jmsCallback.isReseted(), "JMS message is not unacknowledged properly");
    }

    @Test (description = "Test Ballerina native JMS Transaction commit method ")
    public void testTransactionCommit() {
        Context ctx = new Context(result.getProgFile());

        TestTransactionCallback jmsCallback = new TestTransactionCallback(null, null);
        BServerConnectorFuture connectorFuture = new BServerConnectorFuture();
        ConnectorFutureListener futureListener = new JMSConnectorFutureListener(jmsCallback);
        connectorFuture.setConnectorFutureListener(futureListener);

        ctx.setConnectorFuture(connectorFuture);
        ctx.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, Session.SESSION_TRANSACTED);

        BValue[] inputArgs = { null };
        BTestUtils.invoke(result, "testCommit", inputArgs, ctx);

        Assert.assertTrue(jmsCallback.isCommited(), "JMS message is not committed properly");
    }

    @Test (description = "Test Ballerina native JMS Transaction rollback")
    public void testTransactionRollback() {
        Context ctx = new Context(result.getProgFile());

        TestTransactionCallback jmsCallback = new TestTransactionCallback(null, null);
        BServerConnectorFuture connectorFuture = new BServerConnectorFuture();
        ConnectorFutureListener futureListener = new JMSConnectorFutureListener(jmsCallback);
        connectorFuture.setConnectorFutureListener(futureListener);

        ctx.setConnectorFuture(connectorFuture);
        ctx.setProperty(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE, Session.SESSION_TRANSACTED);

        BValue[] inputArgs = { null };
        BTestUtils.invoke(result, "testRollback", inputArgs, ctx);

        Assert.assertTrue(jmsCallback.isRollbacked(), "JMS message is not rollbacked properly");
    }

}
