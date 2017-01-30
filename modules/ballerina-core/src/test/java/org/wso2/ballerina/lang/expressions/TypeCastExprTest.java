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
package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class TypeCastExprTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.convertors:_xml->_json")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("lang/expressions/type-conversion.bal", symScope);
    }

    @Test
    public void testXMLToJSON() {
        BValue[] args = {new BXML("<name>chanaka</name>")};
        BValue[] returns = Functions.invoke(bFile, "xmltojson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJSONToXML() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = Functions.invoke(bFile, "jsontoxml", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        final String expected = "<name>chanaka</name>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testDoubleToFloat() {
        BValue[] args = {new BDouble(222222.44444d)};
        BValue[] returns = Functions.invoke(bFile, "doubletofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "222222.44";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testDoubleToLong() {
        BValue[] args = {new BDouble(222222.44444d)};
        BValue[] returns = Functions.invoke(bFile, "doubletolong", args);
        Assert.assertTrue(returns[0] instanceof BLong);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testDoubleToInt() {
        BValue[] args = {new BDouble(21474836471.44444d)};
        BValue[] returns = Functions.invoke(bFile, "doubletoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "2147483647";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToLong() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = Functions.invoke(bFile, "floattolong", args);
        Assert.assertTrue(returns[0] instanceof BLong);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToInt() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = Functions.invoke(bFile, "floattoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLongToInt() {
        BValue[] args = {new BLong(2147483647L)};
        BValue[] returns = Functions.invoke(bFile, "longtoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "2147483647";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToLong() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = Functions.invoke(bFile, "inttolong", args);
        Assert.assertTrue(returns[0] instanceof BLong);
        final String expected = "55555555";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToFloat() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = Functions.invoke(bFile, "inttofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "5.5555556E7";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToDouble() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = Functions.invoke(bFile, "inttodouble", args);
        Assert.assertTrue(returns[0] instanceof BDouble);
        final String expected = "5.5555555E7";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLongToFloat() {
        BValue[] args = {new BLong(55555555L)};
        BValue[] returns = Functions.invoke(bFile, "longtofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "5.5555556E7";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLongToDouble() {
        BValue[] args = {new BLong(55555555L)};
        BValue[] returns = Functions.invoke(bFile, "longtodouble", args);
        Assert.assertTrue(returns[0] instanceof BDouble);
        final String expected = "5.5555555E7";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToDouble() {
        BValue[] args = {new BFloat(22222.333f)};
        BValue[] returns = Functions.invoke(bFile, "floattodouble", args);
        Assert.assertTrue(returns[0] instanceof BDouble);
        final String expected = "22222.33203125";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToInt() {
        BValue[] args = {new BString("100")};
        BValue[] returns = Functions.invoke(bFile, "stringtoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "100";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToLong() {
        BValue[] args = {new BString("214748364777")};
        BValue[] returns = Functions.invoke(bFile, "stringtolong", args);
        Assert.assertTrue(returns[0] instanceof BLong);
        final String expected = "214748364777";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToFloat() {
        BValue[] args = {new BString("2222.333f")};
        BValue[] returns = Functions.invoke(bFile, "stringtofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "2222.333";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToDouble() {
        BValue[] args = {new BString("4444.333d")};
        BValue[] returns = Functions.invoke(bFile, "stringtodouble", args);
        Assert.assertTrue(returns[0] instanceof BDouble);
        final String expected = "4444.333";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToJSON() {
        BValue[] args = {new BString("{\"name\":\"chanaka\"}")};
        BValue[] returns = Functions.invoke(bFile, "stringtojson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToXML() {
        BValue[] args = {new BString("<name>chanaka</name>")};
        BValue[] returns = Functions.invoke(bFile, "stringtoxml", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        final String expected = "<name>chanaka</name>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToString() {
        BValue[] args = {new BInteger(111)};
        BValue[] returns = Functions.invoke(bFile, "inttostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLongToString() {
        BValue[] args = {new BLong(214748364777L)};
        BValue[] returns = Functions.invoke(bFile, "longtostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "214748364777";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToString() {
        BValue[] args = {new BFloat(111.333f)};
        BValue[] returns = Functions.invoke(bFile, "floattostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testDoubleToString() {
        BValue[] args = {new BDouble(111.333d)};
        BValue[] returns = Functions.invoke(bFile, "doubletostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testXMLToString() {
        BValue[] args = {new BXML("<name>chanaka</name>")};
        BValue[] returns = Functions.invoke(bFile, "xmltostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "<name>chanaka</name>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJSONToString() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = Functions.invoke(bFile, "jsontostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntArrayToLongArray() {
        BValue[] returns = Functions.invoke(bFile, "intarrtolongarr");
        Assert.assertTrue(returns[0] instanceof BArray);
        BArray result = (BArray) returns[0];
        Assert.assertTrue(result.get(0) instanceof BLong);
        Assert.assertEquals(result.get(0).stringValue(), "999");
        Assert.assertTrue(result.get(1) instanceof BLong);
        Assert.assertEquals(result.get(1).stringValue(), "95");
        Assert.assertTrue(result.get(2) instanceof BLong);
        Assert.assertEquals(result.get(2).stringValue(), "889");
    }

    @Test
    public void testFloatArrayToDoubleArray() {
        BValue[] returns = Functions.invoke(bFile, "floatarrtodoublearr");
        Assert.assertTrue(returns[0] instanceof BArray);
        BArray result = (BArray) returns[0];
        Assert.assertTrue(result.get(0) instanceof BDouble);
        Assert.assertEquals(result.get(0).stringValue(), "99.98999786376953");
        Assert.assertTrue(result.get(1) instanceof BDouble);
        Assert.assertEquals(result.get(1).stringValue(), "4.5");
        Assert.assertTrue(result.get(2) instanceof BDouble);
        Assert.assertEquals(result.get(2).stringValue(), "23.559999465942383");
    }
}
