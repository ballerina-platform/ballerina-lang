package org.ballerinalang.semver.checker.evaluator;

import org.testng.annotations.Test;

public class ClassDefinitionTests extends BaseClassTest {

    private static final String CLASS_DEFINITION_ANNOTATION_TESTCASE = "src/test/resources/testcases/moduleClassDefinition/annotation.json";
    private static final String CLASS_DEFINITION_DOCUMENTATION_TESTCASE = "src/test/resources/testcases/moduleClassDefinition/documentation.json";
    private static final String CLASS_DEFINITION_CLASS_MEMBERS_TESTCASE = "src/test/resources/testcases/moduleClassDefinition/classMembers.json";
    private static final String CLASS_DEFINITION_IDENTIFIER_TESTCASE = "src/test/resources/testcases/moduleClassDefinition/identifier.json";


    @Test
    public void testClassDefinitionDocumentation() throws Exception {

        testEvaluate(CLASS_DEFINITION_DOCUMENTATION_TESTCASE);
    }

    @Test
    public void testClassDefinitionAnnotation() throws Exception {

        testEvaluate(CLASS_DEFINITION_ANNOTATION_TESTCASE);
    }

    @Test
    public void testClassDefinitionIdentifier() throws Exception {

        testEvaluate(CLASS_DEFINITION_IDENTIFIER_TESTCASE);
    }

    @Test
    public void testClassDefinitionClassMembers() throws Exception {

        testEvaluate(CLASS_DEFINITION_CLASS_MEMBERS_TESTCASE);
    }
}
