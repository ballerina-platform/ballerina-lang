/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.dataflow.analysis;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Test cases related to unused variable analysis.
 *
 * @since 2.0.0
 */
@Test
public class UnusedVariableTest {

    @Test
    public void testUnusedVariableAnalysis() {
        CompileResult result = BCompileUtil.compileWithoutInitInvocation(
                "test-src/dataflow/analysis/unused_variable_analysis_test.bal");
        int i = 0;
        validateWarning(result, i++, getUnusedVariableWarning("i"), 18, 5);
        validateWarning(result, i++, getUnusedVariableWarning("j"), 20, 5);
        validateWarning(result, i++, getUnusedVariableWarning("k"), 22, 5);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 43, 5);
        validateWarning(result, i++, getUnusedVariableWarning("a"), 51, 5);
        validateWarning(result, i++, getUnusedVariableWarning("a"), 55, 13);
        validateWarning(result, i++, getUnusedVariableWarning("b"), 55, 16);
        validateWarning(result, i++, getUnusedVariableWarning("c"), 55, 19);
        validateWarning(result, i++, getUnusedVariableWarning("d"), 57, 57);
        validateWarning(result, i++, getUnusedVariableWarning("e"), 57, 61);
        validateWarning(result, i++, getUnusedVariableWarning("f"), 57, 64);
        validateWarning(result, i++, getUnusedVariableWarning("g"), 57, 71);
        validateWarning(result, i++, getUnusedVariableWarning("h"), 61, 64);
        validateWarning(result, i++, getUnusedVariableWarning("m"), 61, 73);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 61, 80);
        validateWarning(result, i++, getUnusedVariableWarning("k"), 61, 83);
        validateWarning(result, i++, getUnusedVariableWarning("l"), 61, 90);
        validateWarning(result, i++, getUnusedVariableWarning("c"), 71, 19);
        validateWarning(result, i++, getUnusedVariableWarning("f"), 74, 64);
        validateWarning(result, i++, getUnusedVariableWarning("h"), 78, 64);
        validateWarning(result, i++, getUnusedVariableWarning("m"), 78, 73);
        validateWarning(result, i++, getUnusedVariableWarning("k"), 78, 83);
        validateWarning(result, i++, getUnusedVariableWarning("l"), 87, 5);
        validateWarning(result, i++, getUnusedVariableWarning("m"), 90, 9);
        validateWarning(result, i++, getUnusedVariableWarning("p"), 96, 9);
        validateWarning(result, i++, getUnusedVariableWarning("a"), 112, 5);
        validateWarning(result, i++, getUnusedVariableWarning("e"), 122, 7);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 163, 5);
        validateWarning(result, i++, getUnusedVariableWarning("a"), 171, 5);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 183, 13);
        validateWarning(result, i++, getUnusedVariableWarning("e"), 192, 13);
        validateWarning(result, i++, getUnusedVariableWarning("m"), 196, 9);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 202, 18);
        validateWarning(result, i++, getUnusedVariableWarning("j1"), 202, 25);
        validateWarning(result, i++, getUnusedVariableWarning("j2"), 202, 29);
        validateWarning(result, i++, getUnusedVariableWarning("jr"), 202, 33);
        validateWarning(result, i++, getUnusedVariableWarning("km"), 202, 50);
        validateWarning(result, i++, getUnusedVariableWarning("kd1"), 202, 58);
        validateWarning(result, i++, getUnusedVariableWarning("kd2"), 202, 63);
        validateWarning(result, i++, getUnusedVariableWarning("jr"), 205, 33);
        validateWarning(result, i++, getUnusedVariableWarning("km"), 205, 50);
        validateWarning(result, i++, getUnusedVariableWarning("kd2"), 205, 63);
        validateWarning(result, i++, getUnusedVariableWarning("x"), 206, 9);
        validateWarning(result, i++, getUnusedVariableWarning("x"), 226, 17);
        validateWarning(result, i++, getUnusedVariableWarning("y"), 226, 28);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 234, 20);
        validateWarning(result, i++, getUnusedVariableWarning("j"), 241, 21);
        validateWarning(result, i++, getUnusedVariableWarning("customer"), 251, 26);
        validateWarning(result, i++, getUnusedVariableWarning("person"), 252, 30);
        validateWarning(result, i++, getUnusedVariableWarning("customer"), 258, 26);
        validateWarning(result, i++, getUnusedVariableWarning("a"), 277, 9);
        validateWarning(result, i++, getUnusedVariableWarning("b"), 277, 9);
        validateWarning(result, i++, getUnusedVariableWarning("f"), 280, 9);
        validateWarning(result, i++, getUnusedVariableWarning("g"), 283, 9);
        validateWarning(result, i++, getUnusedVariableWarning("b"), 290, 9);
        validateWarning(result, i++, getUnusedVariableWarning("s"), 291, 13);
        validateWarning(result, i++, getUnusedVariableWarning("d"), 293, 9);
        validateWarning(result, i++, getUnusedVariableWarning("m"), 295, 9);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 302, 9);
        validateWarning(result, i++, getUnusedVariableWarning("j"), 312, 9);
        validateWarning(result, i++, getUnusedVariableWarning("j"), 321, 9);
        validateWarning(result, i++, getUnusedVariableWarning("x"), 332, 9);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 343, 5);
        validateWarning(result, i++, getUnusedVariableWarning("k"), 345, 5);
        validateWarning(result, i++, getUnusedVariableWarning("s"), 351, 5);
        validateWarning(result, i++, getUnusedVariableWarning("m"), 359, 5);
        validateWarning(result, i++, getUnusedVariableWarning("n"), 365, 5);
        validateWarning(result, i++, getUnusedVariableWarning("h"), 385, 5);
        validateWarning(result, i++, getUnusedVariableWarning("i"), 388, 5);
        validateWarning(result, i++, getUnusedVariableWarning("j"), 397, 9);
        validateWarning(result, i++, getUnusedVariableWarning("k"), 405, 9);
        validateWarning(result, i++, getUnusedVariableWarning("k1"), 418, 5);
        validateWarning(result, i++, getUnusedVariableWarning("k2"), 421, 5);
        Assert.assertEquals(result.getWarnCount(), i);
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    private String getUnusedVariableWarning(String varName) {
        return "unused variable '" + varName + "'";
    }
}
