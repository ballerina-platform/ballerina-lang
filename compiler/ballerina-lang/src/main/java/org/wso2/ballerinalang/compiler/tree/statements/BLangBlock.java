package org.wso2.ballerinalang.compiler.tree.statements;


import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;



public class BLangBlock extends BLangStatement implements BlockNode {

    public BLangBlockStmt body;

    public BLangBlock() {
    }

    public BLangBlock(BLangBlockStmt body) {

        this.body = body;
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void setBody(BlockStatementNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.BLOCK;
    }

    @Override
    public String toString() {
        return "{"
                + (body != null ? String.valueOf(body) : "") + "}";
    }
}
