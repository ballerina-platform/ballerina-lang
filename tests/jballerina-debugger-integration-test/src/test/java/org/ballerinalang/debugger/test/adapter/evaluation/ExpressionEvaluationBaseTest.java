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

import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Base implementation for debug expression evaluation scenarios.
 */
public abstract class ExpressionEvaluationBaseTest extends BaseTestCase {

    protected StoppedEventArguments context;

    // local variable names defined in test sources
    protected static final String NIL_VAR = "varVariable";
    protected static final String BOOLEAN_VAR = "booleanVar";
    protected static final String INT_VAR = "intVar";
    protected static final String UNSIGNED8INT_VAR = "unsigned8IntVar";
    protected static final String UNSIGNED16INT_VAR = "unsigned16IntVar";
    protected static final String UNSIGNED32INT_VAR = "unsigned32IntVar";
    protected static final String SIGNED8INT_VAR = "signed8IntVar";
    protected static final String SIGNED16INT_VAR = "signed16IntVar";
    protected static final String SIGNED32INT_VAR = "signed32IntVar";
    protected static final String FLOAT_VAR = "floatVar";
    protected static final String DECIMAL_VAR = "decimalVar";
    protected static final String STRING_VAR = "stringVar";
    protected static final String XML_VAR = "xmlVar";
    protected static final String ARRAY_VAR = "arrayVar";
    protected static final String TUPLE_VAR = "tupleVar";
    protected static final String MAP_VAR = "mapVar";
    protected static final String RECORD_VAR = "recordVar";
    protected static final String ANON_RECORD_VAR = "anonRecord";
    protected static final String ERROR_VAR = "errorVar";
    protected static final String ANON_FUNCTION_VAR = "anonFunctionVar";
    protected static final String FUTURE_VAR = "futureVar";
    protected static final String OBJECT_VAR = "objectVar";
    protected static final String ANON_OBJECT_VAR = "anonObjectVar";
    protected static final String CLIENT_OBJECT_VAR = "clientObjectVar";
    protected static final String TYPEDESC_VAR = "typedescVar";
    protected static final String UNION_VAR = "unionVar";
    protected static final String OPTIONAL_VAR = "optionalVar";
    protected static final String ANY_VAR = "anyVar";
    protected static final String ANYDATA_VAR = "anydataVar";
    protected static final String BYTE_VAR = "byteVar";
    protected static final String JSON_VAR = "jsonVar";
    protected static final String TABLE_VAR = "tableWithKeyVar";
    protected static final String STREAM_VAR = "oddNumberStream";
    protected static final String NEVER_VAR = "neverVar";
    protected static final String SERVICE_VAR = "serviceVar";

    // global variable names defined in test sources
    protected static final String GLOBAL_VAR_01 = "nameWithoutType";
    protected static final String GLOBAL_VAR_02 = "nameWithType";
    protected static final String GLOBAL_VAR_03 = "nameMap";
    protected static final String GLOBAL_VAR_04 = "nilWithoutType";
    protected static final String GLOBAL_VAR_05 = "nilWithType";
    protected static final String GLOBAL_VAR_06 = "stringValue";
    protected static final String GLOBAL_VAR_07 = "decimalValue";
    protected static final String GLOBAL_VAR_08 = "byteValue";
    protected static final String GLOBAL_VAR_09 = "floatValue";
    protected static final String GLOBAL_VAR_10 = "jsonVar";
    protected static final String GLOBAL_VAR_11 = "'\\ \\/\\:\\@\\[\\`\\{\\~\\u{03C0}_IL";

    // basic, simple types
    protected static final String INT_TYPE_DESC = "int";
    protected static final String FLOAT_TYPE_DESC = "float";
    protected static final String DECIMAL_TYPE_DESC = "decimal";
    protected static final String BOOLEAN_TYPE_DESC = "boolean";

    // basic, sequence types
    protected static final String STRING_TYPE_DESC = "string";

    // other types
    protected static final String BYTE_TYPE_DESC = "byte";
    protected static final String JSON_TYPE_DESC = "json";
    protected static final String ANY_TYPE_DESC = "any";
    protected static final String ANYDATA_TYPE_DESC = "anydata";
    protected static final String NEVER_TYPE_DESC = "never";
    protected static final String PARENTHESISED_TYPE_DESC = "(int)";

    protected DebugTestRunner debugTestRunner;

    @BeforeMethod(alwaysRun = true)
    protected void beginSoftAssertions() {
        if (debugTestRunner.isSoftAssertionsEnabled()) {
            debugTestRunner.beginSoftAssertions();
        }
    }

    @AfterMethod(alwaysRun = true)
    protected void endSoftAssertions() {
        if (debugTestRunner.isSoftAssertionsEnabled()) {
            debugTestRunner.endSoftAssertions();
        }
    }

    protected abstract void prepareForEvaluation() throws BallerinaTestException;

    public abstract void literalEvaluationTest() throws BallerinaTestException;

    public abstract void listConstructorEvaluationTest() throws BallerinaTestException;

    public abstract void mappingConstructorEvaluationTest() throws BallerinaTestException;

    public abstract void stringTemplateEvaluationTest() throws BallerinaTestException;

    public abstract void xmlTemplateEvaluationTest() throws BallerinaTestException;

    public abstract void newConstructorEvaluationTest() throws BallerinaTestException;

    public abstract void nameReferenceEvaluationTest() throws BallerinaTestException;

    public abstract void builtInNameReferenceEvaluationTest() throws BallerinaTestException;

    public abstract void fieldAccessEvaluationTest() throws BallerinaTestException;

    public abstract void xmlAttributeAccessEvaluationTest() throws BallerinaTestException;

    public abstract void annotationAccessEvaluationTest() throws BallerinaTestException;

    public abstract void memberAccessEvaluationTest() throws BallerinaTestException;

    public abstract void functionCallEvaluationTest() throws BallerinaTestException;

    public abstract void methodCallEvaluationTest() throws BallerinaTestException;

    public abstract void errorConstructorEvaluationTest() throws BallerinaTestException;

    public abstract void anonymousFunctionEvaluationTest() throws BallerinaTestException;

    public abstract void letExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void typeCastEvaluationTest() throws BallerinaTestException;

    public abstract void typeOfExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void unaryExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void multiplicativeExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void additiveExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void shiftExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void rangeExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void comparisonEvaluationTest() throws BallerinaTestException;

    public abstract void typeTestEvaluationTest() throws BallerinaTestException;

    public abstract void equalityEvaluationTest() throws BallerinaTestException;

    public abstract void binaryBitwiseEvaluationTest() throws BallerinaTestException;

    public abstract void logicalEvaluationTest() throws BallerinaTestException;

    public abstract void conditionalExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void checkingExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void trapExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void queryExpressionEvaluationTest() throws BallerinaTestException;

    public abstract void xmlNavigationEvaluationTest() throws BallerinaTestException;

    public abstract void remoteCallActionEvaluationTest() throws BallerinaTestException;
}
