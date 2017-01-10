/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.nativeimpl.connectors;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class ConnectorActionTest {
    private BallerinaFile bFile;

    @BeforeClass()
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/connectors/connector-actions.bal");
    }

    @Test(description = "Test connector actions")
    public void testConnectorAction() {
        BValue[] returns = Functions.invoke(bFile, "callConnector");

        Assert.assertEquals(returns.length, 2);

        BBoolean actionInvoked = (BBoolean) returns[0];
        Assert.assertSame(actionInvoked.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionInvoked.booleanValue(), true, "action named action1 failed");

        BString parameter = (BString) returns[1];
        Assert.assertSame(parameter.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(parameter.stringValue(), "MyParameter", "action named action3 failed");
    }
}
