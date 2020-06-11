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
package org.ballerinalang.test.statements.transaction;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in TransactionStatement.
 */

public class RetryStmtTest {

    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        resultNegative = BCompileUtil.compile("test-src/statements/transaction/retry_stmt_negative.bal");
    }

    @Test(description = "Test retry statement with errors")
    public void testRetryStatementNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        int index = 0;
        BAssertUtil.validateError(resultNegative, index++, "unknown type 'MyRetryMgr'", 10, 11);
        BAssertUtil.validateError(resultNegative, index++, "undefined symbol 'value'", 16, 28);
    }
}
