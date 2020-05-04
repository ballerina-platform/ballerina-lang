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

import org.ballerinalang.core.model.values.BFloat;
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns[1]);
        Assert.assertEquals(adrsMap.get("country"), new BString("USA"));
        Assert.assertEquals(adrsMap.get("state"), new BString("CA"));

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 25);
    }

    @Test(description = "Test using expressions as index for struct arrays")
    public void testExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");
    }
    
/*    @Test(description = "Test struct operations inside a connector")
    public void testStructInConnector() {
        BValue[] returns = Functions.invoke(bLangProgram, "testAction1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
    }*/

    @Test(description = "Test using structs inside structs")
    public void testStructOfStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructOfStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "USA");
    }

    @Test(description = "Test returning fields of a struct")
    public void testReturnStructAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnStructAttributes");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test using struct expression as a index in another struct expression")
    public void testStructExpressionAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test default value of a struct field")
    public void testDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultVal");

        // Check default value of a field where the default value is set
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "");

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedFieldDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedFieldDefaultVal");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Smith");

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedStructInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedStructInit");

        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> person = ((BMap<String, BValue>) returns[0]);
        Assert.assertEquals(person.get("name").stringValue(), "aaa");
        Assert.assertEquals(((BInteger) person.get("age")).intValue(), 25);

        Assert.assertTrue(person.get("parent") instanceof BMap);
        BMap<String, BValue> parent = ((BMap<String, BValue>) person.get("parent"));
        Assert.assertEquals(parent.get("name").stringValue(), "bbb");
        Assert.assertEquals(((BInteger) parent.get("age")).intValue(), 50);
    }

    @Test(description = "Test negative default values in struct")
    public void testNegativeDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getStructNegativeValues");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BFloat.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -9);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -8);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), -88.234);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), -24.99);
    }

    @Test(description = "Test negative default values in struct")
    public void testStructToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getStruct");
        Assert.assertEquals(returns[0].stringValue(), "{name:\"aaa\", lname:\"\", adrs:{}, age:25, " +
                "family:{spouse:\"\", noOfChildren:0, children:[]}, parent:{name:\"bbb\", lname:\"ccc\", " +
                "adrs:{}, age:50, family:{spouse:\"\", noOfChildren:0, children:[]}, parent:()}}");
    }

    @Test
    public void testStructLiteral() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/proj/src/default/struct-literals.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructLiteral1");
        Assert.assertEquals(returns[0].stringValue(), "{dptName:\"\", employees:[], manager:" +
                "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:()}}");

        returns = BRunUtil.invoke(compileResult, "testStructLiteral2");
        Assert.assertEquals(returns[0].stringValue(),
                "{name:\"default first name\", lname:\"\", adrs:{}, age:999, child:()}");
    }

    @Test
    public void testStructLiteralInitFunc() {
        CompileResult result = BCompileUtil.compile("test-src/structs/proj/src/default/nested-struct-inline-init.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCreateStruct");
        Assert.assertEquals(returns[0].stringValue(),
                "{name:\"default first name\", fname:\"\", lname:\"Doe\", adrs:{}, age:999, " +
                        "family:{spouse:\"Jane\", noOfChildren:0, children:[\"Alex\", \"Bob\"]}}");
    }
}
