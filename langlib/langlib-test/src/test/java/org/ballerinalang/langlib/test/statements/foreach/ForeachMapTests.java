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
package org.ballerinalang.langlib.test.statements.foreach;

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
 * @since 0.96.0
 */
public class ForeachMapTests {

    private CompileResult program;
    private Map<String, String> values = new LinkedHashMap<>();

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-maps.bal");
        values.put("a", "1A");
        values.put("b", "2B");
        values.put("c", "3C");
        values.put("d", "4D");
    }

    @AfterClass
    public void tearDown() {
        program = null;
        values = null;
    }

    @Test
    public void testMapWithArityOne() {
        StringBuilder sb = new StringBuilder();
        values.forEach((key, value) -> sb.append(value).append(" "));
        Object returns = BRunUtil.invoke(program, "testMapWithArityOne");
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testMapWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        values.forEach((key, value) -> sb.append(value).append(" "));
        Object returns = BRunUtil.invoke(program, "testMapWithArityTwo");
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testDeleteWhileIteration() {
        String result = "1A 2B ";
        Object returns = BRunUtil.invoke(program, "testDeleteWhileIteration");
        Assert.assertEquals(returns.toString(), result);
    }

    @Test
    public void testAddWhileIteration() {
        String result = "1A 1A 2B 3C 1A1A \n" +
                        "2B 1A 2B 3C 1A1A 2B2B \n" +
                        "3C 1A 2B 3C 1A1A 2B2B 3C3C \n";
        Object returns = BRunUtil.invoke(program, "testAddWhileIteration");
        Assert.assertEquals(returns.toString(), result);
    }

    @Test
    public void testWildcardBindingPatternInForeachStatement() {
        BRunUtil.invoke(program, "testWildcardBindingPatternInForeachStatement");
    }
}
