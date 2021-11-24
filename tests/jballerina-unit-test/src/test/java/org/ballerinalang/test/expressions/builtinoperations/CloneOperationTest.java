/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.expressions.builtinoperations;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the test cases to clone operation.
 *
 * @version 0.983.0
 */

public class CloneOperationTest {

    private CompileResult result;
    private CompileResult negativeResult;
    private CompileResult taintCheckResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinoperations/clone-operation.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/builtinoperations/clone-operation-negative.bal");
        taintCheckResult =
                BCompileUtil.compile("test-src/expressions/builtinoperations/clone-operation-taint-negative.bal");
    }

    @Test(enabled = false)
    public void testCloneNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "too many arguments in call to 'clone()'", 19, 13);
        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to untainted parameter 'intArg'", 12, 22);

    }

    @Test
    public void testCloneCyclicMapsArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicMapsArray");
        Assert.assertNotNull(results);
        Assert.assertNotSame(((BValueArray) results[0]).getRefValue(0), ((BValueArray) results[1]).getRefValue(0));
        Assert.assertNotSame(((BValueArray) results[0]).getRefValue(1), ((BValueArray) results[1]).getRefValue(1));
    }

    @Test
    public void testCloneCyclicRecord() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicRecord");
        Assert.assertNotNull(results);

        BMap record = (BMap) results[0];
        BMap fieldA = (BMap) record.get("a");

        BValueArray arr = (BValueArray) fieldA.get("arr");
        BMap fieldB = (BMap) record.get("b");
        BMap fieldAOfB = (BMap) fieldB.get("aa");
        BValueArray arrOfAA = (BValueArray) fieldAOfB.get("arr");

        Assert.assertEquals(arr.getInt(0), 10);
        Assert.assertEquals(arrOfAA.getInt(0), 10);

        record = (BMap) results[1];
        BMap fieldA1 = (BMap) record.get("a");

        arr = (BValueArray) fieldA1.get("arr");
        fieldB = (BMap) record.get("b");
        BMap fieldAOfB1 = (BMap) fieldB.get("aa");
        arrOfAA = (BValueArray) fieldAOfB1.get("arr");

        Assert.assertEquals(arr.getInt(0), 1);
        Assert.assertEquals(arrOfAA.getInt(0), 1);

        Assert.assertSame(fieldA, fieldAOfB);
        Assert.assertSame(fieldA1, fieldAOfB1);

        Assert.assertNotSame(fieldA, fieldA1);
        Assert.assertNotSame(fieldAOfB, fieldAOfB1);
    }

    @Test
    public void testCloneCyclicArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicArray");
        Assert.assertNotNull(results);

        BValueArray[] arr = new BValueArray[2];
        arr[0] = (BValueArray) results[0];
        arr[1] = (BValueArray) results[1];

        BMap record1 = (BMap) arr[0].getBValue(2);
        BMap record2 = (BMap) arr[0].getBValue(3);

        BMap record3 = (BMap) arr[1].getBValue(2);
        BMap record4 = (BMap) arr[1].getBValue(3);

        BValueArray intArr1 = (BValueArray) record1.get("arr");
        BValueArray intArr2 = (BValueArray) record2.get("arr");

        BValueArray intArr3 = (BValueArray) record3.get("arr");
        BValueArray intArr4 = (BValueArray) record4.get("arr");

        Assert.assertSame(intArr1, intArr2);
        Assert.assertSame(intArr3, intArr4);

        Assert.assertNotSame(intArr1, intArr3);
        Assert.assertNotSame(intArr2, intArr4);
    }

    @Test(dataProvider = "function-names-provider")
    public void testCloneValues(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "function-names-provider")
    public Object[] functionNameProvider() {
        return new String[]{
                "cloneInt", "cloneFloat", "cloneDecimalValue", "cloneByte", "cloneBoolean", "cloneString", "cloneXML"
                , "cloneJSON", "cloneJSONArray", "cloneIntArray", "cloneDecimalArray", "cloneByteArray",
                "cloneStringArray", "cloneFloatArray", "cloneUnionArray", "cloneUnion", "cloneTable", "cloneMap",
                "cloneNilableInt", "cloneReturnValues", "cloneArrayOfArrays", "cloneTuple", "cloneAnydataRecord",
                "cloneAnydata", "cloneFrozenAnydata", "cloneNullJson", "cloneNilAnydata", "testCloneArrayWithError",
                "testCloneMapWithError", "cloneRecordWithArrayField", "cloneArrayWithRecordElement"
        };
    }
}
