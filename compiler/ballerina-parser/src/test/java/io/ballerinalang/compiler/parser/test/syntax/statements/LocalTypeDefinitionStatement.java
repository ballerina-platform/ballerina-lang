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
package io.ballerinalang.compiler.parser.test.syntax.statements;

import org.testng.annotations.Test;

/**
 * Test parsing object type definitions.
 */
public class LocalTypeDefinitionStatement extends AbstractStatementTest {

    // Local type definitions are not allowed in new spec. This test is only to check what happens if user mistakenly
    // declare a local type def

    @Test
    public void testLocalTypedef() {
        testFile("local-type-defn-stmt/local_type_defn_stmt_source_01.bal",
        "local-type-defn-stmt/local_type_defn_stmt_assert_01.json");
    }

}
