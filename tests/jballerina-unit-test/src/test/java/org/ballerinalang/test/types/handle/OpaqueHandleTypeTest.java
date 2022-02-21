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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(result, "getHandle");
        Assert.assertNull(returns);
    }

    @Test(description = "Test a function that accepts and returns a handle value")
    public void testHandleValueAsAParameter() {
        UUID uuidValue = UUID.randomUUID();
        Object[] args = new Object[1];
        args[0] = new HandleValue(uuidValue);
        Object returns = BRunUtil.invoke(result, "getHandleValueAsAParameter", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), uuidValue);
    }

    @Test(description = "Test a function that accepts any and returns a handle")
    public void testAcceptHandleValueWithAny() {
        String strValue = "any string value";
        Object[] args = new Object[1];
        args[0] = new HandleValue(strValue);
        Object returns = BRunUtil.invoke(result, "acceptHandleValueWithAny", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), strValue);
    }

    @Test(description = "Test the equality of two handle values")
    public void testHandleValueEquility() {
        UUID uuidValue1 = UUID.randomUUID();
        Object[] args1 = new Object[2];
        args1[0] = new HandleValue(uuidValue1);
        args1[1] = new HandleValue(uuidValue1);
        Object returns1 = BRunUtil.invoke(result, "testHandleValueEquality", args1);
        Assert.assertTrue((Boolean) returns1);

        UUID uuidValue2 = UUID.randomUUID();
        Object[] args2 = new Object[2];
        args2[0] = new HandleValue(uuidValue1);
        args2[1] = new HandleValue(uuidValue2);
        Object returns2 = BRunUtil.invoke(result, "testHandleValueEquality", args2);
        Assert.assertFalse((Boolean) returns2);
    }

    @Test(description = "Test the inequality of two handle values")
    public void testHandleValueInequality() {
        UUID uuidValue1 = UUID.randomUUID();
        Object[] args1 = new Object[2];
        args1[0] = new HandleValue(uuidValue1);
        args1[1] = new HandleValue(uuidValue1);
        Object returns1 = BRunUtil.invoke(result, "testHandleValueInequality", args1);
        Assert.assertFalse((Boolean) returns1);

        UUID uuidValue2 = UUID.randomUUID();
        Object[] args2 = new Object[2];
        args2[0] = new HandleValue(uuidValue1);
        args2[1] = new HandleValue(uuidValue2);
        Object returns2 = BRunUtil.invoke(result, "testHandleValueInequality", args2);
        Assert.assertTrue((Boolean) returns2);
    }

    @Test(description = "Test a function that works with module-level handle variable")
    public void testModuleLevelHandleVariableAccess() {
        String strValue = "any string value";
        Object[] args = new Object[1];
        args[0] = new HandleValue(strValue);
        Object returns = BRunUtil.invoke(result, "setAndGetModuleLevelHandleValue", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), strValue);
    }

    @Test(description = "Test a union types that include the handle type")
    public void testUnionsWithHandleType() {
        String strValue = "String handle value";
        Object[] args = new Object[1];
        args[0] = new HandleValue(strValue);
        Object returns = BRunUtil.invoke(result, "testUnionsWithHandleType", args);
        Assert.assertEquals(returns.toString(), "handle");

        args = new Object[1];
        args[0] = StringUtils.fromString("String handle value");
        returns = BRunUtil.invoke(result, "testUnionsWithHandleType", args);
        Assert.assertEquals(returns.toString(), "string");

        args = new Object[1];
        args[0] = (10);
        returns = BRunUtil.invoke(result, "testUnionsWithHandleType", args);
        Assert.assertEquals(returns.toString(), "int");
    }

    @Test(description = "Test array access of handle values")
    public void testArrayAccessOfHandleValues() {
        BArray bValueArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_HANDLE));
        bValueArray.add(0, new HandleValue(UUID.randomUUID()));
        bValueArray.add(1, new HandleValue(UUID.randomUUID()));
        bValueArray.add(2, new HandleValue(UUID.randomUUID()));
        bValueArray.add(3, new HandleValue(UUID.randomUUID()));
        bValueArray.add(4, new HandleValue(UUID.randomUUID()));

        int testIndex = 2;
        Object testValue = ((HandleValue) bValueArray.getRefValue(testIndex)).getValue();
        Object[] args = new Object[2];
        args[0] = bValueArray;
        args[1] = (testIndex); // index
        Object returns = BRunUtil.invoke(result, "testArrayAccessOfHandleValues", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), testValue);
    }

    @Test(description = "Test array store of handle values")
    public void testArrayStoreOfHandleValues() {
        BArray bValueArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_HANDLE));
        bValueArray.add(0, new HandleValue(UUID.randomUUID()));
        bValueArray.add(1, new HandleValue(UUID.randomUUID()));
        bValueArray.add(2, new HandleValue(UUID.randomUUID()));
        bValueArray.add(3, new HandleValue(UUID.randomUUID()));
        bValueArray.add(4, new HandleValue(UUID.randomUUID()));

        int testIndex = 3;
        HandleValue testValue = new HandleValue(UUID.randomUUID());
        Object[] args = new Object[3];
        args[0] = bValueArray;
        args[1] = (testIndex); // index
        args[2] = testValue;
        BArray returns = (BArray) BRunUtil.invoke(result, "testArrayStoreOfHandleValues", args);
        Assert.assertEquals(((HandleValue) returns.get(testIndex)).getValue(), testValue.getValue());
    }

    @Test(description = "Test creation of a handle array")
    public void testCreateArrayOfHandleValues() {
        int testIndex = 3;
        Object[] args = new Object[5];
        args[0] = new HandleValue(UUID.randomUUID());
        args[1] = new HandleValue(UUID.randomUUID());
        args[2] = new HandleValue(UUID.randomUUID());
        args[3] = new HandleValue(UUID.randomUUID());
        args[4] = (testIndex);
        Object returns = BRunUtil.invoke(result, "testCreateArrayOfHandleValues", args);
        Assert.assertEquals(((HandleValue) returns).getValue(), ((HandleValue) args[testIndex]).getValue());
    }

    @Test(description = "Test creation of a handle map")
    public void testCreateMapOfHandleValues() {
        Object[] args = new Object[4];
        args[0] = new HandleValue(UUID.randomUUID());
        args[1] = new HandleValue(UUID.randomUUID());
        args[2] = new HandleValue(UUID.randomUUID());
        args[3] = new HandleValue(UUID.randomUUID());
        Object returns = BRunUtil.invoke(result, "testCreateMapOfHandleValues", args);
        Assert.assertEquals(((HandleValue) returns).getValue(),
                ((HandleValue) args[1]).getValue());
    }

    @Test(description = "Test creation of a record")
    public void testCreateRecordOfHandleValues() {
        Object[] args = new Object[2];
        args[0] = new HandleValue(UUID.randomUUID());
        args[1] = new HandleValue(UUID.randomUUID());
        Object returns = BRunUtil.invoke(result, "testCreateRecordWithHandleValues", args);
        Assert.assertEquals(((HandleValue) returns).getValue(),
                ((HandleValue) args[1]).getValue());
    }

    @Test(description = "Test creation of a tuple with handle values")
    public void testCreateTuplesWithHandleValues() {
        Object[] args = new Object[2];
        args[0] = new HandleValue(UUID.randomUUID());
        args[1] = new HandleValue(UUID.randomUUID());
        Object returns = BRunUtil.invoke(result, "testCreateTuplesWithHandleValues", args);
        Assert.assertEquals(((HandleValue) returns).getValue(),
                ((HandleValue) args[0]).getValue());
    }

    @Test(description = "Test creation of an object with handle values")
    public void testCreateObjectWithHandleValues() {
        Object[] args = new Object[2];
        args[0] = new HandleValue(UUID.randomUUID());
        args[1] = new HandleValue(UUID.randomUUID());
        Object returns = BRunUtil.invoke(result, "testCreateObjectWithHandleValues", args);
        Assert.assertEquals(((HandleValue) returns).getValue(),
                ((HandleValue) args[1]).getValue());
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
