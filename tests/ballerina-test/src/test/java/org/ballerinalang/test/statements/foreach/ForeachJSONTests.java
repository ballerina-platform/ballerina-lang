/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.foreach;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with JSON type.
 *
 * @since 0.96.0
 */
public class ForeachJSONTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-json.bal");
    }

    @Test
    public void testJSONObject() {
        String result = "bob 10 true [{\"subject\":\"maths\",\"marks\":75},{\"subject\":\"English\",\"marks\":85}] ";
        BValue[] returns = BRunUtil.invoke(program, "testJSONObject");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testJSONArray() {
        String result = "{\"subject\":\"maths\",\"marks\":75} {\"subject\":\"English\",\"marks\":85} ";
        BValue[] returns = BRunUtil.invoke(program, "testJSONArray");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testArrayOfJSON() {
        String result = "0:{\"subject\":\"maths\",\"marks\":75} 1:{\"subject\":\"English\",\"marks\":85} ";
        BValue[] returns = BRunUtil.invoke(program, "testArrayOfJSON");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testJSONString() {
        String result = "";
        BValue[] returns = BRunUtil.invoke(program, "testJSONString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testJSONNumber() {
        String result = "";
        BValue[] returns = BRunUtil.invoke(program, "testJSONNumber");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testJSONBoolean() {
        String result = "";
        BValue[] returns = BRunUtil.invoke(program, "testJSONBoolean");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testJSONNull() {
        String result = "";
        BValue[] returns = BRunUtil.invoke(program, "testJSONNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testJSONToStructCast() {
        String result = "a-h1 b-h2 ";
        BValue[] returns = BRunUtil.invoke(program, "testJSONToStructCast");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }

    @Test
    public void testAddWhileIteration() {
        String result = "bob 10 true [{\"subject\":\"maths\",\"marks\":75},{\"subject\":\"English\",\"marks\":85}] ";
        BValue[] returns = BRunUtil.invoke(program, "testAddWhileIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result + "smith ");
    }

    @Test
    public void testDeleteWhileIteration() {
        String result = "bob 10 true [{\"subject\":\"maths\",\"marks\":75},{\"subject\":\"English\",\"marks\":85}] " +
                "bob 10 true ";
        BValue[] returns = BRunUtil.invoke(program, "testDeleteWhileIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), result);
    }
}
