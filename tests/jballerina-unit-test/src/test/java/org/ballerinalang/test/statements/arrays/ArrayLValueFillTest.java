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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
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
    public void testObjectArrays() {
        BArray arr = (BArray) BRunUtil.invoke(compileResult, "testObjectArrays");
        assertEquals(arr.size(), 2);

        BObject person = (BObject) arr.getRefValue(0);
        assertEquals(getType(person).getName(), "PersonObj");
        assertEquals(person.get(StringUtils.fromString("name")).toString(), "John Doe");

        person = (BObject) arr.getRefValue(1);
        assertEquals(getType(person).getName(), "PersonObj");
        assertEquals(person.get(StringUtils.fromString("name")).toString(), "Pubudu");
    }

    @Test
    public void test2DObjectArrays() {
        BArray arr = (BArray) BRunUtil.invoke(compileResult, "test2DObjectArrays");

        assertEquals(arr.size(), 3);
        assertEquals(((BArray) arr.getRefValue(0)).size(), 0);
        assertEquals(((BArray) arr.getRefValue(1)).size(), 0);
        assertEquals(((BArray) arr.getRefValue(2)).size(), 2);

        BArray peopleArr = (BArray) arr.getRefValue(2);
        for (int i = 0; i < peopleArr.size(); i++) {
            BObject person = (BObject) peopleArr.getRefValue(i);
            assertEquals(getType(person).getName(), "PersonObj");
            assertEquals(person.get(StringUtils.fromString("name")).toString(), "John Doe");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IllegalListInsertion " +
                    "\\{\"message\":\"array of length 0 cannot be expanded into array of length 2 without " +
                    "filler values.*")
    public void testObjectArrays2() {
        BRunUtil.invoke(compileResult, "testObjectArrays2");
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

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IllegalListInsertion " +
                  "\\{\"message\":\"array of length 0 cannot be expanded into array of length 2 without " +
                  "filler values.*")
    public void testNoDefFiniteTyped2DArrays() {
        BRunUtil.invoke(compileResult, "testNoDefFiniteTyped2DArrays");
    }

    @Test(dataProvider = "arrayFillerValueTestFunctions")
    public void testArrayFillerValues(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider(name = "arrayFillerValueTestFunctions")
    public Object[] arrayFillerValueTestFunctions() {
        return new String[]{
                "testSimpleBasic2DArrays",
                "testRecordArrays",
                "test2DRecordArrays",
                "testArraysInRecordFields",
                "testArraysInObjectFields",
                "testArraysInUnionTypes",
                "testArraysOfTuples",
                "test2DArrayInATuple",
                "testFiniteTyped2DArrays",
                "testMapArrayAsAnLValue",
                "testMDArrayFillerValues",
                "test2DObjectArrays2"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
