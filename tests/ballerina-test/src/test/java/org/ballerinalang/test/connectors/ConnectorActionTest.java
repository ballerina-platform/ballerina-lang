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
package org.ballerinalang.test.connectors;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for Connector actions.
 */
@Test(enabled = false)
public class ConnectorActionTest {
    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass()
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/connector-actions.bal");
        resultNegative = BCompileUtil.compile("test-src/connectors/connector-negative.bal");
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        BValue[] returns = BRunUtil.invoke(result, "testAction1");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), false, "action named action1 failed");
    }

    @Test(description = "Test TestConnector action2")
    public void testConnectorAction2() {
        BValue[] returns = BRunUtil.invoke(result, "testAction2");

        Assert.assertEquals(returns.length, 0);
    }

    @Test(description = "Test TestConnector action3")
    public void testConnectorAction3() {
        BValue[] returns = BRunUtil.invoke(result, "testAction3");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), false, "action named action3 failed");
    }

    @Test(description = "Test TestConnector action2 and action3")
    public void testConnectorAction2andAction3() {
        BValue[] returns = BRunUtil.invoke(result, "testAction2andAction3");

        Assert.assertEquals(returns.length, 1);

        BBoolean actionReturned = (BBoolean) returns[0];
        Assert.assertSame(actionReturned.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(actionReturned.booleanValue(), true, "action 2 and 3 failed");
    }

    @Test(description = "Test TestConnector action4")
    public void testConnectorAction4() {
        String inputParam = "inputParam";
        BValue[] functionArgs = new BValue[] { new BString(inputParam) };
        BValue[] returns = BRunUtil.invoke(result, "testAction4", functionArgs);

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
        BValue[] returns = BRunUtil.invoke(result, "testAction5", functionArgs);

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

    @Test(description = "Test action invocation dot notation")
    public void testDotActionInvocation() {
        String functionArg1 = "inputParam1";
        String functionArg2 = "inputParam2";
        int functionArg3 = 3;
        String functionArg4 = "inputParam4";
        BValue[] functionArgs = new BValue[] {
                new BString(functionArg1), new BString(functionArg2), new BInteger(functionArg3),
                new BString(functionArg4)
        };
        BValue[] returns = BRunUtil.invoke(result, "testDotActionInvocation", functionArgs);

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
        BValue[] returns = BRunUtil.invoke(result, "testEmptyParamAction", args);
        Assert.assertEquals(returns.length, 1);

        BString returnStr = (BString) returns[0];
        Assert.assertSame(returnStr.getClass(), BString.class, "Invalid class type returned.");
        Assert.assertEquals(returnStr.stringValue(), input, "emptyParamConnAction failed on return value");

    }

    @Test(description = "Test chained execution of connector actions and returning connector from a action")
    public void testChainedActionInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedActionInvocation");
        Assert.assertEquals(returns.length, 1);

        BInteger returnInt = (BInteger) returns[0];
        Assert.assertSame(returnInt.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(returnInt.intValue(), 60, "invalid value returned");
    }

    @Test(description = "Test chained execution of connector actions and functions and returning connector from a" +
            "function")
    public void testChainedFunctionActionInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedFunctionActionInvocation");
        Assert.assertEquals(returns.length, 1);

        BInteger returnInt = (BInteger) returns[0];
        Assert.assertSame(returnInt.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(returnInt.intValue(), 60, "invalid value returned");
    }

    @Test(description = "Test passing connector as a function parameter")
    public void testPassConnectorAsFunctionParameter() {
        BValue[] returns = BRunUtil.invoke(result, "testPassConnectorAsFunctionParameter");
        Assert.assertEquals(returns.length, 1);

        BInteger returnInt = (BInteger) returns[0];
        Assert.assertSame(returnInt.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(returnInt.intValue(), 87, "invalid value returned");
    }

    @Test(description = "Test passing connector as a action parameter")
    public void testPassConnectorAsActionParameter() {
        BValue[] returns = BRunUtil.invoke(result, "testPassConnectorAsActionParameter");
        Assert.assertEquals(returns.length, 1);

        BInteger returnInt = (BInteger) returns[0];
        Assert.assertSame(returnInt.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(returnInt.intValue(), 30, "invalid value returned");
    }

    @Test(description = "Test connectors with errors")
    public void testConnectorNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        BAssertUtil.validateError(resultNegative, 0, "redeclared symbol 'TestConnector1'", 37, 1);
        BAssertUtil.validateError(resultNegative, 1, "redeclared symbol 'foo'", 54, 5);
        BAssertUtil.validateError(resultNegative, 2, "unknown type 'UndefinedConnector'", 2, 5);
        BAssertUtil.validateError(resultNegative, 3, "undefined connector 'UndefinedConnector'", 2, 40);
        BAssertUtil.validateError(resultNegative, 4, "undefined action 'foo' in " +
                "connector 'TestConnector'", 24, 13);
    }

//    @Test(description = "Test defining duplicate action",
//            expectedExceptions = {SemanticException.class },
//            expectedExceptionsMessageRegExp = "duplicate-action.bal:9: redeclared symbol 'foo'")
//    public void testActionInvokeDotWrongNotation() {
//        String functionArg1 = "inputParam1";
//        String functionArg2 = "inputParam2";
//        int functionArg3 = 3;
//        String functionArg4 = "inputParam4";
//        BValue[] functionArgs = new BValue[] {
//                new BString(functionArg1), new BString(functionArg2), new BInteger(functionArg3),
//                new BString(functionArg4)
//        };
//        BLangFunctions.invokeNew(programFile, "testDotActionInvocationWrong", functionArgs);
//    }

//    @Test(description = "Test incorrect action invocation",
//            expectedExceptions = {SemanticException.class },
//            expectedExceptionsMessageRegExp = "incorrect-action-invocation.bal:3: invalid action invocation. " +
//                    "connector variable expected")
    public void testIncorrectActionInvocation() {
        BCompileUtil.compile("lang/connectors/incorrect-action-invocation.bal");
    }

//    @Test(description = "Test invalid action invocation",
//            expectedExceptions = {SemanticException.class },
//            expectedExceptionsMessageRegExp = "invalid-action-invocation.bal:2: undefined connector 'lk'")
    public void testInvalidActionInvocation() {
        BCompileUtil.compile("lang/connectors/invalid-action-invocation.bal");
    }

//    @Test(description = "Test action invocation with no arg")
    public void testActionInvocationWithNoArgs() {
        BCompileUtil.compile("lang/connectors/action-invocation-with-no-args.bal");
    }
}
