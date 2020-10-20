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
 * Test cases for Key Value pair Syntax errors in TOML.
 */
public class KeyValueErrorTest extends AbstractErrorTest {
    @Test
    public void testMissingEquals() {
        super.testFile("key-value/missing-equal.toml", "key-value/missing-equal.json");
    }

    @Test
    public void testMissingKey() {
        super.testFile("key-value/missing-key.toml", "key-value/missing-key.json");
    }

    @Test
    public void testArrayMissingComma() {
        super.testFile("key-value/array/array-missing-comma.toml", "key-value/array/array-missing-comma.json");
    }

    @Test
    public void testMissingNewLine() {
        super.testFile("key-value/missing-new-line.toml", "key-value/missing-new-line.json");
    }
}
