/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TestCases for foreach with Maps.
 *
 * @since 0.995.0
 */
@Test
public class ForeachMapTests {

    private CompileResult program;
    private Map<String, String> values = new LinkedHashMap<>();

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/jvm/foreach-maps.bal");
        values.put("a", "1A");
        values.put("b", "2B");
        values.put("c", "3C");
        values.put("d", "4D");
    }

    @Test
    public void testMapWithArityOne() {
        StringBuilder sb = new StringBuilder();
        values.forEach((key, value) -> sb.append("_").append(":").append(value).append(" "));
        Object returns = BRunUtil.invoke(program, "testMapWithArityOne");

        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testMapWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        values.forEach((key, value) -> sb.append(key).append(":").append(value).append(" "));
        Object returns = BRunUtil.invoke(program, "testMapWithArityTwo");

        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testAddWhileIteration() {
        String result = "a:1A a:1A b:2B c:3C aa:1A1A \n" +
                "b:2B a:1A b:2B c:3C aa:1A1A bb:2B2B \n" +
                "c:3C a:1A b:2B c:3C aa:1A1A bb:2B2B cc:3C3C \n";
        Object returns = BRunUtil.invoke(program, "testAddWhileIteration");

        Assert.assertEquals(returns.toString(), result);
    }

    @AfterClass
    public void tearDown() {
        program = null;
    }
}
