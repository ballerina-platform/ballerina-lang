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
package org.ballerinalang.natives.connectors;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for Connector actions.
 */
public class ConnectorActionTest {
    private BLangProgram bLangProgram;

    @BeforeClass()
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/connectors/connector-actions.bal");
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAction1");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), false, "action named action1 failed");
    }

    @Test(description = "Test TestConnector action2")
    public void testConnectorAction2() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAction2");

        Assert.assertEquals(returns.length, 0);
    }

    @Test(description = "Test TestConnector action3")
    public void testConnectorAction3() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAction3");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), false, "action named action3 failed");
    }

    @Test(description = "Test TestConnector action2 and action3")
    public void testConnectorAction2andAction3() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAction2andAction3");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), true, "action 2 and 3 failed");
    }

    @Test(description = "Test TestConnector action4")
    public void testConnectorAction4() {
        String inputParam = "inputParam";
        BValue[] functionArgs = new BValue[] { new BString(inputParam) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAction4", functionArgs);

        Assert.assertEquals(returns.length, 1);

        BString actionReturned = (BString) returns[0];
        Assert.assertSame(actionReturned.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.stringValue(), inputParam, "action 4 failed");
    }

    @Test(description = "Test TestConnector action5")
    public void testConnectorAction5() {
        String functionArg1 = "inputParam1";
        String functionArg2 = "inputParam2";
        int functionArg3 = 3;
        String functionArg4 = "inputParam4";
        BValue[] functionArgs = new BValue[] {
                new BString(functionArg1), new BString(functionArg2), new BInteger(functionArg3),
                new BString(functionArg4)
        };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAction5", functionArgs);

        Assert.assertEquals(returns.length, 3);

        BString returnVal1 = (BString) returns[0];
        Assert.assertSame(returnVal1.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(returnVal1.stringValue(), functionArg4, "action 5 failed on first return value");

        BString returnVal2 = (BString) returns[1];
        Assert.assertSame(returnVal2.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(returnVal2.stringValue(), functionArg2, "action 5 failed on 2nd return value");

        BInteger returnVal3 = (BInteger) returns[2];
        Assert.assertSame(returnVal3.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(returnVal3.intValue(), functionArg3, "action 5 failed on 3rd return value");
    }

    @Test
    public void testEmptyParamConnector() {
        String input = "hello";
        BValue[] args = new BValue[] { new BString(input) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testEmptyParamAction", args);
        Assert.assertEquals(returns.length, 1);

        BString returnStr = (BString) returns[0];
        Assert.assertSame(returnStr.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(returnStr.stringValue(), input, "emptyParamConnAction failed on return value");

    }
    
    @Test(description = "Test invoking an undefined connector",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "undefined-connector.bal:4: undefined type 'UndefinedConnector'")
    public void testUndefinedConnector() {
        BTestUtils.parseBalFile("lang/connectors/undefined-connector.bal");
    }
    
    @Test(description = "Test invoking an undefined action",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "undefined-actions.bal:16: undefined action 'TestConnector.foo'")
    public void testUndefinedAction() {
        BTestUtils.parseBalFile("lang/connectors/undefined-actions.bal");
    }
    
    @Test(description = "Test defining duplicate connector",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "duplicate-connector.bal:13: redeclared symbol 'TestConnector'")
    public void testDuplicateConnectorDef() {
        BTestUtils.parseBalFile("lang/connectors/duplicate-connector.bal");
    }
    
    @Test(description = "Test defining duplicate action",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "duplicate-action.bal:11: redeclared symbol 'foo'")
    public void testDuplicateAction() {
        BTestUtils.parseBalFile("lang/connectors/duplicate-action.bal");
    }
}
