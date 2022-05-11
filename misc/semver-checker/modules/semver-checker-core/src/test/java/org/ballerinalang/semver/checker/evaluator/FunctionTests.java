package org.ballerinalang.semver.checker.evaluator;

import org.testng.annotations.Test;

public class FunctionTests extends BaseFunctionTest {

    private static final String FUNCTION_ANNOTATION_TESTCASE = "src/test/resources/testcases/functionDefinition/annotation.json";
    private static final String FUNCTION_DOCUMENTATION_TESTCASE = "src/test/resources/testcases/functionDefinition/documentation.json";
    private static final String FUNCTION_BODY_TESTCASE = "src/test/resources/testcases/functionDefinition/functionBody.json";
    private static final String FUNCTION_IDENTIFIER_TESTCASE = "src/test/resources/testcases/functionDefinition/identifier.json";
    private static final String FUNCTION_PARAMETER_TESTCASE = "src/test/resources/testcases/functionDefinition/parameter.json";
    private static final String FUNCTION_QUALIFIER_TESTCASE = "src/test/resources/testcases/functionDefinition/qualifier.json";
    private static final String FUNCTION_RETURN_TYPE_TESTCASE = "src/test/resources/testcases/functionDefinition/returnType.json";
    private static final String ADVANCE_FUNCTION_TESTCASE = "src/test/resources/testcases/functionDefinition/advanceFunction.json";

    @Test
    public void testFunctionAnnotation() throws Exception {

        testEvaluate(FUNCTION_ANNOTATION_TESTCASE);
    }

    @Test
    public void testFunctionDocumentation() throws Exception {

        testEvaluate(FUNCTION_DOCUMENTATION_TESTCASE);
    }

    @Test
    public void testFunctionBody() throws Exception {

        testEvaluate(FUNCTION_BODY_TESTCASE);
    }

    @Test
    public void testFunctionIdentifier() throws Exception {

        testEvaluate(FUNCTION_IDENTIFIER_TESTCASE);
    }

    @Test
    public void testFunctionParameter() throws Exception {

        testEvaluate(FUNCTION_PARAMETER_TESTCASE);
    }

    @Test
    public void testFunctionQualifier() throws Exception {

        testEvaluate(FUNCTION_QUALIFIER_TESTCASE);
    }

    @Test
    public void testFunctionReturnType() throws Exception {

        testEvaluate(FUNCTION_RETURN_TYPE_TESTCASE);
    }

    @Test
    public void testAdvanceFunction() throws Exception {

        testEvaluate(ADVANCE_FUNCTION_TESTCASE);
    }

}
