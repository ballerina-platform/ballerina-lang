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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Complex test cases written for foreach statement.
 *
 * @since 0.96.0
 */
public class ForeachComplexTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-complex.bal");
    }

    @Test
    public void testNestedForeach() {
        String[] days = {"mon", "tue", "wed", "thu", "fri"};
        String[] people = {"tom", "bob", "sam"};
        StringBuilder sb = new StringBuilder();
        int i = -1;
        for (String day : days) {
            sb.append(++i).append(":").append(day).append(" ");
            for (String person : people) {
                sb.append(i).append(":").append(person).append(" ");
            }
            sb.append("\n");
        }
        BValue[] returns = BRunUtil.invoke(program, "testNestedForeach");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), sb.toString());
    }

    @Test
    public void testIntRangeSimple() {
        BValue[] args = new BValue[]{new BInteger(-5), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(program, "testIntRangeSimple", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), getIntRangOutput(false, -5, 5));

        args = new BValue[]{new BInteger(5), new BInteger(-5)};
        returns = BRunUtil.invoke(program, "testIntRangeSimple", args);
        Assert.assertEquals(returns[0].stringValue(), getIntRangOutput(false, 5, -5));
    }

    @Test
    public void testIntRangeSimpleArity2() {
        BValue[] args = new BValue[]{new BInteger(-5), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(program, "testIntRangeSimpleArity2", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), getIntRangOutput(true, -5, 5));

        args = new BValue[]{new BInteger(5), new BInteger(-5)};
        returns = BRunUtil.invoke(program, "testIntRangeSimple", args);
        Assert.assertEquals(returns[0].stringValue(), getIntRangOutput(true, 5, -5));
    }

    @Test
    public void testIntRangeEmptySet() {
        BValue[] returns = BRunUtil.invoke(program, "testIntRangeEmptySet");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), getIntRangOutput(false, 5, 0));
    }

    @Test
    public void testIntRangeComplex() {
        BValue[] returns = BRunUtil.invoke(program, "testIntRangeComplex");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), getIntRangOutput(false, 0, 10));
    }

    private String getIntRangOutput(boolean includeIndex, int start, int end) {
        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        for (int i = start; i <= end; i++, cursor++) {
            if (includeIndex) {
                sb.append(cursor).append(":");
            }
            sb.append(i).append(" ");
        }
        return sb.toString();
    }
}
