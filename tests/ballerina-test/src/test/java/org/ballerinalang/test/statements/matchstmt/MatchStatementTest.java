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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains a set of test cases to verify the
 * behaviour of the match statement in Ballerina.
 *
 * @since 0.966.0
 */
public class MatchStatementTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/match_stmt_basic.bal");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics1() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "boolean value received: true",
                "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "file open success");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics3() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "file open error: file not found: /tmp/foo.txt");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics4() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "This is working");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics5() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics5", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "file is matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics6() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics6", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "File is matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics7() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics7", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "found string");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics8() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics8", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string | null matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics9() {
        JsonNode jsonNode = new JsonNode(10);
        BJSON jsonValue = new BJSON(jsonNode);
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics9", new BValue[]{jsonValue});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json int| null matched");

        jsonNode = new JsonNode("string value");
        jsonValue = new BJSON(jsonNode);
        returns = BRunUtil.invoke(result, "testMatchStatementBasics9", new BValue[]{jsonValue});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json string | boolean matched");

        jsonNode = new JsonNode(false);
        jsonValue = new BJSON(jsonNode);
        returns = BRunUtil.invoke(result, "testMatchStatementBasics9", new BValue[]{jsonValue});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json string | boolean matched");

        jsonNode = new JsonNode(10.89);
        jsonValue = new BJSON(jsonNode);
        returns = BRunUtil.invoke(result, "testMatchStatementBasics9", new BValue[]{jsonValue});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics10() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics8", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string | null matched");
    }


    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics11() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics11", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "int| null matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics12() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics12", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string | boolean matched");
    }


    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics13() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics13", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "any matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics14() {
        JsonNode jsonNode = new JsonNode(10);
        BJSON jsonValue = new BJSON(jsonNode);
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics14", new BValue[]{jsonValue});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "int| null matched");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics15() {
        JsonNode jsonNode = new JsonNode(false);
        BJSON jsonValue = new BJSON(jsonNode);
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics14", new BValue[]{jsonValue});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json string | boolean matched");
    }

    @Test(description = "Test basics of match statement with function pointers")
    public void testMatchStatementBasics16() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics16", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Piyal");
    }

    @Test(description = "Test basics of match statement with function pointers")
    public void testMatchStatementBasics17() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics17", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "function pointer matched");
    }

    @Test(description = "Test basics of match statement with function pointers")
    public void testMatchStatementBasics18() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics18", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "testing with json");
    }

    @Test(description = "Test basics of match statement with function pointers")
    public void testMatchStatementBasics19() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics19", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12345);
    }

    @Test(description = "Test basics of match statement with function pointers")
    public void testMatchStatementBasics20() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics20", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test(description = "Test basics of match statement with function pointers")
    public void testMatchStatementBasics21() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics21", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 10.333);
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStmtNegative1() {
        CompileResult compile = BCompileUtil.compile("test-src/statements/matchstmt/match_stmt_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 4);

        BAssertUtil.validateError(compile, 0, "this function must return a result", 5, 1);
        BAssertUtil.validateError(compile, 1, "A matching pattern cannot be guaranteed for types '[boolean]'", 9, 31);
        BAssertUtil.validateError(compile, 2, "pattern will not be matched", 18, 9);
        BAssertUtil.validateError(compile, 3, "unreachable pattern: preceding patterns are " +
                "too general or the pattern ordering is not correct", 21, 9);
    }
}

