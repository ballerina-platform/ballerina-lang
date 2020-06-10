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
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerinalang.compiler.parser.test.ParserTestUtils;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.LineRange;
import org.testng.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An abstract class that contains utilities for {@code SyntaxNodeVisitor} tests.
 *
 * @since 1.3.0
 */
abstract class AbstractSyntaxTreeAPITest {

    protected SyntaxTree parseFile(String sourceFileName) {
        return ParserTestUtils.parseFile(getPath(sourceFileName));
    }

    protected SyntaxTree parseFile(Path sourceFilePath) {
        return ParserTestUtils.parseFile(getPath(sourceFilePath));
    }

    protected String getFileContentAsString(String filePath) {
        return ParserTestUtils.getSourceText(getPath(filePath));
    }

    protected Path getPath(String fileName) {
        return Paths.get("tree", fileName);
    }

    protected Path getPath(Path filePath) {
        return Paths.get("tree").resolve(filePath);
    }

    protected void assertLineRange(LineRange actualLineRange, LineRange expectedLineRange) {
        Assert.assertEquals(actualLineRange.filePath(), expectedLineRange.filePath());
        Assert.assertEquals(actualLineRange.startLine(), expectedLineRange.startLine());
        Assert.assertEquals(actualLineRange.endLine(), expectedLineRange.endLine());
    }

    protected ModulePartNode getModulePartNode(String sourceFileName) {
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        return syntaxTree.modulePart();
    }

    protected void testTree(Node node, Path jsonFilePath) {
        ParserTestUtils.testTree(node, Paths.get("tree").resolve(jsonFilePath));
    }
}
