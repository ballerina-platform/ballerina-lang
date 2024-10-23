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

import org.testng.annotations.Test;

/**
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class ObjectEvaluatorTest extends AbstractEvaluatorTest {
    private static final String CLASS_EVALUATOR_TESTCASE = "testcases/evaluator/object.class.json";
    private static final String INIT_EVALUATOR_TESTCASE = "testcases/evaluator/object.init.json";
    private static final String METHODS_EVALUATOR_TESTCASE = "testcases/evaluator/object.methods.json";
    private static final String ASSIGN_EVALUATOR_TESTCASE = "testcases/evaluator/object.assign.json";
    private static final String TYPE_EVALUATOR_TESTCASE = "testcases/evaluator/object.type.json";
    private static final String CONSTRUCTOR_EVALUATOR_TESTCASE = "testcases/evaluator/object.constructor.json";
    private static final String READONLY_EVALUATOR_TESTCASE = "testcases/evaluator/object.readonly.json";
    private static final String REF_EVALUATOR_TESTCASE = "testcases/evaluator/object.ref.json";

    @Test
    public void testEvaluateClass() {
        testEvaluate(CLASS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateInit() {
        testEvaluate(INIT_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateMethod() {
        testEvaluate(METHODS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateAssign() {
        testEvaluate(ASSIGN_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateType() {
        testEvaluate(TYPE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateConstructor() {
        testEvaluate(CONSTRUCTOR_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateReadonly() {
        testEvaluate(READONLY_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateRef() {
        testEvaluate(REF_EVALUATOR_TESTCASE);
    }
}
