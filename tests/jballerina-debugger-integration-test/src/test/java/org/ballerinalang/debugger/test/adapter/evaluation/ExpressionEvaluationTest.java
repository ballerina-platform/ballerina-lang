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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Common expression evaluation test scenarios for Ballerina packages and single file projects.
 */
public abstract class ExpressionEvaluationTest extends ExpressionEvaluationBaseTest {

    @BeforeClass(alwaysRun = true)
    public void setup() throws BallerinaTestException {
        prepareForEvaluation();
    }

    @Override
    @Test
    public void literalEvaluationTest() throws BallerinaTestException {
        // nil literal
        debugTestRunner.assertExpression(context, "()", "()", "nil");
        // boolean literal
        debugTestRunner.assertExpression(context, "true", "true", "boolean");
        // numeric literal (decimal int)
        debugTestRunner.assertExpression(context, "10", "10", "int");
        debugTestRunner.assertExpression(context, "-20", "-20", "int");
        // numeric literal (hex int)
        debugTestRunner.assertExpression(context, "0xabc", "2748", "int");
        debugTestRunner.assertExpression(context, "-0X999", "-2457", "int");
        // numeric literal (decimal float)
        debugTestRunner.assertExpression(context, "20.0", "20.0", "float");
        debugTestRunner.assertExpression(context, "-30.0f", "-30.0", "float");
        debugTestRunner.assertExpression(context, "-40.0F", "-40.0", "float");
        debugTestRunner.assertExpression(context, "-5.0e34f", "-5.0E34", "float");
        debugTestRunner.assertExpression(context, "-30.0d", "-30.0", "decimal");
        debugTestRunner.assertExpression(context, "-40.0D", "-40.0", "decimal");
        debugTestRunner.assertExpression(context, "-5.0e34d", "-5.0E+34", "decimal");
        // Todo - add following tests after the implementation
        //  - hex float
        //  - string literal
        //  - byte-array literal
    }

    @Override
    @Test(enabled = false)
    public void listConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test(enabled = false)
    public void mappingConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void stringTemplateEvaluationTest() throws BallerinaTestException {
        // without interpolations
        debugTestRunner.assertExpression(context, "string `name: John, age: 20`", "\"name: John, age: 20\"", "string");
        // with interpolations
        debugTestRunner.assertExpression(context, "string `name: ${" + STRING_VAR + "}, age: ${" + INT_VAR + "}`",
                "\"name: foo, age: 20\"", "string");
    }

    @Override
    @Test
    public void xmlTemplateEvaluationTest() throws BallerinaTestException {
        // XML element
        debugTestRunner.assertExpression(context, "xml `<book>The Lost World</book>`", "XMLElement",
                "xml");
        // XML text
        debugTestRunner.assertExpression(context, "xml `Hello, world!`", "Hello, world!", "xml");
        // XML comment
        debugTestRunner.assertExpression(context, "xml `<!--I am a comment-->`", "<!--I am a comment-->", "xml");
        // XML processing instruction
        debugTestRunner.assertExpression(context, "xml `<?target data?>`", "<?target data?>", "xml");
        // concatenated XML
        debugTestRunner.assertExpression(context, "xml `<book>The Lost World</book>Hello, world!" +
                "<!--I am a comment--><?target data?>`", "XMLSequence (size = 4)", "xml");
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertExpression(context, "new Location(\"New York\",\"USA\")", "Location", "object");
    }

    @Override
    @Test
    public void builtInNameReferenceEvaluationTest() throws BallerinaTestException {
        // basic, simple types
        debugTestRunner.assertExpression(context, INT_TYPE_DESC, "int", "typedesc");
        debugTestRunner.assertExpression(context, FLOAT_TYPE_DESC, "float", "typedesc");
        debugTestRunner.assertExpression(context, DECIMAL_TYPE_DESC, "decimal", "typedesc");
        debugTestRunner.assertExpression(context, BOOLEAN_TYPE_DESC, "boolean", "typedesc");
        // basic, sequence types
        debugTestRunner.assertExpression(context, STRING_TYPE_DESC, "string", "typedesc");
        // other types
        debugTestRunner.assertExpression(context, BYTE_TYPE_DESC, "byte", "typedesc");
        debugTestRunner.assertExpression(context, JSON_TYPE_DESC, "json", "typedesc");
        debugTestRunner.assertExpression(context, ANY_TYPE_DESC, "any", "typedesc");
        debugTestRunner.assertExpression(context, ANYDATA_TYPE_DESC, "anydata", "typedesc");
        debugTestRunner.assertExpression(context, NEVER_TYPE_DESC, "never", "typedesc");
        debugTestRunner.assertExpression(context, PARENTHESISED_TYPE_DESC, "int", "typedesc");
    }

    @Override
    @Test
    public void nameReferenceEvaluationTest() throws BallerinaTestException {
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
        debugTestRunner.assertExpression(context, STRING_VAR, "\"foo\"", "string");
        // xml variable test
        debugTestRunner.assertExpression(context, XML_VAR, "XMLElement", "xml");
        // array variable test
        debugTestRunner.assertExpression(context, ARRAY_VAR, "any[4]", "array");
        // tuple variable test
        debugTestRunner.assertExpression(context, TUPLE_VAR, "tuple[int,string] (size = 2)", "tuple");
        // map variable test
        debugTestRunner.assertExpression(context, MAP_VAR, "map (size = 4)", "map");
        // record variable test (Student record)
        debugTestRunner.assertExpression(context, RECORD_VAR, " /:@[`{~π_123_ƮέŞŢ_Student", "record");
        // anonymous record variable test
        debugTestRunner.assertExpression(context, ANON_RECORD_VAR, "record {| string city; string country; |}",
                "record");
        // error variable test
        debugTestRunner.assertExpression(context, ERROR_VAR, "SimpleErrorType", "error");
        // anonymous function variable test
        debugTestRunner.assertExpression(context, ANON_FUNCTION_VAR, "isolated function (string,string) " +
                "returns (string)", "function");
        // future variable test
        debugTestRunner.assertExpression(context, FUTURE_VAR, "future<int>", "future");
        // object variable test (Person object)
        debugTestRunner.assertExpression(context, OBJECT_VAR, "Person_ /<>:@[`{~π_ƮέŞŢ", "object");
        // type descriptor variable test
        debugTestRunner.assertExpression(context, TYPEDESC_VAR, "int", "typedesc");
        // union variable test
        debugTestRunner.assertExpression(context, UNION_VAR, "\"foo\"", "string");
        // optional variable test
        debugTestRunner.assertExpression(context, OPTIONAL_VAR, "\"foo\"", "string");
        // any variable test
        debugTestRunner.assertExpression(context, ANY_VAR, "15.0", "float");
        // anydata variable test
        debugTestRunner.assertExpression(context, ANYDATA_VAR, "619", "int");
        // byte variable test
        debugTestRunner.assertExpression(context, BYTE_VAR, "128", "int");
        // table variable test
        debugTestRunner.assertExpression(context, TABLE_VAR, "table<Employee> (entries = 3)", "table");
        // stream variable test
        debugTestRunner.assertExpression(context, STREAM_VAR, "stream<int, error?>", "stream");
        // never variable test
        debugTestRunner.assertExpression(context, NEVER_VAR, "XMLSequence (size = 0)", "xml");
        // json variable test
        debugTestRunner.assertExpression(context, JSON_VAR, "json (size = 3)", "json");
        // anonymous object variable test (AnonPerson object)
        debugTestRunner.assertExpression(context, ANON_OBJECT_VAR, "Person_ /<>:@[`{~π_ƮέŞŢ", "object");
        // service object variable test
        debugTestRunner.assertExpression(context, SERVICE_VAR, "service", "service");

        debugTestRunner.assertExpression(context, "nameWithType", "\"Ballerina\"", "string");
        debugTestRunner.assertExpression(context, "nameWithoutType", "\"Ballerina\"", "string");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_04, "()", "nil");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_05, "()", "nil");
        // global variables
        debugTestRunner.assertExpression(context, GLOBAL_VAR_06, "\"Ballerina\"", "string");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_07, "100.0", "decimal");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_08, "2", "int");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_09, "2.0", "float");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_10, "json (size = 3)", "json");
        debugTestRunner.assertExpression(context, GLOBAL_VAR_11, "\"IL with global var\"", "string");

        // with qualified literals (i.e. imported modules)
        debugTestRunner.assertExpression(context, "int:MAX_VALUE", "9223372036854775807", "int");

        // qualified name references with import alias
        debugTestRunner.assertExpression(context, "langFloat:PI", "3.141592653589793", "float");
    }

    @Override
    @Test
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // objects fields
        debugTestRunner.assertExpression(context, OBJECT_VAR + ".address", "\"No 20, Palm grove\"", "string");
        // record fields
        debugTestRunner.assertExpression(context, RECORD_VAR + ".'Ȧɢέ_\\ \\/\\:\\@\\[\\`\\{\\~π", "20", "int");
        // json fields
        debugTestRunner.assertExpression(context, JSON_VAR + ".name", "\"apple\"", "string");
        // service object fields
        debugTestRunner.assertExpression(context, String.format("%s.i", SERVICE_VAR), "5", "int");

        // nested field access (chain access)
        debugTestRunner.assertExpression(context, RECORD_VAR + ".grades.maths", "80", "int");
        // optional field access
        debugTestRunner.assertExpression(context, RECORD_VAR + "?.undefined", "()", "nil");
        // additional field access
        debugTestRunner.assertExpression(context, RECORD_VAR + ".course", "\"ballerina\"", "string");
    }

    @Override
    @Test
    public void xmlAttributeAccessEvaluationTest() throws BallerinaTestException {
        // XML attribute access
        debugTestRunner.assertExpression(context, XML_VAR + ".gender", "\"male\"", "string");
        // XML optional attribute access
        debugTestRunner.assertExpression(context, XML_VAR + "?.gender", "\"male\"", "string");
        // XML optional attribute access on non existing attribute
        debugTestRunner.assertExpression(context, XML_VAR + "?.name", "()", "nil");
    }

    @Override
    @Test
    public void annotationAccessEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertExpression(context, "(typeof a).@v1", "map (size = 2)", "map");
        debugTestRunner.assertExpression(context, "(typeof a).@v2", "()", "nil");
        debugTestRunner.assertExpression(context, "(typeof a).@v1[\"foo\"]", "\"v1 value\"", "string");
        debugTestRunner.assertExpression(context, "(typeof a).@v1[\"bar\"]", "1", "int");
    }

    @Override
    @Test
    public void memberAccessEvaluationTest() throws BallerinaTestException {
        // strings
        debugTestRunner.assertExpression(context, STRING_VAR + "[0]", "\"f\"", "string");
        // lists
        debugTestRunner.assertExpression(context, ARRAY_VAR + "[0]", "1", "int");
        // maps
        debugTestRunner.assertExpression(context, MAP_VAR + "[\"country\"]", "\"Sri Lanka\"", "string");
        debugTestRunner.assertExpression(context, MAP_VAR + "[\"undefined\"]", "()", "nil");
        // json
        debugTestRunner.assertExpression(context, JSON_VAR + "[\"color\"]", "\"red\"", "string");
        debugTestRunner.assertExpression(context, JSON_VAR + "[\"undefined\"]", "()", "nil");
        // XML
        debugTestRunner.assertExpression(context, XML_VAR + "[0]", "XMLElement", "xml");
        debugTestRunner.assertExpression(context, XML_VAR + "[0][0]", "Praveen", "xml");
        debugTestRunner.assertExpression(context, XML_VAR + "[2]", "XMLSequence (size = 0)", "xml");
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

        // Todo - Enable once the semantic API blocker is fixed
        //  https://github.com/ballerina-platform/ballerina-lang/issues/32176
        // Call the function by passing a value only for the `baseSalary` parameter.
        // The `annualIncrement` and `bonusRate` parameters default to 20 and 0.02 respectively.
        // debugTestRunner.assertExpression(context, "printSalaryDetails(2500)", "\"[2500, 20, 0.02]\"", "string");

        // Todo - Enable once the semantic API blocker is fixed
        //  https://github.com/ballerina-platform/ballerina-lang/issues/32176
        // Call the function by passing values only for the `baseSalary` and `annualIncrement`
        // parameters. The value for the `annualIncrement` parameter is passed as a named argument.
        // The `bonusRate` parameter defaults to 0.02.
        // debugTestRunner.assertExpression(context, "printSalaryDetails(2500, annualIncrement = 100)",
        //        "\"[2500, 100, 0.02]\"", "string");

        // Todo - Enable once the semantic API blocker is fixed
        //  https://github.com/ballerina-platform/ballerina-lang/issues/32176
        // Call the function again by passing values only for the `baseSalary` and `annualIncrement`
        // parameters, now passing the value for the `annualIncrement` parameter as a positional argument.
        // The `bonusRate` parameter defaults to 0.02.
        // debugTestRunner.assertExpression(context, "printSalaryDetails(2500, 100);", "\"[2500, 100, 0.02]\"",
        // "string");

        // Todo - Enable once the semantic API blocker is fixed
        //  https://github.com/ballerina-platform/ballerina-lang/issues/32176
        // Call the function by passing values only for the `baseSalary` and `bonusRate` parameters.
        // The `annualIncrement` parameter defaults to 20.
        // debugTestRunner.assertExpression(context, "printSalaryDetails(2500, bonusRate = 0.1);", "\"[2500, 20,
        // 0.1]\"", "string");

        // In order to pass the value for `bonusRate` as a positional argument, a value would
        // have to be specified for the `annualIncrement` parameter too.
        // All arguments are positional arguments here.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, 20, 0.1);", "\"[2500, 20, 0.1]\"",
                "string");

        // Call the function by passing values for all three parameters, the first argument as
        // a positional argument and the rest as named arguments.
        debugTestRunner.assertExpression(context, "printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);",
                "\"[2500, 100, 0.1]\"", "string");

        // Call the function by passing all three arguments as named arguments.
        // Any and all arguments after the first named argument need to be specified
        // as named arguments but could be specified in any order.
        debugTestRunner.assertExpression(context, "printSalaryDetails(annualIncrement = 100, baseSalary = 2500, " +
                "bonusRate = 0.1);", "\"[2500, 100, 0.1]\"", "string");

        // ----------------------------  Rest Parameters  ------------------------------------------

        // Todo - Enable once the semantic API blocker is fixed
        //  https://github.com/ballerina-platform/ballerina-lang/issues/32176
        // Call the function by passing only the required parameter.
        // debugTestRunner.assertExpression(context, "printDetails(\"Alice\");", "\"[Alice, 18, Module(s): ()]\"",
        //        "string");

        // Call the function by passing the required parameter and the defaultable parameter.Named arguments can
        // also be used since values are not passed for the rest parameter.
        debugTestRunner.assertExpression(context, "printDetails(\"Bob\", 20);", "\"[Bob, 20, Module(s): ()]\"",
                "string");

        // Call the function by passing the required parameter, the defaultable parameter, and one value for the rest
        // parameter.Arguments cannot be passed as named arguments since values are specified for the rest parameter.
        debugTestRunner.assertExpression(context, "printDetails(\"Corey\", 19, \"Math\");",
                "\"[Corey, 19, Module(s): Math,]\"", "string");

        // Call the function by passing the required parameter, defaultable parameter,
        //  and multiple values for the rest parameter.
        debugTestRunner.assertExpression(context, "printDetails(\"Diana\", 20, \"Math\", \"Physics\");",
                "\"[Diana, 20, Module(s): Math,Physics,]\"", "string");

        // Pass an array as the rest parameter instead of calling the
        // function by passing each value separately.
        debugTestRunner.assertExpression(context, "printDetails(\"Diana\", 20, ...stringArrayVar);",
                "\"[Diana, 20, Module(s): foo,bar,]\"", "string");

        // ----------------------------  Other Scenarios  ------------------------------------------

        // Function which includes asynchronous calls.
        debugTestRunner.assertExpression(context, "getSum(10, 20);", "30", "int");

        // with qualified literals (i.e. imported modules)
        debugTestRunner.assertExpression(context, "int:abs(-6)", "6", "int");

        // with typedesc values as arguments
        debugTestRunner.assertExpression(context, "processTypeDesc(int)", "int", "typedesc");
    }

    @Override
    @Test
    public void methodCallEvaluationTest() throws BallerinaTestException {

        // 1. object methods (with async method calls)
        debugTestRunner.assertExpression(context, OBJECT_VAR + ".getSum(34,56)", "90", "int");

        // 2. lang library methods

        // array
        debugTestRunner.assertExpression(context, ARRAY_VAR + ".length()", "4", "int");
        debugTestRunner.assertExpression(context, ARRAY_VAR + ".slice(1,3)", "any[2]", "array");

        // Todo - boolean
        // decimal
        debugTestRunner.assertExpression(context, DECIMAL_VAR + ".round()", "4", "decimal");
        debugTestRunner.assertExpression(context, DECIMAL_VAR + ".abs()", "3.5", "decimal");
        // error
        debugTestRunner.assertExpression(context, ERROR_VAR + ".message()", "\"SimpleErrorType\"", "string");
        // float
        debugTestRunner.assertExpression(context, FLOAT_VAR + ".sin()", "0.5440211108893698", "float");
        debugTestRunner.assertExpression(context, FLOAT_VAR + ".pow(3.0)", "-1000.0", "float");
        // future
        debugTestRunner.assertExpression(context, FUTURE_VAR + ".cancel()", "()", "nil");
        // int
        debugTestRunner.assertExpression(context, INT_VAR + ".abs()", "20", "int");
        // map
        debugTestRunner.assertExpression(context, MAP_VAR + ".get(\"country\")", "\"Sri Lanka\"", "string");
        // Todo - object
        // Todo - query
        // Todo - stream
        // string
        debugTestRunner.assertExpression(context, STRING_VAR + ".getCodePoint(1)", "111", "int");
        debugTestRunner.assertExpression(context, STRING_VAR + ".substring(1,3)", "\"oo\"", "string");
        // Todo - table
        // Todo - typedesc

        // value
        debugTestRunner.assertExpression(context, TYPEDESC_VAR + ".toBalString()", "\"typedesc int\"", "string");

        // xml
        debugTestRunner.assertExpression(context, XML_VAR + ".getName()", "\"person\"", "string");
        debugTestRunner.assertExpression(context, XML_VAR + ".children()", "XMLSequence (size = 2)", "xml");
    }

    @Override
    @Test
    public void errorConstructorEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertExpression(context, "error(\"Simple Error\")", "Simple Error", "error");
        debugTestRunner.assertExpression(context, "error(\"Simple Error\", " + ERROR_VAR + ")", "Simple Error",
                "error");
        debugTestRunner.assertExpression(context, "error(\"Simple Error\", count=1)", "Simple Error", "error");
        debugTestRunner.assertExpression(context, "error(\"Simple Error\", " + ERROR_VAR + ",count=1)", "Simple Error",
                "error");
    }

    @Override
    @Test
    public void anonymousFunctionEvaluationTest() throws BallerinaTestException {
        // Implicit anonymous function expressions
        debugTestRunner.assertExpression(context, "function(int x, int y) returns int => x + y;",
                "isolated function (int,int) returns (int)", "function");

        // Explicit anonymous function expressions
        debugTestRunner.assertExpression(context, "function(string x, string y) returns (string) { return x + y; }",
                "isolated function (string,string) returns (string)", "function");
    }

    @Override
    @Test
    public void letExpressionEvaluationTest() throws BallerinaTestException {
        // Basic let expression
        debugTestRunner.assertExpression(context, "let int x = 4 in 2 * x * globalVar", "16", "int");
        // Basic let expression with var
        debugTestRunner.assertExpression(context, "let int x = 4 in 2 * x * globalVar", "16", "int");
        // Multiple var Declarations
        debugTestRunner.assertExpression(context, "let int x = globalVar*2, int z = 5 in z * x * globalVar", "40",
                "int");
        // Multiple var Declarations with reuse
        debugTestRunner.assertExpression(context, "let int x = 2, int z = 5+x in z * x * globalVar;", "28", "int");
        // Function calls in declarations
        debugTestRunner.assertExpression(context, "let int x = 4, int y = 1, int z = func(y + y*2 + globalVar) in z *" +
                " (x + globalVar + y)", "70", "int");
        // Function calls in expression
        debugTestRunner.assertExpression(context, "let int x = 4, int z = 10 in func(x * z)", "80", "int");
        // Let expression as a function arg
        // Todo - https://github.com/ballerina-platform/ballerina-lang/issues/34151
        // debugTestRunner.assertExpression(context, "func2(let string x = \"aa\", string y = \"bb\" in x+y)", "4",
        //    "int");
        // Let expression tuple
        debugTestRunner.assertExpression(context, "let [[string, [int, [boolean, byte]]], [float, int]] " +
                "v1 = [[\"Ballerina\", [3, [true, 34]]], [5.6, 45]], int x = 2 in v1[0][1][0] + x;", "5", "int");
        // Let expression tuple binding
        debugTestRunner.assertExpression(context, "let [[string, int], [boolean, float]] [[c1, c2],[c3, c4]] = " +
                "[[\"Ballerina\", 34], [true, 6.7]], int x = 2 in c2 + x", "36", "int");
        // Let expression with error binding
        debugTestRunner.assertExpression(context, "let SampleError error(reason, info = info, fatal = fatal) = " +
                "getSampleError(), int x = 1 in reason.length() + x;", "13", "int");
        // Let expression with record constrained error binding
        debugTestRunner.assertExpression(context, "let var error(_, detailMsg = detailMsg, isFatal = isFatal) = " +
                "getRecordConstrainedError() in detailMsg", "\"Failed Message\"", "string");
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
        debugTestRunner.assertExpression(context, String.format("typeof %s", BOOLEAN_VAR), "true", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof %s", INT_VAR), "20", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof %s", FLOAT_VAR), "-10.0", "typedesc");
        // reference types
        debugTestRunner.assertExpression(context, String.format("typeof %s", JSON_VAR), "map<json>", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof %s[0]", STRING_VAR), "f", "typedesc");
        debugTestRunner.assertExpression(context, String.format("typeof typeof %s", BOOLEAN_VAR), "typedesc<true>",
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
        // float * float
        debugTestRunner.assertExpression(context, String.format("%s * %s", FLOAT_VAR, FLOAT_VAR), "100.0", "float");
        // decimal * decimal
        debugTestRunner.assertExpression(context, String.format("%s * %s", DECIMAL_VAR, DECIMAL_VAR), "12.25",
                "decimal");

        ///////////////////////////////-------------division--------------------//////////////////////////////////////
        // int / int
        debugTestRunner.assertExpression(context, String.format("%s / %s", INT_VAR, INT_VAR), "1", "int");
        // float / float
        debugTestRunner.assertExpression(context, String.format("%s / %s", FLOAT_VAR, FLOAT_VAR), "1.0", "float");
        // decimal / decimal
        debugTestRunner.assertExpression(context, String.format("%s / %s", DECIMAL_VAR, DECIMAL_VAR), "1", "decimal");

        /////////////////////////////////-----------modulus----------------///////////////////////////////////////////
        // int % int
        debugTestRunner.assertExpression(context, String.format("%s %% %s", INT_VAR, INT_VAR), "0", "int");
        // float % float
        debugTestRunner.assertExpression(context, String.format("%s %% %s", FLOAT_VAR, FLOAT_VAR), "-0.0", "float");
        // decimal % decimal
        debugTestRunner.assertExpression(context, String.format("%s %% %s", DECIMAL_VAR, DECIMAL_VAR), "0",
                "decimal");
    }

    @Override
    @Test
    public void additiveExpressionEvaluationTest() throws BallerinaTestException {
        //////////////////////////////-------------addition------------------/////////////////////////////////////////
        // int + int
        debugTestRunner.assertExpression(context, String.format("%s + %s", INT_VAR, INT_VAR), "40", "int");
        // float + float
        debugTestRunner.assertExpression(context, String.format("%s + %s", FLOAT_VAR, FLOAT_VAR), "-20.0", "float");
        // decimal + decimal
        debugTestRunner.assertExpression(context, String.format("%s + %s", DECIMAL_VAR, DECIMAL_VAR), "7.0", "decimal");

        // string + string
        debugTestRunner.assertExpression(context, String.format("%s + %s", STRING_VAR, STRING_VAR), "\"foofoo\"",
                "string");
        // string with leading and trailing whitespaces
        debugTestRunner.assertExpression(context, "\" one \" + \" two \" + \" three \"", "\" one  two  three \"",
                "string");
        // string template concatenation
        String bStringTemplateExpr = String.format("string `name: ${%s}, age: ${%s}`", STRING_VAR, INT_VAR);
        debugTestRunner.assertExpression(context, String.format("%s + %s + %s", bStringTemplateExpr,
                        bStringTemplateExpr, bStringTemplateExpr),
                "\"name: foo, age: 20name: foo, age: 20name: foo, age: 20\"", "string");

        // xml + xml
        debugTestRunner.assertExpression(context, String.format("%s + %s", XML_VAR, XML_VAR),
                "XMLSequence (size = 2)", "xml");

        //////////////////////////////-------------subtraction------------------//////////////////////////////////////
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s - %s", INT_VAR, INT_VAR), "0", "int");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s - %s", FLOAT_VAR, FLOAT_VAR), "0.0", "float");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s - %s", DECIMAL_VAR, DECIMAL_VAR), "0", "decimal");
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
        // inclusive range expressions
        debugTestRunner.assertExpression(context, "1...10", "__IntRange", "object");
        debugTestRunner.assertExpression(context, String.format("%s...%s+5", INT_VAR, INT_VAR), "__IntRange", "object");
        // exclusive range expressions
        debugTestRunner.assertExpression(context, "1..<10", "__IntRange", "object");
        debugTestRunner.assertExpression(context, String.format("%s..<%s+5", INT_VAR, INT_VAR), "__IntRange",
                "object");
    }

    @Override
    @Test
    public void comparisonEvaluationTest() throws BallerinaTestException {
        // expression < expression
        // nil - nil
        debugTestRunner.assertExpression(context, String.format("%s < %s", NIL_VAR, NIL_VAR), "false", "boolean");
        // boolean - boolean
        debugTestRunner.assertExpression(context, String.format("%s < %s", BOOLEAN_VAR, BOOLEAN_VAR), "false",
                "boolean");
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s < %s", INT_VAR, INT_VAR), "false", "boolean");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s < %s", FLOAT_VAR, FLOAT_VAR), "false", "boolean");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s < %s", DECIMAL_VAR, DECIMAL_VAR), "false",
                "boolean");
        // string - string
        debugTestRunner.assertExpression(context, String.format("%s < %s", STRING_VAR, STRING_VAR), "false",
                "boolean");

//        Todo - enable once https://github.com/ballerina-platform/ballerina-lang/issues/38642 is fixed
//        // boolean[] - boolean[]
//        debugTestRunner.assertExpression(context, String.format("%s < %s", "booleanArrayVar", "booleanArrayVar"),
//                "false", "boolean");
//        // int[] - int[]
//        debugTestRunner.assertExpression(context, String.format("%s < %s", "intArrayVar", "intArrayVar"),
//                "false", "boolean");
//        // float[] - float[]
//        debugTestRunner.assertExpression(context, String.format("%s < %s", "floatArrayVar", "floatArrayVar"),
//                "false", "boolean");
//        // decimal[] - decimal[]
//        debugTestRunner.assertExpression(context, String.format("%s < %s", "decimalArrayVar", "decimalArrayVar"),
//                "false", "boolean");
//        // string[] - string[]
//        debugTestRunner.assertExpression(context, String.format("%s < %s", "stringArrayVar", "stringArrayVar"),
//                "false", "boolean");

        // expression <= expression
        // nil - nil
        debugTestRunner.assertExpression(context, String.format("%s <= %s", NIL_VAR, NIL_VAR), "true", "boolean");
        // boolean - boolean
        debugTestRunner.assertExpression(context, String.format("%s <= %s", BOOLEAN_VAR, BOOLEAN_VAR), "true",
                "boolean");
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s <= %s", INT_VAR, INT_VAR), "true", "boolean");
        // float - float
        debugTestRunner.assertExpression(context, String.format("%s <= %s", FLOAT_VAR, FLOAT_VAR), "true", "boolean");
        // decimal - decimal
        debugTestRunner.assertExpression(context, String.format("%s <= %s", DECIMAL_VAR, DECIMAL_VAR), "true",
                "boolean");
        // string - string
        debugTestRunner.assertExpression(context, String.format("%s <= %s", STRING_VAR, STRING_VAR), "true",
                "boolean");

//        Todo - enable once https://github.com/ballerina-platform/ballerina-lang/issues/38642 is fixed
//        // boolean[] - boolean[]
//        debugTestRunner.assertExpression(context, String.format("%s <= %s", "booleanArrayVar", "booleanArrayVar"),
//                "true", "boolean");
//        // int[] - int[]
//        debugTestRunner.assertExpression(context, String.format("%s <= %s", "intArrayVar", "intArrayVar"),
//                "true", "boolean");
//        // float[] - float[]
//        debugTestRunner.assertExpression(context, String.format("%s <= %s", "floatArrayVar", "floatArrayVar"),
//                "true", "boolean");
//        // decimal[] - decimal[]
//        debugTestRunner.assertExpression(context, String.format("%s <= %s", "decimalArrayVar", "decimalArrayVar"),
//                "true", "boolean");
//        // string[] - string[]
//        debugTestRunner.assertExpression(context, String.format("%s <= %s", "stringArrayVar", "stringArrayVar"),
//                "true", "boolean");

        // expression > expression
        // Note ::= Not required to test all the possibilities in here, as 'X > Y' is processed as '!(X <= Y)' by the
        // evaluation engine.
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s > %s", INT_VAR, INT_VAR), "false", "boolean");

        // expression >= expression
        // Note ::= Not required to test all the possibilities in here, as `X >= Y` is processed as '!(X < Y)' by the
        // evaluation engine.
        // int - int
        debugTestRunner.assertExpression(context, String.format("%s >= %s", INT_VAR, INT_VAR), "true", "boolean");
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        // predefined types
        debugTestRunner.assertExpression(context, String.format("%s is map<string>", JSON_VAR), "false", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s is json", JSON_VAR), "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s is error", ERROR_VAR), "true", "boolean");
        debugTestRunner.assertExpression(context, String.format("%s is readonly", ERROR_VAR), "true", "boolean");

        // other named types
        // Todo - https://github.com/ballerina-platform/ballerina-lang/issues/34151
        // debugTestRunner.assertExpression(context, String.format("%s is 'Person_\\ \\/\\<\\>\\:\\@\\[\\`\\{\\" +
        //   "~\\u{03C0}_ƮέŞŢ", OBJECT_VAR), "true", "boolean");

        // union types
        debugTestRunner.assertExpression(context, String.format("%s is int | string", STRING_VAR), "true", "boolean");

        // intersection types
        debugTestRunner.assertExpression(context, String.format("%s is map<string> & readonly", JSON_VAR), "false",
                "boolean");
        debugTestRunner.assertExpression(context, String.format("%s is (string & readonly) | int", STRING_VAR), "true",
                "boolean");
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
    @Test(enabled = false)
    public void checkingExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void trapExpressionEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertExpression(context, "trap sum(1,2)", "3", "int");
    }

    @Override
    @Test
    public void queryExpressionEvaluationTest() throws BallerinaTestException {

        // String from query evaluation
        debugTestRunner.assertExpression(context, "from var student in studentList" +
                        "    where student.score >= 2.0" +
                        "    select student.firstName + \" \" + student.lastName",
                "string[2]", "array");

        // Query expression evaluation with multiple clauses
        debugTestRunner.assertExpression(context, "from var student in studentList" +
                        "    where student.score >= 2.0" +
                        "    let string degreeName = \"Bachelor of Medicine\", " +
                        "    int expectedGradYear = calGraduationYear(student.intakeYear)" +
                        "    order by student.firstName descending" +
                        "    limit 2" +
                        "    select {" +
                        "        name: student.firstName + \" \" + student.lastName," +
                        "        degree: degreeName," +
                        "        expectedGradYear: expectedGradYear" +
                        "    };",
                "record {| string name; string degree; int expectedGradYear; |}[2]", "array");

        // Query stream evaluation
        debugTestRunner.assertExpression(context, "stream from var student in studentList" +
                        "    where student.score >= 2.0" +
                        "    let string degreeName = \"Bachelor of Medicine\", " +
                        "    int graduationYear = calGraduationYear(student.intakeYear)" +
                        "    order by student.firstName descending" +
                        "    limit 2" +
                        "    select {" +
                        "        name: student.firstName + \" \" + student.lastName," +
                        "                degree: degreeName," +
                        "                graduationYear: graduationYear" +
                        "    };",
                "stream<evaluation_executor:record {| string name; string degree; int graduationYear; |}>", "stream");

        // Query join expression evaluation
        debugTestRunner.assertExpression(context, "from var student in gradStudentList" +
                        "    join var department in departmentList" +
                        "    on student.deptId equals department.deptId" +
                        "    limit 3" +
                        "    select { " +
                        "        name: student.firstName + \" \" + student.lastName, " +
                        "        deptName: department.deptName, " +
                        "        degree: \"Bachelor of Science\", " +
                        "        intakeYear: student.intakeYear " +
                        "    }",
                "record {| string name; string deptName; string degree; int intakeYear; |}[3]", "array");

        // Table query with contextually expected type (type cast).
        debugTestRunner.assertExpression(context,
                "<CustomerTable|error> table key(id, name) from var customer in customerList" +
                        "     select {" +
                        "         id: customer.id," +
                        "         name: customer.name," +
                        "         noOfItems: customer.noOfItems" +
                        "     }" +
                        "     on conflict onConflictError;",
                "table<Customer> (entries = 3)", "table");

        // Table query with conflicts.
        debugTestRunner.assertExpression(context,
                "<CustomerTable|error> table key(id, name) from var customer in conflictedCustomerList" +
                        "     select {" +
                        "         id: customer.id," +
                        "         name: customer.name," +
                        "         noOfItems: customer.noOfItems" +
                        "     }" +
                        "     on conflict onConflictError;",
                "Key Conflict", "error");

        // Nested from clauses
        debugTestRunner.assertExpression(context, "from var i in from var j in [1, 2, 3] select j select i",
                "int[3]", "array");

        // Queries with library import usages
        debugTestRunner.assertExpression(context, "from var student in studentList" +
                        "    where student.score.toBalString() != int:MAX_VALUE.toBalString()" +
                        "    select student.firstName + \" \" + student.lastName",
                "string[3]", "array");
    }

    @Override
    @Test
    public void xmlNavigationEvaluationTest() throws BallerinaTestException {
        // XML filter expressions
        debugTestRunner.assertExpression(context, "xmlVar2.<items>", "XMLSequence (size = 1)", "xml");

        // XML step expressions
        debugTestRunner.assertExpression(context, "xmlVar2/*", "XMLSequence (size = 8)", "xml");
        debugTestRunner.assertExpression(context, "xmlVar2/<planner>", "XMLSequence (size = 2)", "xml");
        debugTestRunner.assertExpression(context, "xmlVar2/<planner|pen>", "XMLSequence (size = 4)", "xml");
        debugTestRunner.assertExpression(context, "xmlVar2/<*>", "XMLSequence (size = 8)", "xml");
        debugTestRunner.assertExpression(context, "xmlVar2/**/<name>", "XMLSequence (size = 8)", "xml");
    }

    @Override
    @Test
    public void remoteCallActionEvaluationTest() throws BallerinaTestException {
        // Todo - Enable once the semantic API blocker is fixed
        //  https://github.com/ballerina-platform/ballerina-lang/issues/32176
        // debugTestRunner.assertExpression(context, String.format("%s->getName(\"John\")", CLIENT_OBJECT_VAR),
        //        "\"John\"", "string");
        debugTestRunner.assertExpression(context, String.format("%s->getName(\"John\",\" Doe\")", CLIENT_OBJECT_VAR),
                "\"John Doe\"", "string");
        debugTestRunner.assertExpression(context, String.format("%s->getTotalMarks(78,90)", CLIENT_OBJECT_VAR),
                "168", "int");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
        this.context = null;
    }
}
