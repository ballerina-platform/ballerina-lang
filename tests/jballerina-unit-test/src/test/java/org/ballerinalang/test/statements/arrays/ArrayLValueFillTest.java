/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for filling the elements of a multi-dimensional array when it is used as an LValue.
 *
 * @since 1.2.0
 */
public class ArrayLValueFillTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array_lvalue_fill_test.bal");
    }

    @Test
    public void testSimpleBasic2DArrays() {
        BRunUtil.invoke(compileResult, "testSimpleBasic2DArrays");
    }

    @Test
    public void testRecordArrays() {
        BRunUtil.invoke(compileResult, "testRecordArrays");
    }

    @Test
    public void test2DRecordArrays() {
        BRunUtil.invoke(compileResult, "test2DRecordArrays");
    }

    @Test
    public void testObjectArrays() {
        BValueArray arr = (BValueArray) BRunUtil.invokeFunction(compileResult, "testObjectArrays")[0];
        assertEquals(arr.size(), 2);

        BMap person = (BMap) arr.getRefValue(0);
        assertEquals(person.getType().getName(), "PersonObj");
        assertEquals(person.get("name").stringValue(), "John Doe");

        person = (BMap) arr.getRefValue(1);
        assertEquals(person.getType().getName(), "PersonObj");
        assertEquals(person.get("name").stringValue(), "Pubudu");
    }

    @Test
    public void test2DObjectArrays() {
        BValueArray arr = (BValueArray) BRunUtil.invokeFunction(compileResult, "test2DObjectArrays")[0];

        assertEquals(arr.size(), 3);
        assertEquals(arr.getRefValue(0).size(), 0);
        assertEquals(arr.getRefValue(1).size(), 0);
        assertEquals(arr.getRefValue(2).size(), 2);

        BValueArray peopleArr = (BValueArray) arr.getRefValue(2);
        for (int i = 0; i < peopleArr.size(); i++) {
            BMap person = (BMap) peopleArr.getRefValue(i);
            assertEquals(person.getType().getName(), "PersonObj");
            assertEquals(person.get("name").stringValue(), "John Doe");
        }
    }

    @Test(enabled = false)
    public void test2DObjectArrays2() {
        BValueArray arr = (BValueArray) BRunUtil.invokeFunction(compileResult, "test2DObjectArrays2")[0];

        assertEquals(arr.size(), 3);
        assertEquals(arr.getRefValue(0).size(), 0);
        assertEquals(arr.getRefValue(1).size(), 0);
        assertEquals(arr.getRefValue(2).size(), 2);

        BValueArray peopleArr = (BValueArray) arr.getRefValue(2);
        for (int i = 0; i < peopleArr.size(); i++) {
            BMap person = (BMap) peopleArr.getRefValue(i);
            assertEquals(person.getType().getName(), "PersonObj");
            assertEquals(person.get("name").stringValue(), "John Doe");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IllegalListInsertion " +
                  "\\{\"message\":\"array of length 0 cannot be expanded into array of length 2 without " +
                  "filler values.*")
    public void test2DObjectArrays3() {
        BRunUtil.invoke(compileResult, "test2DObjectArrays3");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IllegalListInsertion " +
                  "\\{\"message\":\"array of length 0 cannot be expanded into array of length 2 without " +
                  "filler values.*")
    public void testRecordsWithoutFillerValues() {
        BRunUtil.invoke(compileResult, "testRecordsWithoutFillerValues");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IllegalListInsertion " +
                  "\\{\"message\":\"array of length 0 cannot be expanded into array of length 1 without " +
                  "filler values.*")
    public void testRecordsWithoutFillerValues2() {
        BRunUtil.invoke(compileResult, "testRecordsWithoutFillerValues2");
    }

    @Test
    public void testArraysInRecordFields() {
        BRunUtil.invoke(compileResult, "testArraysInRecordFields");
    }

    @Test
    public void testArraysInObjectFields() {
        BRunUtil.invoke(compileResult, "testArraysInObjectFields");
    }

    @Test
    public void testArraysInUnionTypes() {
        BRunUtil.invoke(compileResult, "testArraysInUnionTypes");
    }

    @Test
    public void testArraysOfTuples() {
        BRunUtil.invoke(compileResult, "testArraysOfTuples");
    }

    @Test
    public void test2DArrayInATuple() {
        BRunUtil.invoke(compileResult, "test2DArrayInATuple");
    }

    @Test
    public void testFiniteTyped2DArrays() {
        BRunUtil.invoke(compileResult, "testFiniteTyped2DArrays");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IllegalListInsertion " +
                  "\\{\"message\":\"array of length 0 cannot be expanded into array of length 2 without " +
                  "filler values.*")
    public void testNoDefFiniteTyped2DArrays() {
        BRunUtil.invoke(compileResult, "testNoDefFiniteTyped2DArrays");
    }

    @Test
    public void testMapArrayAsAnLValue() {
        BRunUtil.invoke(compileResult, "testMapArrayAsAnLValue");
    }
}
