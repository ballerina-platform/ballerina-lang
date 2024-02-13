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
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * TestCases for foreach with Arrays.
 *
 * @since 0.995.0
 */
@Test
public class ForeachArrayTests {

    private CompileResult program;

    private int[] iValues = {1, -3, 5, -30, 4, 11, 25, 10};
    private double[] fValues = {1.123, -3.35244, 5.23, -30.45, 4.32, 11.56, 25.967, 10.345};
    private String[] sValues = {"foo", "bar", "bax", "baz"};
    private boolean[] bValues = {true, false, false, false, true, false};
    private String[] tValues = {"name=bob,age=10", "name=tom,age=16"};

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/jvm/foreach-arrays.bal");
    }

    @Test
    public void testIntArrayWithArityOne() {
        Object returns = BRunUtil.invoke(program, "testIntArrayWithArityOne");
        
        Assert.assertEquals(returns, (long) Arrays.stream(iValues).sum());
    }

    @Test
    public void testIntArrayWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iValues.length; i++) {
            sb.append(i).append(":").append(iValues[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testIntArrayWithArityTwo");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testFloatArrayWithArityOne() {
        StringBuilder sb = new StringBuilder();
        for (double fValue : fValues) {
            sb.append(0).append(":").append(fValue).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testFloatArrayWithArityOne");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testFloatArrayWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fValues.length; i++) {
            sb.append(i).append(":").append(fValues[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testFloatArrayWithArityTwo");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testStringArrayWithArityOne() {
        StringBuilder sb = new StringBuilder();
        for (String sValue : sValues) {
            sb.append(0).append(":").append(sValue).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testStringArrayWithArityOne");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testStringArrayWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sValues.length; i++) {
            sb.append(i).append(":").append(sValues[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testStringArrayWithArityTwo");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testBooleanArrayWithArityOne() {
        StringBuilder sb = new StringBuilder();
        for (boolean bValue : bValues) {
            sb.append(0).append(":").append(bValue).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testBooleanArrayWithArityOne");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testBooleanArrayWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bValues.length; i++) {
            sb.append(i).append(":").append(bValues[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testBooleanArrayWithArityTwo");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testStructArrayWithArityOne() {
        StringBuilder sb = new StringBuilder();
        for (String tValue : tValues) {
            sb.append("0").append(":").append(tValue).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testStructArrayWithArityOne");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testStructArrayWithArityTwo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tValues.length; i++) {
            sb.append(i).append(":").append(tValues[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testStructArrayWithArityTwo");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testArrayInsertInt() {
        int[] values = new int[]{0, 0, 0, 3, 0, 0, 6};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(":").append(values[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testArrayInsertInt");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testArrayInsertString() {
        String[] values = new String[]{"d0", "", "", "d3", "", "", "d6"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(":").append(values[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testArrayInsertString");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testArrayInsertInForeach() {
        String[] values = new String[]{"d0", "d1", "d2"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(":").append(values[i]).append(" ");
        }
        Object returns = BRunUtil.invoke(program, "testArrayInsertInForeach");
        
        Assert.assertEquals(returns.toString(), sb.toString());
    }

    @Test
    public void testBreak() {
        Object returns = BRunUtil.invoke(program, "testBreak");
        
        Assert.assertEquals(returns.toString(), "0:d0 break");
    }

    @Test
    public void testContinue() {
        Object returns = BRunUtil.invoke(program, "testContinue");
        
        Assert.assertEquals(returns.toString(), "0:d0 continue 2:d2 ");
    }

    @Test
    public void testReturn() {
        Object returns = BRunUtil.invoke(program, "testReturn");
        
        Assert.assertEquals(returns.toString(), "0:d0 ");
    }

    @Test
    public void testArrayWithNullElements() {
        Object returns = BRunUtil.invoke(program, "testArrayWithNullElements");
        
        Assert.assertEquals(returns.toString(), "0:d0 1: 2:d2 3: ");
    }

    @Test(dataProvider = "listIterationTestFunctions")
    public void testListIteration(String functionName) {
        BRunUtil.invoke(program, functionName);
    }

    @DataProvider(name = "listIterationTestFunctions")
    public Object[][] listIterationTestFunctions() {
        return new Object[][]{
                {"testQueryInsideLoop"},
                {"testListConstructor"},
                {"testTuple"},
                {"testEmptyArray"},
                {"testFunctionCall"},
                {"testQueryExpressions"}
        };
    }

    // These ensure observable behaviour of foreach statements are the same even when we optimize away the iterator
    @Test
    public void testMutatingArray() {
        BRunUtil.invoke(program, "testMutatingArray");
    }

    @Test
    public void testRemoveElementsWhileIterating() {
        try {
            BRunUtil.invoke(program, "testRemoveElementsWhileIterating");
        } catch (BLangTestException ex) {
            Assert.assertTrue(ex.getMessage().contains("array index out of range: index: 2, size: 1"));
        }
    }

    @AfterClass
    public void tearDown() {
        program = null;
    }
}
