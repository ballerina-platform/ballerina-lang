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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(result, "testIntToVarAssignment",
                new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 81L);
    }

    @Test(description = "Test multiple int to var assignment.")
    public void testMultipleIntToVarAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testMultipleIntToVarAssignment",
                new Object[]{});
        Assert.assertEquals(returns.size(), 4);

        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertNotNull(returns.get(3));

        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 200L);
        Assert.assertEquals(returns.get(2), 300L);
        Assert.assertEquals(returns.get(3), 400L);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscore() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testMultipleIntToVarAssignmentWithUnderscore",
                new Object[]{});
        Assert.assertEquals(returns.size(), 2);

        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));

        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 200L);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseOne() {
        BArray returns = (BArray) BRunUtil.invoke(result,
                "testMultipleIntToVarAssignmentWithUnderscoreOrderCaseOne",
                new Object[]{});
        Assert.assertEquals(returns.size(), 2);

        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));

        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 300L);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseTwo() {
        BArray returns = (BArray) BRunUtil.invoke(result,
                "testMultipleIntToVarAssignmentWithUnderscoreOrderCaseTwo",
                new Object[]{});
        Assert.assertEquals(returns.size(), 2);

        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));

        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 400L);
    }

    @Test(description = "Test string to var assignment.")
    public void testStringToVarAssignment() {
        Object returns = BRunUtil.invoke(result, "testStringToVarAssignment",
                new Object[]{});
        Assert.assertEquals(returns.toString(), "name");
    }

    @Test(description = "Test multiple string to var assignment.")
    public void testMultipleStringToVarAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testMultipleStringToVarAssignment",
                new Object[]{});
        Assert.assertEquals(returns.size(), 4);

        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertNotNull(returns.get(3));

        Assert.assertEquals(returns.get(0).toString(), "name_1");
        Assert.assertEquals(returns.get(1).toString(), "name_2");
        Assert.assertEquals(returns.get(2).toString(), "name_3");
        Assert.assertEquals(returns.get(3).toString(), "name_4");
    }

    @Test(description = "Test boolean to var assignment.")
    public void testBooleanToVarAssignment() {
        Object returns = BRunUtil.invoke(result, "testBooleanToVarAssignment",
                new Object[]{});
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test object to var assignment.")
    public void testObjectToVarAssignment() {
        BRunUtil.invoke(result, "testObjectToVarAssignment");
    }

    @Test(description = "Test object to var assignment.")
    public void testObjectToVarAssignment2() {
        BRunUtil.invoke(result, "testObjectToVarAssignment2");
    }

    @Test(description = "Test var in variable def.")
    public void testVarTypeInVariableDefStatement() {
        //var type is not not allowed in variable def statements
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-type-variable-def-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "initializer required for variables declared with var", 2, 5);
    }

    @Test
    public void testVarDeclarationWithStructFieldAssignmentLHSExpr() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-invalid-usage-struct-field-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 5);
        BAssertUtil.validateError(res, 0, "redeclared symbol 'human'", 9, 8);
        BAssertUtil.validateError(res, 1, "missing equal token", 9, 13);
        BAssertUtil.validateError(res, 2, "missing identifier", 9, 13);
        BAssertUtil.validateError(res, 3, "missing identifier", 9, 19);
        BAssertUtil.validateError(res, 4, "missing semicolon token", 9, 19);
    }

    @Test
    public void testVarDeclarationWithDuplicateVariableRefs() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-duplicate-variable-ref-lhs-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 2);
        BAssertUtil.validateError(res, 0, "redeclared symbol 'age'", 2, 15);
        BAssertUtil.validateError(res, 1, "undefined symbol 'some'", 11, 16);
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
        Object returns = BRunUtil.invoke(res, "testVarDeclarationWithAllIgnoredSymbols");
        Assert.assertEquals(returns.toString(), "success");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testIncompatibleJsonToStructWithErrors() {
        Object returns = BRunUtil.invoke(result, "testIncompatibleJsonToStructWithErrors",
                new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<json>' value cannot be converted to 'Person': " +
                        "\n\t\tmissing required field 'a' of type 'anydata' in record 'Person'" +
                        "\n\t\tmissing required field 'score' of type 'float' in record 'Person'" +
                        "\n\t\tmissing required field 'alive' of type 'boolean' in record 'Person'" +
                        "\n\t\t{" +
                        "\n\t\t  missing required field 'parent.a' of type 'anydata' in record 'Person'" +
                        "\n\t\t  missing required field 'parent.score' of type 'float' in record 'Person'" +
                        "\n\t\t  missing required field 'parent.alive' of type 'boolean' in record 'Person'" +
                        "\n\t\t  field 'parent.parent' in record 'Person' should be of type 'Person?', found " +
                        "'\"Parent\"'\n\t\t  field 'parent.marks' in record 'Person' should be of type 'int[]'," +
                        " found '()'\n\t\t}");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testJsonToStructWithErrors() {
        Object returns = BRunUtil.invoke(result, "testJsonToStructWithErrors",
                new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<json>' value cannot be converted to 'PersonA': " +
                        "\n\t\tfield 'age' in record 'PersonA' should be of type 'int', found '\"25\"'");
    }

    @Test(description = "Test compatible struct with force casting.")
    public void testCompatibleStructForceCasting() {
        Object returns = BRunUtil.invoke(result, "testCompatibleStructForceCasting");

        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> structC = (BMap<String, Object>) returns;

        Assert.assertEquals(structC.get(StringUtils.fromString("x")).toString(), "x-valueof-a");

        Assert.assertEquals((structC.get(StringUtils.fromString("y"))), 4L);
    }

    @Test(description = "Test incompatible struct with force casting.")
    public void testInCompatibleStructForceCasting() {
        Object returns = BRunUtil.invoke(result, "testInCompatibleStructForceCasting", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: 'B' cannot be cast to 'A'");
    }

    @Test(description = "Test any to string with errors.")
    public void testAnyToStringWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToStringWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: 'int' cannot be cast to 'string'");
    }

    @Test(description = "Test any null to string with errors.") //TODO check this
    public void testAnyNullToStringWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToStringWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: '()' cannot be cast to 'string'");
    }

    @Test(description = "Test any to boolean with errors.")
    public void testAnyToBooleanWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToBooleanWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: 'int' cannot be cast to 'boolean'");
    }

    @Test(description = "Test any null to boolean with errors.")
    public void testAnyNullToBooleanWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToBooleanWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: '()' cannot be cast to 'boolean'");
    }

    @Test(description = "Test any to int with errors.")
    public void testAnyToIntWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToIntWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: 'string' cannot be cast to 'int'");
    }

    @Test(description = "Test any null to int with errors.")
    public void testAnyNullToIntWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToIntWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: '()' cannot be cast to 'int'");
    }

    @Test(description = "Test any to float with errors.")
    public void testAnyToFloatWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToFloatWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: 'string' cannot be cast to 'float'");
    }

    @Test(description = "Test any null to float with errors.")
    public void testAnyNullToFloatWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToFloatWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: '()' cannot be cast to 'float'");
    }

    @Test(description = "Test any to map with errors.")
    public void testAnyToMapWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToMapWithErrors", new Object[]{});

        Assert.assertTrue(returns instanceof BError);

        Assert.assertEquals(
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "incompatible types: 'string' cannot be cast to 'map'");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
