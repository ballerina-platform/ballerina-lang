package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.UntaintNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.963.0
 */
public class BLangUntaint extends BLangStatement implements UntaintNode {

    public List<BLangVariableReference> varRefs;

    public BLangUntaint() {
        this.varRefs = new ArrayList<>();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.UNTAINT;
    }

    @Override
    public String toString() {
        return "BLangUntaint: " + varRefs;
    }
}
