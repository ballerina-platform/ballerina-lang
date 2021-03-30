package io.ballerina.quoter.parser;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * Parser that parses as a statement.
 */
public class ExpressionParser extends StatementParser {
    private static final String QUOTE = ";";

    public ExpressionParser(long timeoutMs) {
        super(timeoutMs);
    }

    @Override
    public Node parse(String source) {
        source = source.endsWith(QUOTE) ? source : source + QUOTE;
        String sourceCode = String.format("function main(){return %s}", source);
        TextDocument document = TextDocuments.from(sourceCode);
        SyntaxTree tree = getSyntaxTree(document);
        ModulePartNode node = tree.rootNode();
        NodeList<ModuleMemberDeclarationNode> moduleDclns = node.members();
        assertIf(!moduleDclns.isEmpty(), "not a valid expression: " +
                "expected at least one member");
        ModuleMemberDeclarationNode moduleDeclaration = moduleDclns.get(0);
        FunctionDefinitionNode mainFunction = (FunctionDefinitionNode) moduleDeclaration;
        FunctionBodyBlockNode mainFunctionBody = (FunctionBodyBlockNode) mainFunction.functionBody();

        assertIf(!mainFunctionBody.statements().isEmpty(), "not a valid expression: " +
                "could not find any statements");
        StatementNode statementNode = mainFunctionBody.statements().get(0);
        assertIf(statementNode instanceof ReturnStatementNode, "not a valid expression: " +
                "expected a return statement");
        assert statementNode instanceof ReturnStatementNode;
        ReturnStatementNode returnStatement = (ReturnStatementNode) statementNode;
        assertIf(returnStatement.expression().isPresent(), "not a valid expression: " +
                "expected an expression on return");
        return returnStatement.expression().get();
    }
}
