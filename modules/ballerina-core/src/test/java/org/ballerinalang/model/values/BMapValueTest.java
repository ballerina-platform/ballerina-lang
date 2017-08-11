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
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Test class for ballerina map.
 */
public class BMapValueTest   {

    private ProgramFile programFile;

    @Test
    public void testStandardJavaMap() {
        // Standard Map
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("Chanaka", 1);
        map.put("Udaya", 2);
        map.put("Chanaka", 1);
        assertEquals((int) map.get("Chanaka"), 1);

        for (int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), i);
        }
        assertEquals(map.size(), 102);
        assertEquals((int) map.get("51"), 51);

        map.remove("Chanaka");
        assertEquals(map.size(), 101);

        map.remove("WSO2");
        assertEquals(map.size(), 101);
    }

    @Test
    public void testBMap() {

        BMap<BString, BInteger> map = new BMap<>();
        map.put(new BString("Chanaka"), new BInteger(1));
        map.put(new BString("Udaya"), new BInteger(2));
        map.put(new BString("Chanaka"), new BInteger(1));
        assertEquals(map.get(new BString("Chanaka")), new BInteger(1));
        for (int i = 0; i < 100; i++) {
            map.put(new BString(String.valueOf(i)), new BInteger(i));
        }
        assertEquals(map.size(), 102);
        assertEquals(map.get(new BString("51")), new BInteger(51));

        map.remove(new BString("Chanaka"));
        assertEquals(map.size(), 101);

        map.remove(new BString("Chanaka"));
        assertEquals(map.size(), 101);
    }

    @Test
    void testGrammar() {
        programFile = BTestUtils.getProgramFile("lang/values/map-value.bal");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAny() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithAny", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(returnVals[0].stringValue(), "Lion", "Return value din't match. Expected Lion");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithMap() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithMap", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(returnVals[0].stringValue(), "item1", "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAnyValue() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithAnyValue", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 5, "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAnyDifferentValue() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithAnyDifferentValue", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(returnVals[0].stringValue(), "aString", "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithBinaryExpression() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithBinaryExpression", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithFunctionInvocations() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithFunctionInvocations", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(returnVals[0].stringValue(), "item1", "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAnyFunctionInvocations() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMapWithAnyFunctionInvocations", new BValue[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.length, 1, "Return value size din't match.");
        Assert.assertNotNull(returnVals[0], "Return value can't be null.");
        Assert.assertEquals(returnVals[0].stringValue(), "item2", "Return value din't match.");
    }

    @Test(description = "Testing map value access in variableDefStmt", expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'any' cannot be assigned to 'string'")
    void testInvalidGrammar1() {
        BTestUtils.getProgramFile("lang/values/map-value-invalid1.bal");
    }

    @Test(description = "Testing map value access in assignStmt", expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'any' cannot be assigned to 'string'")
    void testInvalidGrammar2() {
        BTestUtils.getProgramFile("lang/values/map-value-invalid2.bal");
    }

    @Test(description = "Testing map value access in binary expression", expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = "map-value-invalid3.bal:3: invalid operation: incompatible " +
                    "types 'any' and 'int'")
    void testInvalidGrammar3() {
        BTestUtils.getProgramFile("lang/values/map-value-invalid3.bal");
    }
    
    @Test
    public void testBMapToString() {
        BMap<String, BRefType> map = new BMap<>();
        map.put(new String("key1"), new BInteger(1));
        map.put(new String("key2"), new BString("foo"));
        map.put(new String("key3"), new BXMLItem("<bar>hello</bar>"));
        
        Assert.assertEquals(map.stringValue(), "{\"key1\":1, \"key2\":\"foo\", \"key3\":<bar>hello</bar>}");
    }
}
