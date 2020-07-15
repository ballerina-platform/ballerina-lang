/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.WorkerMultipleReceiveNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link BLangWorkerMultipleReceive}.
 *
 */
public class BLangWorkerMultipleReceive extends BLangExpression implements WorkerMultipleReceiveNode {

    public BType matchingSendsError;

    public BLangRecordLiteral receiveFieldsMapLiteral;

    public int currentReceiveFieldIndex;

    public List<WorkerReceiveFieldNode> receiveFields = new ArrayList<>();

    @Override
    public BLangRecordLiteral getReceiveFieldsMapLiteral() {

        return receiveFieldsMapLiteral;
    }

    @Override
    public List<WorkerReceiveFieldNode> getReceiveFields() {

        return receiveFields;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {

        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {

        return NodeKind.WORKER_MULTIPLE_RECEIVE;
    }

    private List<String> getReceivedWorkers() {

        List<String> receivedWorkers = new ArrayList<>();
        for (WorkerReceiveFieldNode receiveField : receiveFields) {
            if (receiveField.getWorkerFieldName() != null) {
                receivedWorkers.add(
                        receiveField.getWorkerFieldName().getValue() + ":"
                                + receiveField.getWorkerName().getValue()
                                   );
            } else {
                receivedWorkers.add(receiveField.getWorkerName().getValue());
            }
        }

        return receivedWorkers;
    }

    public boolean canMoveToNext() {

        for (WorkerReceiveFieldNode receiveField : this.receiveFields) {
            if (!receiveField.getHasReceived()) {
                return false;
            }
        }
        return true;
    }

    public String toActionString() {

        return " <- { " + String.join(", ", getReceivedWorkers()) + " }";
    }

    @Override
    public String toString() {

        return "BLangWorkerMultipleReceive: " + this.toActionString();
    }

    /**
     * This static inner class represents the worker name/worker field name of a receive field collection.
     *
     */
    public static class BLangWorkerReceiveField implements WorkerReceiveFieldNode {

        public BLangIdentifier workerIdentifier;
        public BLangIdentifier workerFieldName;
        public BLangExpression keyExpr;
        public boolean isChannel = false;
        public SymbolEnv env;
        public BType workerType;
        public BType matchingSendsError; // TODO: Remove this line

        public BLangExpression getSendExpression() {

            return sendExpression;
        }

        public BLangExpression sendExpression;
        public boolean hasReceived = false;

        @Override
        public void setSendExpression(BLangExpression sendExpression) {

            this.sendExpression = sendExpression;
        }

        public BLangExpression getKeyExpr() {

            return keyExpr;
        }

        @Override
        public void setWorkerName(IdentifierNode identifierNode) {

            this.workerIdentifier = (BLangIdentifier) identifierNode;
        }

        @Override
        public void setWorkerFieldName(IdentifierNode identifierNode) {

            this.workerFieldName = (BLangIdentifier) identifierNode;
        }

        @Override
        public ExpressionNode getKeyExpression() {

            return keyExpr;
        }

        @Override
        public IdentifierNode getWorkerName() {

            return workerIdentifier;
        }

        @Override
        public IdentifierNode getWorkerFieldName() {

            return workerFieldName;
        }

        @Override
        public void setHasReceived(boolean value) {

            this.hasReceived = value;
        }

        @Override
        public boolean getHasReceived() {

            return this.hasReceived;
        }
    }
}
