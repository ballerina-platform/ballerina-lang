/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.test.evaluator;

import io.ballerina.shell.exceptions.BallerinaShellException;
import org.testng.annotations.Test;

/**
 * Test Json/Xml based snippets.
 *
 * @since 2.0.0
 */
public class JsonXmlEvaluatorTest extends AbstractEvaluatorTest {
    private static final String JSON_EVALUATOR_TESTCASE = "testcases/evaluator/json.json.json";
    private static final String JSON_OBJECTS_EVALUATOR_TESTCASE = "testcases/evaluator/json.objects.json";
    private static final String JSON_ARRAYS_EVALUATOR_TESTCASE = "testcases/evaluator/json.arrays.json";
    private static final String JSON_ACCESS_EVALUATOR_TESTCASE = "testcases/evaluator/json.access.json";
    private static final String JSON_CONV_MAP_EVALUATOR_TESTCASE = "testcases/evaluator/json.convert.json";
    private static final String XML_EVALUATOR_TESTCASE = "testcases/evaluator/xml.basic.json";
    private static final String LITERAL_EVALUATOR_TESTCASE = "testcases/evaluator/xml.literal.json";
    private static final String ATTRIBUTE_EVALUATOR_TESTCASE = "testcases/evaluator/xml.attributes.json";
    private static final String NAMESPACE_EVALUATOR_TESTCASE = "testcases/evaluator/xml.namespaces.json";
    private static final String ACCESS_EVALUATOR_TESTCASE = "testcases/evaluator/xml.access.json";
    private static final String FUNCTION_EVALUATOR_TESTCASE = "testcases/evaluator/xml.function.json";

    @Test
    public void testEvaluateJson() throws BallerinaShellException {
        testEvaluate(JSON_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateJsonObjects() throws BallerinaShellException {
        testEvaluate(JSON_OBJECTS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateJsonArrays() throws BallerinaShellException {
        testEvaluate(JSON_ARRAYS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateJsonAccess() throws BallerinaShellException {
        testEvaluate(JSON_ACCESS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateJsonConvertMap() throws BallerinaShellException {
        testEvaluate(JSON_CONV_MAP_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXml() throws BallerinaShellException {
        testEvaluate(XML_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlLiteral() throws BallerinaShellException {
        testEvaluate(LITERAL_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlAttribute() throws BallerinaShellException {
        testEvaluate(ATTRIBUTE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlNamespace() throws BallerinaShellException {
        testEvaluate(NAMESPACE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlAccess() throws BallerinaShellException {
        testEvaluate(ACCESS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlFunction() throws BallerinaShellException {
        testEvaluate(FUNCTION_EVALUATOR_TESTCASE);
    }
}
