package io.ballerina.quoter.parser;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * Parser that parses as a statement.
 */
public class StatementParser extends QuoterParser {
    public StatementParser(long timeoutMs) {
        super(timeoutMs);
    }

    @Override
    public Node parse(String source) {
        String sourceCode = String.format("function main(){%s}", source);
        TextDocument document = TextDocuments.from(sourceCode);
        SyntaxTree tree = getSyntaxTree(document);
        ModulePartNode node = tree.rootNode();
        NodeList<ModuleMemberDeclarationNode> moduleDclns = node.members();
        assertIf(!moduleDclns.isEmpty(), "not a valid statement: " +
                "expected at least one member");
        ModuleMemberDeclarationNode moduleDeclaration = moduleDclns.get(0);
        FunctionDefinitionNode mainFunction = (FunctionDefinitionNode) moduleDeclaration;
        FunctionBodyBlockNode mainFunctionBody = (FunctionBodyBlockNode) mainFunction.functionBody();
        return NodeFactory.createBlockStatementNode(mainFunctionBody.openBraceToken(),
                mainFunctionBody.statements(), mainFunctionBody.closeBraceToken());
    }
}
