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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic1", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : S, 23, 5.6");
    }

    @Test(description = "Test basics of structured pattern match statement 2")
    public void testMatchStatementBasics2() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic2", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : S, 23, 5.6, 12");
    }

    @Test(description = "Test basics of structured pattern match statement 3")
    public void testMatchStatementBasics3() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic3", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : 12, {\"s\":\"S\",\"i\":23,\"f\":5.6}");
    }

    @Test(description = "Test basics of structured pattern match statement 4")
    public void testMatchStatementBasics4() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic4", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,"
                + "\"f\":5.6}}");
    }

    @Test(description = "Test basics of structured pattern match statement 5")
    public void testMatchStatementBasics5() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasics5", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "two vars : Hello, 150");
        Assert.assertEquals(results.getString(++i), msg + "two vars : 12.4, true");
        Assert.assertEquals(results.getString(++i), msg + "three vars : Hello, 150, true");
        Assert.assertEquals(results.getString(++i), msg + "single var : Hello");
    }

    @Test(description = "Test structured pattern match statement complex 1")
    public void testStructuredMatchPatternComplex1() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternComplex1", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "two vars : Ballerina, 500");
        Assert.assertEquals(results.getString(++i), msg + "three vars : Language, Ballerina, 500");
        Assert.assertEquals(results.getString(++i), msg + "single var : bar2");
    }

    @Test(description = "Test structured pattern runtime matching")
    public void testRuntimeCheck() {
        Object returns = BRunUtil.invoke(result, "testRuntimeCheck", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "five vars : S, 23, 5.6, 50, true");
        Assert.assertEquals(results.getString(++i), msg + "three vars : S, 23, 5.6");
        Assert.assertEquals(results.getString(++i), msg + "four vars : S, 23, 5.6, 12");
        Assert.assertEquals(results.getString(++i), msg + "five vars : S, 23, 5.6, 50, true");
        Assert.assertEquals(results.getString(++i), msg + "three vars : S, 23, 5.6");
        Assert.assertEquals(results.getString(++i), msg + "four vars : S, 23, 5.6, 12");
    }

    @Test(description = "Test structured pattern match with type guard 1")
    public void testStructuredMatchPatternWithTypeGuard1() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard1", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string : Hello added text with 45");
        Assert.assertEquals(results.getString(++i), msg + "record int : Ballerina with 512");
        Assert.assertEquals(results.getString(++i), msg + "record with ClosedBar1 : Language with Ballerina");
        Assert.assertEquals(results.getString(++i), msg + "boolean : 455, true");
        Assert.assertEquals(results.getString(++i), msg + "default type - float : 5.6");
    }

    @Test(description = "Test structured pattern match with type guard 2")
    public void testStructuredMatchPatternWithTypeGuard2() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard2", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string and int : Ballerina");
        Assert.assertEquals(results.getString(++i), msg + "string and ClosedBar1 : Ballerina");
        Assert.assertEquals(results.getString(++i), msg + "Default");
    }

    @Test(description = "Test structured pattern match with type guard 3")
    public void testStructuredMatchPatternWithTypeGuard3() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard3", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "foo : 18.6");
        Assert.assertEquals(results.getString(++i), msg + "bar : 17");
        Assert.assertEquals(results.getString(++i), msg + "default : true");
    }

    @Test(description = "Test structured pattern match with type guard 4")
    public void testStructuredMatchPatternWithTypeGuard4() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard4", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "restparam : {}");
        Assert.assertEquals(results.getString(++i), msg + "restparam : {\"var2\":true}");
        Assert.assertEquals(results.getString(++i), msg + "restparam : {\"var2\":true,\"var3\":true}");
    }

    @Test(description = "Test structured pattern with closed record")
    public void testClosedRecord() {
        Object returns = BRunUtil.invoke(result, "testClosedRecord", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "closed pattern");
        Assert.assertEquals(results.getString(++i), msg + "opened pattern");
    }

    @Test(description = "Test structured pattern match with empty record")
    public void testStructuredMatchPatternWithEmptyRecord() {
        Object returns = BRunUtil.invoke(result, "testStructuredMatchPatternWithEmptyRecord", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);
        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "empty record");
        Assert.assertEquals(results.getString(++i), msg + "a: 1");
        Assert.assertEquals(results.getString(++i), msg + "a: 1, b: 2");
        Assert.assertEquals(results.getString(++i), msg + "a: 1, b: 2, c: 3");
        Assert.assertEquals(results.getString(++i), msg + "a: 1, b: 2, c: 3");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
