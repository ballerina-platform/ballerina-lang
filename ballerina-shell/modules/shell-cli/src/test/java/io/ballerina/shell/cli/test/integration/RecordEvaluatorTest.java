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
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class RecordEvaluatorTest extends AbstractIntegrationTest {
    private static final String RECORD_TESTCASE = "testcases/record.basic.json";
    private static final String OPTIONAL_TESTCASE = "testcases/record.optional.json";
    private static final String REF_TESTCASE = "testcases/record.ref.json";

    @Test
    public void testRecordBasic() throws Exception {
        test(RECORD_TESTCASE);
    }

    @Test
    public void testRecordOptional() throws Exception {
        test(OPTIONAL_TESTCASE);
    }

    @Test
    public void testRecordRef() throws Exception {
        test(REF_TESTCASE);
    }
}
