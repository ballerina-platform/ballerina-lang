/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.typemappers;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NativeTypeMappersTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/typeMapperTest.bal");
    }

    @Test
    public void testXMLToJSON() {
        BValue[] args = {new BXML("<name>chanaka</name>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "xmltojson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJSONToXML() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "jsontoxml", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        final String expected = "<jsonObject><name>chanaka</name></jsonObject>";
        Assert.assertEquals(returns[0].stringValue().replaceAll("\\r|\\n|\\t| ", ""), expected);
    }

    @Test
    public void testStringToJSON() {
        BValue[] args = {new BString("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "stringtojson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToXML() {
        BValue[] args = {new BString("<name>chanaka</name>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "stringtoxml", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        final String expected = "<name>chanaka</name>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testXMLToString() {
        BValue[] args = {new BXML("<name>chanaka</name>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "xmltostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "<name>chanaka</name>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJSONToString() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "jsontostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    
    @Test
    public void testMultiRootedJSONToXML() {
        BValue[] args = { new BJSON("{\"name\":\"chanaka\", \"company\":\"wso2\"}") };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "jsontoxml", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        final String expected = "<jsonObject><name>chanaka</name><company>wso2</company></jsonObject>";
        Assert.assertEquals(returns[0].stringValue().replaceAll("\\r|\\n|\\t| ", ""), expected);
    }

}
