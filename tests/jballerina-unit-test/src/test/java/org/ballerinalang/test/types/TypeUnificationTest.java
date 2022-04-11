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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class TypeUnificationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/map-struct-json-unified.bal");
    }

    @Test(description = "Test inline initializing of a struct and its fields")
    public void testMultiValuedStructInlineInit() {
        Object returns = BRunUtil.invoke(compileResult, "testMultiValuedStructInlineInit");

        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> person = ((BMap<String, Object>) returns);

        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "aaa");
        Assert.assertEquals((person.get(StringUtils.fromString("age"))), 25L);

        // check inner struct
        Assert.assertTrue(person.get(StringUtils.fromString("parent")) instanceof BMap);
        BMap<String, Object> parent = ((BMap<String, Object>) person.get(StringUtils.fromString("parent")));
        Assert.assertEquals(parent.get(StringUtils.fromString("name")).toString(), "bbb");
        Assert.assertEquals((parent.get(StringUtils.fromString("age"))), 50L);

        // check inner json
        Assert.assertTrue(person.get(StringUtils.fromString("info")) instanceof BMap);
        Assert.assertEquals(person.get(StringUtils.fromString("info")).toString(), "{\"status\":\"single\"}");

        // check inner map
        Assert.assertTrue(person.get(StringUtils.fromString("address")) instanceof BMap);
        BMap<String, ?> address = ((BMap<String, ?>) person.get(StringUtils.fromString("address")));
        Assert.assertEquals(address.get(StringUtils.fromString("city")).toString(), "Colombo");
        Assert.assertEquals(address.get(StringUtils.fromString("country")).toString(), "SriLanka");
    }

    @Test
    public void testAccessJsonInStruct() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAccessJsonInStruct");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "married");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "married");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "married");
    }

    @Test
    public void testAccessMapInStruct() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAccessMapInStruct");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Colombo");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Colombo");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Colombo");

        Assert.assertTrue(returns.get(3) instanceof BString);
        Assert.assertEquals(returns.get(3).toString(), "Colombo");
    }

    @Test
    public void testAccessArrayInStruct() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAccessArrayInStruct");

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 94L);

        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 72L);
    }

    @Test
    public void testSetValueToJsonInStruct() {
        Object returns = BRunUtil.invoke(compileResult, "testSetValueToJsonInStruct");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"status\":\"widowed\",\"retired\":true}");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
