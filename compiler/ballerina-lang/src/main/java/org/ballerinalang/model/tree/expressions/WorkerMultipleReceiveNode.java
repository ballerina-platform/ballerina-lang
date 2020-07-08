package org.ballerinalang.model.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerMultipleReceive;

import java.util.List;
/**
 * Implementation of {@link BLangWorkerMultipleReceive}.
 *
 */
public interface WorkerMultipleReceiveNode extends ExpressionNode {
    BLangRecordLiteral getReceiveFieldsMapLiteral();

    List<WorkerReceiveFieldNode> getReceiveFields();

    /**
     * This static inner class represents worker name/worker field name of receive field collection.
     *
     */
    interface WorkerReceiveFieldNode {

        void setWorkerName(IdentifierNode identifierNode);

        void setWorkerFieldName(IdentifierNode identifierNode);

        ExpressionNode getKeyExpression();

        IdentifierNode getWorkerName();

        IdentifierNode getWorkerFieldName();

        void setSendExpression(BLangExpression sendExpression);
        public BLangExpression getSendExpression();

        void setHasReceived(boolean value);

        boolean getHasReceived();

    }


}
