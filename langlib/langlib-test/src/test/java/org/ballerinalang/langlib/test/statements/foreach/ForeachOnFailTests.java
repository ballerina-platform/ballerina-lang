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

import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

    @Test
    public void testIntArrayWithArityOne() {
        BValue[] returns = BRunUtil.invoke(program, "testIntArrayWithArityOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Positive:1, Negative; hence failed, Positive:5, " +
                "Negative; hence failed, Positive:4, Positive:11, Positive:25, Positive:10, ");
    }

    @Test(description = "Test foreach with check which evaluates to error")
    public void testWhileStmtWithCheck() {
        BValue[] returns = BRunUtil.invoke(program, "testForeachStmtWithCheck");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Value: 1 Value: -3 -> error caught. " +
                "Hence value returning", "mismatched output value");
    }

    @Test(description = "Test nested foreach with fail")
    public void testNestedWhileStmtWithFail() {
        BValue[] returns = BRunUtil.invoke(program, "testNestedWhileStmtWithFail");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "level3-> error caught at level 3, level2-> error caught at level 2, " +
                "level1-> error caught at level 1.";
        Assert.assertEquals(actual, expected);
    }
}
