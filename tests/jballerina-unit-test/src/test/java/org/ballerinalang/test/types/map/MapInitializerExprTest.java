/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.map;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test map initializer expression.
 *
 * @since 0.8.0
 */
public class MapInitializerExprTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/map/map-initializer-expr.bal");
    }

    @Test(description = "Test map initializer expression")
    public void testMapInitExpr() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "mapInitTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<String, BString> mapValue = (BMap<String, BString>) returns[0];
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.get("animal1").stringValue(), "Lion");
        Assert.assertEquals(mapValue.get("animal2").stringValue(), "Cat");
        Assert.assertEquals(mapValue.get("animal4").stringValue(), "Dog");
    }

    @Test(description = "Test map initializing with different types")
    public void testMultiTypeMapInit() {
        BCompileUtil.compile("test-src/types/map/multi-type-map-initializer.bal");
    }
    
    @Test
    public void testNestedMapInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedMapInit", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> outerMap = (BMap<String, BValue>) returns[0];
        BValue info = outerMap.get("info");
        Assert.assertTrue(info instanceof BMap<?, ?>);
        BMap<String, BValue> infoMap = (BMap<String, BValue>) info;
        Assert.assertEquals(infoMap.get("city"), new BString("Colombo"));
        Assert.assertEquals(infoMap.get("country"), new BString("SriLanka"));
    }
    
    @Test
    public void testMapInitWithJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapInitWithJson", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> outerMap = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(outerMap.get("name"), new BString("Supun"));

        BValue info = outerMap.get("info");
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.stringValue(), "{\"city\":\"Colombo\", \"country\":\"SriLanka\"}");
    }
    
    @Test
    public void testComplexMapInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexMapInit", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> outerMap = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(outerMap.get("name").stringValue(), "Supun");

        BValue adrsArray = outerMap.get("addressArray");
        Assert.assertTrue(adrsArray instanceof BValueArray);
        BValueArray addressArray = (BValueArray) adrsArray;

        BValue adrs1 = addressArray.getRefValue(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        BValue address = ((BMap) adrs1).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Colombo");

        BValue adrs2 = addressArray.getRefValue(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Kandy");

        BValue adrs3 = addressArray.getRefValue(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Galle");
    }

    @Test()
    public void testMapInitWithPackageVars() {
        CompileResult result = BCompileUtil.compile(this, "test-src/types/map/pkgvars", "a.b");
        BValue[] returns = BRunUtil.invoke(result, "testMapInitWithPackageVars");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<String, ?> mapValue = (BMap<String, BString>) returns[0];
        Assert.assertEquals(mapValue.size(), 2);
        Assert.assertEquals(mapValue.get("name").stringValue(), "PI");
        Assert.assertEquals(((BFloat) mapValue.get("value")).floatValue(), 3.14159);
    }

    @Test
    public void testMapInitWithStringTemplateAsKey() {
        CompileResult result = BCompileUtil.compile("test-src/types/map/map-initializer-with-string-template.bal");
        BValue[] returns = BRunUtil.invoke(result, "testMapInitWithStringTemplateAsKey");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BString> mapValue = (BMap<String, BString>) returns[0];
        Assert.assertEquals(mapValue.get("firstname").stringValue(), "John");
    }

    @Test(description = "Test map initializer expression")
    public void mapInitWithIdentifiersTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "mapInitWithIdentifiersTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<String, BString> mapValue = (BMap<String, BString>) returns[0];
        Assert.assertEquals(mapValue.size(), 3);

        Assert.assertEquals(mapValue.get("a").stringValue(), "Lion");
        Assert.assertEquals(mapValue.get("key1").stringValue(), "Cat");
        Assert.assertEquals(mapValue.get("key2").stringValue(), "Dog");
    }

    @Test
    public void testEmptyMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyMap", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>, "empty map initialization with {}");
        Assert.assertEquals(((BMap) returns[0]).size(), 0, "incorrect empty map size");
    }

    @Test
    public void testExpressionsAsKeys() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpressionAsKeys");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());

        returns = BRunUtil.invoke(compileResult, "testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
