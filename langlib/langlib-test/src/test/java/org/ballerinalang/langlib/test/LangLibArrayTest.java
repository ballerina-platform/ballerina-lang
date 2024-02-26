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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test
    public void testLength() {
        Object returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(returns, 4L);
    }

    @Test
    public void testIterator() {
        Object returns = BRunUtil.invoke(compileResult, "testIterator");
        assertEquals(returns.toString(), "HelloWorld!FromBallerina");
    }

    @Test
    public void testEnumerate() {
        Object returns = BRunUtil.invoke(compileResult, "testEnumerate");
        assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns;
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.TUPLE_TAG);

        BArray elem = (BArray) arr.getRefValue(0);
        assertEquals(elem.getRefValue(0), 0L);
        assertEquals(elem.getRefValue(1).toString(), "Hello");

        elem = (BArray) arr.getRefValue(1);
        assertEquals(elem.getRefValue(0), 1L);
        assertEquals(elem.getRefValue(1).toString(), "World!");

        elem = (BArray) arr.getRefValue(2);
        assertEquals(elem.getRefValue(0), 2L);
        assertEquals(elem.getRefValue(1).toString(), "From");

        elem = (BArray) arr.getRefValue(3);
        assertEquals(elem.getRefValue(0), 3L);
        assertEquals(elem.getRefValue(1).toString(), "Ballerina");
    }

    @Test
    public void testMap() {
        Object returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns;
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.INT_TAG);
        assertEquals(arr.getInt(0), 1);
        assertEquals(arr.getInt(1), 2);
        assertEquals(arr.getInt(2), 3);
        assertEquals(arr.getInt(3), 4);
    }

    @Test
    public void testForeach() {
        Object returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns.toString(), "HelloWorld!fromBallerina");
    }

    @Test
    public void testSlice() {
        Object returns = BRunUtil.invoke(compileResult, "testSlice");
        BArray result = (BArray) returns;

        BArray arr = (BArray) result.getRefValue(0);
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getFloat(0), 23.45);
        assertEquals(arr.getFloat(1), 34.56);
        assertEquals(arr.getFloat(2), 45.67);
        assertEquals(result.getRefValue(1), 3L);

        arr = (BArray) result.getRefValue(2);
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getFloat(0), 34.56);
        assertEquals(arr.getFloat(1), 45.67);
        assertEquals(arr.getFloat(2), 56.78);
        assertEquals(result.getRefValue(3), 3L);

        arr = (BArray) result.getRefValue(4);
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 2);
        assertEquals(arr.getFloat(0), 45.67);
        assertEquals(arr.getFloat(1), 56.78);
        assertEquals(result.getRefValue(5), 2L);
    }

    @Test(dataProvider = "testSliceOfReadonlyArrays")
    public void testSliceOfReadonlyArray(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "testSliceOfReadonlyArrays")
    public Object[] testSliceOfReadonlyArrays() {
        return new Object[][]{
                {"testModificationAfterSliceOfReadonlyIntArray"},
                {"testModificationAfterSliceOfReadonlyStringArray"},
                {"testModificationAfterSliceOfReadonlyBooleanArray"},
                {"testModificationAfterSliceOfReadonlyByteArray"},
                {"testModificationAfterSliceOfReadonlyFloatArray"},
                {"testSliceOfIntersectionOfReadonlyRecordArray"}
        };
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InherentTypeViolation " +
                    "\\{\"message\":\"incompatible types: expected '\\(Employee & readonly\\)', found 'Employee'\"}.*")
    public void testModificationAfterSliceOfReadonlyRecordArray() {
        BRunUtil.invoke(compileResult, "testModificationAfterSliceOfReadonlyRecordArray");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InherentTypeViolation " +
                    "\\{\"message\":\"incompatible types: expected '\\(map<string> & readonly\\)', " +
                    "found 'map<string>'\"}.*")
    public void testPushAfterSliceOfReadonlyMapArray() {
        BRunUtil.invoke(compileResult, "testPushAfterSliceOfReadonlyMapArray");
        Assert.fail();
    }

    @Test
    public void testRemove() {
        Object returns = BRunUtil.invoke(compileResult, "testRemove");
        BArray result = (BArray) returns;

        assertEquals(result.get(0).toString(), "FooFoo");

        assertEquals(getType(result.get(1)).getTag(), TypeTags.ARRAY_TAG);
        BArray arr = (BArray) result.get(1);

        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getString(0), "Foo");
        assertEquals(arr.getString(1), "Bar");
        assertEquals(arr.getString(2), "BarBar");
    }

    @Test
    public void testReduce() {
        Object returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(returns, 13.8);
    }

    @Test
    public void testIterableOpChain() {
        Object returns = BRunUtil.invoke(compileResult, "testIterableOpChain");
        assertEquals(returns, 3.25);
    }

    @Test
    public void testIterableOpChain2() {
        Object returns = BRunUtil.invoke(compileResult, "testIterableOpChain2");
        assertEquals(returns, 420L);
    }

    @Test
    public void testIndexOf() {
        Object returns = BRunUtil.invoke(compileResult, "testIndexOf");
        BArray result = (BArray) returns;
        assertEquals(result.get(0), 4L);
        assertNull(result.get(1));
    }

    @Test
    public void testForEach() {
        Object returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals(returns.toString(), "SunMonTues");
    }

    @Test(dataProvider = "setLengthDataProvider")
    public void testSetLength(int setLengthTo, long lenAfterSet, String arrayAfterSet, String arrayLenPlusOneAfterSet) {
        Object returns = BRunUtil.invoke(compileResult, "testSetLength", new Object[] {(setLengthTo)});
        BArray result = (BArray) returns;
        assertEquals(result.get(0), lenAfterSet);
        assertEquals(result.get(1).toString(), arrayAfterSet);
        assertEquals(result.get(2).toString(), arrayLenPlusOneAfterSet);
    }

    @DataProvider(name = "setLengthDataProvider")
    public static Object[][] setLengthDataProvider() {
        return new Object[][]{
                {0, 0, "[]", "[0]"},
                {1, 1, "[1]", "[1,0]"},
                {6, 6, "[1,2,3,4,5,6]", "[1,2,3,4,5,6,0]"},
                {7, 7, "[1,2,3,4,5,6,7]", "[1,2,3,4,5,6,7,0]"},
                {8, 8, "[1,2,3,4,5,6,7,0]", "[1,2,3,4,5,6,7,0,0]"},
        };
    }

    @Test
    public void testShift() {
        Object returns = BRunUtil.invoke(compileResult, "testShift");
        BArray result = (BArray) returns;
        assertEquals(result.get(0).toString(), "[2,3,4,5]");
        assertEquals(result.get(1).toString(), "1");
    }

    @Test
    public void testUnshift() {
        Object returns = BRunUtil.invoke(compileResult, "testUnshift");
        assertEquals(returns.toString(), "[8,8,1,2,3,4,5]");
    }

    @Test
    public void testUnshiftTypeWithoutFillerValues() {
        Object returns = BRunUtil.invoke(compileResult, "testUnshiftTypeWithoutFillerValues");
        BArray result = (BArray) returns;
        assertEquals(result.size(), 2);
    }

    @Test
    public void testRemoveAll() {
        Object returns = BRunUtil.invoke(compileResult, "testRemoveAll");
        assertEquals(returns.toString(), "[]");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InherentTypeViolation " +
                    "\\{\"message\":\"cannot change the length of an array of fixed length '7' to '0'\"}.*")
    public void testRemoveAllFixedLengthArray() {
        BRunUtil.invoke(compileResult, "testRemoveAllFixedLengthArray");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array}InherentTypeViolation \\{\"message\":\"cannot change the length"
                            + " of a tuple of fixed length '2' to '3'\"}.*")
    public void testTupleResize() {
        BRunUtil.invoke(compileResult, "testTupleResize");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array}InherentTypeViolation \\{\"message\":\"cannot change the " +
                            "length of a tuple of fixed length '2' to '0'\"}.*")
    public void testTupleRemoveAll() {
        BRunUtil.invoke(compileResult, "testTupleRemoveAll");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array}InherentTypeViolation \\{\"message\":\"cannot change the length"
                            + " of a tuple with '2' mandatory member\\(s\\) to '0'\"}.*")
    public void testTupleRemoveAllForTupleWithRestMemberType() {
        BRunUtil.invoke(compileResult, "testTupleRemoveAllForTupleWithRestMemberType");
        Assert.fail();
    }

    @Test
    public void testTupleRemoveAllForTupleWithJustRestMemberType() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleRemoveAllForTupleWithJustRestMemberType");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTupleSetLengthLegal() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleSetLengthLegal");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTupleSetLengthToSameAsOriginal() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleSetLengthToSameAsOriginal");
        Assert.assertTrue((Boolean) returns);
    }

    @Test(expectedExceptions = BLangTestException.class,
          expectedExceptionsMessageRegExp =
                  "error: \\{ballerina/lang.array}InherentTypeViolation \\{\"message\":\"cannot change the length " +
                          "of a tuple with '2' mandatory member\\(s\\) to '1'\"}.*")
    public void testTupleSetLengthIllegal() {
        BRunUtil.invoke(compileResult, "testTupleSetLengthIllegal");
        Assert.fail();
    }

    @Test
    public void testAsyncFpArgsWithArrays() {
        Object results = BRunUtil.invoke(compileResult, "testAsyncFpArgsWithArrays");
        BArray resultArray = (BArray) results;
        assertTrue(resultArray.get(0) instanceof Long);
        assertTrue(resultArray.get(1) instanceof  BArray);
        assertEquals(resultArray.get(0), 19L);
        BArray bValueArray = (BArray) resultArray.get(1);
        assertEquals(bValueArray.getInt(0), 4);
        assertEquals(bValueArray.getInt(1), 6);
        assertEquals(bValueArray.getInt(2), 3);

    }
    @Test
    public void testArrayLibNegativeCases() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/arraylib_test_negative.bal");
        int errorIndex = 0;
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'push' on fixed length list(s) of type " +
                                          "'int[1]'",
                                  20, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'push' on fixed length list(s) of type " +
                                          "'[int,int]'",
                                  25, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'pop' on fixed length list(s) of type " +
                                          "'int[1]'",
                                  30, 35);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'pop' on fixed length list(s) of type " +
                                          "'[int,int]'",
                                  35, 35);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'shift' on fixed length list(s) of type " +
                                          "'int[1]'",
                                  46, 30);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'unshift' on fixed length list(s) of " +
                                          "type 'int[1]'",
                                  51, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'shift' on fixed length list(s) of type " +
                                          "'[int,int]'",
                                  56, 35);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'unshift' on fixed length list(s) of type '[int,int]'",
                                  61, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'push' on fixed length list(s) of type " +
                                          "'int[2]'",
                                  67, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'pop' on fixed length list(s) of type " +
                                          "'int[2]'",
                                  68, 30);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'shift' on fixed length list(s) of type " +
                                          "'int[2]'",
                                  69, 26);
        BAssertUtil.validateError(negativeResult, errorIndex++, "cannot call 'unshift' on fixed length list(s) of " +
                                          "type 'int[2]'",
                                  70, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'push' on fixed length list(s) of type '(int[1]|float[1])'",
                                  75, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'push' on fixed length list(s) of type '([int,int][1]|[float," +
                                          "float][1])'",
                                  80, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'shift' on tuple(s) of type '[int,string...]': cannot violate inherent" +
                                          " type",
                                  85, 24);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'shift' on tuple(s) of type '[int,string,int...]': cannot violate " +
                                          "inherent type",
                                  90, 24);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'push' on fixed length list(s) of type '([int,int]|[float,float])'",
                                  101, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                                  "cannot call 'shift' on fixed length list(s) of type '[string,int]'",
                                  119, 24);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'ballerina/lang.array:0.0.0:SortDirection', " +
                        "found 'function (int) returns (int)'", 133, 32);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'ballerina/lang.array:0.0.0:SortDirection', " +
                        "found 'function (int) returns (int)'", 137, 33);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'isolated function (ballerina/lang.array:0.0.0:Type) returns" +
                        " (ballerina/lang.array:0.0.0:OrderedType)?', found 'string'", 139, 8);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(Person|int)[]' is not an ordered type",
                143, 33);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(Person|int)[]' is not an ordered type",
                145, 33);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(Person|int)[]' is not an ordered type",
                147, 33);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'isolated function (ballerina/lang.array:0.0.0:Type) returns " +
                        "(ballerina/lang.array:0.0.0:OrderedType)?', found 'isolated function ((Person|int)) " +
                        "returns ((Person|int))'", 149, 61);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: 'map<string>?[]' is not an ordered type",
                155, 35);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'isolated function (ballerina/lang.array:0.0.0:Type) returns " +
                        "(ballerina/lang.array:0.0.0:OrderedType)?', found 'isolated function (map<string>?) " +
                        "returns (map<string>?)'", 157, 62);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "too many arguments in call to 'sort()'", 161, 24);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(Person|int)[]' is not an ordered type", 165, 45);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(Person|int)[]' is not an ordered type", 167, 45);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(Person|int)[]' is not an ordered type", 169, 45);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: 'map<string>?[]' is not an ordered type", 171, 47);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'ballerina/lang.array:0.0.0:OrderedType', found 'any'", 174, 60);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(string|int)[]' is not an ordered type", 178, 34);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(string|int)[]' is not an ordered type", 180, 34);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid member type of the array/tuple to sort: '(string|int)[]' is not an ordered type", 182, 34);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "invalid sort key function return type: '(string|int)' is not an ordered type", 184, 62);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'ballerina/lang.array:0.0.0:AnydataType[]', " +
                        "found '(Person|error)[]'", 195, 15);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'ballerina/lang.array:0.0.0:AnydataType[]', " +
                        "found '(Person|error)[]'", 196, 15);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'ballerina/lang.array:0.0.0:AnydataType[]', " +
                        "found 'function[]'", 199, 15);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: expected " +
                "'ballerina/lang.array:0.0.0:AnydataType', found 'function (int) returns (int)'", 199, 26);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "undefined function 'som' in type '[int,int]'", 207, 16);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'function (ballerina/lang.array:0.0.0:Type) returns (boolean)', " +
                        "found 'function (int) returns (int)'", 215, 21);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'int', found 'boolean'", 219, 13);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: expected 'int', found 'string'", 224, 18);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "operator '>' not defined for 'string' and 'int'", 229, 25);
        BAssertUtil.validateError(negativeResult, errorIndex++, "undefined function 'ever' in type '[int,int]'",
                233, 16);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: " +
                "expected 'function (ballerina/lang.array:0.0.0:Type) returns (boolean)', " +
                "found 'function (int) returns (int)'", 237, 22);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: expected 'int', found 'boolean'",
                241, 13);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: expected 'int', found 'string'",
                246, 19);
        BAssertUtil.validateError(negativeResult, errorIndex++, "operator '>' not defined for 'string' and 'int'",
                251, 26);
        BAssertUtil.validateError(negativeResult, errorIndex++, "invalid member type of the array/tuple to sort: " +
                        "'[string,T][]' is not an ordered type", 258, 31);
        BAssertUtil.validateError(negativeResult, errorIndex++, "invalid member type of the array/tuple to sort: " +
                        "'[string,T][]' is not an ordered type", 259, 28);
        BAssertUtil.validateError(negativeResult, errorIndex++, "invalid sort key function return type: " +
                        "'[string,T]' is not an ordered type", 261, 69);
        BAssertUtil.validateError(negativeResult, errorIndex++, "invalid sort key function return type: " +
                        "'[string,T]' is not an ordered type", 264, 32);
        BAssertUtil.validateError(negativeResult, errorIndex++, "invalid member type of the array/tuple to sort: " +
                "'[string,T][]' is not an ordered type", 268, 28);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: expected 'Obj', found 'object { " +
                "int i; }'", 288, 9);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: expected 'int', found 'string'",
                                  305, 19);
        BAssertUtil.validateError(negativeResult, errorIndex++, "incompatible types: expected 'int', found 'string'",
                                  306, 41);
        BAssertUtil.validateError(negativeResult, errorIndex++, "missing non-defaultable required record field " +
                "'empCount'", 318, 16);
        BAssertUtil.validateError(negativeResult, errorIndex++, "missing required parameter 'j' in call to 'new()'",
                                  324, 14);
        BAssertUtil.validateError(negativeResult, errorIndex++, "missing error detail arg for error detail field " +
                "'code'", 331, 14);
        Assert.assertEquals(negativeResult.getErrorCount(), errorIndex);
    }

    @Test
    public void testArrayLibNegativeSemantics() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/arraylib_test_negative_semantic.bal");
        int errorIndex = 0;
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: 'int' will not be matched to 'string'", 18, 28);
        BAssertUtil.validateError(negativeResult, errorIndex++,
                "incompatible types: 'int' will not be matched to 'string'", 22, 29);
        Assert.assertEquals(negativeResult.getErrorCount(), errorIndex);
    }

    @Test(dataProvider = "FunctionList")
    public void testArrayFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @Test
    public void testSort3() {
        Object returns = BRunUtil.invoke(compileResult, "testSort3");

        assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);
        BArray arr = (BArray) returns;

        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.INT_TAG);
        assertEquals(arr.size(), 100);

        for (int i = 1; i < arr.size(); i++) {
            assertTrue(arr.getInt(i) < arr.getInt(i - 1));
        }
    }

    @DataProvider(name = "FunctionList")
    public Object[] testFunctions() {
        return new String[]{
                "testSliceOnTupleWithRestDesc",
                "testLastIndexOf",
                "testPush",
                "testShiftOperation",
                "testSort1",
                "testSort2",
                "testSort4",
                "testSort5",
                "testSort6",
                "testSort7",
                "testSort8",
                "testSort9",
                "testSort10",
                "testReadOnlyArrayFilter",
                "testTupleFilter",
                "testTupleReverse",
                "testToStreamOnImmutableArray",
                "testPushAfterSlice",
                "testPushAfterSliceFixed",
                "testLastIndexOf",
                "testReverseInt",
                "testReverseFloat",
                "testReverseStr",
                "testReverseBool",
                "testReverseByte",
                "testReverseMap",
                "testReverseRecord",
                "testArrayReverseEquality",
                "testPushAfterSliceOnTuple",
                "testUnshiftLargeValues",
                "testSome1",
                "testSome2",
                "testSome3",
                "testSome4",
                "testSome5",
                "testSome6",
                "testSome7",
                "testSome8",
                "testSome9",
                "testModificationWithinSome",
                "testEvery1",
                "testEvery2",
                "testEvery3",
                "testEvery4",
                "testEvery5",
                "testEvery6",
                "testEvery7",
                "testEvery8",
                "testEvery9",
                "testModificationWithinEvery",
                "testArrSortWithNamedArgs1",
                "testArrSortWithNamedArgs2",
                "testArrSortWithNamedArgs3",
                "testArrayPop",
                "testSetLengthNegative",
                "testArrayFilterWithEmptyArrayAndTypeBinding",
                "testArrayReverseWithEmptyArrayAndTypeBinding"
        };
    }
}
