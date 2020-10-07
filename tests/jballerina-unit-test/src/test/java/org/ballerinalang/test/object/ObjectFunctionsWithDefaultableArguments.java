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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(result, "testObjectInitWithDefaultValues");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "default");
        Assert.assertTrue(((BBoolean) bValueArray.getRefValue(1)).booleanValue());
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(2)).intValue(), 100);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(3)).floatValue(), 1.1);

        BMap<String, BValue> record = (BMap) bValueArray.getRefValue(4);
        Assert.assertEquals(record.get("a").stringValue(), "default");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 50);
        Assert.assertFalse(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 11.1);

        BMap<String, BValue> object = (BMap) bValueArray.getRefValue(5);
        Assert.assertEquals(object.get("a").stringValue(), "def");
        Assert.assertEquals(((BInteger) object.get("b")).intValue(), 200);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "given");
        Assert.assertFalse(((BBoolean) bValueArray.getRefValue(1)).booleanValue());
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(2)).intValue(), 99);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(3)).floatValue(), 1.1);

        record = (BMap) bValueArray.getRefValue(4);
        Assert.assertEquals(record.get("a").stringValue(), "given2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 49);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 10.9);

        object = (BMap) bValueArray.getRefValue(5);
        Assert.assertEquals(object.get("a").stringValue(), "def2");
        Assert.assertEquals(((BInteger) object.get("b")).intValue(), 199);

    }

    @Test(description = "Test object init function with default values 2")
    public void testObjectInitWithDefaultValues2() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectInitWithDefaultValues2");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 205);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "defdefault");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        BMap<String, BValue> record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get("a").stringValue(), "default2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 150);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 33.3);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 10);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "defdefault");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get("a").stringValue(), "given");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 49);
        Assert.assertFalse(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 10.9);

    }

    @Test(description = "Test object attached functions with default values")
    public void testObjectAttachedFunction1() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectAttachedFunction1");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "global");
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(1)).intValue(), 200);

        BMap<String, BValue> record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get("a").stringValue(), "default");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 50);
        Assert.assertFalse(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 11.1);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "given");
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(1)).intValue(), 200);

        record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get("a").stringValue(), "given2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 140);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 22.2);
    }

    @Test(description = "Test object attached functions with default values 2")
    public void testObjectAttachedFunction2() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectAttachedFunction2");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 210);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "defdefaultglobal");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        BMap<String, BValue> record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get("a").stringValue(), "default2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 150);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 33.3);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 210);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "given");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get("a").stringValue(), "given2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 140);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 22.2);
    }

    @Test(description = "Test object attached functions with default values 3")
    public void testObjectAttachedFunction3() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectAttachedFunction3");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "global");
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(1)).intValue(), 200);

        BMap<String, BValue> record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get("a").stringValue(), "default");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 50);
        Assert.assertFalse(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 11.1);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "given");
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(1)).intValue(), 200);

        record = (BMap) bValueArray.getRefValue(2);
        Assert.assertEquals(record.get("a").stringValue(), "given2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 140);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 22.2);
    }

    @Test(description = "Test object attached functions with default values 4")
    public void testObjectAttachedFunction4() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectAttachedFunction4");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 210);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "defdefaultglobal");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        BMap<String, BValue> record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get("a").stringValue(), "default2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 150);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 33.3);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 210);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "given");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        record = (BMap) bValueArray.getRefValue(3);
        Assert.assertEquals(record.get("a").stringValue(), "given2");
        Assert.assertEquals(((BInteger) record.get("b")).intValue(), 140);
        Assert.assertTrue(((BBoolean) record.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) record.get("d")).floatValue(), 22.2);
    }

    @Test(description = "Test object casting 1")
    public void testObjectCasting1() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectCasting1");
        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 200);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 2.2);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 40);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 2.2);

        bValueArray = (BValueArray) returns[2];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 40);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 22.2);

        bValueArray = (BValueArray) returns[3];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 200);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 22.2);
    }

    @Test(description = "Test object casting 2")
    public void testObjectCasting12() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectCasting2");
        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 400);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 4.4);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 80);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 4.4);

        bValueArray = (BValueArray) returns[2];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 80);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 44.4);

        bValueArray = (BValueArray) returns[3];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 400);
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(1)).floatValue(), 44.4);
    }
}
