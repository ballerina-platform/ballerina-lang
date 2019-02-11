/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXMLItem;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class ArrayTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;
    private CompileResult arrayImplicitInitialValueNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-test.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/arrays/array-negative.bal");
        arrayImplicitInitialValueNegative =
                BCompileUtil.compile("test-src/statements/arrays/array-implicit-initial-value-negative.bal");
    }

    @Test
    public void testFloatArrayLength() {
        BValueArray arrayValue = new BValueArray(BTypes.typeFloat);
        arrayValue.add(0, 10f);
        arrayValue.add(1, 11.1f);
        arrayValue.add(2, 12.2f);

        BValue[] args = {arrayValue};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testFloatArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testIntArrayLength() {
        BValueArray arrayValue = new BValueArray(BTypes.typeInt);
        arrayValue.add(0, 10);
        arrayValue.add(1, 11);
        arrayValue.add(2, 12);
        BValue[] args = {arrayValue};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testIntArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 4, "Length didn't match");
    }

    @Test
    public void testStringArrayLength() {
        BValueArray arrayValue = new BValueArray(BTypes.typeString);
        arrayValue.add(0, "Hello");
        arrayValue.add(1, "World");
        BValue[] args = {arrayValue};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testStringArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testXMLArrayLength() {
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testXMLArrayLength");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
    }

    @Test()
    public void testJSONArrayLength() {
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testJSONArrayLength");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 2, "Length didn't match");
        Assert.assertEquals(((BInteger) returnVals[1]).intValue(), 3, "Length didn't match");
    }

    @Test(description = "Test readable string value when containing a NIL element")
    public void testArrayStringRepresentationWithANilElement() {
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testArrayWithNilElement");
        String str = returnVals[0].stringValue();
        Assert.assertEquals(str, "[\"abc\", \"d\", (), \"s\"]");
    }
    
    @Test
    public void testArrayToString() {
        String[] strArray = { "aaa", "bbb", "ccc" };
        BValueArray bStringArray = new BValueArray(strArray);
        Assert.assertEquals(bStringArray.stringValue(), "[\"aaa\", \"bbb\", \"ccc\"]");

        long[] longArray = { 6, 3, 8, 4 };
        BValueArray bIntArray = new BValueArray(longArray);
        Assert.assertEquals(bIntArray.stringValue(), "[6, 3, 8, 4]");

        double[] doubleArray = { 6.4, 3.7, 8.8, 7.4 };
        BValueArray bFloatArray = new BValueArray(doubleArray);
        Assert.assertEquals(bFloatArray.stringValue(), "[6.4, 3.7, 8.8, 7.4]");

        int[] boolArray = { 1, 1, 0 };
        BValueArray bBooleanArray = new BValueArray(boolArray);
        Assert.assertEquals(bBooleanArray.stringValue(), "[true, true, false]");

        BXMLItem[] xmlArray = { new BXMLItem("<foo> </foo>"), new BXMLItem("<bar>hello</bar>") };
        BValueArray bXmlArray = new BValueArray(xmlArray, BTypes.typeXML);
        Assert.assertEquals(bXmlArray.stringValue(), "[<foo> </foo>, <bar>hello</bar>]");
    }

    @Test(description = "Test arrays with errors")
    public void testConnectorNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "function invocation on type 'int[]' is not supported", 3, 18);
        BAssertUtil.validateError(resultNegative, 1, "function invocation on type 'string[]' is not supported", 8, 21);
    }

    @Test(description = "Test arrays of types without implicit initial values")
    public void testArrayImplicitInitialValues() {
        Assert.assertEquals(arrayImplicitInitialValueNegative.getErrorCount(), 9);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 0, "array element type 'ObjInitWithParam' " +
                "does not have an implicit initial value, use 'ObjInitWithParam?'", 53, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 1, "array element type 'FT' does not have an " +
                "implicit initial value, use 'FT?'", 74, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 2, "array element type 'FTUnion' does not have" +
                " an implicit initial value, use 'FTUnion?'", 89, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 3, "array element type 'FTSingle' does not have" +
                " an implicit initial value, use 'FTSingle?'", 92, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 4, "array element type 'FTNUnion' does not have" +
                " an implicit initial value, use 'FTNUnion?'", 95, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 5, "array element type 'error<>' does not have" +
                " an implicit initial value, use 'error<>?'", 100, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 6, "array element type 'UN' does not have" +
                " an implicit initial value, use 'UN?'", 115, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 7, "array element type 'error<>' does not have" +
                " an implicit initial value, use 'error<>?'", 141, 1);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 8, "array element type 'FTUnion' does not have" +
                " an implicit initial value, use 'FTUnion?'", 149, 9);
    }

    @Test(description = "Test arrays of types without implicit initial values")
    public void testArrayImplicitInitialValuesOfFiniteType() {
        CompileResult negResult = BCompileUtil.compile(
                "test-src/statements/arrays/array-implicit-initial-value-finite-type-negative.bal");
        Assert.assertEquals(negResult.getErrorCount(), 4);
        BAssertUtil.validateError(negResult, 0,
                "array element type 'allInitNonZero' does not have an implicit initial value, use 'allInitNonZero?'",
                22, 1);
        BAssertUtil.validateError(negResult, 1,
                "array element type 'allFloatNonZero' does not have an implicit initial value, use 'allFloatNonZero?'",
                29, 1);
        BAssertUtil.validateError(negResult, 2,
                "array element type 'bTrue' does not have an implicit initial value, use 'bTrue?'",
                36, 1);
        BAssertUtil.validateError(negResult, 3,
                "array element type 'allStrNonEmpty' does not have an implicit initial value, use 'allStrNonEmpty?'",
                43, 1);
    }
}
