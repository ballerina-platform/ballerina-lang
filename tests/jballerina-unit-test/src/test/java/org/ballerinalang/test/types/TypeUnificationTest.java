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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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

        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> person = ((BMap<String, BValue>) returns[0]);

        Assert.assertEquals(person.get("name").stringValue(), "aaa");
        Assert.assertEquals(((BInteger) person.get("age")).intValue(), 25);

        // check inner struct
        Assert.assertTrue(person.get("parent") instanceof BMap);
        BMap<String, BValue> parent = ((BMap<String, BValue>) person.get("parent"));
        Assert.assertEquals(parent.get("name").stringValue(), "bbb");
        Assert.assertEquals(((BInteger) parent.get("age")).intValue(), 50);

        // check inner json
        Assert.assertTrue(person.get("info") instanceof BMap);
        Assert.assertEquals(person.get("info").stringValue(), "{\"status\":\"single\"}");

        // check inner map
        Assert.assertTrue(person.get("address") instanceof BMap);
        BMap<String, ?> address = ((BMap<String, ?>) person.get("address"));
        Assert.assertEquals(address.get("city").stringValue(), "Colombo");
        Assert.assertEquals(address.get("country").stringValue(), "SriLanka");
    }

    @Test
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
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"status\":\"widowed\", \"retired\":true}");
    }
}
