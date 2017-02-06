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
import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.HTTPConnector;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.runtime.registry.PackageRegistry;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

//import org.wso2.ballerina.core.model.values.BInteger;

/**
 * Test class for Connector actions.
 */
public class ConnectorActionTest {
    private BallerinaFile bFile;
    private SymScope symScope;

    @BeforeClass()
    public void setup() {
        symScope = GlobalScopeHolder.getInstance().getScope();
        PackageRegistry.getInstance().registerNativeConnector(new HTTPConnector());
        bFile = ParserUtils.parseBalFile("lang/connectors/connector-actions.bal");
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        BValue[] returns = Functions.invoke(bFile, "testAction1");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), false, "action named action1 failed");
    }

    @Test(description = "Test TestConnector action2")
    public void testConnectorAction2() {
        BValue[] returns = Functions.invoke(bFile, "testAction2");

        Assert.assertEquals(returns.length, 0);
    }

    @Test(description = "Test TestConnector action3")
    public void testConnectorAction3() {
        BValue[] returns = Functions.invoke(bFile, "testAction3");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), false, "action named action3 failed");
    }

    @Test(description = "Test TestConnector action2 and action3")
    public void testConnectorAction2andAction3() {
        BValue[] returns = Functions.invoke(bFile, "testAction2andAction3");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), true, "action 2 and 3 failed");
    }

    @Test(description = "Test TestConnector action4")
    public void testConnectorAction4() {
        String inputParam = "inputParam";
        BValue[] functionArgs = new BValue[] { new BString(inputParam) };
        BValue[] returns = Functions.invoke(bFile, "testAction4", functionArgs);

        Assert.assertEquals(returns.length, 1);

        BString actionReturned = (BString) returns[0];
        Assert.assertSame(actionReturned.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.stringValue(), inputParam, "action 4 failed");
    }

//    @Test(description = "Test TestConnector action5")
//    public void testConnectorAction5() {
//        String functionArg1 = "inputParam1";
//        String functionArg2 = "inputParam2";
//        int functionArg3 = 3;
//        String functionArg4 = "inputParam4";
//        BValue[] functionArgs = new BValue[] {
//                new BString(functionArg1), new BString(functionArg2), new BInteger(functionArg3),
//                new BString(functionArg4)
//        };
//        BValue[] returns = Functions.invoke(bFile, "testAction5", functionArgs);
//
//        Assert.assertEquals(returns.length, 3);
//
//        BString returnVal1 = (BString) returns[0];
//        Assert.assertSame(returnVal1.getClass(), BString.class, "Invalid class type returned.");
//        Assert.assertEquals(returnVal1.stringValue(), functionArg4, "action 5 failed on first return value");

        //TODO: uncomment the following assertion once the multi value return support is added. Issue #702
//        BString returnVal2 = (BString) returns[1];
//        Assert.assertSame(returnVal2.getClass(), BString.class, "Invalid class type returned.");
//        Assert.assertEquals(returnVal2.stringValue(), functionArg2, "action 5 failed on 2nd return value");
//
//        BInteger returnVal3 = (BInteger) returns[2];
//        Assert.assertSame(returnVal3.getClass(), BString.class, "Invalid class type returned.");
//        Assert.assertEquals(returnVal3.intValue(), functionArg3, "action 5 failed on 3rd return value");
//    }
    
    @Test(description = "Test invoking an undefined connector",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Connector : ballerina.net.http:HTTPConnector not found in " +
            "undefined-action-stmt.bal:4")
    public void testUndefinedConnector() {
        ParserUtils.parseBalFile("lang/statements/undefined-action-stmt.bal");
    }
    
    @Test(description = "Test invoking an undefined action",
            expectedExceptions = {LinkerException.class },
            expectedExceptionsMessageRegExp = "Undefined action: foo in undefined-action-stmt.bal:5")
    public void testUndefinedAction() {
        ParserUtils.parseBalFile("lang/statements/undefined-action-stmt.bal", symScope);
    }
    
    @Test(description = "Test defining duplicate connector",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Duplicate connector definition: samples.connectors.test:TestConnector" +
            " in duplicate-connector.bal:13")
    public void testDuplicateConnectorDef() {
        ParserUtils.parseBalFile("lang/connectors/duplicate-connector.bal", symScope);
    }
    
    @Test(description = "Test defining duplicate action",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Duplicate action definition: " +
            "samples.connectors.test:TestConnector.foo_TestConnector in duplicate-action.bal:13")
    public void testDuplicateAction() {
        ParserUtils.parseBalFile("lang/connectors/duplicate-action.bal", symScope);
    }
}
