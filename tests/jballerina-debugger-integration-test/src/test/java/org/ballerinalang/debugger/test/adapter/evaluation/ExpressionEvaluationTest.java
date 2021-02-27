/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugger.test.adapter.evaluation;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.Test;

/**
 * Test implementation for debug expression evaluation scenarios.
 */
public class ExpressionEvaluationTest extends ExpressionEvaluationBaseTest {

    @Override
    @Test
    public void literalEvaluationTest() throws BallerinaTestException {
        // nil literal
        debugTestRunner.assertExpression(context, "()", "()", "nil");
        // boolean literal
        debugTestRunner.assertExpression(context, "true", "true", "boolean");
        // numeric literal (int)
        debugTestRunner.assertExpression(context, "10", "10", "int");
        // numeric literal (float)
        debugTestRunner.assertExpression(context, "20.0", "20.0", "float");
        // Todo - add following tests after the implementation
        //  - hex int
        //  - hex float
        //  - string literal
        //  - byte-array literal
    }

    @Override
    @Test
    public void listConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void mappingConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void stringTemplateEvaluationTest() throws BallerinaTestException {
        // without interpolations
        debugTestRunner.assertExpression(context, "string `name: John, age: 20`", "name: John, age: 20", "string");
        // with interpolations
        debugTestRunner.assertExpression(context, "string `name: ${" + STRING_VAR + "}, age: ${" + INT_VAR + "}`",
                "name: foo, age: 20", "string");
    }

    @Override
    @Test
    public void xmlTemplateEvaluationTest() throws BallerinaTestException {
        // XML element
        debugTestRunner.assertExpression(context, "xml `<book>The Lost World</book>`", "<book>The Lost World</book>",
            "xml");
        // Todo - enable after https://github.com/ballerina-platform/ballerina-lang/issues/26589 is fixed from the
        //  runtime.
        // XML text
        // debugTestRunner.assertExpression(context, "xml `Hello, world!`", "Hello, world!", "xml");
        // XML comment
        // debugTestRunner.assertExpression(context, "xml `<!--I am a comment-->`", "<!--I am a comment", "xml");
        // XML processing instruction
        // debugTestRunner.assertExpression(context, "xml `<?target data?>`", "<?target data?>", "xml");
        // concatenated XML
        // assertExpression(context, "xml `<book>The Lost World</book>Hello, world!<!--I am a comment--><?target
        // data?>`", "<?target data?>", "xml");
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void variableReferenceEvaluationTest() throws BallerinaTestException {
        // var variable test
        debugTestRunner.assertExpression(context, NIL_VAR, "()", "nil");
        // boolean variable test
        debugTestRunner.assertExpression(context, BOOLEAN_VAR, "true", "boolean");
        // int variable test
        debugTestRunner.assertExpression(context, INT_VAR, "20", "int");
        // float variable test
        debugTestRunner.assertExpression(context, FLOAT_VAR, "-10.0", "float");
        // decimal variable test
        debugTestRunner.assertExpression(context, DECIMAL_VAR, "3.5", "decimal");
        // string variable test
        debugTestRunner.assertExpression(context, STRING_VAR, "foo", "string");
        // xml variable test
        debugTestRunner.assertExpression(context, XML_VAR, "<person " +
                "gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>", "xml");
        // array variable test
        debugTestRunner.assertExpression(context, ARRAY_VAR, "any[4]", "array");
        // tuple variable test
        debugTestRunner.assertExpression(context, TUPLE_VAR, "tuple[int,string]", "tuple");
        // map variable test
        debugTestRunner.assertExpression(context, MAP_VAR, "map", "map");
        // record variable test (Student record)
        debugTestRunner.assertExpression(context, RECORD_VAR, " /:@[`{~⌤_123_ƮέŞŢ_Student", "record");
        // anonymous record variable test
        debugTestRunner.assertExpression(context, ANON_RECORD_VAR,
                                         "record {| string city; string country; |}", "record");
        // error variable test
        debugTestRunner.assertExpression(context, ERROR_VAR, "SimpleErrorType", "error");
        // anonymous function variable test
        debugTestRunner.assertExpression(context, ANON_FUNCTION_VAR, "function (string,string) returns (string)",
            "function");
        // future variable test
        debugTestRunner.assertExpression(context, FUTURE_VAR, "future", "future");
        // object variable test (Person object)
        debugTestRunner.assertExpression(context, OBJECT_VAR, "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");
        // type descriptor variable test
        debugTestRunner.assertExpression(context, TYPEDESC_VAR, "int", "typedesc");
        // union variable test
        debugTestRunner.assertExpression(context, UNION_VAR, "foo", "string");
        // optional variable test
        debugTestRunner.assertExpression(context, OPTIONAL_VAR, "foo", "string");
        // any variable test
        debugTestRunner.assertExpression(context, ANY_VAR, "15.0", "float");
        // anydata variable test
        debugTestRunner.assertExpression(context, ANYDATA_VAR, "619", "int");
        // byte variable test
        debugTestRunner.assertExpression(context, BYTE_VAR, "128", "int");
        // table variable test
        debugTestRunner.assertExpression(context, TABLE_VAR, "table<Employee>[3]", "table");
        // stream variable test
        debugTestRunner.assertExpression(context, STREAM_VAR, "stream<int>", "stream");
        // never variable test
        debugTestRunner.assertExpression(context, NEVER_VAR, "", "xml");
        // json variable test
        debugTestRunner.assertExpression(context, JSON_VAR, "map<json>", "json");
        // anonymous object variable test (AnonPerson object)
        debugTestRunner.assertExpression(context, ANON_OBJECT_VAR, "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");

        // Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/26139
        // debugTestRunner.assertExpression(context, GL, "Ballerina", "string");
        // debugTestRunner.assertExpression(context, "gv02_nameWithType", "Ballerina", "string");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_03, "map", "map");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_04, "()", "nil");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_05, "()", "nil");
        // global variables
        debugTestRunner.assertExpression(context, GLOBAL_VAR_06, "Ballerina", "string");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_07, "100.0", "decimal");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_08, "2", "int");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_09, "2.0", "float");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_10, "map<json>", "json");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_11, "IL with global var", "string");

        // Todo - add test for qualified name references, after adding support
    }

    @Override
    @Test
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // objects fields
        debugTestRunner.assertExpression(context, OBJECT_VAR + ".address", "No 20, Palm grove", "string");
        // record fields
        debugTestRunner.assertExpression(context, RECORD_VAR + ".'Ȧɢέ_\\ \\/\\:\\@\\[\\`\\{\\~⌤", "20", "int");
        // json fields
        debugTestRunner.assertExpression(context, JSON_VAR + ".name", "John", "string");
        // nested field access (chain access)
        debugTestRunner.assertExpression(context, RECORD_VAR + ".grades.maths", "80", "int");
        // optional field access
        debugTestRunner.assertExpression(context, RECORD_VAR + "?.undefined", "()", "nil");
    }

    @Override
    @Test
    public void xmlAttributeAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void annotationAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void memberAccessEvaluationTest() throws BallerinaTestException {
        // strings
        debugTestRunner.assertExpression(context, STRING_VAR + "[0]", "f", "string");
        // lists
        debugTestRunner.assertExpression(context, ARRAY_VAR + "[0]", "1", "int");
        // maps
        // debugTestRunner.assertExpression(context, mapVar + "[\"country\"]", "Sri Lanka", "string");
        // debugTestRunner.assertExpression(context, mapVar + "[\"undefined\"]", "()", "nil");
        // json
        // debugTestRunner.assertExpression(context, jsonVar + "[\"color\"]", "red", "string");
        // debugTestRunner.assertExpression(context, jsonVar + "[\"undefined\"]", "()", "nil");
        // Todo - add following tests after the implementation
        //  - xml member access
    }

    @Override
    @Test
    public void functionCallEvaluationTest() throws BallerinaTestException {

        // ---------------------- Required Parameters + named arguments ---------------------------------

        // Arguments for required parameters can be passed as positional arguments.
        // Positional arguments need to be passed in the expected order.
        debugTestRunner.assertExpression(context, " calculate ( 5 , 6 , 7 ) ;", "38", "int");

        // Arguments for required parameters can also be passed as named arguments.
        // Named arguments do not have to be specified in the order in which the parameters are defined.
        debugTestRunner.assertExpression(context, "calculate ( 5 , c  =  7 , b  =  6 );", "38", "int");

        // ---------------------- Defaultable Parameters + named arguments  ---------------------------------

        // Call the function by passing a value only for the `baseSalary` parameter.
        // The `annualIncrement` and `bonusRate` parameters default to 20 and 0.02 respectively.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500)", "[2500, 20, 0.02]", "string");

        // Call the function by passing values only for the `baseSalary` and `annualIncrement`
        // parameters. The value for the `annualIncrement` parameter is passed as a named argument.
        // The `bonusRate` parameter defaults to 0.02.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, annualIncrement = 100)",
            "[2500, 100, 0.02]", "string");

        // Call the function again by passing values only for the `baseSalary` and `annualIncrement`
        // parameters, now passing the value for the `annualIncrement` parameter as a positional argument.
        // The `bonusRate` parameter defaults to 0.02.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, 100);", "[2500, 100, 0.02]", "string");

        // Call the function by passing values only for the `baseSalary` and `bonusRate` parameters.
        // The `annualIncrement` parameter defaults to 20.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, bonusRate = 0.1);", "[2500, 20, 0.1]",
            "string");

        // In order to pass the value for `bonusRate` as a positional argument, a value would
        // have to be specified for the `annualIncrement` parameter too.
        // All arguments are positional arguments here.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, 20, 0.1);", "[2500, 20, 0.1]", "string");

        // Call the function by passing values for all three parameters, the first argument as
        // a positional argument and the rest as named arguments.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);",
                "[2500, 100, 0.1]", "string");

        // Call the function by passing all three arguments as named arguments.
        // Any and all arguments after the first named argument need to be specified
        // as named arguments but could be specified in any order.
        debugTestRunner.assertExpression(context,
            "printSalaryDetails(annualIncrement = 100, baseSalary = 2500, bonusRate = 0.1);",
                "[2500, 100, 0.1]", "string");

        // ----------------------------  Rest Parameters  ------------------------------------------
        // Todo - Enable once the debugger runtime helper module is restored.

        // Call the function by passing only the required parameter.
        // debugTestRunner.assertExpression(context, "printDetails(\"Alice\");", "[2500, 20, 0.02]", "string");

        // Call the function by passing the required parameter and the defaultable parameter. Named arguments can
        // also be used since values are not passed for the rest parameter.
        // debugTestRunner.assertExpression(context, "printDetails(\"Bob\", 20);", "[2500, 20, 0.02]", "string");

        // Call the function by passing the required parameter, the defaultable parameter, and one value for the rest
        // parameter. Arguments cannot be passed as named arguments since values are specified for the rest parameter.
        // debugTestRunner.assertExpression(context, "printDetails(\"Corey\", 19, \"Math\");", "[2500, 20, 0.02]",
        // "string");

        // Call the function by passing the required parameter, defaultable parameter,
        // and multiple values for the rest parameter.
        // debugTestRunner.assertExpression(context, "printDetails(\"Diana\", 20, \"Math\", \"Physics\");",
        // "[2500, 20, 0.02]", "string");

        // Pass an array as the rest parameter instead of calling the
        // function by passing each value separately.
        // debugTestRunner.assertExpression(context, "printDetails(\"Diana\", 20, ...modules);", "[2500, 20, 0.02]",
        // "string");
    }

    @Override
    @Test
    public void methodCallEvaluationTest() throws BallerinaTestException {

        // 1. object methods
        debugTestRunner.assertExpression(context, OBJECT_VAR + ".getSum(34,56)", "90", "int");

        // 2. lang library methods

        // array
        // Todo - Enable after semantic API fixes (https://github.com/ballerina-platform/ballerina-lang/issues/27520)
        // debugTestRunner.assertExpression(context, ARRAY_VAR + ".length()", "4", "int");
        // debugTestRunner.assertExpression(context, ARRAY_VAR + ".slice(1,3)", "any[2]", "array");

        // Todo - boolean
        // decimal
        debugTestRunner.assertExpression(context, DECIMAL_VAR + ".round()", "4", "decimal");
        debugTestRunner.assertExpression(context, DECIMAL_VAR + ".abs()", "3.5", "decimal");
        // error
        debugTestRunner.assertExpression(context, ERROR_VAR + ".message()", "SimpleErrorType", "string");
        // float
        debugTestRunner.assertExpression(context, FLOAT_VAR + ".sin()", "0.5440211108893698", "float");
        debugTestRunner.assertExpression(context, FLOAT_VAR + ".pow(3.0)", "-1000.0", "float");
        // future
        debugTestRunner.assertExpression(context, FUTURE_VAR + ".cancel()", "()", "nil");
        // int
        debugTestRunner.assertExpression(context, INT_VAR + ".abs()", "20", "int");
        // map
        debugTestRunner.assertExpression(context, MAP_VAR + ".get(\"country\")", "Sri Lanka", "string");
        // Todo - object
        // Todo - query
        // Todo - stream
        // string
        debugTestRunner.assertExpression(context, STRING_VAR + ".getCodePoint(1)", "111", "int");
        debugTestRunner.assertExpression(context, STRING_VAR + ".substring(1,3)", "oo", "string");
        // Todo - table
        // Todo - typedesc

        // value
        // Todo - Enable after semantic API fixes (https://github.com/ballerina-platform/ballerina-lang/issues/27520)
        // debugTestRunner.assertExpression(context, TYPEDESC_VAR + ".toBalString()", "typedesc int", "string");

        // xml
        debugTestRunner.assertExpression(context, XML_VAR + ".getName()", "person", "string");
        debugTestRunner.assertExpression(context, XML_VAR + ".children()",
                "<firstname>Praveen</firstname><lastname>Nada</lastname>", "xml");
    }

    @Override
    @Test
    public void errorConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void anonymousFunctionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void letExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void typeCastEvaluationTest() throws BallerinaTestException {
        // casting into a basic type
        debugTestRunner.assertExpression(context, String.format("<float>%s", ANYDATA_VAR), "619.0", "float");
        // casting into a union type
        debugTestRunner.assertExpression(context, String.format("<float|boolean>%s", ANYDATA_VAR), "619.0", "float");
    }

    @Override
    @Test
    public void typeOfExpressionEvaluationTest() throws BallerinaTestException {
        // primitive types
        debugTestRunner.assertExpression(context, String.format("typeof %s", BOOLEAN_VAR), "boolean", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof %s", INT_VAR), "int", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof %s", FLOAT_VAR), "float", "typedesc");
        // reference types
        debugTestRunner.assertExpression(context, String.format("typeof %s", JSON_VAR), "map<json>", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof %s[0]", STRING_VAR), "string", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof typeof %s", BOOLEAN_VAR), "typedesc",
            "typedesc");
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        // unary plus operator
        // int
        debugTestRunner.assertExpression(context, String.format("+%s", INT_VAR), "20", "int");
        // float
        debugTestRunner.assertExpression(context, String.format("+%s", FLOAT_VAR), "-10.0", "float");
        // decimal
        debugTestRunner.assertExpression(context, String.format("+%s", DECIMAL_VAR), "3.5", "decimal");

        // unary minus operator
        // int
        debugTestRunner.assertExpression(context, String.format("-%s", INT_VAR), "-20", "int");
        // float
        debugTestRunner.assertExpression(context, String.format("-%s", FLOAT_VAR), "10.0", "float");
        // decimal
        debugTestRunner.assertExpression(context, String.format("-%s", DECIMAL_VAR), "-3.5", "decimal");

        // unary invert operator
        // int
        debugTestRunner.assertExpression(context, String.format("~%s", INT_VAR), "-21", "int");

        // unary negation operator
        // boolean
        debugTestRunner.assertExpression(context, String.format("!%s", BOOLEAN_VAR), "false", "boolean");
    }

    @Override
    @Test
    public void multiplicativeExpressionEvaluationTest() throws BallerinaTestException {
        ///////////////////////////////-----------multiplication----------------//////////////////////////////////////
        // int * int
        debugTestRunner.assertExpression(context, String.format("%s * %s", INT_VAR, INT_VAR), "400", "int");
        // float * int
        debugTestRunner.assertExpression(context, String.format("%s * %s", FLOAT_VAR, INT_VAR), "-200.0", "float");
        // int * float
        debugTestRunner.assertExpression(context, String.format("%s * %s", INT_VAR, FLOAT_VAR), "-200.0", "float");
        // float * float
        debugTestRunner.assertExpression(context, String.format("%s * %s", FLOAT_VAR, FLOAT_VAR), "100.0", "float");
        // decimal * decimal
        debugTestRunner.assertExpression(context, String.format("%s * %s", DECIMAL_VAR, DECIMAL_VAR), "12.25",
            "decimal");
        // int * decimal
        debugTestRunner.assertExpression(context, String.format("%s * %s", INT_VAR, DECIMAL_VAR), "70.00", "decimal");
        // decimal * int
        debugTestRunner.assertExpression(context, String.format("%s * %s", DECIMAL_VAR, INT_VAR), "70.00", "decimal");
        // float * decimal
        debugTestRunner.assertExpression(context, String.format("%s * %s", FLOAT_VAR, DECIMAL_VAR), "-35.0", "decimal");
        // decimal * float
        debugTestRunner.assertExpression(context, String.format("%s * %s", DECIMAL_VAR, FLOAT_VAR), "-35.0", "decimal");

        ///////////////////////////////-------------division--------------------//////////////////////////////////////
        // int / int
        debugTestRunner.assertExpression(context, String.format("%s / %s", INT_VAR, INT_VAR), "1", "int");
        // float / int
        debugTestRunner.assertExpression(context, String.format("%s / %s", FLOAT_VAR, INT_VAR), "-0.5", "float");
        // int / float
        debugTestRunner.assertExpression(context, String.format("%s / %s", INT_VAR, FLOAT_VAR), "-2.0", "float");
        // float / float
        debugTestRunner.assertExpression(context, String.format("%s / %s", FLOAT_VAR, FLOAT_VAR), "1.0", "float");
        // decimal / decimal
        debugTestRunner.assertExpression(context, String.format("%s / %s", DECIMAL_VAR, DECIMAL_VAR), "1", "decimal");
        // int / decimal
        debugTestRunner.assertExpression(context, String.format("%s / %s", INT_VAR, DECIMAL_VAR),
        "5.714285714285714285714285714285714",
                "decimal");
        // decimal / int
        debugTestRunner.assertExpression(context, String.format("%s / %s", DECIMAL_VAR, INT_VAR), "0.175", "decimal");
        // float / decimal
        debugTestRunner.assertExpression(context, String.format("%s / %s", FLOAT_VAR, DECIMAL_VAR),
                "-2.857142857142857142857142857142857", "decimal");
        // decimal / float
        debugTestRunner.assertExpression(context, String.format("%s / %s", DECIMAL_VAR, FLOAT_VAR), "-0.35", "decimal");

        /////////////////////////////////-----------modulus----------------///////////////////////////////////////////
        // int % int
        debugTestRunner.assertExpression(context, String.format("%s %% %s", INT_VAR, INT_VAR), "0", "int");
        // float % int
        debugTestRunner.assertExpression(context, String.format("%s %% %s", FLOAT_VAR, INT_VAR), "-10.0", "float");
        // int % float
        debugTestRunner.assertExpression(context, String.format("%s %% %s", INT_VAR, FLOAT_VAR), "0.0", "float");
        // float % float
        debugTestRunner.assertExpression(context, String.format("%s %% %s", FLOAT_VAR, FLOAT_VAR), "-0.0", "float");
        // decimal % decimal
        debugTestRunner.assertExpression(context, String.format("%s %% %s", DECIMAL_VAR, DECIMAL_VAR), "0.0",
            "decimal");
        // int % decimal
        debugTestRunner.assertExpression(context, String.format("%s %% %s", INT_VAR, DECIMAL_VAR), "2.5", "decimal");
        // decimal % int
        debugTestRunner.assertExpression(context, String.format("%s %% %s", DECIMAL_VAR, INT_VAR), "3.5", "decimal");
    }

    @Override
    @Test
    public void additiveExpressionEvaluationTest() throws BallerinaTestException {
        //////////////////////////////-------------addition------------------/////////////////////////////////////////
        // int + int
        debugTestRunner.assertExpression(context, String.format("%s + %s", INT_VAR, INT_VAR), "40", "int");
        // float + int
        debugTestRunner.assertExpression(context, String.format("%s + %s", FLOAT_VAR, INT_VAR), "10.0", "float");
        // int + float
        debugTestRunner.assertExpression(context, String.format("%s + %s", INT_VAR, FLOAT_VAR), "10.0", "float");
        // float + float
        debugTestRunner.assertExpression(context, String.format("%s + %s", FLOAT_VAR, FLOAT_VAR), "-20.0", "float");
        // decimal + decimal
        debugTestRunner.assertExpression(context, String.format("%s + %s", DECIMAL_VAR, DECIMAL_VAR), "7.0", "decimal");
        // int + decimal
        debugTestRunner.assertExpression(context, String.format("%s + %s", INT_VAR, DECIMAL_VAR), "23.5", "decimal");
        // decimal + int
        debugTestRunner.assertExpression(context, String.format("%s + %s", DECIMAL_VAR, INT_VAR), "23.5", "decimal");
        // float + decimal
        debugTestRunner.assertExpression(context, String.format("%s + %s", FLOAT_VAR, DECIMAL_VAR), "-6.5", "decimal");
        // decimal + float
        debugTestRunner.assertExpression(context, String.format("%s + %s", DECIMAL_VAR, FLOAT_VAR), "-6.5", "decimal");

        // string + string
        debugTestRunner.assertExpression(context, String.format("%s + %s", STRING_VAR, STRING_VAR), "foofoo", "string");
        // string with leading and trailing whitespaces
        debugTestRunner.assertExpression(context, "\" one \" + \" two \" + \" three \"", " one  two  three ", "string");
        // string template concatenation
        String bStringTemplateExpr = String.format("string `name: ${%s}, age: ${%s}`", STRING_VAR, INT_VAR);
        debugTestRunner.assertExpression(context, String.format("%s + %s + %s", bStringTemplateExpr,
                bStringTemplateExpr, bStringTemplateExpr), "name: foo, age: 20name: foo, age: 20name: foo, age: 20",
                "string");

        // xml + xml
        debugTestRunner.assertExpression(context, String.format("%s + %s", XML_VAR, XML_VAR),
            "<person gender=\"male\">" +
                "<firstname>Praveen</firstname><lastname>Nada</lastname></person><person gender=\"male\">" +
                "<firstname>Praveen</firstname><lastname>Nada</lastname></person>", "xml");

        //////////////////////////////-------------subtraction------------------//////////////////////////////////////
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s - %s", INT_VAR, INT_VAR), "0", "int");
        // float - int
        debugTestRunner.assertExpression(context, String.format("%s - %s", FLOAT_VAR, INT_VAR), "-30.0", "float");
        // int - float
        debugTestRunner.assertExpression(context, String.format("%s - %s", INT_VAR, FLOAT_VAR), "30.0", "float");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s - %s", FLOAT_VAR, FLOAT_VAR), "0.0", "float");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s - %s", DECIMAL_VAR, DECIMAL_VAR), "0.0", "decimal");
        // int - decimal
        debugTestRunner.assertExpression(context, String.format("%s - %s", INT_VAR, DECIMAL_VAR), "16.5", "decimal");
        // decimal - int
        debugTestRunner.assertExpression(context, String.format("%s - %s", DECIMAL_VAR, INT_VAR), "-16.5", "decimal");
        // float - decimal
        debugTestRunner.assertExpression(context, String.format("%s - %s", FLOAT_VAR, DECIMAL_VAR), "-13.5", "decimal");
        // decimal - float
        debugTestRunner.assertExpression(context, String.format("%s - %s", DECIMAL_VAR, FLOAT_VAR), "13.5", "decimal");
    }

    @Override
    @Test
    public void shiftExpressionEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertExpression(context, String.format("%s << %s", INT_VAR, INT_VAR), "20971520", "int");
        debugTestRunner.assertExpression(context, String.format("%s << %s", SIGNED32INT_VAR, SIGNED8INT_VAR), "0",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s << %s", SIGNED32INT_VAR, UNSIGNED8INT_VAR), "-2000",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s << %s", UNSIGNED32INT_VAR, SIGNED8INT_VAR), "0",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s << %s", UNSIGNED32INT_VAR, UNSIGNED8INT_VAR),
            "2000", "int");

        debugTestRunner.assertExpression(context, String.format("%s >> %s", INT_VAR, INT_VAR), "0", "int");
        debugTestRunner.assertExpression(context, String.format("%s >> %s", SIGNED32INT_VAR, SIGNED8INT_VAR), "-1",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s >> %s", SIGNED32INT_VAR, UNSIGNED8INT_VAR), "-500",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s >> %s", UNSIGNED32INT_VAR, SIGNED8INT_VAR), "0",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s >> %s", UNSIGNED32INT_VAR, UNSIGNED8INT_VAR), "500",
            "int");

        debugTestRunner.assertExpression(context, String.format("%s >>> %s", INT_VAR, INT_VAR), "0", "int");
        debugTestRunner.assertExpression(context, String.format("%s >>> %s", SIGNED32INT_VAR, SIGNED8INT_VAR), "1",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s >>> %s", SIGNED32INT_VAR, UNSIGNED8INT_VAR),
        "9223372036854775308",
                "int");
        debugTestRunner.assertExpression(context, String.format("%s >>> %s", UNSIGNED32INT_VAR, SIGNED8INT_VAR), "0",
            "int");
        debugTestRunner.assertExpression(context, String.format("%s >>> %s", UNSIGNED32INT_VAR, UNSIGNED8INT_VAR),
            "500", "int");
    }

    @Override
    @Test
    public void rangeExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void comparisonEvaluationTest() throws BallerinaTestException {
        // expression < expression
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s < %s", INT_VAR, INT_VAR), "false", "boolean");
        // float - int
        debugTestRunner.assertExpression(context, String.format("%s < %s", FLOAT_VAR, INT_VAR), "true", "boolean");
        // int - float
        debugTestRunner.assertExpression(context, String.format("%s < %s", INT_VAR, FLOAT_VAR), "false", "boolean");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s < %s", FLOAT_VAR, FLOAT_VAR), "false", "boolean");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s < %s", DECIMAL_VAR, DECIMAL_VAR), "false",
            "boolean");
        // int - decimal
        debugTestRunner.assertExpression(context, String.format("%s < %s", INT_VAR, DECIMAL_VAR), "false", "boolean");
        // decimal - int
        debugTestRunner.assertExpression(context, String.format("%s < %s", DECIMAL_VAR, INT_VAR), "true", "boolean");
        // float - decimal
        debugTestRunner.assertExpression(context, String.format("%s < %s", FLOAT_VAR, DECIMAL_VAR), "true", "boolean");
        // decimal - float
        debugTestRunner.assertExpression(context, String.format("%s < %s", DECIMAL_VAR, FLOAT_VAR), "false", "boolean");

        // expression > expression
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s > %s", INT_VAR, INT_VAR), "false", "boolean");
        // float - int
        debugTestRunner.assertExpression(context, String.format("%s > %s", FLOAT_VAR, INT_VAR), "false", "boolean");
        // int - float
        debugTestRunner.assertExpression(context, String.format("%s > %s", INT_VAR, FLOAT_VAR), "true", "boolean");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s > %s", FLOAT_VAR, FLOAT_VAR), "false", "boolean");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s > %s", DECIMAL_VAR, DECIMAL_VAR), "false",
            "boolean");
        // int - decimal
        debugTestRunner.assertExpression(context, String.format("%s > %s", INT_VAR, DECIMAL_VAR), "true", "boolean");
        // decimal - int
        debugTestRunner.assertExpression(context, String.format("%s > %s", DECIMAL_VAR, INT_VAR), "false", "boolean");
        // float - decimal
        debugTestRunner.assertExpression(context, String.format("%s > %s", FLOAT_VAR, DECIMAL_VAR), "false", "boolean");
        // decimal - float
        debugTestRunner.assertExpression(context, String.format("%s > %s", DECIMAL_VAR, FLOAT_VAR), "true", "boolean");

        // expression <= expression
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s <= %s", INT_VAR, INT_VAR), "true", "boolean");
        // float - int
        debugTestRunner.assertExpression(context, String.format("%s <= %s", FLOAT_VAR, INT_VAR), "true", "boolean");
        // int - float
        debugTestRunner.assertExpression(context, String.format("%s <= %s", INT_VAR, FLOAT_VAR), "false", "boolean");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s <= %s", FLOAT_VAR, FLOAT_VAR), "true", "boolean");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s <= %s", DECIMAL_VAR, DECIMAL_VAR), "true",
            "boolean");
        // int - decimal
        debugTestRunner.assertExpression(context, String.format("%s <= %s", INT_VAR, DECIMAL_VAR), "false", "boolean");
        // decimal - int
        debugTestRunner.assertExpression(context, String.format("%s <= %s", DECIMAL_VAR, INT_VAR), "true", "boolean");
        // float - decimal
        debugTestRunner.assertExpression(context, String.format("%s <= %s", FLOAT_VAR, DECIMAL_VAR), "true", "boolean");
        // decimal - float
        debugTestRunner.assertExpression(context, String.format("%s <= %s", DECIMAL_VAR, FLOAT_VAR), "false",
            "boolean");

        // expression >= expression
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s >= %s", INT_VAR, INT_VAR), "true", "boolean");
        // float - int
        debugTestRunner.assertExpression(context, String.format("%s >= %s", FLOAT_VAR, INT_VAR), "false", "boolean");
        // int - float
        debugTestRunner.assertExpression(context, String.format("%s >= %s", INT_VAR, FLOAT_VAR), "true", "boolean");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s >= %s", FLOAT_VAR, FLOAT_VAR), "true", "boolean");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s >= %s", DECIMAL_VAR, DECIMAL_VAR), "true",
            "boolean");
        // int - decimal
        debugTestRunner.assertExpression(context, String.format("%s >= %s", INT_VAR, DECIMAL_VAR), "true", "boolean");
        // decimal - int
        debugTestRunner.assertExpression(context, String.format("%s >= %s", DECIMAL_VAR, INT_VAR), "false", "boolean");
        // float - decimal
        debugTestRunner.assertExpression(context, String.format("%s >= %s", FLOAT_VAR, DECIMAL_VAR), "false",
            "boolean");
        // decimal - float
        debugTestRunner.assertExpression(context, String.format("%s >= %s", DECIMAL_VAR, FLOAT_VAR), "true", "boolean");
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        // predefined types
        debugTestRunner.assertExpression(context, String.format("%s is string", INT_VAR), "false", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s is int", INT_VAR), "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s is error", ERROR_VAR), "true", "boolean");
        // union types
        debugTestRunner.assertExpression(context, String.format("%s is int | string", STRING_VAR), "true", "boolean");
        // other named types
        debugTestRunner.assertExpression(context, String.format("%s is 'Person_\\\\\\ \\/\\<\\>\\:\\@\\[\\`\\{\\~" +
                "\\u{2324}_ƮέŞŢ", OBJECT_VAR), "true", "boolean");
        // Todo: add tests for full qualified type resolving, after adding support
    }

    @Override
    @Test
    public void equalityEvaluationTest() throws BallerinaTestException {
        // value equality
        debugTestRunner.assertExpression(context, "2.0==2.00", "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s==%s", INT_VAR, FLOAT_VAR), "false", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s==%s", OBJECT_VAR, ANON_OBJECT_VAR), "false",
            "boolean");
        debugTestRunner.assertExpression(context, "2.0!=2.00", "false", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s!=%s", INT_VAR, FLOAT_VAR), "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s!=%s", OBJECT_VAR, ANON_OBJECT_VAR), "true",
            "boolean");
        // reference equality
        debugTestRunner.assertExpression(context, "2.0===2.00", "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s===%s", INT_VAR, FLOAT_VAR), "false", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s===%s", OBJECT_VAR, ANON_OBJECT_VAR), "false",
            "boolean");
        debugTestRunner.assertExpression(context, "2.0!==2.00", "false", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s!==%s", INT_VAR, FLOAT_VAR), "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s!==%s", OBJECT_VAR, ANON_OBJECT_VAR), "true",
            "boolean");
    }

    @Override
    @Test
    public void binaryBitwiseEvaluationTest() throws BallerinaTestException {
        // bitwise AND
        debugTestRunner.assertExpression(context, String.format("%s & %s", INT_VAR, INT_VAR), "20", "int");
        // bitwise OR
        debugTestRunner.assertExpression(context, String.format("%s | %s", INT_VAR, INT_VAR), "20", "int");
        // bitwise XOR
        debugTestRunner.assertExpression(context, String.format("%s ^ %s", INT_VAR, INT_VAR), "0", "int");
    }

    @Override
    @Test
    public void logicalEvaluationTest() throws BallerinaTestException {
        // logical AND
        debugTestRunner.assertExpression(context, String.format("%s && false", BOOLEAN_VAR), "false", "boolean");
        // logical OR
        debugTestRunner.assertExpression(context, String.format("%s || false", BOOLEAN_VAR), "true", "boolean");
    }

    @Override
    @Test
    public void conditionalExpressionEvaluationTest() throws BallerinaTestException {
        // expression ? expression : expression
        debugTestRunner.assertExpression(context, String.format("%s ? %s : %s", BOOLEAN_VAR, INT_VAR, FLOAT_VAR), "20",
            "int");
        // expression ?: expression
        debugTestRunner.assertExpression(context, String.format("%s ?: %s", INT_VAR, FLOAT_VAR), "20", "int");
        debugTestRunner.assertExpression(context, String.format("%s ?: %s", NIL_VAR, FLOAT_VAR), "-10.0", "float");
    }

    @Override
    @Test
    public void checkingExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void trapExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void queryExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlNavigationEvaluationTest() throws BallerinaTestException {
        // Todo
    }
}
