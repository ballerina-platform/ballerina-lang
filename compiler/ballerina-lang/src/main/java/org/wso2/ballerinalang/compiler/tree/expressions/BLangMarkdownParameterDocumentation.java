package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.MarkdownDocumentationParameterAttributeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a parameter documentation node.
 *
 * @since 0.981.0
 */
public class BLangMarkdownParameterDocumentation extends BLangExpression
        implements MarkdownDocumentationParameterAttributeNode {

    public BLangIdentifier parameterName;
    public List<String> parameterDocumentationLines;
    public BVarSymbol symbol;

    public BLangMarkdownParameterDocumentation() {
        parameterDocumentationLines = new LinkedList<>();
    }

    @Override
    public IdentifierNode getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(IdentifierNode parameterName) {
        this.parameterName = (BLangIdentifier) parameterName;
    }

    @Override
    public List<String> getParameterDocumentationLines() {
        return parameterDocumentationLines;
    }

    @Override
    public void addParameterDocumentationLine(String text) {
        parameterDocumentationLines.add(text);
    }

    @Override
    public String getParameterDocumentation() {
        return parameterDocumentationLines.stream().collect(Collectors.joining("\n"));
    }

    @Override
    public BVarSymbol getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(BVarSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.DOCUMENTATION_PARAMETER;
    }

    @Override
    public String toString() {
        return "BLangMarkdownParameterDocumentation: " + parameterName + " : " + parameterDocumentationLines;
    }
}
