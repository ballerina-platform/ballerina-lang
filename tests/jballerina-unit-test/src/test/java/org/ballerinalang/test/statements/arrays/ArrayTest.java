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

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import static java.lang.String.format;

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

    @Test
    public void testElementTypesWithoutImplicitInitVal() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testElementTypesWithoutImplicitInitVal");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED_SEALED);
        Assert.assertEquals(arr.stringValue(), "[1, 2]");
    }

    @Test
    public void testArrayFieldInRecord() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArrayFieldInRecord");
        BMap barRec = (BMap) retVals[0];
        BValueArray arr = (BValueArray) barRec.get("fArr");
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED_SEALED);
        Assert.assertEquals(arr.stringValue(), "[1, 2]");
    }

    @Test
    public void testArrayFieldInObject() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArrayFieldInObject");
        BMap barRec = (BMap) retVals[0];
        BValueArray arr = (BValueArray) barRec.get("fArr");
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED_SEALED);
        Assert.assertEquals(arr.stringValue(), "[1, 2]");
    }

    @Test
    public void testArraysAsFuncParams() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArraysAsFuncParams");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED_SEALED);
        Assert.assertEquals(arr.stringValue(), "[1, 3]");
    }

    @Test
    public void testArraysOfCyclicDependentTypes() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArraysOfCyclicDependentTypes");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(arr.stringValue(), "[{b:{b1:\"\"}, a1:\"\"}, {b:{b1:\"\"}, a1:\"\"}, {b:{b1:\"\"}, " +
                "a1:\"\"}, {a1:\"A1\", b:{b1:\"B1\"}}]");
    }

    @Test
    public void testArraysOfCyclicDependentTypes2() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArraysOfCyclicDependentTypes2");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(arr.stringValue(), "[{b1:\"\"}, {b1:\"\"}, {b1:\"\"}, {b1:\"B1\"}]");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*error: " +
            "\\{ballerina}StackOverflow \\{\"message\":\"stack overflow\"}.*")
    public void testArraysOfCyclicDependentTypes3() {
        BRunUtil.invokeFunction(compileResult, "testArraysOfCyclicDependentTypes3");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*error: " +
            "\\{ballerina}StackOverflow \\{\"message\":\"stack overflow\"}.*")
    public void testArraysOfCyclicDependentTypes4() {
        BRunUtil.invokeFunction(compileResult, "testArraysOfCyclicDependentTypes4");
    }

    @Test(description = "Test arrays with errors")
    public void testConnectorNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "function invocation on type 'int[]' is not supported", 3, 18);
        BAssertUtil.validateError(resultNegative, 1, "function invocation on type 'string[]' is not supported", 8, 21);
    }

    @Test(description = "Test arrays of types without implicit initial values")
    public void testArrayImplicitInitialValues() {
        String errMsgFormat = "array element type '%s' does not have an implicit initial value, use '%s'";
        Assert.assertEquals(arrayImplicitInitialValueNegative.getErrorCount(), 19);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 0,
                                  format(errMsgFormat, "ObjInitWithParam", "ObjInitWithParam?"), 53, 41);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 1, format(errMsgFormat, "1|2|3", "1|2|3?"),
                                  74, 24);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 2,
                                  format(errMsgFormat, "1|2|3", "1|2|3?"), 89, 16);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 3,
                                  format(errMsgFormat, "error", "error?"), 103, 18);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 4,
                                  format(errMsgFormat, "1|2|3", "1|2|3?"), 110, 19);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 5, format(errMsgFormat, "int|float", "int|float?"),
                                  118, 11);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 6,
                                  format(errMsgFormat, "error", "error?"), 144, 22);

        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 7,
                                  format(errMsgFormat, "(int|string,float)[]", "(int|string,float)[]?"), 159, 33);

        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 8,
                                  format(errMsgFormat, "(int|string,float)[]", "(int|string,float)[]?"), 161, 33);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 9,
                                  format(errMsgFormat, "(int|string,float)", "(int|string,float)?"), 161, 34);

        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 10,
                                  format(errMsgFormat, "int|float[]", "int|float[]?"), 163, 25);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 11,
                                  format(errMsgFormat, "int|float", "int|float?"), 163, 26);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 12,
                                  format(errMsgFormat, "int|float", "int|float?"), 163, 36);

        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 13,
                                  format(errMsgFormat, "boolean|float[]", "boolean|float[]?"), 165, 29);

        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 14,
                                  format(errMsgFormat, "1|2|3", "1|2|3?"), 171, 11);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 15,
                                  format(errMsgFormat, "1|2|3", "1|2|3?"), 179, 25);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 16,
                                  format(errMsgFormat, "1|2|3", "1|2|3?"), 186, 21);
        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 17,
                                  format(errMsgFormat, "1|2|3", "1|2|3?"), 196, 29);

        BAssertUtil.validateError(arrayImplicitInitialValueNegative, 18,
                                  format(errMsgFormat, "A", "A?"), 218, 15);
    }

    @Test(description = "Test arrays of types without implicit initial values")
    public void testArrayImplicitInitialValuesOfFiniteType() {
        CompileResult negResult = BCompileUtil.compile(
                "test-src/statements/arrays/array-implicit-initial-value-finite-type-negative.bal");
        Assert.assertEquals(negResult.getErrorCount(), 3);
        BAssertUtil.validateError(negResult, 0,
                                  "array element type '1|2|3' does not have an implicit initial value, use '1|2|3?'",
                                  22, 24);
        BAssertUtil.validateError(negResult, 1,
                                  "array element type '1.0f|3.143f' does not have an implicit initial value, use " +
                                          "'1.0f|3.143f?'",
                                  29, 26);
        BAssertUtil.validateError(negResult, 2,
                                  "array element type 'a|b|c' does not have an implicit initial value, use 'a|b|c?'",
                                  43, 41);
    }
}
