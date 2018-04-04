/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class TypeUnificationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        // Todo - Fix any type issue
        compileResult = BCompileUtil.compile("test-src/types/map-struct-json-unified.bal");
    }

    @Test(description = "Test inline initializing of a struct and its fields")
    public void testMultiValuedStructInlineInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMultiValuedStructInlineInit");

        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct person = ((BStruct) returns[0]);

        Assert.assertEquals(person.getStringField(0), "aaa");
        Assert.assertEquals(person.getIntField(0), 25);

        // check inner struct
        Assert.assertTrue(person.getRefField(0) instanceof BStruct);
        BStruct parent = ((BStruct) person.getRefField(0));
        Assert.assertEquals(parent.getStringField(0), "bbb");
        Assert.assertEquals(parent.getIntField(0), 50);

        // check inner json
        Assert.assertTrue(person.getRefField(1) instanceof BJSON);
        BJSON info = ((BJSON) person.getRefField(1));
        Assert.assertEquals(info.getMessageAsString(), "{\"status\":\"single\"}");

        // check inner map
        Assert.assertTrue(person.getRefField(2) instanceof BMap);
        BMap<String, ?> address = ((BMap<String, ?>) person.getRefField(2));
        Assert.assertEquals(address.get("city").stringValue(), "Colombo");
        Assert.assertEquals(address.get("country").stringValue(), "SriLanka");
    }

    @Test(enabled = false)
    public void testAccessJsonInStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessJsonInStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "married");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "married");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "married");
    }

    @Test
    public void testAccessMapInStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessMapInStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Colombo");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "Colombo");
    }

    @Test
    public void testAccessArrayInStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessArrayInStruct");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 94);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 72);
    }

    @Test
    public void testSetValueToJsonInStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetValueToJsonInStruct");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"status\":\"widowed\",\"retired\":true}");
    }
}
