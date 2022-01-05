/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.test.validator;

import io.ballerina.shell.cli.jline.validator.InputValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for input validator.
 *
 * @since 2.0.0
 */
public class ValidatorTest {

    @Test
    public void  testValidator() {
        InputValidator inputValidator = new InputValidator();
        Assert.assertFalse(inputValidator.isIncomplete("int i = 12"));
        Assert.assertFalse(inputValidator.isIncomplete("int i = 12;"));
        Assert.assertFalse(inputValidator.isIncomplete("int[] x = [1,2];"));
        Assert.assertFalse(inputValidator.isIncomplete("foreach var emp in top3 {\n" +
                "        io:println(emp);\n" +
                "}"));
        Assert.assertFalse(inputValidator.isIncomplete("Employee[] employees = [\n" +
                "        {firstName: \"Jones\", lastName: \"Welsh\", salary: 1000.00},\n" +
                "    ];"));
        Assert.assertFalse(inputValidator.isIncomplete("Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending\n" +
                "\n" +
                "                      limit 3\n" +
                "\n" +
                "                      select e;"));
        Assert.assertFalse(inputValidator.isIncomplete("function name() {\n" +
                "}\n" +
                "\n" +
                "function foo() {\n" +
                "}"));
        Assert.assertFalse(inputValidator.isIncomplete("type Coord record {\n" +
                "    int x;\n" +
                "    int y;\n" +
                "};\n" +
                "\n" +
                "type Coord2 record {\n" +
                "    int a;\n" +
                "    int b;\n" +
                "};"));
        Assert.assertFalse(inputValidator.isIncomplete("function foo() {\n" +
                "}"));


        Assert.assertTrue(inputValidator.isIncomplete("x + "));
        Assert.assertTrue(inputValidator.isIncomplete("[1,2,3"));
        Assert.assertTrue(inputValidator.isIncomplete("int[] x = [1,2,3,"));
        Assert.assertTrue(inputValidator.isIncomplete("function parse(string s) returns int|error {"));
        Assert.assertTrue(inputValidator.isIncomplete("function parse(string s)"));
        Assert.assertTrue(inputValidator.isIncomplete("foreach var emp in top3"));
        Assert.assertTrue(inputValidator.isIncomplete("type Coord record"));
        Assert.assertTrue(inputValidator.isIncomplete("type Coord record {\n" + "int x;"));
        Assert.assertTrue(inputValidator.isIncomplete("Employee[] employees = [\n" +
                "        {firstName: \"Jones\", lastName: \"Welsh\", salary: 1000.00},"));
        Assert.assertTrue(inputValidator.isIncomplete("Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending"));
        Assert.assertTrue(inputValidator.isIncomplete(
                "Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending\n" +
                "\n" +
                "                      limit 3\n" +
                "\n" +
                "                      select"));
        Assert.assertTrue(inputValidator.isIncomplete("int[] evenNums = from var i in nums\n" +
                "                     where i % 2 == 0"));
    }
}
