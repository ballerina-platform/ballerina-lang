/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.test.statements.compensation;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test scope,compensation and compensate statements.
 */
public class CompensationsStmtTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/compensation/compensate-stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/compensation/compensation-stmt-negative.bal");
    }

    @Test(description = "Test compensation statements with errors")
    public void testCompensationStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateErrorMessageOnly(resultNegative, 0, "expecting 'compensation'");
        BAssertUtil.validateErrorMessageOnly(resultNegative, 1, "missing token Identifier before ';'");
    }

    @Test(description = "Test nested scopes and scopes in loops")
    public void compensationStmtTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
    }
}
