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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test class for ballerina map.
 */
public class BMapValueTest {

    private CompileResult programFile;

    @Test
    public void testBMap() {

        BMap map = ValueCreator.createMapValue();
        map.put(StringUtils.fromString("Chanaka"), 1L);
        map.put(StringUtils.fromString("Udaya"), 2L);
        map.put(StringUtils.fromString("Chanaka"), 1L);
        assertEquals(map.get(StringUtils.fromString("Chanaka")), 1L);
        for (int i = 0; i < 100; i++) {
            map.put(StringUtils.fromString(String.valueOf(i)), (long) i);
        }
        assertEquals(map.size(), 102);
        assertEquals(map.get(StringUtils.fromString("51")), 51L);

        map.remove(StringUtils.fromString("Chanaka"));
        assertEquals(map.size(), 101);

        map.remove(StringUtils.fromString("Chanaka"));
        assertEquals(map.size(), 101);
    }

    @Test
    public void testBMapClear() {
        BMap map = ValueCreator.createMapValue();
        map.put(StringUtils.fromString("IS"), 0L);
        map.put(StringUtils.fromString("ESB"), 1L);
        map.put(StringUtils.fromString("APIM"), 2L);
        assertEquals(map.size(), 3);
        map.clear();
        assertEquals(map.size(), 0);
    }

    @Test
    public void testBMapHasKey() {
        BMap map = ValueCreator.createMapValue();
        map.put(StringUtils.fromString("IS"), (0));
        map.put(StringUtils.fromString("ESB"), (1));
        map.put(StringUtils.fromString("APIM"), (2));
        assertTrue(map.containsKey(StringUtils.fromString("IS")));
        assertTrue(map.containsKey(StringUtils.fromString("ESB")));
        assertTrue(map.containsKey(StringUtils.fromString("APIM")));
    }

    @Test
    void testGrammar() {
        programFile = BCompileUtil.compile("test-src/types/map/map-value.bal");
    }

    @Test
    public void testUpdateMapValue() {
        BRunUtil.invoke(programFile, "testUpdateMapValue");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAny() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithAny", new Object[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.toString(), "Lion", "Return value din't match. Expected Lion");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithMap() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithMap", new Object[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.toString(), "item1", "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAnyValue() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithAnyValue", new Object[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals, 5L, "Return value didn't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAnyDifferentValue() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithAnyDifferentValue", new Object[0]);
        Assert.assertNotNull(returnVals, "Return value can't be null.");
        Assert.assertEquals(returnVals.toString(), "aString", "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithBinaryExpression() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithBinaryExpression", new Object[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals, 3L, "Return value didn't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithFunctionInvocations() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithFunctionInvocations", new Object[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.toString(), "item1", "Return value din't match.");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapWithAnyFunctionInvocations() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapWithAnyFunctionInvocations", new Object[0]);
        Assert.assertNotNull(returnVals, "Return values can't be null.");
        Assert.assertEquals(returnVals.toString(), "item2", "Return value din't match.");
    }

    @Test(description = "Testing map value access in variableDefStmt")
    void testInvalidGrammar1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/map/map-value-validator-1-negative.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].message(),
                "incompatible types: expected 'string', found 'any'");
    }

    @Test(description = "Testing map value access in assignStmt")
    void testInvalidGrammar2() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/map/map-value-validator-2-negative.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].message(),
                "incompatible types: expected 'string', found 'any'");
    }

    @Test(description = "Testing map value access in binary expression")
    void testInvalidGrammar3() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/map/map-value-validator-3-negative.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].message(),
                "operator '+' not defined for 'any' and 'int'");
    }

    @Test(description = "Testing convert map values to string")
    public void testBMapToString() {
        MapValue<io.ballerina.runtime.api.values.BString, Object> map = new MapValueImpl<>();
        map.put(StringUtils.fromString("key1"), 1);
        map.put(StringUtils.fromString("key2"), StringUtils.fromString("foo"));
        map.put(StringUtils.fromString("key3"), XmlFactory.parse("<bar>hello</bar>"));
        Assert.assertEquals(map.stringValue(null), "{\"key1\":1,\"key2\":\"foo\",\"key3\":`<bar>hello</bar>`}");
    }

    @Test(dependsOnMethods = "testGrammar")
    public void testMapOrder() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapOrder", new Object[0]);
        BMap m = (BMap) returnVals;
        Object[] keys = m.getKeys();
        int counter = 0;
        String[] values = {"Element 1", "Element 2", "Element 3"};
        for (Object key : keys) {
            Assert.assertEquals(m.get(key).toString(), values[counter]);
            counter++;
        }
        String mapString = m.toString();
        Assert.assertEquals(mapString, "{\"key1\":\"Element 1\",\"key2\":\"Element 2\",\"key3\":\"Element 3\"}");
    }

    @Test(description = "Test string representations of a map with a nil value", dependsOnMethods = "testGrammar")
    public void testMapStringRepresentation() {
        Object returnVals = BRunUtil.invoke(programFile, "testMapStringRepresentation", new Object[0]);
        BMap m = (BMap) returnVals;
        String mapString = m.toString();
        Assert.assertEquals(mapString, "{\"key1\":\"Element 1\",\"key2\":\"Element 2\",\"key3\":null}");
    }

    @Test
    public void testBMapOrder() {
        BMap map = ValueCreator.createMapValue();
        map.put(StringUtils.fromString("Entry1"), StringUtils.fromString("foo"));
        map.put(StringUtils.fromString("Entry2"), StringUtils.fromString("bar"));
        map.put(StringUtils.fromString("Entry3"), StringUtils.fromString("foobar"));

        Object[] keys = map.getKeys();
        Assert.assertEquals(map.get(keys[0]).toString(), "foo");
        Assert.assertEquals(map.get(keys[1]).toString(), "bar");
        Assert.assertEquals(map.get(keys[2]).toString(), "foobar");

    }

    @AfterClass
    public void tearDown() {
        programFile = null;
    }
}
