/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.object;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.values.ObjectValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Function Arguments in Objects with Default Expressions.
 *
 * @since 0.995.0
 */
public class ObjectFunctionsWithDefaultableArguments {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/object/object_functions_with_default_parameters.bal");
    }

    @Test(description = "Test object function method with default values")
    public void testObjectInitWithDefaultValues() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectInitWithDefaultValues");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "default");
        Assert.assertTrue((Boolean) bValueArray.getRefValue(1));
        Assert.assertEquals(bValueArray.getRefValue(2), 100L);
        Assert.assertEquals(bValueArray.getRefValue(3), 1.1);

        BMap<String, Object> record = (BMap) bValueArray.getRefValue(4);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "default");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 50L);
        Assert.assertFalse((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 11.1);

        ObjectValue object = (ObjectValue) bValueArray.getRefValue(5);
        Assert.assertEquals(object.get(StringUtils.fromString("a")).toString(), "def");
        Assert.assertEquals(object.get(StringUtils.fromString("b")), 200L);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "given");
        Assert.assertFalse((Boolean) bValueArray.getRefValue(1));
        Assert.assertEquals(bValueArray.getRefValue(2), 99L);
        Assert.assertEquals(bValueArray.getRefValue(3), 1.1);

        record = (BMap) bValueArray.getRefValue(4);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "given2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 49L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 10.9);

        object = (ObjectValue) bValueArray.getRefValue(5);
        Assert.assertEquals(object.get(StringUtils.fromString("a")).toString(), "def2");
        Assert.assertEquals(object.get(StringUtils.fromString("b")), 199L);

    }

    @Test(description = "Test object init function with default values 2")
    public void testObjectInitWithDefaultValues2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectInitWithDefaultValues2");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 205L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "defdefault");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        BMap<String, Object> record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "default2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 150L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 33.3);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 10L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "defdefault");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "given");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 49L);
        Assert.assertFalse((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 10.9);

    }

    @Test(description = "Test object attached functions with default values")
    public void testObjectAttachedFunction1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectAttachedFunction1");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "global");
        Assert.assertEquals(bValueArray.getRefValue(1), 200L);

        BMap<String, Object> record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "default");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 50L);
        Assert.assertFalse((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 11.1);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "given");
        Assert.assertEquals(bValueArray.getRefValue(1), 200L);

        record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "given2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 140L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 22.2);
    }

    @Test(description = "Test object attached functions with default values 2")
    public void testObjectAttachedFunction2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectAttachedFunction2");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 210L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "defdefaultglobal");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        BMap<String, Object> record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "default2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 150L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 33.3);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 210L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "given");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "given2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 140L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 22.2);
    }

    @Test(description = "Test object attached functions with default values 3")
    public void testObjectAttachedFunction3() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectAttachedFunction3");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "global");
        Assert.assertEquals(bValueArray.getRefValue(1), 200L);

        BMap<String, Object> record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "default");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 50L);
        Assert.assertFalse((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 11.1);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "given");
        Assert.assertEquals(bValueArray.getRefValue(1), 200L);

        record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "given2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 140L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 22.2);
    }

    @Test(description = "Test object attached functions with default values 4")
    public void testObjectAttachedFunction4() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectAttachedFunction4");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 210L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "defdefaultglobal");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        BMap<String, Object> record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "default2");
        Assert.assertEquals((record.get(StringUtils.fromString("b"))), 150L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals((record.get(StringUtils.fromString("d"))), 33.3);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 210L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "given");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get(StringUtils.fromString("a")).toString(), "given2");
        Assert.assertEquals(record.get(StringUtils.fromString("b")), 140L);
        Assert.assertTrue((Boolean) record.get(StringUtils.fromString("c")));
        Assert.assertEquals(record.get(StringUtils.fromString("d")), 22.2);
    }

    @Test(description = "Test object casting 1")
    public void testObjectCasting1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectCasting1");
        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 200L);
        Assert.assertEquals(bValueArray.getRefValue(1), 2.2);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 40L);
        Assert.assertEquals(bValueArray.getRefValue(1), 2.2);

        bValueArray = (BArray) returns.get(2);
        Assert.assertEquals(bValueArray.getRefValue(0), 40L);
        Assert.assertEquals(bValueArray.getRefValue(1), 22.2);

        bValueArray = (BArray) returns.get(3);
        Assert.assertEquals(bValueArray.getRefValue(0), 200L);
        Assert.assertEquals(bValueArray.getRefValue(1), 22.2);
    }

    @Test(description = "Test object casting 2")
    public void testObjectCasting12() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectCasting2");
        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 400L);
        Assert.assertEquals(bValueArray.getRefValue(1), 4.4);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 80L);
        Assert.assertEquals(bValueArray.getRefValue(1), 4.4);

        bValueArray = (BArray) returns.get(2);
        Assert.assertEquals(bValueArray.getRefValue(0), 80L);
        Assert.assertEquals(bValueArray.getRefValue(1), 44.4);

        bValueArray = (BArray) returns.get(3);
        Assert.assertEquals(bValueArray.getRefValue(0), 400L);
        Assert.assertEquals(bValueArray.getRefValue(1), 44.4);
    }

    @Test(description = "Negative tests for functions with defaultable parameters")
    public void testFunctionsWithDefaultableParametersNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/object/object_functions_with_default_parameters_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 20, 36);
        BAssertUtil.validateError(result, i++, "self referenced variable 'i'", 28, 33);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 37, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 's'", 37, 53);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 51, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 's'", 51, 53);
        BAssertUtil.validateError(result, i++, "undefined symbol 'u'", 51, 74);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 59, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 71, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 75, 37);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 85, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 88, 37);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 97, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 101, 37);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 106, 45);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 117, 29);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 122, 29);
        BAssertUtil.validateError(result, i++, "undefined symbol 'i'", 123, 29);
        BAssertUtil.validateError(result, i++, "undefined symbol 'self'", 135, 36);
        BAssertUtil.validateError(result, i++, "undefined symbol 'self'", 135, 58);
        BAssertUtil.validateError(result, i++, "undefined symbol 'self'", 135, 84);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test(description = "Test an object constructor when value of default parameter is a local variable. " +
            "Currently fails at BIR generation (#35359)", enabled = false)
    public void testReferLocalVarInObjConstructor() {
        CompileResult result =
                BCompileUtil.compile("test-src/object/object_functions_with_default_parameters_rt_failure.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
