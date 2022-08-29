/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.query;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test group by clause in query expression.
 *
 * @since 2201.3.0
 */
@Test(groups = {"disableOnOldParser"})
public class GroupByClauseTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/group-by-clause.bal");
        negativeResult = BCompileUtil.compile("test-src/query/group-by-clause-negative.bal");
    }

    @Test(description = "Test group by clause using variable reference grouping key")
    public void testGroupByWithVarRef() {
        BRunUtil.invoke(result, "testGroupByWithVarRef");
    }

    @Test(description = "Test group by clause using variable def grouping key")
    public void testGroupByWithVarDef() {
        BRunUtil.invoke(result, "testGroupByWithVarDef");
    }

    @Test(description = "Test group by clause using both kinds of grouping keys")
    public void testGroupByWithVarDefAndVarRef() {
        BRunUtil.invoke(result, "testGroupByWithVarDefAndVarRef");
    }

    @Test(description = "Test negative scenarios for group by clause")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int index = 0;

        validateError(negativeResult, index++, "undefined symbol 'year'", 38, 18);
        validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'",
                46, 36);
        validateError(negativeResult, index, "operator '+' not defined for 'int' and 'string'",
                54, 36);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
