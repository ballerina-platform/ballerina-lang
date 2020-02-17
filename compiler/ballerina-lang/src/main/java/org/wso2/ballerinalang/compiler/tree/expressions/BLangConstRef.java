package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Represents a constant reference variable.
 *
 * @since 1.2.0
 */
public class BLangConstRef extends BLangSimpleVarRef implements LiteralNode, RecordLiteralNode.RecordVarNameFieldNode {

    public Object value;
    public String originalValue;

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CONSTANT_REF;
    }

    @Override
    public boolean isKeyValueField() {
        return false;
    }
}
