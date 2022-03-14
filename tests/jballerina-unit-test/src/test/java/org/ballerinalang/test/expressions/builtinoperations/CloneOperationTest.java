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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
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
        Object arr = BRunUtil.invoke(result, "cloneCyclicMapsArray");
        BArray results = (BArray) arr;
        Assert.assertNotNull(results);
        Assert.assertNotSame(((BArray) results.get(0)).getRefValue(0), ((BArray) results.get(1)).getRefValue(0));
        Assert.assertNotSame(((BArray) results.get(0)).getRefValue(1), ((BArray) results.get(1)).getRefValue(1));
    }

    @Test
    public void testCloneCyclicRecord() {
        Object returns = BRunUtil.invoke(result, "cloneCyclicRecord");
        BArray results = (BArray) returns;
        Assert.assertNotNull(results);

        BMap record = (BMap) results.get(0);
        BMap fieldA = (BMap) record.get(StringUtils.fromString("a"));

        BArray arr = (BArray) fieldA.get(StringUtils.fromString("arr"));
        BMap fieldB = (BMap) record.get(StringUtils.fromString("b"));
        BMap fieldAOfB = (BMap) fieldB.get(StringUtils.fromString("aa"));
        BArray arrOfAA = (BArray) fieldAOfB.get(StringUtils.fromString("arr"));

        Assert.assertEquals(arr.getInt(0), 10);
        Assert.assertEquals(arrOfAA.getInt(0), 10);

        record = (BMap) results.get(1);
        BMap fieldA1 = (BMap) record.get(StringUtils.fromString("a"));

        arr = (BArray) fieldA1.get(StringUtils.fromString("arr"));
        fieldB = (BMap) record.get(StringUtils.fromString("b"));
        BMap fieldAOfB1 = (BMap) fieldB.get(StringUtils.fromString("aa"));
        arrOfAA = (BArray) fieldAOfB1.get(StringUtils.fromString("arr"));

        Assert.assertEquals(arr.getInt(0), 1);
        Assert.assertEquals(arrOfAA.getInt(0), 1);

        Assert.assertSame(fieldA, fieldAOfB);
        Assert.assertSame(fieldA1, fieldAOfB1);

        Assert.assertNotSame(fieldA, fieldA1);
        Assert.assertNotSame(fieldAOfB, fieldAOfB1);
    }

    @Test
    public void testCloneCyclicArray() {
        Object returns = BRunUtil.invoke(result, "cloneCyclicArray");
        BArray results = (BArray) returns;
        Assert.assertNotNull(results);

        BArray[] arr = new BArray[2];
        arr[0] = (BArray) results.get(0);
        arr[1] = (BArray) results.get(1);

        BMap record1 = (BMap) arr[0].getRefValue(2);
        BMap record2 = (BMap) arr[0].getRefValue(3);

        BMap record3 = (BMap) arr[1].getRefValue(2);
        BMap record4 = (BMap) arr[1].getRefValue(3);

        BArray intArr1 = (BArray) record1.get(StringUtils.fromString("arr"));
        BArray intArr2 = (BArray) record2.get(StringUtils.fromString("arr"));

        BArray intArr3 = (BArray) record3.get(StringUtils.fromString("arr"));
        BArray intArr4 = (BArray) record4.get(StringUtils.fromString("arr"));

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
