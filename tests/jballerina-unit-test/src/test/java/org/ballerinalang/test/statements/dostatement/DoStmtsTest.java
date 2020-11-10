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
package org.ballerinalang.test.statements.dostatement;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in Do Statements.
 */
public class DoStmtsTest {

    private CompileResult programFile, negativeFile1, negativeFile2;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/dostatement/do-stmt.bal");
        negativeFile1 = BCompileUtil.compile("test-src/statements/dostatement/do-stmt-negative-1.bal");
        negativeFile2 = BCompileUtil.compile("test-src/statements/dostatement/do-stmt-negative-2.bal");
    }

    @Test
    public void testOnFailStatement() {
        BRunUtil.invoke(programFile, "testOnFailStatement");
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative1() {
        Assert.assertEquals(negativeFile1.getErrorCount(), 6);
        BAssertUtil.validateError(negativeFile1, 0, "unreachable code", 15, 6);
        BAssertUtil.validateError(negativeFile1, 1, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 30, 4);
        BAssertUtil.validateError(negativeFile1, 2, "unreachable code", 60, 7);
        BAssertUtil.validateError(negativeFile1, 3, "this function must return a result", 66, 1);
        BAssertUtil.validateError(negativeFile1, 4, "unreachable code", 72, 3);
        BAssertUtil.validateError(negativeFile1, 5, "incompatible error definition type: " +
                "'ErrorTypeB' will not be matched to 'ErrorTypeA'", 90, 4);
    }

    @Test(description = "Check on fail scope.")
    public void testNegative2() {
        Assert.assertEquals(negativeFile2.getErrorCount(), 3);
        BAssertUtil.validateError(negativeFile2, 0, "type 'str' not allowed here; " +
                "expected an 'error' or a subtype of 'error'.", 6, 11);
        BAssertUtil.validateError(negativeFile2, 1, "incompatible types: expected 'string', " +
                "found 'error'", 8, 12);
        BAssertUtil.validateError(negativeFile2, 2, "undefined symbol 'd'", 26, 12);
    }
}
