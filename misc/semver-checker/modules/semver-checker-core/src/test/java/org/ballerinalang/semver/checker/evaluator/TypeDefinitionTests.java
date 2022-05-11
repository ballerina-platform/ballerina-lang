package org.ballerinalang.semver.checker.evaluator;

import org.testng.annotations.Test;

public class TypeDefinitionTests extends BaseTypeDefinitionTest {

    private static final String TYPE_DEFINITION_DOCUMENTATION_TESTCASE = "src/test/resources/testcases/moduleTypeDefinition/documentation.json";
    private static final String TYPE_DEFINITION_ANNOTATION_TESTCASE = "src/test/resources/testcases/moduleTypeDefinition/annotation.json";
    private static final String TYPE_DEFINITION_IDENTIFIER_TESTCASE = "src/test/resources/testcases/moduleTypeDefinition/identifier.json";
    private static final String TYPE_DEFINITION_TYPE_DESCRIPTOR_TESTCASE = "src/test/resources/testcases/moduleTypeDefinition/typeDescriptor.json";


    @Test
    public void testTypeDefinitionDocumentation() throws Exception {

        testEvaluate(TYPE_DEFINITION_DOCUMENTATION_TESTCASE);
    }

    @Test
    public void testTypeDefinitionAnnotation() throws Exception {

        testEvaluate(TYPE_DEFINITION_ANNOTATION_TESTCASE);
    }

    @Test
    public void testTypeDefinitionIdentifier() throws Exception {

        testEvaluate(TYPE_DEFINITION_IDENTIFIER_TESTCASE);
    }

    @Test
    public void testTypeDefinitionTypeDescriptor() throws Exception {

        testEvaluate(TYPE_DEFINITION_TYPE_DESCRIPTOR_TESTCASE);
    }
}
