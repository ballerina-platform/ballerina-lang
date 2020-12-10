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
 * Syntax Tree tests for Key Value pairs.
 */
public class KeyValueTest extends AbstractTomlParserTest {

    public KeyValueTest() {
        super("key-value");
    }
    @Test
    public void testKeysJson() {
        super.testFile("keys.toml", "keys.json");
    }

    @Test
    public void testValuesJson() {
        super.testFile("values.toml", "values.json");
    }

    @Test
    public void testArrayJson() {
        super.testFile("array.toml", "array.json");
    }

    @Test
    public void testNoNewlineEnd() {
        super.testFile("no-newline-end.toml", "no-newline-end.json");
    }
}
