/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing mapping constructor expression.
 * 
 * @since 1.3.0
 */
public class MappingConstructorTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testEmptyMap() {
        test("{}", "mapping-constructor/mapping_constructor_assert_01.json");
    }

    @Test
    public void testMapWithSpecificFields() {
        test("{age:20, \"name\":\"Supun\", address}", "mapping-constructor/mapping_constructor_assert_02.json");
    }

    @Test
    public void testMapWithComputedFields() {
        test("{[key1]: \"value1\", [\"key2\"]:value2}", "mapping-constructor/mapping_constructor_assert_03.json");
    }

    @Test
    public void testMapWithSpreadFields() {
        test("{...expr1, ... a+b}", "mapping-constructor/mapping_constructor_assert_04.json");
    }

    @Test
    public void testMapWithAllTypesOfFields() {
        test("{age:20, ...marks, \"name\":\"Supun\", address, [expr]: \"value\"}",
                "mapping-constructor/mapping_constructor_assert_05.json");
    }

    @Test
    public void testSpecificKeyWithPackageQualifier() {
        test("{a:b:c}", "mapping-constructor/mapping_constructor_assert_10.json");
    }

    @Test
    public void testNestedMappingConstructor() {
        test("{    age:20, \n" + 
                "  ...marks1,\n" + 
                "  \"name\":\"John\",\n" + 
                "  parent: { age:50,\n" + 
                "            ...marks2,\n" + 
                "            \"name\":\"Jane\",\n" + 
                "            address2,\n" + 
                "            [expr2]:\"value2\"\n" + 
                "           },\n" + 
                "  address,\n" + 
                "  [expr1]: \"value1\"\n" + 
                "}",
                "mapping-constructor/mapping_constructor_assert_15.json");
    }

    @Test
    public void testSpecificKeyWithReadOnly() {
        test("{ readonly a:b};", "mapping-constructor/mapping_constructor_assert_18.json");
    }

    // Recovery tests

    @Test
    public void testMissingClosingBrace() {
        test("{a:b", "mapping-constructor/mapping_constructor_assert_06.json");
    }

    @Test
    public void testMissingColon() {
        test("{a b, \"c\" d}", "mapping-constructor/mapping_constructor_assert_07.json");
    }

    @Test
    public void testMissingSpecificFieldRhs() {
        test("{a:, \"b\":}", "mapping-constructor/mapping_constructor_assert_08.json");
    }

    @Test
    public void testMapWithOnlyCommas() {
        test("{,,}", "mapping-constructor/mapping_constructor_assert_09.json");
    }

    @Test
    public void testSpecificKeyColons() {
        test("{a:b:c:d}", "mapping-constructor/mapping_constructor_assert_11.json");
    }

    @Test
    public void testComputedFieldWithMissingCloseBracket() {
        test("{[key1 value1}", "mapping-constructor/mapping_constructor_assert_12.json");
    }

    @Test
    public void testComputedFieldWithMissingRhs() {
        test("{[key1}", "mapping-constructor/mapping_constructor_assert_13.json");
    }

    @Test
    public void testSpreadFieldWithMissingExpr() {
        test("{... }", "mapping-constructor/mapping_constructor_assert_14.json");
    }

    @Test
    public void testNestedConstructorMissingCloseBrace() {
        test("{name:\"John\", parent: {\"name\": \"Doe\", age:20",
                "mapping-constructor/mapping_constructor_assert_16.json");
    }

    @Test
    public void testMissingFieldName() {
        test("{name:\"John\", :4}", "mapping-constructor/mapping_constructor_assert_17.json");
    }
    
    @Test
    public void testRecoveryInSpecificKeyWithReadOnly() {
        testFile("mapping-constructor/mapping_constructor_source_19.bal",
                "mapping-constructor/mapping_constructor_assert_19.json");
    }
}
