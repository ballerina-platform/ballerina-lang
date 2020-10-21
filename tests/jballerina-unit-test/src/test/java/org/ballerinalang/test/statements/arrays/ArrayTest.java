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

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.BArrayState;


/**
 * Test cases for ballerina.model.arrays.
 */
public class ArrayTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-test.bal");
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
        Assert.assertEquals(str, "[\"abc\",\"d\",null,\"s\"]");
    }

    @Test
    public void testArrayToString() {
        String[] strArray = { "aaa", "bbb", "ccc" };
        ArrayValue bStringArray = new ArrayValueImpl(strArray);
        Assert.assertEquals(bStringArray.stringValue(null), "[\"aaa\",\"bbb\",\"ccc\"]");

        long[] longArray = { 6, 3, 8, 4 };
        ArrayValue bIntArray = new ArrayValueImpl(longArray);
        Assert.assertEquals(bIntArray.stringValue(null), "[6,3,8,4]");

        double[] doubleArray = { 6.4, 3.7, 8.8, 7.4 };
        ArrayValue bFloatArray = new ArrayValueImpl(doubleArray);
        Assert.assertEquals(bFloatArray.stringValue(null), "[6.4,3.7,8.8,7.4]");

        boolean[] boolArray = { true, true, false };
        ArrayValue bBooleanArray = new ArrayValueImpl(boolArray);
        Assert.assertEquals(bBooleanArray.stringValue(null), "[true,true,false]");

        BXML[] xmlArray = { XMLFactory.parse("<foo> </foo>"), XMLFactory.parse("<bar>hello</bar>") };
        ArrayValue bXmlArray = new ArrayValueImpl(xmlArray,
                new io.ballerina.runtime.types.BArrayType(PredefinedTypes.TYPE_XML));
        Assert.assertEquals(bXmlArray.stringValue(null), "[`<foo> </foo>`,`<bar>hello</bar>`]");
    }

    @Test
    public void testElementTypesWithoutImplicitInitVal() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testElementTypesWithoutImplicitInitVal");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED);
        Assert.assertEquals(arr.stringValue(), "[1, 2]");
    }

    @Test
    public void testArrayFieldInRecord() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArrayFieldInRecord");
        BMap barRec = (BMap) retVals[0];
        BValueArray arr = (BValueArray) barRec.get("fArr");
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED);
        Assert.assertEquals(arr.stringValue(), "[1, 2]");
    }

    @Test
    public void testArrayFieldInObject() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArrayFieldInObject");
        BMap barRec = (BMap) retVals[0];
        BValueArray arr = (BValueArray) barRec.get("fArr");
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED);
        Assert.assertEquals(arr.stringValue(), "[1, 2]");
    }

    @Test
    public void testArraysAsFuncParams() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArraysAsFuncParams");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(((BArrayType) arr.getArrayType()).getState(), BArrayState.CLOSED);
        Assert.assertEquals(arr.stringValue(), "[1, 3]");
    }

    @Test
    public void testArraysOfCyclicDependentTypes() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArraysOfCyclicDependentTypes");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(arr.stringValue(),
                            "[{b:{b1:\"B1\"}}, {b:{b1:\"B1\"}}, {b:{b1:\"B1\"}}, {b:{b1:\"B1\"}, a1:\"A1\"}]");
    }

    @Test
    public void testArraysOfCyclicDependentTypes2() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testArraysOfCyclicDependentTypes2");
        BValueArray arr = (BValueArray) retVals[0];
        Assert.assertEquals(arr.stringValue(), "[{b1:\"B1\"}, {b1:\"B1\"}, {b1:\"B1\"}, {b1:\"B1\"}]");
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

    @Test
    public void testGetFromFrozenArray() {
        BValue[] retVals = BRunUtil.invokeFunction(compileResult, "testGetFromFrozenArray");
        BInteger value = (BInteger) retVals[0];
        Assert.assertEquals(value.intValue(), 4);
    }

    @Test
    public void createAbstractObjectEmptyArray() {
        BRunUtil.invokeFunction(compileResult, "createAbstractObjectEmptyArray");
    }

    @Test
    public void testObjectDynamicArrayFilling() {
        BRunUtil.invokeFunction(compileResult, "testObjectDynamicArrayFilling");
    }

    @Test
    public void testMultidimensionalArrayString() {
        BRunUtil.invokeFunction(compileResult, "testMultidimensionalArrayString");
    }

    @Test
    public void testArrayMapString() {
        BRunUtil.invokeFunction(compileResult, "testArrayMapString");
    }

    @Test
    public void testArrayUnionType() {
        BRunUtil.invokeFunction(compileResult, "testArrayUnionType");
    }

    @Test
    public void testArrayTupleType() {
        BRunUtil.invokeFunction(compileResult, "testArrayTupleType");
    }

    @Test
    public void testUpdatingJsonTupleViaArrayTypedVar() {
        BRunUtil.invokeFunction(compileResult, "testUpdatingJsonTupleViaArrayTypedVar");
    }
}
