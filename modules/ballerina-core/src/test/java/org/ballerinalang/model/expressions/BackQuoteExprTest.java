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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Primitive add expression test.
 */
public class BackQuoteExprTest {

    private ProgramFile programFile;

    @Test(description = "Test two int add expression", 
            expectedExceptions = SemanticException.class, 
            expectedExceptionsMessageRegExp = "back-quote-expr.bal:3: backtick expression is not supported")
    public void testBackQuoteExpr() {
        programFile = BTestUtils.getProgramFile("lang/expressions/back-quote-expr.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getProduct");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String actual = ((BXML) returns[0]).stringValue();
        String expected = "<Product><ID>1234</ID><Name>XYZ</Name><Description>Sample Product</Description></Product>";
        Assert.assertEquals(actual, expected);
    }
}
