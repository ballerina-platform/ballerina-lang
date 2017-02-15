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
package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Test cases for testing TryCatchStmt and Throw Stmt.
 */
public class TryCatchThrowStmtTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/statements/tryCatchThrowStmts/testTryCatch.bal");
    }

    @Test(description = "Test try block execution.")
    public void testTryCatchStmt() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = Functions.invoke(bFile, "testNestedTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false, "Try block didn't execute fully.");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "012468", "Try block didn't execute fully.");
    }

    @Test(description = "Test catch block execution.")
    public void testTryCatchWithThrow() {
        BValue[] args = {new BInteger(15)};
        BValue[] returns = Functions.invoke(bFile, "testNestedTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true, "Catch block didn't execute.");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "012357", "Catch block didn't execute.");
    }

    @Test(description = "Test throw statement in a function.")
    public void testTryCatch() {
        BValue[] args = {new BInteger(15)};
        BValue[] returns = Functions.invoke(bFile, "testFunctionThrow", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true, "Catch block didn't execute.");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "013", "Unexpected execution order.");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*redeclared symbol 'e'.*")
    public void testDuplicateExceptionVariable() {
        ParserUtils.parseBalFile("lang/statements/tryCatchThrowStmts/duplicate-var-try-catch.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*type 'exception' is " +
            "allowed in throw statement*")
    public void testInvalidThrow() {
        ParserUtils.parseBalFile("lang/statements/tryCatchThrowStmts/invalid-throw.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*type 'exception' is " +
            "allowed in throw statement.*")
    public void testInvalidFunctionThrow() {
        ParserUtils.parseBalFile("lang/statements/tryCatchThrowStmts/invalid-function-throw.bal");
    }

}
