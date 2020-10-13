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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for anonymous objects.
 *
 * @since 0.970.0
 */
@Test(groups = "brokenOnClassChange")
public class AnonymousObjectTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/anon_object.bal");
    }

    @Test(description = "Test Anonymous object in a function parameter declaration")
    public void testAnonObjectAsFuncParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectAsFuncParam");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 24);
    }

    @Test(description = "Test Anonymous object in a local variable declaration")
    public void testAnonObjectAsLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectAsLocalVar");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 11);
    }

    @Test(description = "Test Anonymous object in a package variable declaration")
    public void testAnonObjectAsPkgVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectAsPkgVar");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "sameera:jayasoma:100");
    }

    @Test(description = "Test Anonymous object in a object field")
    public void testAnonObjectAsObjectField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectAsObjectField");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "JAN:12 Gemba St APT 134:CA:sam");
    }

    @Test(description = "Test Anonymous object with function as global variable")
    public void testAnonObjectWithFunctionsAsGlobalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectWithFunctionAsGlobalVar");

        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "a hello");
    }

    @Test(description = "Test Anonymous object with function as local variable")
    public void testAnonObjectWithFunctionsAsLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectWithFunctionAsLocalVar");

        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "a hello");
    }

    @Test(description = "Test Anonymous casted to normal object")
    public void testAnonObjectCastWithNormalObject() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectEquivalencyBetweenAnonAndNormalObject");

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertTrue(returns[2] instanceof BString);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
        Assert.assertEquals(returns[1].stringValue(), "passed Name");
        Assert.assertEquals(returns[2].stringValue(), "passed Name hello sample value");
    }

    @Test(description = "Test Anonymous object with record literal")
    public void testAnonObjectWithRecordLiteral() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnonObjectWithRecordLiteral");

        Assert.assertEquals(returns.length, 2);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
        Assert.assertEquals(returns[1].stringValue(), "sanjiva");
    }

    @Test(description = "Test object with anonymous record literal")
    public void testObjectWithAnonRecordLiteral() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithAnonRecordLiteral");

        Assert.assertEquals(returns.length, 2);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
        Assert.assertEquals(returns[1].stringValue(), "sanjiva");
    }

    @Test(description = "Test object with self reference")
    public void testObjectWithSelfReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSelfReference");

        Assert.assertEquals(returns.length, 2);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 98);
        Assert.assertEquals(returns[1].stringValue(), "Tyler Jewell");
    }

    @Test(description = "Negative test to test un-defaultable anon object")
    public void testUndefaultableAnonObject() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_un_defaultable_anon.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0,
                "no implementation found for the function 'test' of non-abstract object " +
                        "'object { public int age; public string name; function test () returns (); }'", 3, 54);
        BAssertUtil.validateError(result, 1,
                "no implementation found for the function 'test' of non-abstract object " +
                        "'object { public int age; public string name; function test () returns (); }'", 7, 58);
        BAssertUtil.validateError(result, 2,
                "no implementation found for the function 'test' of non-abstract object " +
                        "'object { public int age; public string name; function test () returns (); }'", 8, 58);
    }

    @Test(description = "Test Code analyzer execution on Anonymous objects")
    public void testCodeAnalyzerRunningOnAnonymousObjectsForDeprecatedFunctionAnnotation() {
        BAssertUtil.validateWarning(compileResult, 0, "usage of construct 'Test()' is deprecated", 218, 17);
    }
}
