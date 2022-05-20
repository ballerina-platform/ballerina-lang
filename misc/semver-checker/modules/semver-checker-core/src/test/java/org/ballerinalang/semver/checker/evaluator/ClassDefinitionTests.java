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
 * Extended classDefinitionTest class for test each test case scenarios separately.
 */
public class ClassDefinitionTests extends BaseClassTest {
    private static final String BaseFolder = "src/test/resources/testcases/moduleClassDefinition";
    private static final String CLASS_DEFINITION_ANNOTATION_TESTCASE = BaseFolder + "/annotation.json";
    private static final String CLASS_DEFINITION_DOCUMENTATION_TESTCASE = BaseFolder + "/documentation.json";
    private static final String CLASS_DEFINITION_CLASS_MEMBERS_TESTCASE = BaseFolder + "/classMembers.json";
    private static final String CLASS_DEFINITION_IDENTIFIER_TESTCASE = BaseFolder + "/identifier.json";


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
