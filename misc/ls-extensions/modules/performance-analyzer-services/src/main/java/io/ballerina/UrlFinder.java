package io.ballerina;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;

public class UrlFinder extends NodeVisitor {

    private String token;

    public String getToken() {

        return token;
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {

        visitSyntaxNode(namedArgumentNode);

    }

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {

        visitSyntaxNode(positionalArgumentNode);

    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {

        visitSyntaxNode(simpleNameReferenceNode);

    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {

        visitSyntaxNode(qualifiedNameReferenceNode);

    }

    @Override
    public void visit(BasicLiteralNode basicLiteralNode) {

        visitSyntaxNode(basicLiteralNode);
    }

    @Override
    public void visit(ParenthesizedArgList parenthesizedArgList) {

        visitSyntaxNode(parenthesizedArgList);

    }

    @Override
    public void visit(Token token) {

        this.token = token.text();
    }
}
