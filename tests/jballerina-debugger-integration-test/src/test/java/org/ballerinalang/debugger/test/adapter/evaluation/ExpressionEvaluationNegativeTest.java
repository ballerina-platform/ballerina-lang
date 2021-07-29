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
 * Test implementation for debug expression evaluation negative scenarios.
 */
public class ExpressionEvaluationNegativeTest extends ExpressionEvaluationBaseTest {

    @BeforeClass(alwaysRun = true)
    public void setup() throws BallerinaTestException {
        prepareForEvaluation();
    }

    @Override
    @Test
    public void literalEvaluationTest() throws BallerinaTestException {
        // Todo
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
        // incompatible result types from expressions.
        debugTestRunner.assertEvaluationError(context, "string `json: ${" + JSON_VAR + "}`",
                String.format(EvaluationExceptionKind.TYPE_MISMATCH.getString(), "(int|float|decimal|string|boolean)",
                        "json", "${jsonVar}"));
    }

    @Override
    @Test
    public void xmlTemplateEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        // Implicit new expressions
        debugTestRunner.assertEvaluationError(context, "new", String.format(EvaluationExceptionKind
                .CUSTOM_ERROR.getString(), "Implicit new expressions are not supported by the evaluator. Try using " +
                "the equivalent explicit expression by specifying the class descriptor (i.e. 'new T()') instead."));
        debugTestRunner.assertEvaluationError(context, "new()", String.format(EvaluationExceptionKind
                .CUSTOM_ERROR.getString(), "Implicit new expressions are not supported by the evaluator. Try using " +
                "the equivalent explicit expression by specifying the class descriptor (i.e. 'new T()') instead."));

        // New expressions with invalid number of args
        debugTestRunner.assertEvaluationError(context, "new Location()", String.format(EvaluationExceptionKind
                .CUSTOM_ERROR.getString(), "missing required parameter 'city'."));

    }

    @Override
    @Test
    public void variableReferenceEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlAttributeAccessEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertEvaluationError(context, XML_VAR + ".name",
                EvaluationExceptionKind.PREFIX + "Undefined attribute `name' in: 'xmlVar'");
        debugTestRunner.assertEvaluationError(context, "(xml `xmlString`).name",
                EvaluationExceptionKind.PREFIX + "Invalid xml attribute access on xml text");
        debugTestRunner.assertEvaluationError(context, "(xml `<!--I am a comment-->`).name",
                EvaluationExceptionKind.PREFIX + "Invalid xml attribute access on xml comment");
        debugTestRunner.assertEvaluationError(context, "(xml `<?target data?>`).name",
                EvaluationExceptionKind.PREFIX + "Invalid xml attribute access on xml pi");
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
        debugTestRunner.assertEvaluationError(context, STRING_VAR + "[-1]", EvaluationExceptionKind.PREFIX +
                "string index out of range: index=-1, size=5");
        debugTestRunner.assertEvaluationError(context, STRING_VAR + "[100]", EvaluationExceptionKind.PREFIX +
                "string index out of range: index=100, size=5");
        debugTestRunner.assertEvaluationError(context, STRING_VAR + "[\"undefined\"]",
                EvaluationExceptionKind.PREFIX + "expected key type 'int'; found 'string' in " +
                        "'stringVar[\"undefined\"]'");
        // lists
        debugTestRunner.assertEvaluationError(context, ARRAY_VAR + "[-1]", EvaluationExceptionKind.PREFIX +
                "array index out of range: index=-1, size=4");
        debugTestRunner.assertEvaluationError(context, ARRAY_VAR + "[100]", EvaluationExceptionKind.PREFIX +
                "array index out of range: index=100, size=4");
        debugTestRunner.assertEvaluationError(context, ARRAY_VAR + "[\"undefined\"]",
                EvaluationExceptionKind.PREFIX + "expected key type 'int'; found 'string' in " +
                        "'arrayVar[\"undefined\"]'");
        // maps
        debugTestRunner.assertEvaluationError(context, MAP_VAR + "[1]", EvaluationExceptionKind.PREFIX +
                "expected key type 'string'; found 'int' in 'mapVar[1]'");
        // JSON
        debugTestRunner.assertEvaluationError(context, JSON_VAR + "[1]", EvaluationExceptionKind.PREFIX +
                "expected key type 'string'; found 'int' in 'jsonVar[1]'");
        // XML
        debugTestRunner.assertEvaluationError(context, XML_VAR + "[-1]", EvaluationExceptionKind.PREFIX +
                "xml index out of range: index=-1, size=2");
        debugTestRunner.assertEvaluationError(context, XML_VAR + "[\"undefined\"]",
                EvaluationExceptionKind.PREFIX + "expected key type 'int'; found 'string' in " +
                        "'xmlVar[\"undefined\"]'");
    }

    @Override
    @Test
    public void functionCallEvaluationTest() throws BallerinaTestException {

        debugTestRunner.assertEvaluationError(context, "calculate(5, 6)", EvaluationExceptionKind.PREFIX +
                "missing required parameter 'c'.");

        debugTestRunner.assertEvaluationError(context, "calculate(5, x = 6, 7)", String.format(EvaluationExceptionKind
                .SYNTAX_ERROR.getString(), "named arg followed by positional arg"));

        debugTestRunner.assertEvaluationError(context, "calculate(5, 6, 7, 8)", EvaluationExceptionKind.PREFIX +
                "too many arguments in call to 'calculate'.");

        debugTestRunner.assertEvaluationError(context, "calculate(5, 6, 7, d = 8)", EvaluationExceptionKind.PREFIX +
                "undefined defaultable parameter 'd'.");

        debugTestRunner.assertEvaluationError(context, "calculate(5, ...b, 7)", String.format(EvaluationExceptionKind
                .SYNTAX_ERROR.getString(), "rest arg followed by another arg"));

        debugTestRunner.assertEvaluationError(context, "calculate(5, ...b, c = 7)",
                String.format(EvaluationExceptionKind.SYNTAX_ERROR.getString(), "rest arg followed by another arg"));

        debugTestRunner.assertEvaluationError(context, "calculate(5, b = 6, ...c)", EvaluationExceptionKind.PREFIX +
                "rest args are not allowed after named args.");

        debugTestRunner.assertEvaluationError(context, "printDetails(...stringArrayVar);",
                EvaluationExceptionKind.PREFIX + "missing required parameter 'name'.");

        debugTestRunner.assertEvaluationError(context, "printDetails(\"Hi\", 20, 30);",
                EvaluationExceptionKind.PREFIX + "Incompatible types: expected `string`, but found " +
                        "'int': in 'modules'");

        debugTestRunner.assertEvaluationError(context, "printDetails(\"Hi\", 20, ...stringArrayVar, 20);",
                EvaluationExceptionKind.PREFIX + "Syntax errors found: " + System.lineSeparator() +
                        "rest arg followed by another arg");
    }

    @Override
    @Test
    public void methodCallEvaluationTest() throws BallerinaTestException {
        // undefined object methods.
        debugTestRunner.assertEvaluationError(context, OBJECT_VAR + ".calculate()",
                EvaluationExceptionKind.PREFIX + "Undefined function 'calculate' in type 'object'");

        // undefined lang library methods.
        debugTestRunner.assertEvaluationError(context, INT_VAR + ".foo()",
                EvaluationExceptionKind.PREFIX + "Undefined function 'foo' in type 'int'");
    }

    @Override
    @Test
    public void errorConstructorEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertEvaluationError(context, "error()", EvaluationExceptionKind.PREFIX +
                "Syntax errors found: " + System.lineSeparator() + "missing arg within parenthesis");
        debugTestRunner.assertEvaluationError(context, "error(1)", EvaluationExceptionKind.PREFIX +
                "incompatible types: expected 'string', found 'int' for error message");
        debugTestRunner.assertEvaluationError(context, "error(count=2)", EvaluationExceptionKind.PREFIX +
                "Syntax errors found: " + System.lineSeparator() + "missing error message in error constructor");
        debugTestRunner.assertEvaluationError(context, "error(...strArray)", EvaluationExceptionKind.PREFIX +
                "Syntax errors found: " + System.lineSeparator() + "missing error message in error constructor" +
                System.lineSeparator() + "rest arg in error constructor");
        debugTestRunner.assertEvaluationError(context, "error(\"Simple Error\",2)", EvaluationExceptionKind.PREFIX +
                "incompatible types: expected 'error?', found 'int' for error cause");
        debugTestRunner.assertEvaluationError(context, "error(\"Simple Error\",...strArray)",
                EvaluationExceptionKind.PREFIX + "Syntax errors found: " + System.lineSeparator() +
                        "rest arg in error constructor");
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
        debugTestRunner.assertEvaluationError(context, String.format("<boolean>%s", ANYDATA_VAR),
                "{ballerina}TypeCastError");
        debugTestRunner.assertEvaluationError(context, String.format("<boolean|string>%s", ANY_VAR),
                "{ballerina}TypeCastError");
    }

    @Override
    @Test
    public void typeOfExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertEvaluationError(context, String.format("+%s", STRING_VAR),
                "operator '+' not defined for 'string'");
        debugTestRunner.assertEvaluationError(context, String.format("-%s", STRING_VAR),
                "operator '-' not defined for 'string'");
        debugTestRunner.assertEvaluationError(context, String.format("~%s", STRING_VAR),
                "operator '~' not defined for 'string'");
        debugTestRunner.assertEvaluationError(context, String.format("!%s", STRING_VAR),
                "operator '!' not defined for 'string'");
    }

    @Override
    @Test
    public void multiplicativeExpressionEvaluationTest() throws BallerinaTestException {
        // semantically incorrect expressions (multiplication between int and string)
        debugTestRunner.assertEvaluationError(context, String.format("%s * %s", INT_VAR, STRING_VAR),
                "operator '*' not defined for 'int' and 'string'.");
        // runtime error (divide by zero)
        debugTestRunner.assertEvaluationError(context, String.format("%s / 0", INT_VAR), "{ballerina}DivisionByZero");
    }

    @Override
    @Test
    public void additiveExpressionEvaluationTest() throws BallerinaTestException {
        // semantically incorrect expressions (addition between int and string)
        debugTestRunner.assertEvaluationError(context, String.format("%s + %s", INT_VAR, STRING_VAR),
                "operator '+' not defined for 'int' and 'string'.");
    }

    @Override
    @Test
    public void shiftExpressionEvaluationTest() throws BallerinaTestException {
        // left shift
        debugTestRunner.assertEvaluationError(context, String.format("%s << %s", INT_VAR, STRING_VAR),
                "operator '<<' not defined for 'int' and 'string'.");
        // signed right shift
        debugTestRunner.assertEvaluationError(context, String.format("%s >> %s", INT_VAR, STRING_VAR),
                "operator '>>' not defined for 'int' and 'string'.");
        // unsigned right shift
        debugTestRunner.assertEvaluationError(context, String.format("%s >>> %s", INT_VAR, STRING_VAR),
                "operator '>>>' not defined for 'int' and 'string'.");
    }

    @Override
    @Test
    public void rangeExpressionEvaluationTest() throws BallerinaTestException {
        // inclusive range expressions
        debugTestRunner.assertEvaluationError(context, String.format("%s...%s", INT_VAR, FLOAT_VAR),
                "operator '...' not defined for 'int' and 'float'.");
        // exclusive range expressions
        debugTestRunner.assertEvaluationError(context, String.format("%s..<%s", INT_VAR, STRING_VAR),
                "operator '..<' not defined for 'int' and 'string'.");
    }

    @Override
    @Test
    public void comparisonEvaluationTest() throws BallerinaTestException {
        // comparison between int and string
        debugTestRunner.assertEvaluationError(context, String.format("%s < %s", INT_VAR, STRING_VAR),
                "operator '<' not defined for 'int' and 'string'.");
        // comparison between int and float (disallowed by the latest spec)
        debugTestRunner.assertEvaluationError(context, String.format("%s < %s", INT_VAR, FLOAT_VAR),
                "operator '<' not defined for 'int' and 'float'.");
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertEvaluationError(context, String.format("%s is NotDefinedClass", OBJECT_VAR),
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "failed to resolve type 'NotDefinedClass'."));
    }

    @Override
    @Test
    public void equalityEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void binaryBitwiseEvaluationTest() throws BallerinaTestException {
        // bitwise AND
        debugTestRunner.assertEvaluationError(context, String.format("%s & %s", INT_VAR, STRING_VAR),
                "operator '&' not defined for 'int' and 'string'.");
        // bitwise OR
        debugTestRunner.assertEvaluationError(context, String.format("%s | %s", INT_VAR, STRING_VAR),
                "operator '|' not defined for 'int' and 'string'.");
        // bitwise XOR
        debugTestRunner.assertEvaluationError(context, String.format("%s ^ %s", INT_VAR, STRING_VAR),
                "operator '^' not defined for 'int' and 'string'.");
    }

    @Override
    @Test
    public void logicalEvaluationTest() throws BallerinaTestException {
        // Logical AND
        debugTestRunner.assertEvaluationError(context, String.format("%s && %s", INT_VAR, STRING_VAR),
                "operator '&&' not defined for 'int' and 'string'.");
        // Logical OR
        debugTestRunner.assertEvaluationError(context, String.format("%s || %s", INT_VAR, STRING_VAR),
                "operator '||' not defined for 'int' and 'string'.");
    }

    @Override
    @Test
    public void conditionalExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
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

    @Test
    public void invalidInputEvaluationTest() throws BallerinaTestException {
        // empty expressions
        debugTestRunner.assertEvaluationError(context, "  ", EvaluationExceptionKind.EMPTY.getString());

        // Ballerina documentation lines
        debugTestRunner.assertEvaluationError(context, "# This is a documentation line",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Documentation is not allowed."));

        // Line comments
        debugTestRunner.assertEvaluationError(context, "// This is a comment",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Empty expressions cannot be " +
                        "evaluated."));
    }

    @Test
    public void unsupportedInputEvaluationTest() throws BallerinaTestException {
        // import statements
        debugTestRunner.assertEvaluationError(context, "import ballerina/http;",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Import declaration evaluation is not" +
                        " supported."));
        debugTestRunner.assertEvaluationError(context, "import ballerina/log",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Import declaration evaluation is not" +
                        " supported."));

        // Top-level definitions
        debugTestRunner.assertEvaluationError(context, "function foo() {}",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Top-level declaration evaluation is" +
                        " not supported."));
        debugTestRunner.assertEvaluationError(context, "class Person { int name = \"John\"; }",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Top-level declaration evaluation is" +
                        " not supported."));

        // statement(s)
        debugTestRunner.assertEvaluationError(context, "int a = 1; int b = 5;",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Statement evaluation is" +
                        " not supported."));
        debugTestRunner.assertEvaluationError(context, "if(true) { boolean isTrue = true; }",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Statement evaluation is" +
                        " not supported."));
        debugTestRunner.assertEvaluationError(context, "int a = 1;",
                String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Statement evaluation is" +
                        " not supported."));

        // unsupported expressions
        debugTestRunner.assertEvaluationError(context, "function(int a) returns int {return a;};",
                String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION.getString(),
                        "'function(int a) returns int {return a;}' - EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION"));
    }

    @Override
    @Test
    public void remoteCallActionEvaluationTest() throws BallerinaTestException {
        debugTestRunner.assertEvaluationError(context, String.format("%s->undefinedFunction()", CLIENT_OBJECT_VAR),
                String.format(EvaluationExceptionKind.REMOTE_METHOD_NOT_FOUND.getString(), "undefinedFunction",
                        "Student"));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
        this.context = null;
    }
}
