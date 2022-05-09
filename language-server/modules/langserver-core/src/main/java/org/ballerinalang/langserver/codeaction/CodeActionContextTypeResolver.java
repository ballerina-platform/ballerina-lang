package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.commons.CodeActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This visitor is used to resolve a type of given context.
 * 
 * @since 2201.1.1
 */
public class CodeActionContextTypeResolver extends NodeTransformer<Optional<TypeSymbol>> {

    CodeActionContext context;
    private final List<Node> visitedNodes = new ArrayList<>();

    public CodeActionContextTypeResolver(CodeActionContext context) {
        this.context = context;
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }
        visitedNodes.add(node);
        return node.apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        return getTypeDescriptorOfVariable(node);
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode assignmentStatementNode) {
        return getTypeDescriptorOfVariable(assignmentStatementNode.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return getTypeDescriptorOfVariable(moduleVariableDeclarationNode);
    }

    @Override
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return getTypeDescriptorOfVariable(letVariableDeclarationNode);
    }

    private Optional<TypeSymbol> getTypeDescriptorOfVariable(Node node) {
        Optional<Symbol> symbol = context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(node));
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }
        return Optional.of(((VariableSymbol) symbol.get()).typeDescriptor());
    }
}
