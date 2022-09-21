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
    public void testValidator() {
        InputValidator inputValidator = new InputValidator();

        // Variable declarations
        Assert.assertTrue(inputValidator.isComplete("int i = 12;"));
        Assert.assertTrue(inputValidator.isComplete("int[] x = [1,2];"));
        Assert.assertTrue(inputValidator.isComplete("int n = flag ? 1 : 2;"));
        Assert.assertTrue(inputValidator.isComplete("int n = flag ? 1 : 2"));

        Assert.assertFalse(inputValidator.isComplete("int[] x = [1,2,3,"));
        Assert.assertFalse(inputValidator.isComplete("map<int> m = {"));

        // If-else statements
        Assert.assertTrue(inputValidator.isComplete("if (x == y) { }"));
        Assert.assertTrue(inputValidator.isComplete("if (x < y) { x = x + 1 }"));
        Assert.assertTrue(inputValidator.isComplete("if ret is error { }"));

        Assert.assertFalse(inputValidator.isComplete("if (x == y) { x = x + 1; x = x + 1;"));
        Assert.assertFalse(inputValidator.isComplete("if (x == y)"));
        Assert.assertFalse(inputValidator.isComplete("if ret is error"));

        // While statements
        Assert.assertTrue(inputValidator.isComplete("while (x < 10) { x = x + 1; }"));

        Assert.assertFalse(inputValidator.isComplete("while (x < y)"));

        // For-each statements
        Assert.assertTrue(inputValidator.isComplete("foreach var emp in top3 {\n" +
                "        io:println(emp);\n" +
                "}"));

        Assert.assertFalse(inputValidator.isComplete("foreach var emp in top3"));
        // TODO : fix #34989
        // Assert.assertFalse(inputValidator.isComplete("foreach Employee e in t"));

        // Function definitions
        Assert.assertTrue(inputValidator.isComplete("function foo() {\n" +
                "}"));
        Assert.assertTrue(inputValidator.isComplete("\n" +
                "\n" +
                "function sum2(float[] v) returns float {\n" +
                "    float r = 0.0;\n" +
                "\n" +
                "    foreach int i in 0 ..< v.length() {\n" +
                "        r += v[i];\n" +
                "    }\n" +
                "\n" +
                "    return r;\n" +
                "}"));
        Assert.assertTrue(inputValidator.isComplete("function name() {\n" +
                "}\n" +
                "\n" +
                "function foo() {\n" +
                "}"));
        Assert.assertTrue(inputValidator.isComplete("var f = function () { x = 12; }"));

        Assert.assertFalse(inputValidator.isComplete("function parse(string s) returns int|error {"));
        Assert.assertFalse(inputValidator.isComplete("function parse(string s)"));

        // Type definitions
        Assert.assertTrue(inputValidator.isComplete("type Coord record {\n" +
                "    int x;\n" +
                "    int y;\n" +
                "};\n" +
                "\n" +
                "type Coord2 record {\n" +
                "    int a;\n" +
                "    int b;\n" +
                "};"));

        Assert.assertFalse(inputValidator.isComplete("type Coord record"));
        Assert.assertFalse(inputValidator.isComplete("type Coord record {\n" + "int x;"));

        // Query Expressions
        Assert.assertTrue(inputValidator.isComplete("Employee[] employees = [\n" +
                "        {firstName: \"Jones\", lastName: \"Welsh\", salary: 1000.00},\n" +
                "    ];"));
        Assert.assertTrue(inputValidator.isComplete("Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending\n" +
                "\n" +
                "                      limit 3\n" +
                "\n" +
                "                      select e;"));

        Assert.assertFalse(inputValidator.isComplete("from var e in employees\n" +
                "                      order by e.salary descending"));
        Assert.assertFalse(inputValidator.isComplete("var names1 = from var {first: f, last: l} in persons"));
        Assert.assertFalse(inputValidator.isComplete("Employee[] employees = [\n" +
                "        {firstName: \"Jones\", lastName: \"Welsh\", salary: 1000.00},"));
        Assert.assertFalse(inputValidator.isComplete("Employee[] top3 = from var e in employees\n" +
                "                      order by e.salary descending"));
        Assert.assertFalse(inputValidator.isComplete(
                "Employee[] top3 = from var e in employees\n" +
                        "                      order by e.salary descending\n" +
                        "\n" +
                        "                      limit 3\n" +
                        "\n" +
                        "                      select"));
        Assert.assertFalse(inputValidator.isComplete("int[] evenNums = from var i in nums\n" +
                "                     where i % 2 == 0"));

        // Class definition
        Assert.assertTrue(inputValidator.isComplete("public class Counter { }"));

        Assert.assertFalse(inputValidator.isComplete("public class Counter {"));

        // Expressions
        Assert.assertTrue(inputValidator.isComplete("from var e in employees\n" +
                "                      order by e.salary descending\n" +
                "\n" +
                "                      limit 3\n" +
                "\n" +
                "                      select e;"));

        Assert.assertFalse(inputValidator.isComplete("x + "));
        Assert.assertFalse(inputValidator.isComplete("from var i in nums\n" +
                "                     where i % 2 == 0"));

        // Transaction statement
        Assert.assertFalse(inputValidator.isComplete("transaction {"));

        // Worker Declaration
        Assert.assertTrue(inputValidator.isComplete("worker { }"));

        Assert.assertFalse(inputValidator.isComplete("worker A {"));

        // Complete code examples
        Assert.assertTrue(inputValidator.isComplete("import ballerina/io;\n" +
                "\n" +
                "public function foo() {\n" +
                "\n" +
                "}\n" +
                "\n" +
                "public function foo2() {\n" +
                "\n" +
                "}"));
        Assert.assertTrue(inputValidator.isComplete("import ballerina/io;\n" +
                "\n" +
                "int x = 1;\n" +
                "\n" +
                "public function foo() {\n" +
                "\n" +
                "}\n" +
                "\n" +
                "public function name() {\n" +
                "\n" +
                "}"));
        Assert.assertTrue(inputValidator.isComplete("function foo() {\n" +
                "\n" +
                "    int x = 5;\n" +
                "\n" +
                "    if (x < 10) {\n" +
                "        io:println(x);\n" +
                "    }\n" +
                "\n" +
                "    while (x < 10) {\n" +
                "        x = x + 1;\n" +
                "    }\n" +
                "\n" +
                "}"));

        // Test case related to annotation not attached to construct error message
        Assert.assertTrue(inputValidator.isComplete("@http:ServiceConfig {\n" +
                "    cors: {\n" +
                "        allowOrigins: [\"http://www.m3.com\", \"http://www.hello.com\"],\n" +
                "        allowCredentials: false,\n" +
                "        allowHeaders: [\"CORELATION_ID\"],\n" +
                "        exposeHeaders: [\"X-CUSTOM-HEADER\"],\n" +
                "        maxAge: 84900\n" +
                "    }\n" +
                "}"));

        // Multiple function testcases
        Assert.assertFalse(inputValidator.isComplete("function name() {\n" +
                "    int x = 1;\n" +
                "}\n" +
                "\n" +
                "function name1() {\n" +
                "    name();"));
        Assert.assertFalse(inputValidator.isComplete("function name() {\n" +
                "    int x = 1\n" +
                "}\n" +
                "\n" +
                "function name1() {\n" +
                "    name();"));

        Assert.assertTrue(inputValidator.isComplete("function name() {\n" +
                "    int x = 1;\n" +
                "}\n" +
                "\n" +
                "function name1() {\n" +
                "    name();" +
                "}"));

        Assert.assertTrue(inputValidator.isComplete("function name() {\n" +
                "    int x = 1\n" +
                "}\n" +
                "\n" +
                "function name1() {\n" +
                "    name();" +
                "}"));

        Assert.assertTrue(inputValidator.isComplete("function name() {\n" +
                "    int x = 1\n" +
                "}\n" +
                "\n" +
                "function name1() {\n" +
                "    name();" +
                "}"));

        // Command related testcases
        Assert.assertTrue(inputValidator.isComplete(" "));
        Assert.assertTrue(inputValidator.isComplete("/remove a"));
        Assert.assertTrue(inputValidator.isComplete("/help"));
        Assert.assertTrue(inputValidator.isComplete("/help topic"));
        Assert.assertTrue(inputValidator.isComplete("/vars"));
        Assert.assertTrue(inputValidator.isComplete("/file file.bal"));
        Assert.assertTrue(inputValidator.isComplete("/remove a b"));
        Assert.assertTrue(inputValidator.isComplete("/remove a b c d function1"));
    }
}
