package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.TaintNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.963.0
 */
public class BLangTaint extends BLangStatement implements TaintNode {

    public List<BLangVariableReference> varRefs;

    public BLangTaint() {
        this.varRefs = new ArrayList<>();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TAINT;
    }

    @Override
    public String toString() {
        return "BLangTaint: " + varRefs;
    }
}
