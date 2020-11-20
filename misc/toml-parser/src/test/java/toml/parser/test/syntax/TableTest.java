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

package toml.parser.test.syntax;

import org.testng.annotations.Test;

/***
 * Syntax Tree tests for Tables and Array of tables.
 */
public class TableTest extends AbstractTomlParserTest {

    public TableTest() {
        super("tables");
    }

    @Test
    public void testTableJson() {
        super.testFile("table.toml", "table.json");
    }

    @Test
    public void testArrayOfTableJson() {
        super.testFile("array-of-tables.toml", "array-of-tables.json");
    }
}
