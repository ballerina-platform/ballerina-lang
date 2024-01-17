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
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "mapInitTest", args);

        Assert.assertTrue(returns instanceof BMap);

        BMap<String, BString> mapValue = (BMap<String, BString>) returns;
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("animal1")).toString(), "Lion");
        Assert.assertEquals(mapValue.get(StringUtils.fromString("animal2")).toString(), "Cat");
        Assert.assertEquals(mapValue.get(StringUtils.fromString("animal4")).toString(), "Dog");
    }

    @Test(description = "Test map initializing with different types")
    public void testMultiTypeMapInit() {
        BCompileUtil.compile("test-src/types/map/multi-type-map-initializer.bal");
    }

    @Test
    public void testNestedMapInit() {
        Object returns = BRunUtil.invoke(compileResult, "testNestedMapInit", new Object[]{});

        Assert.assertTrue(returns instanceof BMap<?, ?>);
        BMap<String, Object> outerMap = (BMap<String, Object>) returns;
        Object info = outerMap.get(StringUtils.fromString("info"));
        Assert.assertTrue(info instanceof BMap<?, ?>);
        BMap<String, Object> infoMap = (BMap<String, Object>) info;
        Assert.assertEquals(infoMap.get(StringUtils.fromString("city")), StringUtils.fromString("Colombo"));
        Assert.assertEquals(infoMap.get(StringUtils.fromString("country")), StringUtils.fromString("SriLanka"));
    }

    @Test
    public void testMapInitWithJson() {
        Object returns = BRunUtil.invoke(compileResult, "testMapInitWithJson", new Object[]{});

        Assert.assertTrue(returns instanceof BMap<?, ?>);
        BMap<String, Object> outerMap = (BMap<String, Object>) returns;
        Assert.assertEquals(outerMap.get(StringUtils.fromString("name")), StringUtils.fromString("Supun"));

        Object info = outerMap.get(StringUtils.fromString("info"));
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.toString(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }

    @Test
    public void testComplexMapInit() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexMapInit", new Object[]{});

        Assert.assertTrue(returns instanceof BMap<?, ?>);
        BMap<String, Object> outerMap = (BMap<String, Object>) returns;
        Assert.assertEquals(outerMap.get(StringUtils.fromString("name")).toString(), "Supun");

        Object adrsArray = outerMap.get(StringUtils.fromString("addressArray"));
        Assert.assertTrue(adrsArray instanceof BArray);
        BArray addressArray = (BArray) adrsArray;

        Object adrs1 = addressArray.getRefValue(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        Object address = ((BMap) adrs1).get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(StringUtils.fromString("city")).toString(), "Colombo");

        Object adrs2 = addressArray.getRefValue(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(StringUtils.fromString("city")).toString(), "Kandy");

        Object adrs3 = addressArray.getRefValue(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(StringUtils.fromString("city")).toString(), "Galle");
    }

    @Test()
    public void testMapInitWithPackageVars() {
        CompileResult result = BCompileUtil.compile("test-src/types/map/MapAccessProject");
        Object returns = BRunUtil.invoke(result, "testMapInitWithPackageVars");

        Assert.assertTrue(returns instanceof BMap);

        BMap<String, ?> mapValue = (BMap<String, BString>) returns;
        Assert.assertEquals(mapValue.size(), 2);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "PI");
        Assert.assertEquals((mapValue.get(StringUtils.fromString("value"))), 3.14159);
    }

    @Test
    public void testMapInitWithStringTemplateAsKey() {
        CompileResult result = BCompileUtil.compile("test-src/types/map/map-initializer-with-string-template.bal");
        Object returns = BRunUtil.invoke(result, "testMapInitWithStringTemplateAsKey");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, BString> mapValue = (BMap<String, BString>) returns;
        Assert.assertEquals(mapValue.get(StringUtils.fromString("firstname")).toString(), "John");
    }

    @Test(description = "Test map initializer expression")
    public void mapInitWithIdentifiersTest() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "mapInitWithIdentifiersTest", args);

        Assert.assertTrue(returns instanceof BMap);

        BMap<String, BString> mapValue = (BMap<String, BString>) returns;
        Assert.assertEquals(mapValue.size(), 3);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("a")).toString(), "Lion");
        Assert.assertEquals(mapValue.get(StringUtils.fromString("key1")).toString(), "Cat");
        Assert.assertEquals(mapValue.get(StringUtils.fromString("key2")).toString(), "Dog");
    }

    @Test
    public void testEmptyMap() {
        Object returns = BRunUtil.invoke(compileResult, "testEmptyMap", new Object[]{});

        Assert.assertTrue(returns instanceof BMap<?, ?>, "empty map initialization with {}");
        Assert.assertEquals(((BMap) returns).size(), 0, "incorrect empty map size");
    }

    @Test
    public void testExpressionsAsKeys() {
        Object returns = BRunUtil.invoke(compileResult, "testExpressionAsKeys");
        Assert.assertTrue((Boolean) returns);

        returns = BRunUtil.invoke(compileResult, "testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames");
        Assert.assertTrue((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
