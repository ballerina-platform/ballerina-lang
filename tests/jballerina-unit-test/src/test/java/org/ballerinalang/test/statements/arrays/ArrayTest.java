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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT));
        arrayValue.add(0, 10f);
        arrayValue.add(1, 11.1f);
        arrayValue.add(2, 12.2f);

        Object[] args = {arrayValue};
        BArray returnVals = (BArray) BRunUtil.invoke(compileResult, "testFloatArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.size() == 0 || returnVals.get(0) == null ||
                returnVals.get(1) == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals.get(0), 3L, "Length didn't match");
        Assert.assertEquals(returnVals.get(1), 3L, "Length didn't match");
    }

    @Test
    public void testIntArrayLength() {
        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT));
        arrayValue.add(0, 10);
        arrayValue.add(1, 11);
        arrayValue.add(2, 12);
        Object[] args = {arrayValue};
        BArray returnVals = (BArray) BRunUtil.invoke(compileResult, "testIntArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.size() == 0, "Invalid Return Values.");
        Assert.assertNotNull(returnVals.get(0));
        Assert.assertNotNull(returnVals.get(1));
        Assert.assertEquals(returnVals.get(0), 3L, "Length didn't match");
        Assert.assertEquals(returnVals.get(1), 4L, "Length didn't match");
    }

    @Test
    public void testStringArrayLength() {
        BArray arrayValue =
                ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING));
        arrayValue.add(0, "Hello");
        arrayValue.add(1, "World");
        Object[] args = {arrayValue};
        BArray returnVals = (BArray) BRunUtil.invoke(compileResult, "testStringArrayLength", args);
        Assert.assertFalse(returnVals == null || returnVals.size() == 0, "Invalid Return Values.");
        Assert.assertNotNull(returnVals.get(0));
        Assert.assertNotNull(returnVals.get(1));
        Assert.assertEquals(returnVals.get(0), 2L, "Length didn't match");
        Assert.assertEquals(returnVals.get(1), 5L, "Length didn't match");
    }

    @Test
    public void testXMLArrayLength() {
        Object returnVals = BRunUtil.invoke(compileResult, "testXMLArrayLength");
        Assert.assertFalse(returnVals == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals, 3L, "Length didn't match");
    }

    @Test()
    public void testJSONArrayLength() {
        BArray returnVals = (BArray) BRunUtil.invoke(compileResult, "testJSONArrayLength");
        Assert.assertFalse(returnVals == null || returnVals.size() == 0, "Invalid Return Values.");
        Assert.assertNotNull(returnVals.get(0));
        Assert.assertNotNull(returnVals.get(1));
        Assert.assertEquals(returnVals.get(0), 2L, "Length didn't match");
        Assert.assertEquals(returnVals.get(1), 3L, "Length didn't match");
    }

    @Test(description = "Test readable string value when containing a NIL element")
    public void testArrayStringRepresentationWithANilElement() {
        Object returnVals = BRunUtil.invoke(compileResult, "testArrayWithNilElement");
        String str = returnVals.toString();
        Assert.assertEquals(str, "[\"abc\",\"d\",null,\"s\"]");
    }

    @Test
    public void testArrayToString() {
        String[] strArray = {"aaa", "bbb", "ccc"};
        ArrayValue bStringArray = new ArrayValueImpl(strArray, false);
        Assert.assertEquals(bStringArray.stringValue(null), "[\"aaa\",\"bbb\",\"ccc\"]");

        long[] longArray = {6, 3, 8, 4};
        ArrayValue bIntArray = new ArrayValueImpl(longArray, false);
        Assert.assertEquals(bIntArray.stringValue(null), "[6,3,8,4]");

        double[] doubleArray = {6.4, 3.7, 8.8, 7.4};
        ArrayValue bFloatArray = new ArrayValueImpl(doubleArray, false);
        Assert.assertEquals(bFloatArray.stringValue(null), "[6.4,3.7,8.8,7.4]");

        boolean[] boolArray = {true, true, false};
        ArrayValue bBooleanArray = new ArrayValueImpl(boolArray, false);
        Assert.assertEquals(bBooleanArray.stringValue(null), "[true,true,false]");

        BXml[] xmlArray = {XmlFactory.parse("<foo> </foo>"), XmlFactory.parse("<bar>hello</bar>")};
        ArrayValue bXmlArray = new ArrayValueImpl(xmlArray,
                new io.ballerina.runtime.internal.types.BArrayType(PredefinedTypes.TYPE_XML));
        Assert.assertEquals(bXmlArray.stringValue(null), "[`<foo> </foo>`,`<bar>hello</bar>`]");
    }

    @Test
    public void testElementTypesWithoutImplicitInitVal() {
        Object retVals = BRunUtil.invoke(compileResult, "testElementTypesWithoutImplicitInitVal");
        BArray arr = (BArray) retVals;
        Assert.assertEquals(((BArrayType) arr.getType()).getState().getValue(), BArrayState.CLOSED.getValue());
        Assert.assertEquals(arr.toString(), "[1,2]");
    }

    @Test
    public void testArrayFieldInRecord() {
        Object retVals = BRunUtil.invoke(compileResult, "testArrayFieldInRecord");
        BMap barRec = (BMap) retVals;
        BArray arr = (BArray) barRec.get(StringUtils.fromString("fArr"));
        Assert.assertEquals(((BArrayType) arr.getType()).getState().getValue(), BArrayState.CLOSED.getValue());
        Assert.assertEquals(arr.toString(), "[1,2]");
    }

    @Test
    public void testArrayFieldInObject() {
        Object retVals = BRunUtil.invoke(compileResult, "testArrayFieldInObject");
        BObject barRec = (BObject) retVals;
        BArray arr = (BArray) barRec.get(StringUtils.fromString("fArr"));
        Assert.assertEquals(((BArrayType) arr.getType()).getState().getValue(), BArrayState.CLOSED.getValue());
        Assert.assertEquals(arr.toString(), "[1,2]");
    }

    @Test
    public void testArraysAsFuncParams() {
        Object retVals = BRunUtil.invoke(compileResult, "testArraysAsFuncParams");
        BArray arr = (BArray) retVals;
        Assert.assertEquals(((BArrayType) arr.getType()).getState().getValue(), BArrayState.CLOSED.getValue());
        Assert.assertEquals(arr.toString(), "[1,3]");
    }

    @Test
    public void testArraysOfCyclicDependentTypes() {
        Object retVals = BRunUtil.invoke(compileResult, "testArraysOfCyclicDependentTypes");
        BArray arr = (BArray) retVals;
        Assert.assertEquals(arr.toString(),
                "[{\"b\":{\"b1\":\"B1\"}},{\"b\":{\"b1\":\"B1\"}},{\"b\":{\"b1\":\"B1\"}},{\"b\":{\"b1\":\"B1\"}," +
                        "\"a1\":\"A1\"}]");
    }

    @Test
    public void testArraysOfCyclicDependentTypes2() {
        Object retVals = BRunUtil.invoke(compileResult, "testArraysOfCyclicDependentTypes2");
        BArray arr = (BArray) retVals;
        Assert.assertEquals(arr.toString(), "[{\"b1\":\"B1\"},{\"b1\":\"B1\"},{\"b1\":\"B1\"},{\"b1\":\"B1\"}]");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*error: " +
            "\\{ballerina}StackOverflow \\{\"message\":\"stack overflow\"}.*")
    public void testArraysOfCyclicDependentTypes3() {
        BRunUtil.invoke(compileResult, "testArraysOfCyclicDependentTypes3");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*error: " +
            "\\{ballerina}StackOverflow \\{\"message\":\"stack overflow\"}.*")
    public void testArraysOfCyclicDependentTypes4() {
        BRunUtil.invoke(compileResult, "testArraysOfCyclicDependentTypes4");
    }

    @Test
    public void testGetFromFrozenArray() {
        Object retVals = BRunUtil.invoke(compileResult, "testGetFromFrozenArray");
        long value = (long) retVals;
        Assert.assertEquals(value, 4);
    }

    @Test(dataProvider = "functionNamesProvider")
    public void testInvokeFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "functionNamesProvider")
    public Object[] getFunctionNames() {
        return new String[]{"createAbstractObjectEmptyArray", "testObjectDynamicArrayFilling",
                "testMultidimensionalArrayString", "testArrayMapString", "testArrayUnionType", "testArrayTupleType",
                "testUpdatingJsonTupleViaArrayTypedVar", "testVarArgsArray"};
    }

    @Test
    public void testArraysWithSyntaxErrors() {
        CompileResult compileResultNegative =
                BCompileUtil.compile("test-src/statements/arrays/array_test_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compileResultNegative, index++, "invalid token '*'", 18, 10);
        BAssertUtil.validateError(compileResultNegative, index++, "invalid token '2'", 18, 11);
        BAssertUtil.validateError(compileResultNegative, index++, "invalid token 'wed2'", 20, 8);
        Assert.assertEquals(compileResultNegative.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
