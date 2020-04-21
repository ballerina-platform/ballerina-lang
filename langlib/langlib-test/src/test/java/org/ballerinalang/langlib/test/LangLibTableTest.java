/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for the lang.table library.
 *
 * @since 1.0
 */
public class LangLibTableTest {

    private CompileResult compileResult, negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/tablelib_test.bal");
        negativeResult = BCompileUtil.compile("test-src/tablelib_test_negative.bal");
    }

//    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTableLength");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

//    @Test
    public void testIterator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterator");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void getKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getValueFromKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void testMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMap");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void testForeach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns[0].stringValue(), "Chiran Granier ");
    }

//    @Test
    public void testFilter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFilter");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void testReduce() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(((BFloat) returns[0]).floatValue(), 35.5);
    }

//    @Test
    public void testRemoveWithKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeWithKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void removeIfHasKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeIfHasKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void testHasKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    //todo improve when KeyType.type is resolved correctly
//    @Test
    public void testGetKeyList() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetKeyList");
        assertEquals(returns.length, 4);
    }

//    @Test
    public void testRemoveAllFromTable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeAllFromTable");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

//    @Test
    public void testTableToArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "tableToArray");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    //@Test
    public void testNextKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNextKey");
        assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".* KeyNotFound message=cannot find key 'AAA'\n" +
                    "\tat ballerina.lang_table:get(table.bal:64)\n" +
                    "\t   tablelib_test_negative:getValueFromKeyNegative(tablelib_test_negative.bal:89)",
            enabled = false)
    public void getKeyNegative() {
        BRunUtil.invoke(negativeResult, "getValueFromKeyNegative");
        Assert.fail();
    }

    @Test
    public void testCompilerNegativeCases() {
        validateError(negativeResult, 0, "incompatible types: expected " +
                "'object { public function next () returns (record {| Employee value; |}?); }', found " +
                "'object { public function next () returns (record {| Person value; |}?); }'",
                85, 92);
        assertEquals(negativeResult.getErrorCount(), 1);
    }
}

