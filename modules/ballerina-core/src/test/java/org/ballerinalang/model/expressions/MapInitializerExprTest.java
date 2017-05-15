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
package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test map initializer expression.
 *
 * @since 0.8.0
 */
public class MapInitializerExprTest {

    private BLangProgram bLangProgram;

    @BeforeTest
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/map-initializer-expr.bal");
    }

    @Test(description = "Test map initializer expression")
    public void testMapInitExpr() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "mapInitTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<BString, BString> mapValue = (BMap) returns[0];
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.get(new BString("animal1")).stringValue(), "Lion");
        Assert.assertEquals(mapValue.get(new BString("animal2")).stringValue(), "Cat");
        Assert.assertEquals(mapValue.get(new BString("animal4")).stringValue(), "Dog");
    }

    @Test(description = "Test map initializing with different types")
    public void testMultiTypeMapInit() {
        BTestUtils.parseBalFile("lang/expressions/multi-type-map-initializer.bal");
    }
    
    @Test
    public void testNestedMapInit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNestedMapInit", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<BString, BValue> outerMap = (BMap<BString, BValue>) returns[0];
        Assert.assertEquals(outerMap.get(new BString("name")), new BString("Supun"));

        BValue info = outerMap.get(new BString("info"));
        Assert.assertTrue(info instanceof BMap<?, ?>);
        BMap<BString, BValue> infoMap = (BMap<BString, BValue>) info;
        Assert.assertEquals(infoMap.get(new BString("city")), new BString("Colombo"));
        Assert.assertEquals(infoMap.get(new BString("country")), new BString("SriLanka"));
    }
    
    @Test
    public void testMapInitWithJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testMapInitWithJson", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<BString, BValue> outerMap = (BMap<BString, BValue>) returns[0];
        Assert.assertEquals(outerMap.get(new BString("name")), new BString("Supun"));

        BValue info = outerMap.get(new BString("info"));
        Assert.assertTrue(info instanceof BJSON);
        BJSON infoJson = (BJSON) info;
        Assert.assertEquals(infoJson.stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }
    
    @Test
    public void testComplexMapInit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testComplexMapInit", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<BString, BValue> outerMap = (BMap<BString, BValue>) returns[0];
        Assert.assertEquals(outerMap.get(new BString("name")).stringValue(), "Supun");

        BValue adrsArray = outerMap.get(new BString("addressArray"));
        Assert.assertTrue(adrsArray instanceof BArray);
        BArray addressArray = (BArray) adrsArray;

        BValue adrs1 = addressArray.get(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        BValue address = ((BMap) adrs1).get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(new BString("city")).stringValue(), "Colombo");

        BValue adrs2 = addressArray.get(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(new BString("city")).stringValue(), "Kandy");

        BValue adrs3 = addressArray.get(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get(new BString("city")).stringValue(), "Galle");
    }
}
