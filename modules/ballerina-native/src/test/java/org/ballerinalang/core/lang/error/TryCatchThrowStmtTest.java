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
package org.ballerinalang.core.lang.error;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for testing TryCatchStmt and Throw Stmt.
 */
public class TryCatchThrowStmtTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/errors/testTryCatchStmt.bal");
    }

    @Test(description = "Test try block execution.")
    public void testTryCatchStmt() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "start insideTry insideInnerTry endInsideInnerTry innerFinally endInsideTry Finally End",
                "Try block didn't execute fully.");
    }

    @Test(description = "Test catch block execution.")
    public void testTryCatchWithThrow() {
        BValue[] args = {new BInteger(15)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "start insideTry insideInnerTry onError innerTestErrorCatch:test innerFinally TestErrorCatch" +
                        " Finally End", "Try block didn't execute fully.");
    }

    @Test(description = "Test catch block execution, where thrown exception is caught using equivalent catch block ")
    public void testTryCatchEquivalentCatch() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "start insideTry insideInnerTry onInputError innerFinally ErrorCatch Finally End",
                "Try block didn't execute fully.");
    }

    @Test(description = "Test throw statement in a function.")
    public void testTryCatch() {
        BValue[] args = {new BInteger(15)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testFunctionThrow", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true, "Catch block didn't execute.");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "013", "Unexpected execution order.");
    }

    @Test(description = "Test uncaught error in a function.", expectedExceptionsMessageRegExp = ".*uncaught error: " +
            "Error\\{ msg : test message\\}", expectedExceptions = BallerinaException.class)
    public void testUncaughtException() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUncaughtException", args);
    }

    @Test(description = "Test getStack trace of an error in a function.")
    public void testGetStackTrace() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStackTrace", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BStruct);
        BValue value = ((BStruct) returns[0]).getValue(0);
        Assert.assertNotNull(value);
        Assert.assertNotNull(value instanceof BArray);
        BArray bArray = (BArray) value;
        Assert.assertEquals(bArray.size(), 3);
        Assert.assertEquals(((BStruct) bArray.get(0)).getValue(0).stringValue(), "testNestedThrow");
        Assert.assertEquals(((BStruct) bArray.get(1)).getValue(0).stringValue(), "testUncaughtException");
        Assert.assertEquals(((BStruct) bArray.get(2)).getValue(0).stringValue(), "testStackTrace");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*redeclared symbol 'e'.*")
    public void testDuplicateExceptionVariable() {
        BTestUtils.parseBalFile("lang/errors/duplicate-var-try-catch.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*only a struct type " +
            "structurally equivalent to ballerina.lang.errors:Error is allowed here")
    public void testInvalidThrow() {
        BTestUtils.parseBalFile("lang/errors/invalid-throw.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*only a struct type " +
            "structurally equivalent to ballerina.lang.errors:Error is allowed here")
    public void testInvalidFunctionThrow() {
        BTestUtils.parseBalFile("lang/errors/invalid-function-throw.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*error 'TestError' " +
            "already caught in try catch block")
    public void testDuplicateCatchBlock() {
        BTestUtils.parseBalFile("lang/errors/duplicate-catch-block.bal");
    }

}
