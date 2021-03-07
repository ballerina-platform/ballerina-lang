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

package io.ballerina.shell.cli.test.integration;

import org.testng.annotations.Test;

/**
 * Test Json/Xml based snippets.
 *
 * @since 2.0.0
 */
public class JsonXmlEvaluatorTest extends AbstractIntegrationTest {
    private static final String JSON_EVALUATOR_TESTCASE = "testcases/json.json.json";
    private static final String JSON_OBJECTS_EVALUATOR_TESTCASE = "testcases/json.objects.json";
    private static final String JSON_ACCESS_EVALUATOR_TESTCASE = "testcases/json.access.json";
    private static final String XML_EVALUATOR_TESTCASE = "testcases/xml.basic.json";
    private static final String ATTRIBUTE_EVALUATOR_TESTCASE = "testcases/xml.attributes.json";
    private static final String NAMESPACE_EVALUATOR_TESTCASE = "testcases/xml.namespaces.json";

    @Test
    public void testEvaluateJson() throws Exception {
        test(JSON_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateJsonObjects() throws Exception {
        test(JSON_OBJECTS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateJsonAccess() throws Exception {
        test(JSON_ACCESS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXml() throws Exception {
        test(XML_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlAttribute() throws Exception {
        test(ATTRIBUTE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateXmlNamespace() throws Exception {
        test(NAMESPACE_EVALUATOR_TESTCASE);
    }
}
