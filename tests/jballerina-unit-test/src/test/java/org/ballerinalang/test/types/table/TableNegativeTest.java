/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.table;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Negative test cases for table.
 *
 * @since 1.3.0
 */
public class TableNegativeTest {

    @Test
    public void testTableNegativeCases() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/table/table-negative.bal");
        int index = 0;

        validateError(compileResult, index++, "unknown type 'CusTable'",
                15, 1);
        validateError(compileResult, index++, "table key specifier mismatch. expected: '[id]' " +
                "but found '[id, firstName]'", 20, 28);
        validateError(compileResult, index++, "table key specifier mismatch with key constraint. " +
                "expected: '[string]' fields but key specifier is empty", 25, 20);
        validateError(compileResult, index++, "table key specifier '[age]' does not match with " +
                "key constraint type '[string]'", 30, 26);
        validateError(compileResult, index++, "table key specifier mismatch. expected: '[id]' but " +
                "found '[address]'", 35, 44);
        validateError(compileResult, index++, "member access is not supported for keyless table " +
                "'customerTable'", 45, 21);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found 'int'", 47, 7);
        validateError(compileResult, index++, "invalid member access with 'map': member access with " +
                "multi-key expression is only allowed with subtypes of 'table'", 52, 13);
        validateError(compileResult, index++, "field 'name' used in key specifier must be a readonly " +
                "field", 62, 34);
        validateError(compileResult, index++, "field 'name' used in key specifier must be a required " +
                "field", 75, 28);
        validateError(compileResult, index++, "value expression of key specifier 'id' must be a " +
                "constant expression", 82, 41);
        validateError(compileResult, index++, "member access is not supported for keyless table " +
                "'keylessCusTab'", 87, 27);
        validateError(compileResult, index++, "value expression of key specifier 'id' must be a " +
                "constant expression", 90, 33);
        validateError(compileResult, index++, "incompatible types: expected 'table<Customer> " +
                "key<string>', found 'table<Customer> key<int>'", 95, 56);
        validateError(compileResult, index++, "field name 'no' used in key specifier is not " +
                "found in table constraint type 'record {| int id; string name; string lname?; " +
                "string address?; |}'", 102, 21);
        validateError(compileResult, index++, "field 'address' used in key specifier must be a " +
                "readonly field", 108, 21);
        validateError(compileResult, index++, "table with constraint of type map cannot have key " +
                "specifier or key type constraint", 114, 21);
        validateError(compileResult, index++, "table with constraint of type map cannot have key " +
                "specifier or key type constraint", 120, 21);
        validateError(compileResult, index++, "cannot infer the member type from table constructor; " +
                "no values are provided in table constructor", 128, 25);
        validateError(compileResult, index++, "incompatible types: expected 'Customer', found 'Customer?'",
                135, 25);
        validateError(compileResult, index++, "incompatible types: expected 'User', found '(User|Customer)?'",
                141, 17);
        validateError(compileResult, index++, "incompatible types: expected 'Customer', found 'Customer?'",
                148, 25);
        validateError(compileResult, index++, "field 'name' used in key specifier must be a readonly field",
                156, 36);
        validateError(compileResult, index++, "invalid type 'k' for field 'Row' used in key specifier, " +
                "expected sub type of anydata", 169, 12);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a " +
                "constant expression", 170, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a " +
                "constant expression", 182, 5);
        validateError(compileResult, index++, "value expression of key specifier 'm' must be a " +
                "constant expression", 188, 5);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found 'any'", 191, 25);
        validateError(compileResult, index++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found 'any'", 194, 14);
        validateError(compileResult, index++, "field name 'id' used in key specifier is not " +
                "found in table constraint type 'Person'", 197, 19);
        validateError(compileResult, index++, "field name 'invalidField' used in key specifier " +
                "is not found in table constraint type 'Person'", 198, 19);
        validateError(compileResult, index++, "table key specifier '[leaves]' does not match " +
                "with key constraint type '[EmployeeId]'", 211, 47);
        validateError(compileResult, index++, "table key specifier mismatch with key constraint. " +
                "expected: '[string, string]' fields but found '[firstname]'", 213, 47);
        validateError(compileResult, index++, "field name 'firstname' used in key specifier " +
                "is not found in table constraint type 'CustomerDetail'", 230, 35);
        validateError(compileResult, index++, "value expression of key specifier 'id' must be " +
                "a constant expression", 237, 9);
        validateError(compileResult, index++, "incompatible types: expected 'table<record {| string name?; |}>',"
                + " found 'table<record {| (string|int|boolean) name?; (int|boolean)...; |}>'", 254, 41);
        validateError(compileResult, index++, "incompatible types: expected 'table<record {| string name?; |}>'," +
                " found 'table<record {| (string|int) name?; int...; |}>'", 263, 41);
        validateError(compileResult, index++, "incompatible types: expected 'table<record {| (string|int) name?; |}>'" +
                        ", found 'table<record {| (string|int) name?; int...; |}>'",
                264, 45);
        validateError(compileResult, index++, "incompatible types: expected 'int'," +
                " found 'table<record {| (int|string) a; |}>'", 276, 13);
        validateError(compileResult, index++, "incompatible types: expected 'int'," +
                " found 'table<record {| int i; int j?; never k?; string l?; never...; |}>'", 291, 13);
        validateError(compileResult, index++, "incompatible types: expected 'int'," +
                " found 'table<record {| (anydata|error) a; |}>'", 301, 13);
        validateError(compileResult, index++, "incompatible types: expected 'int'," +
                " found 'table<record {| (any|error) a; |}>'", 311, 13);
        validateError(compileResult, index++, "incompatible types: expected 'int'," +
                " found 'table<record {| (0|1|2|3) a; |}>'", 324, 13);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl1'", 334, 9);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl2'", 340, 9);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl3'", 346, 9);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl4'", 352, 9);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl5'", 358, 9);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl6'", 364, 9);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl7'", 370, 9);
        validateError(compileResult, index++, "cannot update 'table<Customer>' with member access expression", 378, 5);
        validateError(compileResult, index++, "cannot update 'table<Customer>' with member access expression", 384, 5);
        validateError(compileResult, index++, "cannot update 'table<record {| string name?; |}>' with " +
                        "member access expression", 390, 5);
        validateError(compileResult, index++, "cannot update 'table<record {| string name?; anydata...; |}>' with " +
                "member access expression", 396, 5);
        validateError(compileResult, index++, "cannot update 'table<(Customer & readonly)> & readonly' with " +
                "member access expression", 402, 5);
        validateError(compileResult, index++, "cannot update 'table<(record {| string name?; |} & readonly)> & " +
                "readonly' with member access expression", 408, 5);
        validateError(compileResult, index++, "cannot update 'table<(User|Customer)>' with member access " +
                "expression", 414, 5);
        validateError(compileResult, index++, "incompatible types: expected " +
                "'table<record {| int id; string firstName; string lastName; |}>', found 'CustomerEmptyKeyedTbl'",
                422, 76);
        validateError(compileResult, index++, "incompatible types: expected 'CustomerTable', " +
                        "found 'CustomerEmptyKeyedTbl'", 424, 23);
        validateError(compileResult, index++, "member access is not supported for keyless table 'tbl2'", 433, 9);
        validateError(compileResult, index++, "cannot update 'table<Customer>' with member access expression", 434, 5);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,int,int]'", 448, 21);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,string,string]'",
                462, 21);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,int,int,int]'", 469, 21);
        validateError(compileResult, index, "incompatible types: expected 'int', found '[int,int,int]'", 478, 21);
    }

    @Test
    public void testTableKeyViolations() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/table/table_key_violations.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 9);
        int index = 0;

        validateError(compileResult, index++, "duplicate key found in table row key('id') : '13'",
                9, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id, firstName') : '13, Foo'",
                15, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id') "
                        + ": 'BLangXMLElementLiteral: <BLangXMLQName: () id> </BLangXMLQName: () id> [][123]'",
                45, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : " +
                        "'BLangXMLElementLiteral: <BLangXMLQName: (p) id> </BLangXMLQName: (p) id> " +
                        "[BLangXMLAttribute: BLangXMLQName: (xmlns) p=BLangXMLQuotedString: (DOUBLE_QUOTE) " +
                        "[http://sample.com/wso2/e]][BLangXMLProcInsLiteral: [data], BLangXMLCommentLiteral: " +
                        "[Contents], BLangXMLElementLiteral: <BLangXMLQName: (p) empId> " +
                        "</BLangXMLQName: (p) empId> [][5005]]'",
                54, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('firstName') : '<string> " +
                        "(name is string && ! invalid?(BLangStringTemplateLiteral: [Hello , name, !!!]):James)'",
                64, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : '[5005, 5006]'",
                76, 5);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : ' '",
                102, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : " +
                        "'<(byte[] & readonly)> (base16 `5A`)'",
                128, 9);
        validateError(compileResult, index, "duplicate key found in table row key('id') : 'ID2'",
                136, 9);
    }

    @Test
    public void testAnyTypedTableWithKeySpecifiers() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/table/table-value-any-negative.bal");
        int index = 0;
        validateError(compileResult, index++,
                "key specifier not allowed when the target type is any", 18, 20);
        validateError(compileResult, index++,
                "key specifier not allowed when the target type is any", 25, 15);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }
}
