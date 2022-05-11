package org.ballerinalang.semver.checker.evaluator;

import org.testng.annotations.Test;

public class VariableDeclarationTests extends BaseVariableDeclarationTest {

    private static final String BASICS_VARIABLE_DECLARATION_TESTCASE = "src/test/resources/testcases/variableDeclaration.json";

    @Test
    public void testBasicsTypeDefinition() throws Exception {

        testEvaluate(BASICS_VARIABLE_DECLARATION_TESTCASE);
    }
}
