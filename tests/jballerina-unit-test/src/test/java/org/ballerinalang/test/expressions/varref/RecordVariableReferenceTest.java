/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.varref;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Record Variable References.
 *
 * @since 0.985.0
 */
public class RecordVariableReferenceTest {

    private CompileResult result, resultNegative, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference-negative.bal");
    }

    @Test(description = "Test simple record variable definition")
    public void testVariableAssignment() {
        Object arr = BRunUtil.invoke(result, "testVariableAssignment");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "Peter");
        Assert.assertTrue((Boolean) returns.get(1));
        Assert.assertEquals(returns.get(2), 12L);
        Assert.assertEquals(returns.get(3).toString(), "Y");
    }

    @Test(description = "Test simple record variable definition")
    public void testRecVarRefInsideRecVarRefInsideRecVarRef() {
        Object arr = BRunUtil.invoke(result, "testRecVarRefInsideRecVarRefInsideRecVarRef");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(((BMap) returns.get(0)).get(StringUtils.fromString("mKey1")), 1L);
        Assert.assertEquals(((BMap) returns.get(0)).get(StringUtils.fromString("mKey2")), 2L);
        Assert.assertEquals(returns.get(1), 12L);
        Assert.assertEquals(returns.get(2).toString(), "SomeVar1");
        Assert.assertNull(returns.get(3));
    }

    @Test(description = "Test simple record variable definition")
    public void testRestParam() {
        Object returns = BRunUtil.invoke(result, "testRestParam");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("var3")), 12L);
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("var4")).toString(), "text");
    }

    @Test(description = "Test simple record variable definition")
    public void testRecordTypeInRecordVarRef() {
        Object arr = BRunUtil.invoke(result, "testRecordTypeInRecordVarRef");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(((BMap) returns.get(0)).get(StringUtils.fromString("mKey1")), 1L);
        Assert.assertEquals(((BMap) returns.get(0)).get(StringUtils.fromString("mKey2")), 2L);
        Assert.assertEquals(returns.get(1), 12L);
        Assert.assertEquals((((BMap) returns.get(2)).get(StringUtils.fromString("var1"))).toString(), "SomeVar1");
        Assert.assertNull(((BMap) returns.get(2)).get(StringUtils.fromString("var2")));
    }

    @Test(description = "Test tuple var ref inside record var ref")
    public void testTupleVarRefInRecordVarRef() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefInRecordVarRef");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 7);
        Assert.assertEquals(returns.get(0).toString(), "Mark");
        Assert.assertEquals(((BArray) returns.get(1)).getRefValue(0), 1L);
        Assert.assertEquals(((BArray) returns.get(1)).getRefValue(1), 1L);
        Assert.assertEquals(((BArray) returns.get(1)).getRefValue(2), 1990L);
        Assert.assertEquals(returns.get(2), 1);
        Assert.assertEquals(returns.get(3).toString(), "Mark");
        Assert.assertEquals(returns.get(4), 1L);
        Assert.assertEquals(returns.get(5), 1L);
        Assert.assertEquals(returns.get(6), 1990L);
    }

    @Test(description = "Test record var ref inside tuple var ref inside record var ref")
    public void testRecordInsideTupleInsideRecord() {
        Object arr = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(((BArray) returns.get(0)).getString(0), "A");
        Assert.assertEquals(((BArray) returns.get(0)).getString(1), "B");
        Assert.assertEquals(returns.get(1).toString(), "A");
        BMap child = (BMap) ((BMap) returns.get(2)).get(StringUtils.fromString("child"));
        Assert.assertEquals(child.get(StringUtils.fromString("name")).toString(), "C");
        Assert.assertEquals((((BArray) child.get(StringUtils.fromString("yearAndAge"))).getRefValue(0)), 1996L);
        Assert.assertEquals(((BMap) ((BArray) child.get(StringUtils.fromString("yearAndAge"))).getRefValue(1)).get(
                        StringUtils.fromString("format")).toString(),
                "Z");
    }

    @Test(description = "Test record var ref inside tuple var ref inside record var ref")
    public void testRecordInsideTupleInsideRecord2() {
        Object arr = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord2");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "C");
        Assert.assertEquals(returns.get(1), 1996L);
        Assert.assertEquals(returns.get(2), 22L);
        Assert.assertEquals(returns.get(3).toString(), "Z");
    }

    @Test(description = "Test record var ref rest parameter types")
    public void testRestParameterType() {
        Object returns = BRunUtil.invoke(result, "testRestParameterType");
        Assert.assertTrue((Boolean) returns);
    }

    // TODO: Uncomment below tests once record literal is supported with var ref
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral() {
//        Object returns = JvmRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral");
//        Assert.assertEquals(returns.toString(), "Peter");
//        Assert.assertTrue((Boolean) returns[1]]);
//        Assert.assertEquals(returns[2], 12);
//        Assert.assertEquals(returns[3].toString(), "Y");
//    }
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral2() {
//        Object returns = JvmRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral2");
//        Assert.assertEquals(returns.toString(), "Peter");
//        Assert.assertTrue((Boolean) returns[1]]);
//        Assert.assertEquals(( ((BMap) returns[2]).get(StringUtils.fromString("age"))), 12);
//        Assert.assertEquals(((BMap) returns[2]).get(StringUtils.fromString("format")).toString(), "Y");
//    }
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral3() {
//        Object returns = JvmRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral3");
//        Assert.assertEquals(returns.toString(), "Peter");
//        Assert.assertTrue((Boolean) returns[1]]);
//        Assert.assertEquals(( ((BMap) returns[2]).get(StringUtils.fromString("age"))), 12);
//        Assert.assertEquals(((BMap) returns[2]).get(StringUtils.fromString("format")).toString(), "Y");
//    }
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral4() {
//        Object returns = JvmRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral4");
//        Assert.assertEquals(returns.toString(), "Peter");
//        Assert.assertTrue((Boolean) returns[1]]);
//        Assert.assertEquals(( ((BMap) returns[2]).get(StringUtils.fromString("age"))), 12);
//        Assert.assertEquals(((BMap) returns[2]).get(StringUtils.fromString("format")).toString(), "Y");
//    }

    @Test
    public void testRecordFieldBindingPatternsWithIdentifierEscapes() {
        BRunUtil.invoke(result, "testRecordFieldBindingPatternsWithIdentifierEscapes");
    }

    @Test
    public void testReadOnlyRecordWithMappingBindingPatternInDestructuringAssignment() {
        BRunUtil.invoke(result, "testReadOnlyRecordWithMappingBindingPatternInDestructuringAssignment");
    }

    @Test
    public void testRecordVariablesSemanticsNegative() {
        resultSemanticsNegative = BCompileUtil.compile(
                "test-src/expressions/varref/record-variable-reference-semantics-negative.bal");
        final String undefinedSymbol = "undefined symbol ";

        int i = -1;
        BAssertUtil.validateError(resultSemanticsNegative, ++i, undefinedSymbol + "'fName'", 43, 12);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, undefinedSymbol + "'married'", 43, 19);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, undefinedSymbol + "'theAge'", 43, 40);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, undefinedSymbol + "'format'", 43, 48);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, undefinedSymbol + "'theMap'", 43, 61);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "invalid expression statement", 94, 5);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "variable assignment is required", 94, 5);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "incompatible types: expected 'Bar', found 'string'", 95, 12);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "incompatible types: expected 'string', found 'Bar'", 95, 27);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "incompatible types: expected 'record type', found 'int'", 96, 38);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "record literal is not supported for record binding pattern", 97, 38);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "incompatible types: expected 'Person', found 'Age'", 106, 19);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "multiple matching record references found for field 'name'", 108, 5);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid record binding pattern; unknown field 'unknown2' in record type 'Person'", 119, 5);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid record binding pattern; unknown field 'unknown1' in record type 'Age'", 119, 26);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "unknown type 'Data'", 123, 6);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "unknown type 'Data'", 128, 6);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "incompatible types: expected 'map<int>', found 'record {| never name?; boolean married; int...; |}'",
                161, 16);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "incompatible types: expected 'map<error>', " +
                        "found 'record {| never name?; boolean married; Object...; |}'",
                164, 16);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "invalid expr in assignment lhs", 198, 5);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 220, 27);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 226, 18);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 237, 19);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 243, 6);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                                  "incompatible types: expected 'string[] & readonly', found '(int[] & readonly)'",
                                  255, 6);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                                  "incompatible types: expected 'int[] & readonly', found 'int[]'", 265, 9);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i + 1);
    }

    @Test
    public void testRecordVariablesAssignmentToFinalVarNegative() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/varref/record_variable_reference_assignment_to_final_var_negative.bal");
        int i = -1;
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 27, 16);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'i'", 27, 19);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'f'", 27, 22);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 's'", 28, 6);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 'i'", 28, 9);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 'f'", 28, 12);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's2'", 45, 16);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'iv'", 45, 29);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b3'", 45, 33);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'm2'", 45, 38);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 's2'", 46, 6);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 'iv'", 46, 19);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 'b3'", 46, 23);
        BAssertUtil.validateError(resultNegative, ++i, "cannot assign a value to final 'm2'", 46, 31);
        Assert.assertEquals(resultNegative.getDiagnostics().length, i + 1);
    }

    @Test
    public void testNegativeRecordVariables() {
        int i = 0;
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'x'", 28, 5);
        BAssertUtil.validateError(resultNegative, i++, "variables in a binding pattern must be distinct; found " +
                "duplicate variable 'x'", 30, 16);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'x'", 34, 5);
        BAssertUtil.validateError(resultNegative, i++, "variables in a binding pattern must be distinct; found " +
                "duplicate variable 'x'", 36, 21);
        BAssertUtil.validateError(resultNegative, i++, "variables in a binding pattern must be distinct; found " +
                "duplicate variable 'x'", 36, 27);
        Assert.assertEquals(resultNegative.getDiagnostics().length, i);
    }
}
