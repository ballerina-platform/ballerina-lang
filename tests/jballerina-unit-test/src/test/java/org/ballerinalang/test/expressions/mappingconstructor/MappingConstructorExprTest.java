/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.mappingconstructor;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for mapping constructor expressions.
 */
public class MappingConstructorExprTest {

    private CompileResult result;
    private CompileResult varNameFieldResult;
    private CompileResult inferRecordResult;
    private CompileResult spreadOpFieldResult;
    private CompileResult readOnlyFieldResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/mappingconstructor/mapping_constructor.bal");
        inferRecordResult = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/mapping_constructor_infer_record.bal");
        varNameFieldResult = BCompileUtil.compile("test-src/expressions/mappingconstructor/var_name_field.bal");
        spreadOpFieldResult = BCompileUtil.compile("test-src/expressions/mappingconstructor/spread_op_field.bal");
        readOnlyFieldResult = BCompileUtil.compile("test-src/expressions/mappingconstructor/readonly_field.bal");
    }

    @Test(dataProvider = "mappingConstructorTests")
    public void testMappingConstructor(String test) {
        BRunUtil.invoke(result, test);
    }

    @DataProvider(name = "mappingConstructorTests")
    public Object[][] mappingConstructorTests() {
        return new Object[][] {
                { "testMappingConstuctorWithAnyACET" },
                { "testMappingConstuctorWithAnydataACET" },
                { "testMappingConstuctorWithJsonACET" },
                { "testNonAmbiguousMapUnionTarget" },
                { "testTypeWithReadOnlyInUnionCET" }
        };
    }

    @Test
    public void diagnosticsTest() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/mapping_constructor_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 13);
        validateError(result, 0, "incompatible mapping constructor expression for type '(string|Person)'", 33, 23);
        validateError(result, 1, "ambiguous type '(PersonTwo|PersonThree)'", 37, 31);
        validateError(result, 2,
                      "a type compatible with mapping constructor expressions not found in type '(int|float)'", 41, 19);
        validateError(result, 3, "ambiguous type '(map<int>|map<string>)'", 45, 31);
        validateError(result, 4, "ambiguous type '(map<(int|string)>|map<(string|boolean)>)'", 47, 46);
        validateError(result, 5, "unknown type 'NoRecord'", 55, 5);
        validateError(result, 6, "incompatible types: 'int' cannot be cast to 'string'", 55, 22);
        validateError(result, 7, "invalid operation: type 'PersonThree' does not support field access for " +
                "non-required field 'salary'", 55, 41);
        validateError(result, 8, "undefined symbol 'c'", 55, 55);
        validateError(result, 9, "unknown type 'Foo'", 59, 5);
        validateError(result, 10, "incompatible types: 'string' cannot be cast to 'boolean'", 59, 17);
        validateError(result, 11, "unknown type 'Foo'", 60, 5);
        validateError(result, 12, "incompatible types: 'int' cannot be cast to 'boolean'", 60, 30);
    }

    @Test
    public void testVarNameFieldSemanticAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/var_name_field_semantic_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        validateError(result, 0, "incompatible types: expected 'string', found 'int'", 30, 14);
        validateError(result, 1, "incompatible types: expected 'string', found 'int'", 31, 22);
        validateError(result, 2, "undefined field 'b' in record 'Foo'", 37, 32);
        validateError(result, 3, "undefined symbol 'i'", 41, 26);
        validateError(result, 4, "undefined symbol 'c'", 42, 37);
        validateError(result, 5, "undefined symbol 'PI'", 46, 36);
    }

    @Test
    public void testVarNameFieldCodeAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/var_name_field_code_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        validateError(result, 0, "invalid usage of record literal: duplicate key 's'", 26, 17);
        validateError(result, 1, "invalid usage of record literal: duplicate key 'i'", 26, 23);
        validateError(result, 2, "invalid usage of map literal: duplicate key 'i'", 27, 34);
        validateError(result, 3, "invalid usage of map literal: duplicate key 'i'", 27, 42);
    }

    @Test
    public void testVarNameFieldTaintAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/var_name_field_taint_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "tainted value passed to global variable 'f'", 26, 5);
        validateError(result, 1, "tainted value passed to global variable 'm'", 27, 5);
    }

    @Test(dataProvider = "varNameFieldTests")
    public void testVarNameField(String test) {
        BRunUtil.invoke(varNameFieldResult, test);
    }

    @DataProvider(name = "varNameFieldTests")
    public Object[][] varNameFieldTests() {
        return new Object[][] {
                { "testVarNameAsRecordField" },
                { "testVarNameAsMapField" },
                { "testVarNameAsJsonField" },
                { "testLikeModuleQualifiedVarNameAsJsonField" },
                { "testVarNameFieldInAnnotation" } // final test using `s` since `s` is updated
        };
    }

    @Test
    public void testSpreadOpFieldSemanticAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/spread_op_field_semantic_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 21);
        validateError(result, 0, "incompatible types: expected a map or a record, found 'string'", 33, 17);
        validateError(result, 1, "incompatible types: expected a map or a record, found 'boolean'", 33, 32);
        validateError(result, 2, "incompatible types: expected 'int' for field 'i', found 'float'", 41, 17);
        validateError(result, 3, "incompatible types: expected 'string' for field 's', found 'int'", 41, 17);
        validateError(result, 4, "incompatible types: expected 'int' for field 'i', found 'boolean'", 41, 29);
        validateError(result, 5, "undefined field 'x' in record 'Foo'", 49, 29);
        validateError(result, 6, "incompatible types: expected a map or a record, found 'other'", 53, 26);
        validateError(result, 7, "undefined symbol 'b'", 53, 26);
        validateError(result, 8, "incompatible types: expected a map or a record, found 'int'", 60, 28);
        validateError(result, 9, "incompatible types: expected 'string', found '(int|float)'", 68, 25);
        validateError(result, 10, "incompatible types: expected 'string', found 'anydata'", 68, 39);
        validateError(result, 11, "incompatible types: expected a map or a record, found 'other'", 72, 38);
        validateError(result, 12, "undefined symbol 'b'", 72, 38);
        validateError(result, 13, "incompatible types: expected a map or a record, found 'other'", 72, 44);
        validateError(result, 14, "undefined function 'getFoo'", 72, 44);
        validateError(result, 15, "incompatible types: expected 'json', found 'any'", 82, 18);
        validateError(result, 16, "incompatible types: expected 'json', found 'anydata'", 82, 30);
        validateError(result, 17, "incompatible types: expected 'json', found 'any'", 83, 30);
        validateError(result, 18, "incompatible types: expected 'json', found 'anydata'", 83, 36);
        validateError(result, 19, "incompatible types: expected 'int', found 'string'", 96, 18);
        validateError(result, 20, "incompatible types: expected '(int|float)', found 'string'", 97, 32);
    }

    @Test
    public void testSpreadOpFieldCodeAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/spread_op_field_code_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 19);
        int i = 0;
        validateError(result, i++, "invalid usage of record literal: duplicate key 'i' via spread operator '...f'", 30,
                      31);
        validateError(result, i++, "invalid usage of record literal: duplicate key 's'", 30, 34);
        validateError(result, i++, "invalid usage of map literal: duplicate key 's' via spread operator " +
                "'...b'", 31, 47);
        validateError(result, i++, "invalid usage of map literal: duplicate key 'f'", 31, 50);
        validateError(result, i++, "invalid usage of map literal: duplicate key 'i'", 31, 58);
        validateError(result, i++, "invalid usage of map literal: duplicate key 's' via spread operator " +
                "'... {s: hi,i: 1}'", 32, 38);
        validateError(result, i++, "invalid usage of map literal: duplicate key 'i'", 32, 63);
        validateError(result, i++, "invalid usage of map literal: duplicate key 'i' via spread " +
                "operator '...alpha'", 41, 27);
        validateError(result, i++, "invalid usage of mapping constructor expression: spread field " +
                "'a' may have already specified keys", 50, 28);
        validateError(result, i++, "invalid usage of mapping constructor expression: spread field " +
                "'b' may have already specified keys", 59, 32);
        validateError(result, i++, "invalid usage of mapping constructor expression: key 'i' may " +
                "duplicate a key specified via spread field '...b'", 64, 29);
        validateError(result, i++, "invalid usage of mapping constructor expression: key 'y' may " +
                "duplicate a key specified via spread field '...m1'", 69, 30);
        validateError(result, i++, "invalid usage of mapping constructor expression: spread field 'm1' may" +
                " have already specified keys", 74, 35);
        validateError(result, i++, "invalid usage of mapping constructor expression: multiple " +
                "spread fields of inclusive mapping types are not allowed", 84, 35);
        validateError(result, i++, "invalid usage of mapping constructor expression: spread field 'b2' " +
                "may have already specified keys", 84, 35);
        validateError(result, i++, "invalid usage of map literal: duplicate key 'i' via spread operator " +
                "'...b3'", 94, 35);
        validateError(result, i++, "invalid usage of mapping constructor expression: multiple spread " +
                "fields of inclusive mapping types are not allowed", 94, 35);
        validateError(result, i++, "invalid usage of mapping constructor expression: multiple " +
                "spread fields of inclusive mapping types are not allowed", 100, 30);
        validateError(result, i, "invalid usage of mapping constructor expression: multiple " +
                "spread fields of inclusive mapping types are not allowed", 106, 34);
    }

    @Test
    public void testSpreadOpFieldConstantAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/spread_op_field_constant_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "expression is not a constant expression", 19, 47);
    }

    @Test
    public void testSpreadOpFieldTaintAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/spread_op_field_taint_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "tainted value passed to global variable 'm'", 36, 5);
        validateError(result, 1, "tainted value passed to global variable 'bn'", 37, 5);
    }

    @Test(dataProvider = "spreadOpFieldTests", enabled = false)
    public void testSpreadOpField(String test) {
        BRunUtil.invoke(spreadOpFieldResult, test);
    }

    @DataProvider(name = "spreadOpFieldTests")
    public Object[][] spreadOpFieldTests() {
        return new Object[][] {
                { "testMapRefAsSpreadOp" },
                { "testMapValueViaFuncAsSpreadOp" },
                { "testRecordRefAsSpreadOp" },
                { "testRecordRefWithNeverType" },
                { "testRecordValueViaFuncAsSpreadOp" },
                { "testSpreadOpInConstMap" },
                { "testSpreadOpInGlobalMap" },
                { "testMappingConstrExprAsSpreadExpr" },
        };
    }

    @Test
    public void testRecordInferringInSelectNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/mapping_constructor_infer_record_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 15);
        int index = 0;

        validateError(compileResult, index++, "incompatible types: expected 'string[]', found 'record {| string fn; " +
                              "string ln; |}[]'", 37, 20);
        validateError(compileResult, index++, "undefined field 'x' in 'record {| string fn; string ln; |}'", 38, 5);
        validateError(compileResult, index++, "incompatible types: expected 'record {| anydata...; |}[]', found " +
                "'record {| int i; any...; |}[]'", 53, 12);
        validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 70, 16);
        validateError(compileResult, index++, "invalid operation: type 'record {| int i; boolean b; int...; |}' does " +
                "not support field access for non-required field 'key'", 71, 13);
        validateError(compileResult, index++, "incompatible types: expected 'float', found '(int|boolean)?'", 72, 15);
        validateError(compileResult, index++, "incompatible types: expected 'record {| int i; |}', found 'record {| " +
                "int a; float b; string...; |}'", 85, 12);
        validateError(compileResult, index++, "incompatible types: expected 'int', found 'record {| int x; int y; " +
                "|}'", 90, 13);
        validateError(compileResult, index++, "incompatible types: expected 'record {| int...; |}', found 'record {| " +
                "int x; int y; (string|boolean)...; |}'", 95, 30);
        validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'record {| |} & readonly'",
                      98, 17);
        validateError(compileResult, index++, "incompatible types: expected 'record {| int i; boolean b; decimal a; " +
                "float f; anydata...; |}', found 'record {| (int|string) i; boolean b; decimal a; float f?; anydata.." +
                ".; |}'", 126, 12);
        validateError(compileResult, index++, "incompatible types: expected 'readonly', found 'Rec1'", 139, 9);
        validateError(compileResult, index++, "incompatible types: expected 'readonly', found 'future'", 140, 12);
        validateError(compileResult, index++, "a type compatible with mapping constructor expressions not found in " +
                "type '(readonly|int[])'", 150, 25);
        validateError(compileResult, index, "ambiguous type '(map<map<json>>|readonly)'", 157, 34);
    }

    @Test(dataProvider = "inferRecordTypeTests", enabled = false)
    public void testInferRecordTypeTests(String test) {
        BRunUtil.invoke(inferRecordResult, test);
    }

    @DataProvider(name = "inferRecordTypeTests")
    public Object[][] inferRecordTypeTests() {
        return new Object[][] {
                { "testRecordInferringForMappingConstructorWithoutRestField" },
                { "testRecordInferringForMappingConstructorWithRestField1" },
                { "testRecordInferringForMappingConstructorWithRestField2" },
                { "testRecordInferringForMappingConstructorWithRestField3" },
                { "testMappingConstrExprWithNoACET" },
                { "testMappingConstrExprWithNoACET2" },
                { "testInferredRecordTypeWithOptionalTypeFieldViaSpreadOp" },
                { "testInferenceWithMappingConstrExprAsSpreadExpr" },
                { "testInferringForReadOnly" },
                { "testInferringForReadOnlyInUnion" },
                { "testValidReadOnlyWithDifferentFieldKinds" },
                { "testValidReadOnlyInUnionWithDifferentFieldKinds" }
        };
    }

    @Test(dataProvider = "readOnlyFieldTests", groups = "disableOnOldParser", enabled = false)
    public void testReadOnlyFields(String test) {
        BRunUtil.invoke(readOnlyFieldResult, test);
    }

    @DataProvider(name = "readOnlyFieldTests")
    public Object[][] readOnlyFieldTests() {
        return new Object[][] {
                { "testBasicReadOnlyField1" },
                { "testBasicReadOnlyField2" },
                { "testComplexReadOnlyField" },
                { "testInferredTypeReadOnlynessWithReadOnlyFields" },
                { "testReadOnlyBehaviourWithRecordACETInUnionCET" },
                { "testReadOnlyFieldsWithSimpleMapCET" },
                { "testReadOnlyBehaviourWithMapACETInUnionCET" },
                { "testReadOnlyFieldForAlreadyReadOnlyField" },
                { "testReadOnlyFieldWithInferredType" },
                { "testInferredTypeWithAllReadOnlyFields" },
                { "testIdentifierKeysInConstructorWithReadOnlyFieldsForMap" },
                { "testFieldTypeNarrowing" }
        };
    }

    @Test(groups = "disableOnOldParser")
    public void testReadOnlyFieldsSemanticNegative() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/expressions/mappingconstructor/readonly_field_negative.bal");
        int index = 0;

        validateError(compileResult, index++, "incompatible types: expected '(Details & readonly)', found 'Details'",
                      33, 35);
        validateError(compileResult, index++,
                      "incompatible mapping constructor expression for type '(Employee|Details)'", 34, 27);
        validateError(compileResult, index++, "incompatible types: expected '(Employee & readonly)', found 'Employee'",
                      40, 18);
        validateError(compileResult, index++, "incompatible types: expected '(Details & readonly)', found 'Details'",
                      42, 22);
        validateError(compileResult, index++,
                      "incompatible types: expected '((Details & readonly)|string)', found 'Details'", 54, 49);
        validateError(compileResult, index++,
                      "incompatible mapping constructor expression for type '(map<string>|map<(Details|string)>)'",
                      55, 42);
        validateError(compileResult, index++,
                      "incompatible types: expected 'map<((Details|string) & readonly)> & readonly', " +
                              "found 'map<(Details|string)>'", 61, 18);
        validateError(compileResult, index++, "incompatible types: expected '(Details & readonly)', found 'Details'",
                      63, 13);
        validateError(compileResult, index++,
                      "invalid 'readonly' mapping field 'x': 'future<int>' can never be 'readonly'", 77, 40);
        validateError(compileResult, index++,
                      "incompatible types: expected 'any & readonly', found 'stream<boolean>'", 77, 57);
        validateError(compileResult, index++, "incompatible mapping constructor expression for type '(" +
                "record {| future<any>...; |}|NonReadOnlyFields)'", 78, 57);
        validateError(compileResult, index++,
                      "incompatible types: expected '((any & readonly)|error)', found 'future<int>'", 81, 39);
        validateError(compileResult, index++,
                      "incompatible types: expected '((any & readonly)|error)', found 'stream<boolean>'", 81, 51);
        validateError(compileResult, index++,
                      "incompatible mapping constructor expression for type '(map<(any|error)>|map<future<int>>)'",
                      82, 43);

        validateError(compileResult, index++, "incompatible types: expected 'record {| int i; anydata...; |}', found " +
                "'record {| readonly (Details & readonly) d1; readonly (Details & readonly) d2; " +
                "record {| string str; |} d3; readonly record {| string str; readonly int count; |} & readonly d4; " +
                "int...; |}'", 108, 27);
        validateError(compileResult, index++, "cannot update 'readonly' record field 'd1' in 'record {| readonly " +
                "(Details & readonly) d1; readonly (Details & readonly) d2; record {| string str; |} d3; " +
                "readonly record {| string str; readonly int count; |} & readonly d4; int...; |}'", 109, 5);
        validateError(compileResult, index++, "cannot update 'readonly' record field 'd1' in 'record {| readonly " +
                "(Details & readonly) d1; readonly (Details & readonly) d2; record {| string str; |} d3; " +
                "readonly record {| string str; readonly int count; |} & readonly d4; int...; |}'", 113, 5);
        validateError(compileResult, index++, "incompatible types: expected 'readonly', found 'Details'", 120, 18);
        validateError(compileResult, index++, "incompatible types: expected 'readonly', found 'Details'", 122, 23);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }

    @Test
    public void testReadOnlyFieldsCodeAnalysisNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/readonly_field_code_analysis_negative.bal");
        int index = 0;

        validateError(compileResult, index++, "invalid key 'math': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 31, 9);
        validateError(compileResult, index++, "invalid key 'science': identifiers cannot be used as rest field keys, " +
                "expected a string literal or an expression", 39, 9);

        Assert.assertEquals(compileResult.getErrorCount(), index);
    }
}
