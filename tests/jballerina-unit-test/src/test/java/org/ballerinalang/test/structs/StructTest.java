/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.structs;

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
 * Test cases for user defined struct types in ballerina.
 */
public class StructTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/structs/struct.bal");
    }

    @Test(description = "Test Basic struct operations")
    public void testBasicStruct() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testCreateStruct");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Jack");

        Assert.assertTrue(returns.get(1) instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns.get(1));
        Assert.assertEquals(adrsMap.get(StringUtils.fromString("country")), StringUtils.fromString("USA"));
        Assert.assertEquals(adrsMap.get(StringUtils.fromString("state")), StringUtils.fromString("CA"));

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 25L);
    }

    @Test(description = "Test using expressions as index for struct arrays")
    public void testExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Jane");
    }
    
/*    @Test(description = "Test struct operations inside a connector")
    public void testStructInConnector() {
        Object returns = Functions.invoke(bLangProgram, "testAction1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Jack");
    }*/

    @Test(description = "Test using structs inside structs")
    public void testStructOfStructs() {
        Object returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "USA");
    }

    @Test(description = "Test returning fields of a struct")
    public void testReturnStructAttributes() {
        Object returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test using struct expression as a index in another struct expression")
    public void testStructExpressionAsIndex() {
        Object returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "emily");
    }

    @Test(description = "Test default value of a struct field")
    public void testDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDefaultVal");

        // Check default value of a field where the default value is set
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedFieldDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "default first name");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Smith");

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 999L);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedStructInit() {
        Object returns = BRunUtil.invoke(compileResult, "testNestedStructInit");

        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> person = ((BMap<String, Object>) returns);
        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "aaa");
        Assert.assertEquals((person.get(StringUtils.fromString("age"))), 25L);

        Assert.assertTrue(person.get(StringUtils.fromString("parent")) instanceof BMap);
        BMap<String, Object> parent = ((BMap<String, Object>) person.get(StringUtils.fromString("parent")));
        Assert.assertEquals(parent.get(StringUtils.fromString("name")).toString(), "bbb");
        Assert.assertEquals((parent.get(StringUtils.fromString("age"))), 50L);
    }

    @Test(description = "Test negative default values in struct")
    public void testNegativeDefaultValue() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getStructNegativeValues");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        Assert.assertSame(returns.get(2).getClass(), Double.class);
        Assert.assertSame(returns.get(3).getClass(), Double.class);
        Assert.assertEquals(returns.get(0), -9L);
        Assert.assertEquals(returns.get(1), -8L);
        Assert.assertEquals(returns.get(2), -88.234);
        Assert.assertEquals(returns.get(3), -24.99);
    }

    @Test(description = "Test negative default values in struct")
    public void testStructToString() {
        Object returns = BRunUtil.invoke(compileResult, "getStruct");
        Assert.assertEquals(returns.toString(), "{\"name\":\"aaa\",\"lname\":\"\",\"adrs\":{},\"age\":25," +
                "\"family\":{\"spouse\":\"\",\"noOfChildren\":0,\"children\":[]},\"parent\":{\"name\":\"bbb\"," +
                "\"lname\":\"ccc\",\"adrs\":{},\"age\":50,\"family\":{\"spouse\":\"\",\"noOfChildren\":0," +
                "\"children\":[]},\"parent\":null}}");
    }

    @Test
    public void testStructLiteral() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/structs/ObjectWithPrivateFieldsTestProject/struct-literals.bal");
        Object returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns.toString(), "{\"dptName\":\"\",\"employees\":[],\"manager\":{\"name\":\"default " +
                "first name\",\"lname\":\"\",\"adrs\":{},\"age\":999,\"child\":null}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"default first name\",\"lname\":\"\",\"adrs\":{},\"age\":999,\"child\":null}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile(
                "test-src/structs/ObjectWithPrivateFieldsTestProject/nested-struct-inline-init.bal");
        Object returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"default first name\",\"fname\":\"\",\"lname\":\"Doe\",\"adrs\":{},\"age\":999," +
                        "\"family\":{\"spouse\":\"Jane\",\"noOfChildren\":0,\"children\":[\"Alex\",\"Bob\"]}}");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
