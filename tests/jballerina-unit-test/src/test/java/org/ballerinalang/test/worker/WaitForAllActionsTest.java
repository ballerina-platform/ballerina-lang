/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.worker;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Wait for all action related tests.
 *
 * @since 0.985.0
 */
public class WaitForAllActionsTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait-for-all-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "Wait for all actions test error count");
    }

    @Test
    public void waitTest1() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest1");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "7");
        expectedMap.put("f2", "22");
        expectedMap.put("f3", "hello foo");
        expectedMap.put("f4", "true");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest2() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest2");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "7");
        expectedMap.put("str1", "hello foo");
        expectedMap.put("f3", "150");
        expectedMap.put("str2", "hello xyz");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest3() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest3");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "7");
        expectedMap.put("f3", "150");
        expectedMap.put("f5", "hello bar");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest4() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest4");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "7");
        expectedMap.put("f4", "hello foo");
        expectedMap.put("f2", "22");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest5() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest5");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "66");
        expectedMap.put("name", "hello foo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest6() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest6");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("idField", "150");
        expectedMap.put("stringField", "hello foo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest7() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest7");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "7");
    }

    @Test
    public void waitTest8() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest8");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "8");
        expectedMap.put("name", "hello moo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest9() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest9");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "20");
        expectedMap.put("f5", "hello foo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest10() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest10");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "30");
        expectedMap.put("f5", "hello xyz");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest11() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest11");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f1", "30");
        expectedMap.put("field", "hello bar");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest12() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest12");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "86");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest13() {
        BRunUtil.invoke(result, "waitTest13");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest14() {
        BRunUtil.invoke(result, "waitTest14");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest15() {
        BRunUtil.invoke(result, "waitTest15");
    }

    @Test
    public void waitTest16() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest16");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0");
    }

    @Test
    public void waitTest17() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest17");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).getMap().values().size(), 2);
        Assert.assertEquals(((BMap) returns[0]).getMap().values().size(), 2);
        Assert.assertTrue(Arrays.asList("{\"f2\":150, \"f1\":7}", "{\"f1\":7, \"f2\":150}")
                                .contains(((BMap) returns[0]).getMap().values().toArray()[0].toString()));
        Assert.assertTrue(Arrays.asList("{\"name\":\"hello foo\", \"id\":12}", "{\"id\":\"12\", " +
                "\"name\":\"hello foo\"}").contains(((BMap) returns[0]).getMap().values().toArray()[1].toString()));
    }

    @Test
    public void waitTest18() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest18");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("f5", "hello foo");
        expectedMap.put("f1", "7");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest19() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest19");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("w1", "30");
        expectedMap.put("w2", "16");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }


    @Test
    public void waitTest20() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest20");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("w2", "hello world");
        expectedMap.put("w3", "15");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest21() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest21");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "7");
        expectedMap.put("name", "hello foo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest22() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest22");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "7");
        expectedMap.put("name", "hello foo");
        expectedMap.put("status", "hello bar");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest23() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest23");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "7");
        expectedMap.put("name", "hello foo");
        expectedMap.put("status", "20");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest24() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest24");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "12");
        expectedMap.put("name", "hello foo");
        expectedMap.put("status", "hello bar");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest25() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest25");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "7");
        expectedMap.put("name", "hello foo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest26() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest26");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "30");
        expectedMap.put("name", "hello world");
        expectedMap.put("status", "hello moo");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }

    @Test
    public void waitTest27() {
        BValue[] returns = BRunUtil.invoke(result, "waitTest27");

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("id", "200");
        expectedMap.put("name", "hello mello");
        expectedMap.put("greet", "hello sunshine");

        Assert.assertEquals(returns.length, 1);
        Map<String, String> resultMap = new HashMap<>();
        ((BMap) returns[0]).getMap().forEach((o, o2) -> resultMap.put(o.toString(), o2.toString()));
        Assert.assertTrue(mapsAreEqual(expectedMap, resultMap));
    }
    /**
     * Util method to compare 2 maps.
     *
     * @param expected expected map
     * @param result   resulting map
     * @return if both the maps are equal or not
     */
    private boolean mapsAreEqual(Map<String, String> expected, Map<String, String> result) {
        for (String y : expected.keySet()) {
            if (!result.containsKey(y)) {
                return false;
            } else {
                if (!expected.get(y).equals(result.get(y))) {
                    return false;
                }
            }
        }
        return true;
    }
}
