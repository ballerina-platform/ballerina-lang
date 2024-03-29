/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test parsing worker declarations.
 * 
 * @since 1.3.0
 */
public class WorkerDeclTest extends AbstractMiscTest {

    @Test
    public void testBasicWorkerDecl() {
        testFile("worker-decl/worker_decl_source_01.bal", "worker-decl/worker_decl_assert_01.json");
    }

    @Test
    public void testWorkerDeclWithOnFailClause() {
        testFile("worker-decl/worker_decl_source_06.bal", "worker-decl/worker_decl_assert_06.json");
    }

    // Recovery tests

    @Test
    public void testMissingWorkerName() {
        testFile("worker-decl/worker_decl_source_02.bal", "worker-decl/worker_decl_assert_02.json");
    }

    @Test
    public void testMissingOpenAndCloseBraces() {
        testFile("worker-decl/worker_decl_source_03.bal", "worker-decl/worker_decl_assert_03.json");
    }

    @Test
    public void testMissingReturnTypeDesc() {
        testFile("worker-decl/worker_decl_source_04.bal", "worker-decl/worker_decl_assert_04.json");
    }
    

    @Test
    public void testInvalidUsageOfNamedWorkers() {
        testFile("worker-decl/worker_decl_source_05.bal", "worker-decl/worker_decl_assert_05.json");
    }

    @Test
    public void testWorkerWithOnFailClauseRecovery() {
        testFile("worker-decl/worker_decl_source_07.bal", "worker-decl/worker_decl_assert_07.json");
    }
}
