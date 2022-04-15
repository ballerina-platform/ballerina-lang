/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.object;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for anonymous objects.
 *
 * @since 0.970.0
 */
@Test
public class AnonymousObjectTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/anon_object.bal");
    }

    @Test(description = "Test Anonymous object in a function parameter declaration")
    public void testAnonObjectAsFuncParam() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonObjectAsFuncParam");

        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 24L);
    }

    @Test(description = "Test Anonymous object in a local variable declaration")
    public void testAnonObjectAsLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonObjectAsLocalVar");

        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 11L);
    }

    @Test(description = "Test Anonymous object in a package variable declaration")
    public void testAnonObjectAsPkgVar() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonObjectAsPkgVar");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "sameera:jayasoma:100");
    }

    @Test(description = "Test Anonymous object in a object field")
    public void testAnonObjectAsObjectField() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonObjectAsObjectField");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "JAN:12 Gemba St APT 134:CA:sam");
    }

    @Test(description = "Test Anonymous object with function as global variable")
    public void testAnonObjectWithFunctionsAsGlobalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonObjectWithFunctionAsGlobalVar");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "a hello");
    }

    @Test(description = "Test Anonymous object with function as local variable")
    public void testAnonObjectWithFunctionsAsLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testAnonObjectWithFunctionAsLocalVar");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "a hello");
    }

    @Test(description = "Test Anonymous casted to normal object")
    public void testAnonObjectCastWithNormalObject() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectEquivalencyBetweenAnonAndNormalObject");

        Assert.assertEquals(returns.size(), 3);

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertTrue(returns.get(2) instanceof BString);

        Assert.assertEquals(returns.get(0), 5L);
        Assert.assertEquals(returns.get(1).toString(), "passed Name");
        Assert.assertEquals(returns.get(2).toString(), "passed Name hello sample value");
    }

    @Test(description = "Test Anonymous object with record literal")
    public void testAnonObjectWithRecordLiteral() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testAnonObjectWithRecordLiteral");

        Assert.assertEquals(returns.size(), 2);

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 8L);
        Assert.assertEquals(returns.get(1).toString(), "sanjiva");
    }

    @Test(description = "Test object with anonymous record literal")
    public void testObjectWithAnonRecordLiteral() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithAnonRecordLiteral");

        Assert.assertEquals(returns.size(), 2);

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 8L);
        Assert.assertEquals(returns.get(1).toString(), "sanjiva");
    }

    @Test(description = "Test object with self reference")
    public void testObjectWithSelfReference() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectWithSelfReference");

        Assert.assertEquals(returns.size(), 2);

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 98L);
        Assert.assertEquals(returns.get(1).toString(), "Tyler Jewell");
    }

    @Test(description = "Negative test to test un-defaultable anon object")
    public void testUndefaultableAnonObject() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_un_defaultable_anon.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0,
                "incompatible types: expected 'object { public int age; public string name; function test () returns " +
                        "(); }', found 'object { public int age; public string name; }'", 18, 8);
        BAssertUtil.validateError(result, 1,
                "incompatible types: expected 'object { public int age; public string name; function test () returns " +
                        "(); }', found 'object { public int age; public string name; }'", 32, 12);
        BAssertUtil.validateError(result, 2,
                "incompatible types: expected 'object { public int age; public string name; function test () returns " +
                        "(); }', found 'Foo'", 33, 10);
        BAssertUtil.validateError(result, 3,
                "incompatible types: expected 'object { public int age; public string name; function test () returns " +
                        "(); }', found 'object { public int age; public string name; }'", 38, 12);
    }

    @Test(description = "Test Code analyzer execution on Anonymous objects")
    public void testCodeAnalyzerRunningOnAnonymousObjectsForDeprecatedFunctionAnnotation() {
        BAssertUtil.validateWarning(compileResult, 0, "unused variable 'obj'", 284, 5);
        BAssertUtil.validateWarning(compileResult, 1, "usage of construct 'Test' is deprecated", 287, 25);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
