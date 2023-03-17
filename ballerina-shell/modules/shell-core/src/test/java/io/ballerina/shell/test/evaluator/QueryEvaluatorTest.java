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
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class QueryEvaluatorTest extends AbstractEvaluatorTest {
    private static final String QUERY_EXPR_EVALUATOR_TESTCASE = "testcases/evaluator/query.expr.json";
    private static final String QUERY_JOIN_EVALUATOR_TESTCASE = "testcases/evaluator/query.join.json";

    private static final String QUERY_ACTION_EVALUATOR_TESTCASE = "testcases/evaluator/query.action.json";

    @Test
    public void testEvaluateQueryExpr() throws BallerinaShellException {
        // TODO: (#28389) Compiler crashes for module-level query expression which uses var
        testEvaluate(QUERY_EXPR_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateQueryJoin() throws BallerinaShellException {
        // TODO: (#28390) Module-level query expressions don't work
        testEvaluate(QUERY_JOIN_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateQueryAction() throws BallerinaShellException {
        testEvaluate(QUERY_ACTION_EVALUATOR_TESTCASE);
    }
}
