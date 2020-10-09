/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for typed binding patterns in foreach.
 *
 * @since 0.985.0
 */
public class ForeachJSONTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-json-typed-binding-patterns.bal");
    }

    @Test
    public void testJsonWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testJsonWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:bob 1:10 2:true 3:[{\"subject\":\"maths\", \"marks\":75}, " +
                                                      "{\"subject\":\"English\", \"marks\":85}] ");
    }

    @Test
    public void testJsonWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testJsonWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:bob 1:10 2:true 3:[{\"subject\":\"maths\", \"marks\":75}, " +
                                                      "{\"subject\":\"English\", \"marks\":85}] ");
    }

    @Test
    public void testDirectAccessJsonArrayWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testDirectAccessJsonArrayWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"subject\":\"maths\", \"marks\":75} " +
                "1:{\"subject\":\"English\", \"marks\":85} ");
    }

    @Test
    public void testDirectAccessJsonArrayWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testDirectAccessJsonArrayWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"subject\":\"maths\", \"marks\":75} " +
                "1:{\"subject\":\"English\", \"marks\":85} ");
    }

    @Test
    public void testJsonArrayWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testJsonArrayWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"subject\":\"maths\", \"marks\":75} " +
                "1:{\"subject\":\"English\", \"marks\":85} ");
    }

    @Test
    public void testJsonArrayWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testJsonArrayWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"subject\":\"maths\", \"marks\":75} " +
                "1:{\"subject\":\"English\", \"marks\":85} ");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'error' cannot be cast to 'json'.*")
    public void testDirectAccessInvalidElementWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testDirectAccessInvalidElementWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{ballerina}ConversionError {\"message\":\"cannot convert " 
                + "'null' value to type 'map<json>'\"}");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'error' cannot be cast to 'json'.*")
    public void testDirectAccessInvalidElementWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testDirectAccessInvalidElementWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{ballerina}ConversionError {\"message\":\"cannot convert 'null'" 
                + " " +
                "value to type 'map<json>'\"}");
    }

    @Test
    public void testIteratingCompleteJsonWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testIteratingCompleteJsonWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:bob 1:10 2:true 3:{\"subject\":\"maths\", \"marks\":75} " +
                                                      "3:{\"subject\":\"English\", \"marks\":85} ");
    }

    @Test
    public void testIteratingCompleteJsonWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testIteratingCompleteJsonWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:bob 1:10 2:true 3:{\"subject\":\"maths\", \"marks\":75} " +
                                                      "3:{\"subject\":\"English\", \"marks\":85} ");
    }

    @Test
    public void testEmptyJsonIteration() {
        BValue[] returns = BRunUtil.invoke(program, "testEmptyJsonIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
}
