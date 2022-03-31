/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Test cases to validate the code action syntax.
 *
 * @since 2201.0.3
 */
public class CodeActionNodeValidatorTest {

    private final Path sourcesPath = new File(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("codeaction/node-validator")).getFile()).toPath();

    @Test(dataProvider = "validatorDataProvider")
    public void testInvalidFunctionCall(String srcPath, int line, int character, boolean expectedResult) 
            throws IOException {
        Path sourcePath = sourcesPath.resolve(srcPath);
        String content = Files.readString(sourcePath);

        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);

        Range range = new Range(new Position(line, character), new Position(line, character));
        NonTerminalNode node = CommonUtil.findNode(range, syntaxTree);

        boolean result = CodeActionNodeValidator.validate(node);
        Assert.assertEquals(result, expectedResult);
    }

    @DataProvider(name = "validatorDataProvider")
    public Object[][] dataProvider() {
        return new Object[][] {
                {"validateFunctionCallExpressionNode1.bal", 1, 15, true},
                {"validateFunctionCallExpressionNode2.bal", 1, 15, false},
                {"validateFunctionCallExpressionNode3.bal", 1, 22, false},
                {"validateAssignmentStatementNode.bal", 2, 11, true},
                {"validateBinaryExpressionNode.bal", 1, 11, true},
                {"validateLetVariableDeclarationNode1.bal", 1, 27, true},
                {"validateLetVariableDeclarationNode2.bal", 1, 23, false},
                {"validateLetExpressionNode.bal", 1, 32, true},
                {"validateNamedArgumentNode1.bal", 1, 18, true},
                {"validateNamedArgumentNode2.bal", 1, 30, false},
                {"validateListConstructorExpressionNode.bal", 1, 15, true},
                {"validateCheckExpressionNode.bal", 1, 21, true},
                {"validateTableTypeDescriptorNode.bal", 5, 38, true},
                {"validateExternalTreeNodeList.bal", 1, 21, false},
                {"validateSpreadFieldNode.bal", 5, 24, true},
                {"validatePositionalArgumentNode.bal", 1, 20, true},
                {"validateRestArgumentNode1.bal", 2, 15, true},
                {"validateRestArgumentNode2.bal", 1, 15, false},
                {"validateFieldAccessExpressionNode.bal", 6, 15, true}
        };
    }
}
