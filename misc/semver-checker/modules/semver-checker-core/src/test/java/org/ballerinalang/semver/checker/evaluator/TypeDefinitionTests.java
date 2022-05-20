/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.semver.checker.evaluator;

import org.testng.annotations.Test;

/**
 * Extended TypeDefinitionTest class for test each test case scenarios separately.
 */
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
