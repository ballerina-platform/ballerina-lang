/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.visitors;

import io.ballerinalang.compiler.parser.test.ParserTestUtils;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An abstract class that contains utilities for {@code SyntaxNodeVisitor} tests.
 *
 * @since 1.3.0
 */
public abstract class AbstractVisitorTest {

    public static SyntaxTree parseFile(String sourceFilePath) {
        Path sourcePath = Paths.get("visitors", sourceFilePath);
        return ParserTestUtils.parseFile(sourcePath);
    }
}
