package org.ballerinalang.langserver.validator;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
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


public class ValidatorTest {

    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("validator").getFile()).toPath();

    @Test(dataProvider = "validatorDataProvider")
    public void testInvalidFunctionCall(String srcPath, int line, int character, boolean expectedResult) throws IOException {
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
                {"validateFunctionCallExpressionNode1.bal", 2, 15, true},
                {"validateFunctionCallExpressionNode2.bal", 2, 15, false},
                {"validateAssignmentStatementNode.bal", 3, 11, true},
                {"validateBinaryExpressionNode.bal", 2, 11, true},
                {"validateLetVariableDeclarationNode.bal", 2, 27, true},
                {"validateLetExpressionNode.bal", 2, 32, true},
                {"validateNamedArgumentNode.bal", 2, 18, true},
                {"validateListConstructorExpressionNode.bal", 2, 15, true},
                {"validateCheckExpressionNode.bal", 2, 21, true},
                {"validateTableTypeDescriptorNode.bal", 7, 38, true},
                {"validateExternalTreeNodeList.bal", 2, 21, false},
                {"validateSpreadFieldNode.bal", 7, 24, true},
                {"validatePositionalArgumentNode.bal", 2, 20, true},
                {"validateRestArgumentNode.bal", 3, 15, true}
        };
    }
}