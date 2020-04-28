package org.ballerinalang.model.tree.statements;

public interface BlockNode extends StatementNode {

    BlockStatementNode getBody();

    void setBody(BlockStatementNode body);
}
