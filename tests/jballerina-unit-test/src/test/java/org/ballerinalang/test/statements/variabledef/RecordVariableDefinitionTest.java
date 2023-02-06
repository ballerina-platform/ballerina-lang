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
package org.ballerinalang.test.statements.variabledef;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * TestCases for Record Variable Definitions.
 *
 * @since 0.985.0
 */
@Test
public class RecordVariableDefinitionTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.
                compile("test-src/statements/variabledef/record-variable-definition-stmt.bal");
        resultNegative = BCompileUtil.
                compile("test-src/statements/variabledef/record-variable-definition-stmt-negative.bal");
    }

    @Test(description = "Test simple record variable definition")
    public void simpleDefinition() {
        BArray returns = (BArray) BRunUtil.invoke(result, "simpleDefinition");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "Peter");
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test(description = "Test record variable inside record variable")
    public void recordVarInRecordVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "recordVarInRecordVar");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "Peter");
        Assert.assertEquals(returns.get(1), 29L);
        Assert.assertEquals(returns.get(2).toString(), "Y");
        Assert.assertTrue((Boolean) returns.get(3));
    }

    @Test(description = "Test record variable inside record variable")
    public void recordVarInRecordVar2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "recordVarInRecordVar2");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "Peter");
        Assert.assertEquals((((BMap) returns.get(1)).get(StringUtils.fromString("age"))), 29L);
        Assert.assertEquals(((BMap) returns.get(1)).get(StringUtils.fromString("format")).toString(), "Y");
    }

    @Test(description = "Test record variable inside record variable inside record variable")
    public void recordVarInRecordVarInRecordVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "recordVarInRecordVarInRecordVar");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0).toString(), "Peter");
        Assert.assertTrue((Boolean) returns.get(1));
        Assert.assertEquals(returns.get(2), 1000L);
        Assert.assertEquals(returns.get(3).toString(), "PG");
        Assert.assertEquals(returns.get(4).toString(), "Colombo 10");
    }

    @Test(description = "Test tuple variable inside record variable")
    public void tupleVarInRecordVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "tupleVarInRecordVar");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "John");
        Assert.assertEquals(returns.get(1), 20L);
        Assert.assertEquals(returns.get(2).toString(), "PG");
    }

    @Test(description = "Test declaring 3 record variables and using the variables")
    public void defineThreeRecordVariables() {
        BArray returns = (BArray) BRunUtil.invoke(result, "defineThreeRecordVariables");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "JohnDoePeterYYMMDD");
        Assert.assertEquals(returns.get(1), 50L);
    }

    @Test(description = "Test declaring 3 record variables and using the variables")
    public void recordVariableWithRHSInvocation() {
        Object returns = BRunUtil.invoke(result, "recordVariableWithRHSInvocation");
        Assert.assertEquals(returns.toString(), "Jack Jill");
    }

    @Test(description = "Test declaring 3 record variables and using the variables")
    public void nestedRecordVariableWithRHSInvocation() {
        Object returns = BRunUtil.invoke(result, "nestedRecordVariableWithRHSInvocation");
        Assert.assertEquals(returns.toString(), "Peter Parker");
    }

    @Test(description = "Test rest parameter")
    public void testRestParameter() {
        Object returns = BRunUtil.invoke(result, "testRestParameter");
        Assert.assertTrue(returns instanceof BMap);
        BMap bMap = (BMap) returns;
        Assert.assertEquals(bMap.size(), 2);
        Assert.assertEquals(bMap.get(StringUtils.fromString("work")).toString(), "SE");
        Assert.assertEquals((((BMap) bMap.get(StringUtils.fromString("other"))).get(StringUtils.fromString("age"))),
                99L);
        Assert.assertEquals(
                ((BMap) bMap.get(StringUtils.fromString("other"))).get(StringUtils.fromString("format")).toString(),
                "MM");
    }

    @Test(description = "Test rest parameter in nested record variable")
    public void testNestedRestParameter() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testNestedRestParameter");
        Assert.assertTrue(returns.get(0) instanceof BMap);
        BMap bMap = (BMap) returns.get(0);
        Assert.assertEquals(bMap.size(), 1);
        Assert.assertEquals((bMap.get(StringUtils.fromString("year"))), 1990L);

        BMap bMap2 = (BMap) returns.get(1);
        Assert.assertEquals(bMap2.size(), 1);
        Assert.assertEquals(bMap2.get(StringUtils.fromString("work")).toString(), "SE");
    }

    @Test(description = "Test rest parameter in nested record variable")
    public void testVariableAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVariableAssignment");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0).toString(), "Peter");
        Assert.assertEquals(returns.get(1), 29L);
        Assert.assertEquals(returns.get(2).toString(), "Y");
        Assert.assertTrue((Boolean) returns.get(3));
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("work")).toString(), "SE");
    }

    @Test(description = "Test rest parameter in nested record variable")
    public void testVariableAssignment2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVariableAssignment2");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0).toString(), "James");
        Assert.assertEquals(returns.get(1), 30L);
        Assert.assertEquals(returns.get(2).toString(), "N");
        Assert.assertFalse((Boolean) returns.get(3));
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("added")).toString(), "later");
    }

    @Test(description = "Test tuple var def inside record var def")
    public void testTupleVarDefInRecordVarDef() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDefInRecordVarDef");
        Assert.assertEquals(returns.size(), 7);
        Assert.assertEquals(returns.get(0).toString(), "Mark");
        Assert.assertEquals((((BArray) returns.get(1)).getRefValue(0)), 1L);
        Assert.assertEquals((((BArray) returns.get(1)).getRefValue(1)), 1L);
        Assert.assertEquals((((BArray) returns.get(1)).getRefValue(2)), 1990L);
        Assert.assertEquals(returns.get(2), 1);
        Assert.assertEquals(returns.get(3).toString(), "Mark");
        Assert.assertEquals(returns.get(4), 1L);
        Assert.assertEquals(returns.get(5), 1L);
        Assert.assertEquals(returns.get(6), 1990L);
    }

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordInsideTupleInsideRecord() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord");
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

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordInsideTupleInsideRecord2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord2");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "C");
        Assert.assertEquals(returns.get(1), 1996L);
        Assert.assertEquals(returns.get(2), 22L);
        Assert.assertEquals(returns.get(3).toString(), "Z");
    }

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordInsideTupleInsideRecordWithVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordInsideTupleInsideRecordWithVar");
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

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordInsideTupleInsideRecord2WithVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord2WithVar");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "D");
        Assert.assertEquals(returns.get(1), 1998L);
        Assert.assertEquals(returns.get(2), 20L);
        Assert.assertEquals(returns.get(3).toString(), "A");
    }

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordVarWithUnionType() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordVarWithUnionType");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 50L);
        Assert.assertEquals(returns.get(1), 51.1);
        Assert.assertEquals(getType(returns.get(2)).getName(), "UnionOne");
        Assert.assertEquals(((BMap) returns.get(2)).get(StringUtils.fromString("restP1")).toString(), "stringP1");
    }

    @Test(description = "Test record variable with Union Type")
    public void testUnionRecordVariable() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testUnionRecordVariable");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "A");
        Assert.assertEquals(returns.get(1).toString(), "B");
        Assert.assertNull(returns.get(2));
        Assert.assertNull(returns.get(3));
    }

    @Test(description = "Test record variable with ignore variable")
    public void testIgnoreVariable() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testIgnoreVariable");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "John");
        Assert.assertEquals(returns.get(1), 30L);
    }

    @Test(description = "Test record variable with only a rest parameter")
    public void testRecordVariableWithOnlyRestParam() {
        BMap returns = (BMap) BRunUtil.invoke(result, "testRecordVariableWithOnlyRestParam");

        Assert.assertEquals(returns.toString(),
                "{\"name\":\"John\",\"age\":{\"age\":30,\"format\":\"YY\",\"year\":1990},\"married\":true," +
                        "\"work\":\"SE\"}");
    }

    @Test(description = "Test record variables rest param types")
    public void testRestParameterType() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRestParameterType");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertFalse((Boolean) returns.get(3));
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test resolving the rest field type during record restructuring")
    public void testResolvingRestField() {
        BRunUtil.invoke(result, "testRestFieldResolving");
    }

    @Test
    public void testNegativeRecordVariables() {
        String redeclaredSymbol = "redeclared symbol ";
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'fName'", 37, 26);
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'fiName'", 40, 36);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'Person', found 'PersonWithAge'", 62, 37);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'int'", 95, 13);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'int', found 'string'", 96, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'boolean'", 97, 14);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 98, 15);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '(UnionOne|UnionTwo)', found 'UnionRec1'", 141, 66);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '(string|boolean)', found '(string|boolean)?'", 142, 25);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '(int|float)', found '(int|float)?'", 142, 31);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 149, 10);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 149, 16);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 149, 22);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 150, 10);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 150, 17);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 150, 24);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'anydata'", 152, 13);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'string?'", 152, 31);
        BAssertUtil.validateError(resultNegative, ++i,
                "'_' is a keyword, and may not be used as an identifier", 157, 20);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 157, 20);
        BAssertUtil.validateError(resultNegative, ++i,
                "'_' is a keyword, and may not be used as an identifier", 157, 30);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 157, 30);
        BAssertUtil.validateError(resultNegative, ++i,
                "no new variables on left side", 158, 5);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'XY', " +
                "found 'record {| never x?; never y?; anydata...; |}'", 174, 12);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'map<int>', " +
                "found 'map<(int|string)>'", 188, 12);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'map<int>', " +
                "found 'record {| int x; int y; anydata...; |}'", 198, 12);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'map<string>', " +
                "found 'record {| never name?; int age; string...; |}'", 213, 18);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'map<string>', " +
                "found 'record {| never name?; (int|error) id; string...; |}'", 228, 18);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'int', " +
                "found '(int|error)'", 231, 15);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected " +
                        "'record {| never name?; never age?; (int|string)...; |}', " +
                        "found 'record {| never name?; int age; string...; |}'",
                240, 17);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected " +
                        "'record {| never name?; int...; |}', found 'record {| never name?; int age; string...; |}'",
                251, 44);
        BAssertUtil.validateError(resultNegative, ++i, "invalid field access: 'employed' is not a required field in " +
                        "record 'record {| never name?; int age; string...; |}', use member access to access a field " +
                        "that may have been specified as a rest field",
                253, 23);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'map<string>', found 'record {| never name?; int age; string...; |}'",
                260, 24);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'map<(int|string)>', found " +
                        "'record {| never name?; (int|boolean|string) age; boolean? married?; (string|int)...; |}'",
                285, 29);
        BAssertUtil.validateError(resultNegative, ++i, "field access cannot be used to access an optional field " +
                "of a type that includes nil, use optional field access or member access", 287, 24);
        BAssertUtil.validateError(resultNegative, ++i,
                "a wildcard binding pattern can be used only with a value that belong to type 'any'",
                298, 12);
        BAssertUtil.validateError(resultNegative, ++i,
                "a wildcard binding pattern can be used only with a value that belong to type 'any'",
                301, 20);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 310, 16);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid field binding pattern; can only bind required fields", 316, 40);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'int', found 'int?'", 328, 18);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'int', found 'int?'", 331, 14);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'record {| int b?; anydata...; |}[1]', " +
                        "found 'record {| int b?; anydata...; |}[1]?'", 344, 29);

        BAssertUtil.validateError(resultNegative, ++i,
                "invalid record binding pattern with type 'record {| int b?; anydata...; |}[1]'", 349, 17);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'int?', found 'string?'",
                360, 19);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'record {| readonly (string[] & " +
                "readonly) x; readonly string y; |} & readonly', found 'ReadOnlyRecord'", 373, 17);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'int[] & readonly', found " +
                "'int[]'", 381, 9);
        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }

    @Test
    public void testOptionalFieldRecordAssignment() {
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment1");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment2");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment3");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment4");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment5");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment6");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment7");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment8");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment9");
        BRunUtil.invoke(result, "testOptionalFieldRecordAssignment10");
    }

    @Test
    public void testRecordDefinitionWithOptionalFields() {
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields1");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields2");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields3");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields4");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields5");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields6");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields7");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields8");
        BRunUtil.invoke(result, "testRecordDefinitionWithOptionalFields9");
    }

    @Test
    public void testRecordFieldBindingPatternsWithIdentifierEscapes() {
        BRunUtil.invoke(result, "testRecordFieldBindingPatternsWithIdentifierEscapes");
    }

    @Test
    public void testReadOnlyRecordWithMappingBindingPattern() {
        BRunUtil.invoke(result, "testReadOnlyRecordWithMappingBindingPatternInVarDecl");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
