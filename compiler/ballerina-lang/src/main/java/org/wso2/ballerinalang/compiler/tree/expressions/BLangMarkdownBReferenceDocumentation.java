package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.MarkdownDocumentationBReferenceAttributeNode;
import org.ballerinalang.model.tree.types.DocumentationReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Represents a service reference documentation node.
 *
 * @since 1.0.2
 */
public class BLangMarkdownBReferenceDocumentation extends BLangExpression
        implements MarkdownDocumentationBReferenceAttributeNode {

    public String referenceName;
    public BVarSymbol symbol;
    public NodeKind kind;
    public DocumentationReferenceType type;

    @Override
    public String getReferenceName() {
        return referenceName;
    }

    @Override
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.DOCUMENTATION_REFERENCE;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
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
    public void setType(DocumentationReferenceType type) {
        this.type = type;
    }

    @Override
    public DocumentationReferenceType getType() {
        return type;
    }
}
