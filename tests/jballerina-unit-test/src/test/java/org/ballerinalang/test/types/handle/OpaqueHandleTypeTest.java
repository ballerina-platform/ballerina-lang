/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.handle;

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * This class contains test cases for the handle type in Ballerina.
 *
 * @since 1.0.0
 */
public class OpaqueHandleTypeTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/handle/handle-type.bal");
    }

    @Test(description = "Test a function that returns a value of handle")
    public void testHandleReturnFromFunction() {
        BValue[] returns = BRunUtil.invoke(result, "getHandle");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test a function that accepts and returns a handle value")
    public void testHandleValueAsAParameter() {
        UUID uuidValue = UUID.randomUUID();
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(uuidValue);
        BValue[] returns = BRunUtil.invoke(result, "getHandleValueAsAParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), uuidValue);
    }

    @Test(description = "Test a function that accepts any and returns a handle")
    public void testAcceptHandleValueWithAny() {
        String strValue = "any string value";
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(strValue);
        BValue[] returns = BRunUtil.invoke(result, "acceptHandleValueWithAny", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), strValue);
    }

    @Test(description = "Test the equality of two handle values")
    public void testHandleValueEquility() {
        UUID uuidValue1 = UUID.randomUUID();
        BValue[] args1 = new BValue[2];
        args1[0] = new BHandleValue(uuidValue1);
        args1[1] = new BHandleValue(uuidValue1);
        BValue[] returns1 = BRunUtil.invoke(result, "testHandleValueEquality", args1);
        Assert.assertTrue(((BBoolean) returns1[0]).booleanValue());

        UUID uuidValue2 = UUID.randomUUID();
        BValue[] args2 = new BValue[2];
        args2[0] = new BHandleValue(uuidValue1);
        args2[1] = new BHandleValue(uuidValue2);
        BValue[] returns2 = BRunUtil.invoke(result, "testHandleValueEquality", args2);
        Assert.assertFalse(((BBoolean) returns2[0]).booleanValue());
    }

    @Test(description = "Test the inequality of two handle values")
    public void testHandleValueInequality() {
        UUID uuidValue1 = UUID.randomUUID();
        BValue[] args1 = new BValue[2];
        args1[0] = new BHandleValue(uuidValue1);
        args1[1] = new BHandleValue(uuidValue1);
        BValue[] returns1 = BRunUtil.invoke(result, "testHandleValueInequality", args1);
        Assert.assertFalse(((BBoolean) returns1[0]).booleanValue());

        UUID uuidValue2 = UUID.randomUUID();
        BValue[] args2 = new BValue[2];
        args2[0] = new BHandleValue(uuidValue1);
        args2[1] = new BHandleValue(uuidValue2);
        BValue[] returns2 = BRunUtil.invoke(result, "testHandleValueInequality", args2);
        Assert.assertTrue(((BBoolean) returns2[0]).booleanValue());
    }

    @Test(description = "Test a function that works with module-level handle variable")
    public void testModuleLevelHandleVariableAccess() {
        String strValue = "any string value";
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(strValue);
        BValue[] returns = BRunUtil.invoke(result, "setAndGetModuleLevelHandleValue", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), strValue);
    }

    @Test(description = "Test a union types that include the handle type")
    public void testUnionsWithHandleType() {
        String strValue = "String handle value";
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(strValue);
        BValue[] returns = BRunUtil.invoke(result, "testUnionsWithHandleType", args);
        Assert.assertEquals(returns[0].stringValue(), "handle");

        args = new BValue[1];
        args[0] = new BString("String handle value");
        returns = BRunUtil.invoke(result, "testUnionsWithHandleType", args);
        Assert.assertEquals(returns[0].stringValue(), "string");

        args = new BValue[1];
        args[0] = new BInteger(10);
        returns = BRunUtil.invoke(result, "testUnionsWithHandleType", args);
        Assert.assertEquals(returns[0].stringValue(), "int");
    }

    @Test(description = "Test array access of handle values")
    public void testArrayAccessOfHandleValues() {
        BValueArray bValueArray = new BValueArray(BTypes.typeHandle);
        bValueArray.add(0, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(1, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(2, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(3, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(4, new BHandleValue(UUID.randomUUID()));

        int testIndex = 2;
        Object testValue = ((BHandleValue) bValueArray.getRefValue(testIndex)).getValue();
        BValue[] args = new BValue[2];
        args[0] = bValueArray;
        args[1] = new BInteger(testIndex); // index
        BValue[] returns = BRunUtil.invoke(result, "testArrayAccessOfHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), testValue);
    }

    @Test(description = "Test array store of handle values")
    public void testArrayStoreOfHandleValues() {
        BValueArray bValueArray = new BValueArray(BTypes.typeHandle);
        bValueArray.add(0, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(1, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(2, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(3, new BHandleValue(UUID.randomUUID()));
        bValueArray.add(4, new BHandleValue(UUID.randomUUID()));

        int testIndex = 3;
        BHandleValue testValue = new BHandleValue(UUID.randomUUID());
        BValue[] args = new BValue[3];
        args[0] = bValueArray;
        args[1] = new BInteger(testIndex); // index
        args[2] = testValue;
        BValue[] returns = BRunUtil.invoke(result, "testArrayStoreOfHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[testIndex]).getValue(), testValue.getValue());
    }

    @Test(description = "Test creation of a handle array")
    public void testCreateArrayOfHandleValues() {
        int testIndex = 3;
        BValue[] args = new BValue[5];
        args[0] = new BHandleValue(UUID.randomUUID());
        args[1] = new BHandleValue(UUID.randomUUID());
        args[2] = new BHandleValue(UUID.randomUUID());
        args[3] = new BHandleValue(UUID.randomUUID());
        args[4] = new BInteger(testIndex);
        BValue[] returns = BRunUtil.invoke(result, "testCreateArrayOfHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(),
                ((BHandleValue) args[testIndex]).getValue());
    }

    @Test(description = "Test creation of a handle map")
    public void testCreateMapOfHandleValues() {
        BValue[] args = new BValue[4];
        args[0] = new BHandleValue(UUID.randomUUID());
        args[1] = new BHandleValue(UUID.randomUUID());
        args[2] = new BHandleValue(UUID.randomUUID());
        args[3] = new BHandleValue(UUID.randomUUID());
        BValue[] returns = BRunUtil.invoke(result, "testCreateMapOfHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(),
                ((BHandleValue) args[1]).getValue());
    }

    @Test(description = "Test creation of a record")
    public void testCreateRecordOfHandleValues() {
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue(UUID.randomUUID());
        args[1] = new BHandleValue(UUID.randomUUID());
        BValue[] returns = BRunUtil.invoke(result, "testCreateRecordWithHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(),
                ((BHandleValue) args[1]).getValue());
    }

    @Test(description = "Test creation of a tuple with handle values")
    public void testCreateTuplesWithHandleValues() {
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue(UUID.randomUUID());
        args[1] = new BHandleValue(UUID.randomUUID());
        BValue[] returns = BRunUtil.invoke(result, "testCreateTuplesWithHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(),
                ((BHandleValue) args[0]).getValue());
    }

    @Test(description = "Test creation of an object with handle values")
    public void testCreateObjectWithHandleValues() {
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue(UUID.randomUUID());
        args[1] = new BHandleValue(UUID.randomUUID());
        BValue[] returns = BRunUtil.invoke(result, "testCreateObjectWithHandleValues", args);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(),
                ((BHandleValue) args[1]).getValue());
    }
}
