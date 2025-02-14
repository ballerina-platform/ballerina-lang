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
        validateError(compileResult, index++, "member access is not supported for table " +
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
        validateError(compileResult, index++, "member access is not supported for table " +
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
        validateError(compileResult, index++, "value expression of key specifier 'id' must be " +
                "a constant expression", 240, 9);
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
                " found 'table<record {| (Zero|NaturalNums) a; |}>'", 324, 13);
        validateError(compileResult, index++, "member access is not supported for table 'tbl1'", 334, 9);
        validateError(compileResult, index++, "member access is not supported for table 'tbl2'", 340, 9);
        validateError(compileResult, index++, "member access is not supported for table 'tbl3'", 346, 9);
        validateError(compileResult, index++, "member access is not supported for table 'tbl4'", 352, 9);
        validateError(compileResult, index++, "member access is not supported for table 'tbl5'", 358, 9);
        validateError(compileResult, index++, "member access is not supported for table 'tbl6'", 364, 9);
        validateError(compileResult, index++, "member access is not supported for table 'tbl7'", 370, 9);
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
        validateError(compileResult, index++, "member access is not supported for table 'tbl2'", 433, 9);
        validateError(compileResult, index++, "cannot update 'table<Customer>' with member access expression", 434, 5);
        validateError(compileResult, index++, "incompatible types: expected '(table<Student>|int)', " +
                "found 'table<record {| readonly int id; string firstName; string lastName; |}>'", 444, 28);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,int,int]'", 458, 21);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,string,string]'",
                472, 21);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,int,int,int]'", 479, 21);
        validateError(compileResult, index++, "incompatible types: expected 'int', found '[int,int,int]'", 488, 21);
        validateError(compileResult, index++, "value expression of key specifier 'x' must be a constant expression",
                501, 5);
        validateError(compileResult, index++, "value expression of key specifier 'x' must be a constant expression",
                502, 5);
        validateError(compileResult, index++, "value expression of key specifier 'x' must be a constant expression",
                506, 5);
        validateError(compileResult, index++, "value expression of key specifier 'x' must be a constant expression",
                507, 5);
        validateError(compileResult, index++, "value expression of key specifier 'x' must be a constant expression",
                519, 5);
        validateError(compileResult, index++, "value expression of key specifier 'y' must be a constant expression",
                519, 5);
        validateError(compileResult, index++, "value expression of key specifier 'x' must be a constant expression",
                520, 5);
        validateError(compileResult, index++, "value expression of key specifier 'y' must be a constant expression",
                520, 5);
        validateError(compileResult, index++, "value expression of key specifier 'z' must be a constant expression",
                520, 5);
        validateError(compileResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', " +
                "found 'table<Employee2>'", 533, 9);
        validateError(compileResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', " +
                "found 'table<Employee2>'", 539, 9);
        validateError(compileResult, index++, "incompatible types: expected 'never', found 'int'", 551, 29);
        validateError(compileResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', " +
                "found 'table<record {| int id; string name; |}>'", 563, 9);
        validateError(compileResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', " +
                "found 'table<record {| int id; |}>'", 566, 9);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                576, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                586, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                587, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                588, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                596, 23);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                605, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                606, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                607, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                608, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                616, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                625, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                626, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                627, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                628, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                629, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                630, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                631, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                632, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                633, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                634, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                642, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                643, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                644, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                645, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                646, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                647, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                648, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                649, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                650, 5);
        validateError(compileResult, index++, "value expression of key specifier 'k' must be a constant expression",
                651, 5);
        Assert.assertEquals(compileResult.getErrorCount(), index);
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
                        "(val > 10 && ! (val < 10)?(BLangStringTemplateLiteral: [Hello , name, !!!]):James)'",
                67, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : '[5005, 5006]'",
                79, 5);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : ' '",
                105, 9);
        validateError(compileResult, index++, "duplicate key found in table row key('id') : " +
                        "'<(byte[] & readonly)> (base16 `5A`)'",
                131, 9);
        validateError(compileResult, index, "duplicate key found in table row key('id') : 'ID2'",
                139, 9);
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
