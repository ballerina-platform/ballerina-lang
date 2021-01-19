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
public class RecordEvaluatorTest extends AbstractEvaluatorTest {
    private static final String RECORD_TESTCASE = "testcases/evaluator/record.basic.json";
    private static final String ANON_TESTCASE = "testcases/evaluator/record.anon.json";
    private static final String OPTIONAL_TESTCASE = "testcases/evaluator/record.optional.json";
    private static final String REF_TESTCASE = "testcases/evaluator/record.ref.json";
    private static final String READONLY_TESTCASE = "testcases/evaluator/record.readonly.json";

    @Test
    public void testRecordBasic() throws BallerinaShellException {
        testEvaluate(RECORD_TESTCASE);
    }

    @Test
    public void testRecordAnon() throws BallerinaShellException {
        testEvaluate(ANON_TESTCASE);
    }

    @Test
    public void testRecordOptional() throws BallerinaShellException {
        testEvaluate(OPTIONAL_TESTCASE);
    }

    @Test
    public void testRecordRef() throws BallerinaShellException {
        testEvaluate(REF_TESTCASE);
    }

    @Test
    public void testRecordReadonly() throws BallerinaShellException {
        testEvaluate(READONLY_TESTCASE);
    }
}
