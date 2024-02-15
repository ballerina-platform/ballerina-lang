/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.langlib.test.statements.foreach;

import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with Arrays.
 *
 * @since 0.96.0
 */
public class ForeachOnFailTests {

    private CompileResult program;

    private int[] iValues = {1, -3, 5, -30, 4, 11, 25, 10};
    private double[] fValues = {1.123, -3.35244, 5.23, -30.45, 4.32, 11.56, 25.967, 10.345};
    private String[] sValues = {"foo", "bar", "bax", "baz"};
    private boolean[] bValues = {true, false, false, false, true, false};
    private String[] jValues = {"{\"name\":\"bob\", \"age\":10}", "{\"name\":\"tom\", \"age\":16}"};
    private String[] tValues = {"name=bob,age=10", "name=tom,age=16"};

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-on-fail.bal");
    }

    @AfterClass
    public void tearDown() {
        program = null;
    }

    @Test
    public void testIntArrayWithArityOne() {
        Object returns = BRunUtil.invoke(program, "testIntArrayWithArityOne");
        Assert.assertEquals(returns.toString(), " (Positive:1), (Negative:-3) within grace, "
                + "(Positive:5), (Negative:-30) within grace, (Positive:4), (Positive:11), "
                + "(Negative:-25) Throttle reached");
    }

    @Test(description = "Test foreach with check which evaluates to error")
    public void testWhileStmtWithCheck() {
        Object returns = BRunUtil.invoke(program, "testForeachStmtWithCheck");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Value: 1 Value: -3 -> error caught. " +
                "Hence value returning", "mismatched output value");
    }

    @Test(description = "Test nested foreach with fail")
    public void testNestedWhileStmtWithFail() {
        Object returns = BRunUtil.invoke(program, "testNestedWhileStmtWithFail");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "level3-> error caught at level 3, level2-> error caught at level 2, " +
                "level1-> error caught at level 1.";
        Assert.assertEquals(actual, expected);

        Object result = BRunUtil.invoke(program, "testNestedForeachLoopBreak");
        Assert.assertTrue(result instanceof BString);
        Assert.assertEquals(result.toString(), expected);
    }

    @Test(dataProvider = "onFailClauseWithErrorBPTestDataProvider")
    public void testOnFailWithErrorBP(String funcName) {
        BRunUtil.invoke(program, funcName);
    }

    @DataProvider(name = "onFailClauseWithErrorBPTestDataProvider")
    public Object[] onFailClauseWithErrorBPTestDataProvider() {
        return new Object[]{
                "testSimpleOnFailWithErrorBP",
                "testSimpleOnFailWithErrorBPWithVar",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithError",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithVar",
                "testOnFailWithErrorBPHavingUserDefinedType",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail1",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail2",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail4",
                "testOnFailWithErrorBPHavingAnonDetailRecord",
                "testOnFailWithErrorBPHavingAnonDetailRecordWithVar",
                "testOnFailWithErrorBPHavingAnonDetailRecordWithUnionType",
                "testOnFailWithErrorBPWithErrorArgsHavingBP1",
                "testOnFailWithErrorBPWithErrorArgsHavingBP2",
                "testOnFailWithErrorBPWithErrorArgsHavingBP3",
                "testOnFailWithErrorBPWithErrorArgsHavingBP4",
                "testOnFailWithErrorBPWithErrorArgsHavingBP5",
                "testNestedOnFailWithErrorBP",
                "testNestedOnFailWithErrorBPWithErrorArgsHavingBP",
                "testMultiLevelOnFailWithErrorBP",
                "testMultiLevelOnFailWithoutErrorInOneLevel"
        };
    }
}
