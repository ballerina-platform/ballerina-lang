/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the structured patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStructuredRecordPatternsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/structured_record_match_patterns.bal");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testMatchStatementBasics1() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : S, 23, 5.6");
    }

    @Test(description = "Test basics of structured pattern match statement 2")
    public void testMatchStatementBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : S, 23, 5.6, 12");
    }

    @Test(description = "Test basics of structured pattern match statement 3")
    public void testMatchStatementBasics3() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : 12, {s:\"S\", i:23, f:5.6}");
    }

    @Test(description = "Test basics of structured pattern match statement 4")
    public void testMatchStatementBasics4() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : {f:{s:\"S\", i:23, f:5.6}, b:12}");
    }

    @Test(description = "Test basics of structured pattern match statement 5")
    public void testMatchStatementBasics5() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasics5", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "two vars : Hello, 150");
        Assert.assertEquals(results.get(++i), msg + "two vars : 12.4, true");
        Assert.assertEquals(results.get(++i), msg + "three vars : Hello, 150, true");
        Assert.assertEquals(results.get(++i), msg + "single var : Hello");
    }

    @Test(description = "Test structured pattern match statement complex 1")
    public void testStructuredMatchPatternComplex1() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternComplex1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "two vars : Ballerina, 500");
        Assert.assertEquals(results.get(++i), msg + "three vars : Language, Ballerina, 500");
        Assert.assertEquals(results.get(++i), msg + "single var : bar2");
    }

    @Test(description = "Test structured pattern runtime matching")
    public void testRuntimeCheck() {
        BValue[] returns = BRunUtil.invoke(result, "testRuntimeCheck", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "five vars : S, 23, 5.6, 50, true");
        Assert.assertEquals(results.get(++i), msg + "three vars : S, 23, 5.6");
        Assert.assertEquals(results.get(++i), msg + "four vars : S, 23, 5.6, 12");
        Assert.assertEquals(results.get(++i), msg + "five vars : S, 23, 5.6, 50, true");
        Assert.assertEquals(results.get(++i), msg + "three vars : S, 23, 5.6");
        Assert.assertEquals(results.get(++i), msg + "four vars : S, 23, 5.6, 12");
    }

    @Test(description = "Test structured pattern match with type guard 1")
    public void testStructuredMatchPatternWithTypeGuard1() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "string : Hello added text with 45");
        Assert.assertEquals(results.get(++i), msg + "record int : Ballerina with 512");
        Assert.assertEquals(results.get(++i), msg + "record with ClosedBar1 : Language with Ballerina");
        Assert.assertEquals(results.get(++i), msg + "boolean : 455, true");
        Assert.assertEquals(results.get(++i), msg + "default type - float : 5.6");
    }

    @Test(description = "Test structured pattern match with type guard 2")
    public void testStructuredMatchPatternWithTypeGuard2() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "string and int : Ballerina");
        Assert.assertEquals(results.get(++i), msg + "string and ClosedBar1 : Ballerina");
        Assert.assertEquals(results.get(++i), msg + "Default");
    }

    @Test(description = "Test structured pattern match with type guard 3")
    public void testStructuredMatchPatternWithTypeGuard3() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "foo : 18.6");
        Assert.assertEquals(results.get(++i), msg + "bar : 17");
        Assert.assertEquals(results.get(++i), msg + "default : true");
    }

    @Test(description = "Test structured pattern match with type guard 3")
    public void testStructuredMatchPatternWithTypeGuard4() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "restparam : {}");
        Assert.assertEquals(results.get(++i), msg + "restparam : {\"var2\":true}");
        Assert.assertEquals(results.get(++i), msg + "restparam : {\"var2\":true, \"var3\":true}");
    }

    @Test(description = "Test structured pattern with closed record")
    public void testClosedRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedRecord", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.get(++i), msg + "closed pattern");
        Assert.assertEquals(results.get(++i), msg + "opened pattern");
    }
}
