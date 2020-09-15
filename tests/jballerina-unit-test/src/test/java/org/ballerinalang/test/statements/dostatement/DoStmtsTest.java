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

    private CompileResult programFile, negativeFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/dostatement/do-stmt.bal");
        negativeFile = BCompileUtil.compile("test-src/statements/dostatement/do-stmt-negative.bal");
    }

    @Test
    public void testOnFailStatement() {
        BRunUtil.invoke(programFile, "testOnFailStatement");
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative() {
        Assert.assertEquals(negativeFile.getErrorCount(), 3);
        BAssertUtil.validateError(negativeFile, 0, "unreachable code", 15, 6);
        BAssertUtil.validateError(negativeFile, 1, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 30, 4);
        BAssertUtil.validateError(negativeFile, 2, "unreachable code", 60, 7);
    }
}
