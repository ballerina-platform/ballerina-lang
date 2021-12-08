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

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Base implementation for debug expression evaluation scenarios.
 */
public abstract class ExpressionEvaluationBaseTest extends BaseTestCase {

    protected StoppedEventArguments context;

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

    protected void prepareForEvaluation() throws BallerinaTestException {
        String testProjectName = "evaluation-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 355));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        this.context = debugHitInfo.getRight();

        // Enable to see all the assertion failures at once.
        // debugTestRunner.setAssertionMode(DebugTestRunner.AssertionMode.SOFT_ASSERT);
    }

    // 1. literal expressions
    public abstract void literalEvaluationTest() throws BallerinaTestException;

    // 2. list constructor expressions
    public abstract void listConstructorEvaluationTest() throws BallerinaTestException;

    // 3. mapping constructor expressions
    public abstract void mappingConstructorEvaluationTest() throws BallerinaTestException;

    // Todo - table constructor[preview] and service constructor

    // 4. string template expressions
    public abstract void stringTemplateEvaluationTest() throws BallerinaTestException;

    // 5. xml template expressions
    public abstract void xmlTemplateEvaluationTest() throws BallerinaTestException;

    // 6. new constructor expressions
    public abstract void newConstructorEvaluationTest() throws BallerinaTestException;

    // 7. variable reference expressions
    public abstract void variableReferenceEvaluationTest() throws BallerinaTestException;

    // 8. field access expressions
    public abstract void fieldAccessEvaluationTest() throws BallerinaTestException;

    // 9. XML attribute access expressions
    public abstract void xmlAttributeAccessEvaluationTest() throws BallerinaTestException;

    // 10. Annotation access expressions
    public abstract void annotationAccessEvaluationTest() throws BallerinaTestException;

    // 11. Member access expressions
    public abstract void memberAccessEvaluationTest() throws BallerinaTestException;

    // 12. Function call expressions
    public abstract void functionCallEvaluationTest() throws BallerinaTestException;

    // 13. Method call expressions
    public abstract void methodCallEvaluationTest() throws BallerinaTestException;

    // 14. Error constructor expressions
    public abstract void errorConstructorEvaluationTest() throws BallerinaTestException;

    // 15. Anonymous function expressions
    public abstract void anonymousFunctionEvaluationTest() throws BallerinaTestException;

    // 16. Anonymous function expressions
    public abstract void letExpressionEvaluationTest() throws BallerinaTestException;

    // 17. Type cast expressions
    public abstract void typeCastEvaluationTest() throws BallerinaTestException;

    // 18. Typeof expressions
    public abstract void typeOfExpressionEvaluationTest() throws BallerinaTestException;

    // 19. Unary expressions
    public abstract void unaryExpressionEvaluationTest() throws BallerinaTestException;

    // 20. Multiplicative expressions
    public abstract void multiplicativeExpressionEvaluationTest() throws BallerinaTestException;

    // 21. Additive expressions
    public abstract void additiveExpressionEvaluationTest() throws BallerinaTestException;

    // 22. Shift expressions
    public abstract void shiftExpressionEvaluationTest() throws BallerinaTestException;

    // 23. Range expressions
    public abstract void rangeExpressionEvaluationTest() throws BallerinaTestException;

    // 24. Numerical comparison expressions
    public abstract void comparisonEvaluationTest() throws BallerinaTestException;

    // 25. Type test expressions
    public abstract void typeTestEvaluationTest() throws BallerinaTestException;

    // 26. Equality expressions
    public abstract void equalityEvaluationTest() throws BallerinaTestException;

    // 27. Binary bitwise expressions
    public abstract void binaryBitwiseEvaluationTest() throws BallerinaTestException;

    // 28. Logical expressions
    public abstract void logicalEvaluationTest() throws BallerinaTestException;

    // 29. Conditional expressions
    public abstract void conditionalExpressionEvaluationTest() throws BallerinaTestException;

    // 30. Checking expressions
    public abstract void checkingExpressionEvaluationTest() throws BallerinaTestException;

    // 31. Trap expressions
    public abstract void trapExpressionEvaluationTest() throws BallerinaTestException;

    // 32. Query expressions
    public abstract void queryExpressionEvaluationTest() throws BallerinaTestException;

    // 33. XML navigation expressions
    public abstract void xmlNavigationEvaluationTest() throws BallerinaTestException;

    // 34. Remote method call actions
    public abstract void remoteCallActionEvaluationTest() throws BallerinaTestException;
}
