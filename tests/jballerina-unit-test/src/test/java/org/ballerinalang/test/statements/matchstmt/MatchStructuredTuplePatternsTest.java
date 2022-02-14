/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.matchstmt;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the structured patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStructuredTuplePatternsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/structured_tuple_match_patterns.bal");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testMatchStatementBasics1() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternsBasic1", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : S, 23, 5.6");
    }

    @Test(description = "Test basics of structured pattern match statement 2")
    public void testMatchStatementBasics2() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternsBasic2", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : S, 23, 5.6");
    }

    @Test(description = "Test basics of structured pattern match statement 3")
    public void testMatchStatementBasics3() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternsBasic3", new Object[]{});

        Assert.assertTrue(returns instanceof BString);

        BString bString = (BString) returns;

        Assert.assertEquals(bString.toString(), "Matched Values : S, [23,5.6]");
    }

    @Test(description = "Test basics of structured pattern match statement 5")
    public void testMatchStatementBasics5() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternsBasics5", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "single var : 66.6");
        Assert.assertEquals(results.getString(++i), msg + "two vars : Hello, 12");
        Assert.assertEquals(results.getString(++i), msg + "two vars : 4.5, true");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 6.7, Test, false");
    }

    @Test(description = "Test structured pattern match statement complex 1")
    public void testStructuredMatchPatternComplex1() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternComplex1", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "single var : 66.6");
        Assert.assertEquals(results.getString(++i), msg + "two vars : Hello, 34");
        Assert.assertEquals(results.getString(++i), msg + "four vars : 66.6, Test, 456, true");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 5.6, Ballerina, false");
        Assert.assertEquals(results.getString(++i), msg + "single var : [\"Bal\",543,object NoFillerObject]");
    }

    @Test(description = "Test structured pattern match statement complex 2")
    public void testStructuredMatchPatternComplex2() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternComplex2", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), "Default");
        Assert.assertEquals(results.getString(++i), msg + "two vars : Hello, 34");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 66.6, Test, [true,456]");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 5.6, Ballerina, false");
    }

    @Test(description = "Test structured pattern match statement complex 3")
    public void testStructuredMatchPatternComplex3() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternComplex3", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "single var : 66.6");
        Assert.assertEquals(results.getString(++i), msg + "two vars : Hello, 34");
        Assert.assertEquals(results.getString(++i), msg + "four vars : 66.6, Test, 456, true");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 5.6, Ballerina, false");
        Assert.assertEquals(results.getString(++i), msg + "single var : [\"Bal\",543,object NoFillerObject]");
    }

    @Test(description = "Test structured pattern match statement complex 4")
    public void testStructuredMatchPatternComplex4() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternComplex4", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), "Default");
        Assert.assertEquals(results.getString(++i), msg + "two vars : Hello, 34");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 66.6, Test, [true,456]");
        Assert.assertEquals(results.getString(++i), msg + "three vars : 5.6, Ballerina, false");
    }

    @Test(description = "Test structured pattern match with type guard 1")
    public void testStructuredMatchPatternWithTypeGuard1() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard1", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string : Hello added text with 45");
        Assert.assertEquals(results.getString(++i), msg + "float : 9.0 with true");
        Assert.assertEquals(results.getString(++i), msg + "int : false with 3460");
        Assert.assertEquals(results.getString(++i), msg + "boolean : 455, true");
        Assert.assertEquals(results.getString(++i), msg + "default type - float : 5.6");
    }

    @Test(description = "Test structured pattern match with type guard 2")
    public void testStructuredMatchPatternWithTypeGuard2() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard2", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string : Hello added text with 45");
        Assert.assertEquals(results.getString(++i), msg + "float : 10.2 with true and 67");
        Assert.assertEquals(results.getString(++i), msg + "int : true with 3523 and 7.8");
        Assert.assertEquals(results.getString(++i), msg + "boolean : 678, false");
        Assert.assertEquals(results.getString(++i), "Default");
    }

    @Test(description = "Test structured pattern match with type guard 3")
    public void testStructuredMatchPatternWithTypeGuard3() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard3", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string : Hello added text with 50 with 11.0");
        Assert.assertEquals(results.getString(++i), msg + "float : 10.2 and boolean with true and 67");
        Assert.assertEquals(results.getString(++i), msg + "boolean : true and int with 3523 and float with 7.8");
        Assert.assertEquals(results.getString(++i), msg + "boolean : 678, false");
        Assert.assertEquals(results.getString(++i), "Default");
    }

    @Test(description = "Test structured pattern match with type guard 4")
    public void testStructuredMatchPatternWithTypeGuard4() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard4", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg +
                "FooRec and BarRec : {\"s\":\"S\",\"i\":23,\"f\":5.6} , " +
                "{\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}}");
        Assert.assertEquals(results.getString(++i), msg + "FooRec and float : " +
                "{\"s\":\"S\",\"i\":23,\"f\":5.6} , 4.5");
        Assert.assertEquals(results.getString(++i), msg +
                "BarRec and FooRec : {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}} ," +
                " {\"s\":\"S\",\"i\":23,\"f\":5.6}");
        Assert.assertEquals(results.getString(++i), msg + "BarRec and int : " +
                "{\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}} , 543");
        Assert.assertEquals(results.getString(++i), msg +
                "float and FooRec : 5.2 , {\"s\":\"S\",\"i\":23,\"f\":5.6}");
        Assert.assertEquals(results.getString(++i), msg + "int and " +
                "BarRec : 15 , {\"b\":12,\"f\":{\"s\":\"S\",\"i\":23,\"f\":5.6}}");
        Assert.assertEquals(results.getString(++i), "Default");
        Assert.assertEquals(results.getString(++i), "Default");
    }

    @Test(description = "Test structured pattern match with type guard 5")
    public void testStructuredMatchPatternWithTypeGuard5() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternWithTypeGuard5", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);

        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string : Hello added text with 50 with 11.0");
        Assert.assertEquals(results.getString(++i), msg + "float : 10.2 and boolean with true and 67");
        Assert.assertEquals(results.getString(++i), msg + "boolean : true and int with 3523 and float with 7.8");
        Assert.assertEquals(results.getString(++i), msg + "boolean : 678, false");
        Assert.assertEquals(results.getString(++i), msg + "int : 876");
        Assert.assertEquals(results.getString(++i), "Default");
    }

    @Test(description = "Test structured pattern match with empty tuple")
    public void testStructuredMatchPatternWithEmptyTuple() {
        Object returns = JvmRunUtil.invoke(result, "testStructuredMatchPatternWithEmptyTuple", new Object[]{});

        Assert.assertTrue(returns instanceof BArray);
        BArray results = (BArray) returns;

        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "empty array");
        Assert.assertEquals(results.getString(++i), msg + "i: 1");
        Assert.assertEquals(results.getString(++i), msg + "i: 1, j: 2");
        Assert.assertEquals(results.getString(++i), msg + "i: 1, j: 2, k: 3");
        Assert.assertEquals(results.getString(++i), msg + "default");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
