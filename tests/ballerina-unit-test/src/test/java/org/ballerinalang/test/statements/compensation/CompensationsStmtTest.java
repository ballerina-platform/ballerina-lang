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
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
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

    @Test(description = "Test compensation block execution")
    public void testCompensationBlockExecution() {
        Boolean compensateStatus = executeOtherFuncAndGetStatus(true);
        Assert.assertTrue(compensateStatus);

        compensateStatus = executeOtherFuncAndGetStatus(false);
        Assert.assertFalse(compensateStatus);
    }

    @SuppressWarnings("unchecked")
    private Boolean executeOtherFuncAndGetStatus(boolean doExecCompensate) {
        BBoolean bCompensate = new BBoolean(doExecCompensate);
        BValue[] returns = BRunUtil.invoke(result, "OtherFunc", new BValue[]{bCompensate});
        BMap<String, BRefType> status = (BMap<String, BRefType>) returns[0];
        return (Boolean) status.get("compensated").value();
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Test compensation block execution of nested scopes")
    public void testCompensationBlockExecutionNestedScopes() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedScopes");
        BMap<String, BRefType> resultMap = (BMap<String, BRefType>) returns[0];
        Boolean compensatedA = (Boolean) resultMap.get("scopeACompensated").value();
        Boolean compensatedB = (Boolean) resultMap.get("scopeBCompensated").value();

        Assert.assertTrue(compensatedA, "ScopeA was not compensated");
        Assert.assertTrue(compensatedB, "Inner ScopeB was not compensated");
    }
}
