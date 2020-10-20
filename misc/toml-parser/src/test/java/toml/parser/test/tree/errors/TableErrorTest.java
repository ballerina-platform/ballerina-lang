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

package toml.parser.test.tree.errors;

import org.testng.annotations.Test;

/**
 * Test cases for Table Syntax errors in TOML.
 */
public class TableErrorTest extends AbstractErrorTest {
    @Test
    public void testMissingTableKey() {
        super.testFile("table/empty-table-key.toml", "table/empty-table-key.json");
    }

    @Test
    public void testMissingTableClose() {
        super.testFile("table/empty-table-close.toml", "table/empty-table-close.json");
    }

    @Test
    public void testWrongCloseBraceTableArray() {
        super.testFile("table/wrong-closing-brace.toml", "table/wrong-closing-brace.json");
    }

    @Test
    public void testEmptyTableOpen() {
        super.testFile("table/empty-table-open.toml", "table/empty-table-open.json");
    }
}
