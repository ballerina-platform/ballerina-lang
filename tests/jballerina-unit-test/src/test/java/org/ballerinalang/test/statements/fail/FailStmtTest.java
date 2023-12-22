/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.statements.fail;

import io.ballerina.runtime.api.values.BError;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test fail statement.
 *
 * @since Swan Lake
 */
public class FailStmtTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/fail/fail-statement.bal");
    }

    @Test(description = "Test fail statement basic syntax")
    public void testFailStmt() {
        Object returnValues = BRunUtil.invoke(result, "testFailStmt");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue(returnValues instanceof BError);
        Assert.assertEquals(((BError) returnValues).getMessage(),
                "Custom error thrown explicitly.");
    }

    @Test(description = "Test negative cases in fail statement")
    public void testFailActionNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/statements/fail/fail-statement-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        int index = 0;

        validateError(negativeResult, index++,
                "type '(error|int)' not allowed here; expected an 'error' or a subtype of 'error'",
                5, 10);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
