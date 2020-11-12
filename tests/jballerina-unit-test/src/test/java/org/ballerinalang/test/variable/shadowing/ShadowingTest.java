/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.variable.shadowing;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Positive test cases for scoping rules for identifiers.
 *
 * @since 0.995.0
 */
public class ShadowingTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/variable/shadowing/shadowing.bal");
    }

    @Test
    public void testLocalVarScope() {
        BValue[] returns = BRunUtil.invoke(result, "testLocalVarScope");
        assertEquals(returns[0].stringValue(), "John Doe");
    }

    @Test
    public void testObjMethodScope() {
        BValue[] returns = BRunUtil.invoke(result, "testObjMethodScope");
        assertEquals(returns[0].stringValue(), "Name in object");
    }

    @Test
    public void testRecordScope() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordScope");
        assertEquals(returns[0].stringValue(), "{name:\"Person\", age:20}");
    }

    @Test
    public void testBlockScope1() {
        BValue[] returns = BRunUtil.invoke(result, "testBlockScope1");
        assertEquals(returns[0].stringValue(), "Inside first if block");
        assertEquals(returns[1].stringValue(), "Inside second if block");
    }

    @Test
    public void testBlockScope2() {
        BValue[] returns = BRunUtil.invoke(result, "testBlockScope2");
        assertEquals(returns[0].stringValue(), "Inside else block");
    }

    @Test
    public void testLambdaFunctions() {
        BValue[] returns = BRunUtil.invoke(result, "testLambdaFunctions");
        assertEquals(returns[0].stringValue(), "Inside a lambda function");
    }

    @Test
    public void testFunctionParam() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionParam",
                                           new BValue[]{new BString("This is a function param")});
        assertEquals(returns[0].stringValue(), "This is a function param");
    }

    @Test
    public void testNestedBlocks() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedBlocks");
        assertEquals(returns[0].stringValue(), "var after nested if-else");
    }

    @Test
    public void testNamespaces1() {
        BValue[] returns = BRunUtil.invoke(result, "testNamespaces1");
        assertEquals(returns[0].stringValue(), "<ns:greeting xmlns:ns=\"http://sample.com/wso2/a2\">Hello " +
                "World!</ns:greeting>");
    }

    @Test
    public void testNamespaces2() {
        BValue[] returns = BRunUtil.invoke(result, "testNamespaces2", new BValue[]{new BBoolean(true)});
        assertEquals(returns[0].stringValue(), "<ns:greeting xmlns:ns=\"http://sample.com/wso2/a2\">Hello " +
                "World!</ns:greeting>");

        returns = BRunUtil.invoke(result, "testNamespaces2", new BValue[]{new BBoolean(false)});
        assertEquals(returns[0].stringValue(), "<ns:greeting xmlns:ns=\"http://sample.com/wso2/a3\">Hello " +
                "World!</ns:greeting>");
    }

    @Test(description = "Shadow the type name in side function.")
    public void testTypeNameAsVariable1() {
        BRunUtil.invoke(result, "testTypeNameAsVariable1");
    }

    @Test(description = "Shadow the type name in side function.")
    public void testTypeNameAsVariable2() {
        BRunUtil.invoke(result, "testTypeNameAsVariable2");
    }

    @Test(description = "Shadow the type name in side function.")
    public void testTypeNameAsVariable3() {
        BRunUtil.invoke(result, "testTypeNameAsVariable3");
    }

    @Test(description = "Shadow the type name in side function.")
    public void testTypeNameAsVariable4() {
        BRunUtil.invoke(result, "testTypeNameAsVariable4");
    }

    @Test(description = "Shadow the type name in side function.")
    public void testTypeNameAsVariable5() {
        BRunUtil.invoke(result, "testTypeNameAsVariable5");
    }

    @Test(description = "test shadowing with ballerina generated names")
    public void testGeneratedNames() {
        BRunUtil.invoke(result, "testGeneratedNames");
    }
}
