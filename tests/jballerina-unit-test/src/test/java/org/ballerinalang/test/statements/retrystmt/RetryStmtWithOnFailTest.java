/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.retrystmt;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in Retry Statement.
 */
public class RetryStmtWithOnFailTest {

    private CompileResult programFile, negativeFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/retrystmt/retry_stmt_with_on_fail.bal");
        negativeFile = BCompileUtil.compile("test-src/statements/retrystmt/retry_on_fail_negative.bal");
    }

    @Test
    public void testRetryStatement() {
        BValue[] params = {};
        BRunUtil.invoke(programFile, "testRetryStatement", params);
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative1() {
        int index = 0;
        BAssertUtil.validateError(negativeFile, index++, "unreachable code", 20, 12);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'e'", 21, 8);
        BAssertUtil.validateError(negativeFile, index++, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 32, 6);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'e'", 32, 6);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'e'", 45, 4);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'e'", 59, 4);
        BAssertUtil.validateError(negativeFile, index++, "unreachable code", 62, 7);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'e1'", 78, 4);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'resA'", 97, 6);
        BAssertUtil.validateWarning(negativeFile, index++, "unused variable 'resB'", 98, 6);
        BAssertUtil.validateError(negativeFile, index++, "incompatible error definition type: " +
                "'ErrorTypeB' will not be matched to 'ErrorTypeA'", 100, 4);
        Assert.assertEquals(negativeFile.getDiagnostics().length, index);
    }

    @AfterClass
    public void tearDown() {
        programFile = null;
        negativeFile = null;
    }
}
