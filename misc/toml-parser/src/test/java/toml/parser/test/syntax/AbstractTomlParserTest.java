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

import toml.parser.test.ParserTestUtils;

import java.nio.file.Path;


/**
 * Test util for testing toml.
 */
public abstract class AbstractTomlParserTest {
    private final String resourceDirectoryName;

    public AbstractTomlParserTest(String resourceDirectoryName) {
        this.resourceDirectoryName = resourceDirectoryName;
    }

    void testFile(String path, String filePath) {
        ParserTestUtils.test(Path.of("syntax", resourceDirectoryName, path), Path.of("syntax",
                resourceDirectoryName, filePath));
    }
}
