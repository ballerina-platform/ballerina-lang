/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the lang.array library.
 *
 * @since 1.0
 */
public class LangLibArrayTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/arraylib_test.bal");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

//    @Test
//    public void testIterator() {
//        BValue[] returns = BRunUtil.invoke(compileResult, "testIterator");
//        assertEquals(returns[0].stringValue(), "HelloWorld!FromBallerina");
//    }

    @Test
    public void testEnumerate() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testEnumerate");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.TUPLE_TAG);

        BValueArray elem = (BValueArray) arr.getRefValue(0);
        assertEquals(((BInteger) elem.getRefValue(0)).intValue(), 0);
        assertEquals(elem.getRefValue(1).stringValue(), "Hello");

        elem = (BValueArray) arr.getRefValue(1);
        assertEquals(((BInteger) elem.getRefValue(0)).intValue(), 1);
        assertEquals(elem.getRefValue(1).stringValue(), "World!");

        elem = (BValueArray) arr.getRefValue(2);
        assertEquals(((BInteger) elem.getRefValue(0)).intValue(), 2);
        assertEquals(elem.getRefValue(1).stringValue(), "From");

        elem = (BValueArray) arr.getRefValue(3);
        assertEquals(((BInteger) elem.getRefValue(0)).intValue(), 3);
        assertEquals(elem.getRefValue(1).stringValue(), "Ballerina");
    }

    @Test
    public void testMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.INT_TAG);
        assertEquals(arr.getInt(0), 1);
        assertEquals(arr.getInt(1), 2);
        assertEquals(arr.getInt(2), 3);
        assertEquals(arr.getInt(3), 4);
    }

    @Test
    public void testForeach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns[0].stringValue(), "HelloWorld!fromBallerina");
    }

    @Test
    public void testSlice() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSlice");
        BValueArray result = (BValueArray) returns[0];

        BValueArray arr = (BValueArray) result.getRefValue(0);
        assertEquals(arr.elementType.getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getFloat(0), 23.45);
        assertEquals(arr.getFloat(1), 34.56);
        assertEquals(arr.getFloat(2), 45.67);
        assertEquals(((BInteger) result.getRefValue(1)).intValue(), 3);

        arr = (BValueArray) result.getRefValue(2);
        assertEquals(arr.elementType.getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getFloat(0), 34.56);
        assertEquals(arr.getFloat(1), 45.67);
        assertEquals(arr.getFloat(2), 56.78);
        assertEquals(((BInteger) result.getRefValue(3)).intValue(), 3);

        arr = (BValueArray) result.getRefValue(4);
        assertEquals(arr.elementType.getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 2);
        assertEquals(arr.getFloat(0), 45.67);
        assertEquals(arr.getFloat(1), 56.78);
        assertEquals(((BInteger) result.getRefValue(5)).intValue(), 2);
    }

    @Test
    public void testRemove() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove");
        assertEquals(returns[0].stringValue(), "FooFoo");

        assertEquals(returns[1].getType().getTag(), TypeTags.ARRAY_TAG);
        BValueArray arr = (BValueArray) returns[1];

        assertEquals(arr.elementType.getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getString(0), "Foo");
        assertEquals(arr.getString(1), "Bar");
        assertEquals(arr.getString(2), "BarBar");
    }

    @Test
    public void testSort() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSort");

        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        BValueArray arr = (BValueArray) returns[0];

        assertEquals(arr.elementType.getTag(), TypeTags.INT_TAG);
        assertEquals(arr.size(), 8);
        assertEquals(arr.getInt(0), 1);
        assertEquals(arr.getInt(1), 2);
        assertEquals(arr.getInt(2), 13);
        assertEquals(arr.getInt(3), 13);
        assertEquals(arr.getInt(4), 34);
        assertEquals(arr.getInt(5), 44);
        assertEquals(arr.getInt(6), 87);
        assertEquals(arr.getInt(7), 98);

        assertSame(returns[0], returns[1]);
    }

    @Test
    public void testSort2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSort2");

        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        BValueArray arr = (BValueArray) returns[0];

        assertEquals(arr.elementType.getTag(), TypeTags.INT_TAG);
        assertEquals(arr.size(), 100);

        for (int i = 1; i < arr.size(); i++) {
            assertTrue(arr.getInt(i) > arr.getInt(i - 1));
        }
    }

    @Test
    public void testReduce() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(((BFloat) returns[0]).floatValue(), 13.8);
    }

    @Test
    public void testIterableOpChain() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterableOpChain");
        assertEquals(((BFloat) returns[0]).floatValue(), 3.25);
    }

    @Test
    public void testIterableOpChain2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterableOpChain2");
        assertEquals(((BInteger) returns[0]).intValue(), 420);
    }

    @Test
    public void testIndexOf() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIndexOf");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
        assertNull(returns[1]);
    }

    @Test
    public void testReverse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReverse");

        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        BValueArray arr = (BValueArray) returns[0];

        assertEquals(arr.elementType.getTag(), TypeTags.INT_TAG);
        assertEquals(arr.size(), 5);
        assertEquals(arr.getInt(0), 10);
        assertEquals(arr.getInt(1), 20);
        assertEquals(arr.getInt(2), 30);
        assertEquals(arr.getInt(3), 40);
        assertEquals(arr.getInt(4), 50);

        assertEquals(returns[1].getType().getTag(), TypeTags.ARRAY_TAG);
        arr = (BValueArray) returns[1];

        assertEquals(arr.elementType.getTag(), TypeTags.INT_TAG);
        assertEquals(arr.size(), 5);
        assertEquals(arr.getInt(0), 50);
        assertEquals(arr.getInt(1), 40);
        assertEquals(arr.getInt(2), 30);
        assertEquals(arr.getInt(3), 20);
        assertEquals(arr.getInt(4), 10);
    }

    @Test
    public void testForEach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals(returns[0].stringValue(), "SunMonTues");
    }

    @Test(dataProvider = "setLengthDataProvider")
    public void testSetLength(int setLengthTo, int lenAfterSet, String arrayAfterSet, String arrayLenPlusOneAfterSet) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetLength", new BValue[] {new BInteger(setLengthTo)});
        assertEquals(((BInteger) returns[0]).intValue(), (long) lenAfterSet);
        assertEquals(returns[1].stringValue(), arrayAfterSet);
        assertEquals(returns[2].stringValue(), arrayLenPlusOneAfterSet);
    }

    @DataProvider(name = "setLengthDataProvider")
    public static Object[][] setLengthDataProvider() {
        return new Object[][] {
                { 0, 0, "[]",                       "[0]"},
                { 1, 1, "[1]",                      "[1, 0]"},
                { 6, 6, "[1, 2, 3, 4, 5, 6]",       "[1, 2, 3, 4, 5, 6, 0]"},
                { 7, 7, "[1, 2, 3, 4, 5, 6, 7]",    "[1, 2, 3, 4, 5, 6, 7, 0]"},
                { 8, 8, "[1, 2, 3, 4, 5, 6, 7, 0]", "[1, 2, 3, 4, 5, 6, 7, 0, 0]"},
        };
    }

    @Test
    public void testShift() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testShift");
        assertEquals(returns[0].stringValue(), "[2, 3, 4, 5]");
        assertEquals(returns[1].stringValue(), "1");
    }

    @Test
    public void testUnshift() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnshift");
        assertEquals(returns[0].stringValue(), "[8, 8, 1, 2, 3, 4, 5]");
    }

    @Test
    public void testUnshiftTypeWithoutFillerValues() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnshiftTypeWithoutFillerValues");
        assertEquals(returns.length, 2);
    }

    @Test
    public void testRemoveAll() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveAll");
        assertEquals(returns[0].stringValue(), "[]");
    }

    @Test
    public void testBytePush() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBytePush");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                    "message=cannot change the length of an array of fixed length '7' to '0'.*")
    public void testRemoveAllFixedLengthArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveAllFixedLengthArray");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "message=cannot change the length of a tuple of fixed length '2' to '3'.*")
    public void testTupleResize() {
        BRunUtil.invoke(compileResult, "testTupleResize");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "message=cannot change the length of a tuple of fixed length '2' to '0'.*")
    public void testTupleRemoveAll() {
        BRunUtil.invoke(compileResult, "testTupleRemoveAll");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "message=cannot change the length of a tuple with '2' mandatory member\\(s\\) to '0'.*")
    public void testTupleRemoveAllForTupleWithRestMemberType() {
        BRunUtil.invoke(compileResult, "testTupleRemoveAllForTupleWithRestMemberType");
        Assert.fail();
    }

    @Test
    public void testTupleRemoveAllForTupleWithJustRestMemberType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleRemoveAllForTupleWithJustRestMemberType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTupleSetLengthLegal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleSetLengthLegal");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTupleSetLengthToSameAsOriginal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleSetLengthToSameAsOriginal");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "message=cannot change the length of a tuple with '2' mandatory member\\(s\\) to '1'.*")
    public void testTupleSetLengthIllegal() {
        BRunUtil.invoke(compileResult, "testTupleSetLengthIllegal");
        Assert.fail();
    }
}
