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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
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
        Assert.assertEquals(((BString) returns[0]).stringValue(), "name");
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

        Assert.assertEquals(((BString) returns[0]).stringValue(), "name_1");
        Assert.assertEquals(((BString) returns[1]).stringValue(), "name_2");
        Assert.assertEquals(((BString) returns[2]).stringValue(), "name_3");
        Assert.assertEquals(((BString) returns[3]).stringValue(), "name_4");
    }

    @Test(description = "Test var with at least non declared ref in LHS expr.")
    public void testVarDeclarationWithAtLeaseOneNonDeclaredSymbol() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDeclarationWithAtLeaseOneNonDeclaredSymbol",
                new BValue[]{});

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BStruct.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "{message:\"\", cause:[]}");
    }

    @Test(description = "Test boolean to var assignment.")
    public void testBooleanToVarAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test(description = "Test var in variable def.")
    public void testVarTypeInVariableDefStatement() {
        //var type is not not allowed in variable def statements
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-type-variable-def-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "mismatched input ';'. expecting {'.', '[', '=', '!', '@'}", 2, 12);
    }

    @Test(description = "Test var in global variable def.")
    public void testVarTypeInGlobalVariableDefStatement() {
        //var type is not not allowed in global variable def statements
        CompileResult res = BCompileUtil.compile("test-src/types/var/global-variable-def-var-type-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 2);
        BAssertUtil.validateError(res, 0, "extraneous input 'var'", 1, 1);
        BAssertUtil.validateError(res, 1,
                "mismatched input '='. expecting {'[', '?', '|', Identifier}", 1, 15);
    }

    @Test
    public void testVarTypeInServiceLevelVariableDefStatement() {
        //var type is not not allowed in service level variable def statements
        CompileResult res = BCompileUtil.compile("test-src/types/var/service-level-variable-def-with-var-negative.bal");
        BAssertUtil.validateError(res, 0, "extraneous input 'var'", 9, 5);
    }

    @Test
    public void testVarDeclarationWithStructFieldAssignmentLHSExpr() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-invalid-usage-struct-field-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "invalid assignment in variable 'human.name'", 9, 8);
    }

    @Test
    public void testVarDeclarationWithDuplicateVariableRefs() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-duplicate-variable-ref-lhs-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "redeclared symbol 'age'", 2, 15);
    }

    @Test(enabled = false)
    public void testVarDeclarationWithArrayInit() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-declaration-with-array-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "array literal not allowed here", 2, 17);
    }

    @Test
    public void testVarDeclarationWithAllDeclaredSymbols() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-declared-symbols-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "no new variables on left side", 4, 5);
    }

    @Test
    public void testVarDeclarationWithAllIgnoredSymbols() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-all-ignored-symbols-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BAssertUtil.validateError(res, 0, "no new variables on left side", 3, 5);
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testIncompatibleJsonToStructWithErrors",
                new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(),
                "{message:\"cannot convert 'json' to type 'Person': error while mapping"
                        + " 'parent': incompatible types: expected 'json-object', found 'string'\", cause:null}");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonToStructWithErrors",
                new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(),
                "{message:\"cannot convert 'json' to type 'PersonA': error while mapping 'age': "
                        + "incompatible types: expected 'int', found 'string' in json\", cause:null}");
    }

    @Test(description = "Test compatible struct with force casting.")
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testCompatibleStructForceCasting", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);
        BStruct structC = (BStruct) returns[0];

        Assert.assertEquals(structC.getStringField(0), "updated-x-valueof-a");

        Assert.assertEquals(structC.getIntField(0), 4);
    }

    @Test(description = "Test incompatible struct with force casting.")
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testInCompatibleStructForceCasting", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'B' cannot be cast to 'A'\", cause:null}");
    }

    @Test(description = "Test any to string with errors.")
    public void testAnyToStringWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToStringWithErrors", new BValue[]{});

        // check whether string is null
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }

    @Test(description = "Test any null to string with errors.") //TODO check this
    public void testAnyNullToStringWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToStringWithErrors", new BValue[]{});

        // check whether string is null
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), null);
    }

    @Test(description = "Test any to boolean with errors.")
    public void testAnyToBooleanWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToBooleanWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'int' cannot be cast to 'boolean'\", cause:null}");
    }

    @Test(description = "Test any null to boolean with errors.")
    public void testAnyNullToBooleanWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToBooleanWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'null' cannot be cast to 'boolean'\", cause:null}");
    }

    @Test(description = "Test any to int with errors.")
    public void testAnyToIntWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToIntWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'string' cannot be cast to 'int'\", cause:null}");
    }

    @Test(description = "Test any null to int with errors.")
    public void testAnyNullToIntWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToIntWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'null' cannot be cast to 'int'\", cause:null}");
    }

    @Test(description = "Test any to float with errors.")
    public void testAnyToFloatWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToFloatWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'string' cannot be cast to 'float'\", cause:null}");
    }

    @Test(description = "Test any null to float with errors.")
    public void testAnyNullToFloatWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToFloatWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'null' cannot be cast to 'float'\", cause:null}");
    }

    @Test(description = "Test any to map with errors.")
    public void testAnyToMapWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToMapWithErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStruct.class);

        Assert.assertEquals(returns[0].stringValue(), "{message:\"'string' cannot be cast to 'map'\", cause:null}");
    }

}
