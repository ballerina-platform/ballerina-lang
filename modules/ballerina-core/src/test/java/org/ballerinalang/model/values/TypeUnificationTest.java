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
package org.ballerinalang.model.values;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class TypeUnificationTest   {

    private BLangProgram bLangProgram;
    
    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/values/map-struct-json-unified.bal");
    }
    
    @Test(description = "Test inline initializing of a struct and its fields")
    public void testMultiValuedStructInlineInit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testMultiValuedStructInlineInit");
        
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct person = ((BStruct) returns[0]);
        
        Assert.assertEquals(person.getValue(0).stringValue(), "aaa");
        Assert.assertEquals(((BInteger) person.getValue(1)).intValue(), 25);
        
        // check inner struct
        Assert.assertTrue(person.getValue(2) instanceof BStruct);
        BStruct parent = ((BStruct) person.getValue(2));
        Assert.assertEquals(parent.getValue(0).stringValue(), "bbb");
        Assert.assertEquals(((BInteger) parent.getValue(1)).intValue(), 50);
        
        // check inner json
        Assert.assertTrue(person.getValue(3) instanceof BJSON);
        BJSON info = ((BJSON) person.getValue(3));
        Assert.assertEquals(info.getMessageAsString(), "{\"status\":\"single\"}");
        
        // check inner map
        Assert.assertTrue(person.getValue(4) instanceof BMap);
        BMap<BString, ?> address = ((BMap<BString, ?>) person.getValue(4));
        Assert.assertEquals(address.get(new BString("city")).stringValue(), "Colombo");
        Assert.assertEquals(address.get(new BString("country")).stringValue(), "SriLanka");
    }
    
    @Test
    public void testAccessJsonInStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAccessJsonInStruct");
        
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "married");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "married");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "married");
        
        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "married");
    }
    
    @Test
    public void testAccessMapInStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAccessMapInStruct");
        
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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAccessArrayInStruct");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 94);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 72);
    }
    
    @Test
    public void testMapInitWithAnyType() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testMapInitWithAnyType");

        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(((BMap<BString, BString>) returns[0]).get(new BString("name")).stringValue(), "Supun");

        Assert.assertTrue(returns[1] instanceof BMap);
        Assert.assertEquals(((BMap<BString, BString>) returns[1]).get(new BString("name")).stringValue(), "Supun");

        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertEquals(returns[2].stringValue(), "{\"name\":\"Supun\"}");
    }
    
    @Test
    public void testSetValueToJsonInStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSetValueToJsonInStruct");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"status\":\"widowed\",\"retired\":true}");
    }
}
