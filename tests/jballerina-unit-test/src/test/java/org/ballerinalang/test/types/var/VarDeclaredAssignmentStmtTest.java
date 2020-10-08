/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.var;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Var type assignment statement test.
 *
 * @since 0.88
 */
public class VarDeclaredAssignmentStmtTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/var/var-type-assign-stmt.bal");
    }

    @Test(description = "Test int to var assignment.")
    public void testIntToVarAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testIntToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(returns.length, 1);

        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 81);
    }

    @Test(description = "Test multiple int to var assignment.")
    public void testMultipleIntToVarAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleIntToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(returns.length, 4);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertNotNull(returns[3]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 300);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 400);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscore() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleIntToVarAssignmentWithUnderscore",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseOne() {
        BValue[] returns = BRunUtil.invoke(result,
                "testMultipleIntToVarAssignmentWithUnderscoreOrderCaseOne",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 300);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result,
                "testMultipleIntToVarAssignmentWithUnderscoreOrderCaseTwo",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 400);
    }

    @Test(description = "Test string to var assignment.")
    public void testStringToVarAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testStringToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(returns[0].stringValue(), "name");
    }

    @Test(description = "Test multiple string to var assignment.")
    public void testMultipleStringToVarAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleStringToVarAssignment",
                                           new BValue[]{});
        Assert.assertEquals(returns.length, 4);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertNotNull(returns[3]);

        Assert.assertEquals(returns[0].stringValue(), "name_1");
        Assert.assertEquals(returns[1].stringValue(), "name_2");
        Assert.assertEquals(returns[2].stringValue(), "name_3");
        Assert.assertEquals(returns[3].stringValue(), "name_4");
    }

    @Test(description = "Test boolean to var assignment.")
    public void testBooleanToVarAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanToVarAssignment",
                new BValue[]{});
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test var in variable def.", groups = { "disableOnOldParser" })
    public void testVarTypeInVariableDefStatement() {
        //var type is not not allowed in variable def statements
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-type-variable-def-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "initializer required for variables declared with var", 2, 5);
    }

    @Test(groups = { "brokenOnNewParser" })
    public void testVarTypeInServiceLevelVariableDefStatement() {
        //var type is not not allowed in service level variable def statements
        CompileResult res = BCompileUtil.compile("test-src/types/var/service-level-variable-def-with-var-negative.bal");
        BAssertUtil.validateError(res, 0,
                                  "mismatched input 'var'. expecting {'public', 'private', 'resource', 'function', " +
                                          "'remote', 'transactional', '}', '@', DocumentationLineStart}", 4, 5);
        BAssertUtil.validateError(res, 1, "extraneous input 'resource'", 6, 5);
        BAssertUtil.validateError(res, 2, "extraneous input '}'", 12, 1);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testVarDeclarationWithStructFieldAssignmentLHSExpr() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-invalid-usage-struct-field-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 5);
        BAssertUtil.validateError(res, 0, "initializer required for variables declared with var", 9, 4);
        BAssertUtil.validateError(res, 1, "redeclared symbol 'human'", 9, 8);
        BAssertUtil.validateError(res, 2, "missing semicolon token", 9, 13);
        BAssertUtil.validateError(res, 3, "invalid token '.'", 9, 14);
        BAssertUtil.validateError(res, 4, "undefined symbol 'name'", 9, 14);
    }

    @Test
    public void testVarDeclarationWithDuplicateVariableRefs() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-duplicate-variable-ref-lhs-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "redeclared symbol 'age'", 2, 15);
    }

    @Test
    public void testVarDeclarationWithAllDeclaredSymbols() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-declared-symbols-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 3);
        BAssertUtil.validateError(res, 0, "redeclared symbol 'a'", 4, 10);
        BAssertUtil.validateError(res, 1, "redeclared symbol 's'", 4, 13);
        BAssertUtil.validateError(res, 2, "redeclared symbol 'a'", 14, 10);
    }

    @Test
    public void testVarDeclarationWithAllIgnoredSymbols() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-all-ignored-symbols.bal");
        BValue[] returns = BRunUtil.invoke(res, "testVarDeclarationWithAllIgnoredSymbols");
        Assert.assertEquals(returns[0].stringValue(), "success");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testIncompatibleJsonToStructWithErrors",
                new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "'map<json>' value cannot be converted to 'Person'");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonToStructWithErrors",
                new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "'map<json>' value cannot be converted to 'PersonA'");
    }

    @Test(description = "Test compatible struct with force casting.")
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testCompatibleStructForceCasting");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        BMap<String, BValue> structC = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(structC.get("x").stringValue(), "x-valueof-a");

        Assert.assertEquals(((BInteger) structC.get("y")).intValue(), 4);
    }

    @Test(description = "Test incompatible struct with force casting.")
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testInCompatibleStructForceCasting", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'B' cannot be cast to 'A'");
    }

    @Test(description = "Test any to string with errors.")
    public void testAnyToStringWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToStringWithErrors", new BValue[]{});


        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'int' cannot be cast to 'string'");
    }

    @Test(description = "Test any null to string with errors.") //TODO check this
    public void testAnyNullToStringWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToStringWithErrors", new BValue[]{});

        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: '()' cannot be cast to 'string'");
    }

    @Test(description = "Test any to boolean with errors.")
    public void testAnyToBooleanWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToBooleanWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'int' cannot be cast to 'boolean'");
    }

    @Test(description = "Test any null to boolean with errors.")
    public void testAnyNullToBooleanWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToBooleanWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: '()' cannot be cast to 'boolean'");
    }

    @Test(description = "Test any to int with errors.")
    public void testAnyToIntWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToIntWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'string' cannot be cast to 'int'");
    }

    @Test(description = "Test any null to int with errors.")
    public void testAnyNullToIntWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToIntWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: '()' cannot be cast to 'int'");
    }

    @Test(description = "Test any to float with errors.")
    public void testAnyToFloatWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToFloatWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'string' cannot be cast to 'float'");
    }

    @Test(description = "Test any null to float with errors.")
    public void testAnyNullToFloatWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToFloatWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: '()' cannot be cast to 'float'");
    }

    @Test(description = "Test any to map with errors.")
    public void testAnyToMapWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToMapWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);

        Assert.assertEquals(((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'string' cannot be cast to 'map'");
    }

}
