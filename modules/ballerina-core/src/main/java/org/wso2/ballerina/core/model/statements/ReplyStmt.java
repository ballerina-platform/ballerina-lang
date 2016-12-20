/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.model.statements;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.DefaultBalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * {@code ReplyStmt} represents a reply statement
 *
 * @since 1.0.0
 */
public class ReplyStmt implements Statement {

    private Expression replyExpr;

    public ReplyStmt(Expression replyExpr) {
        this.replyExpr = replyExpr;
    }

    public Expression getReplyExpr() {
        return replyExpr;
    }

    public void interpret(Context ctx) {
        BValueRef bValueRef = ctx.getControlStack().getReturnValue(0);
        if (bValueRef == null) {
            bValueRef = ctx.getControlStack().getValue(0);
        }
        if (bValueRef != null && bValueRef.getBValue() instanceof MessageValue) {
            CarbonMessage messageValue = (CarbonMessage) bValueRef.getBValue().getValue();
            BalCallback callback = ctx.getBalCallback();
            while (!(callback instanceof DefaultBalCallback)) {
                callback = (BalCallback) callback.getParentCallback();
            }
            callback.done(messageValue);
        }
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setNextSibling(Statement statement) {

    }

    @Override
    public Statement getNextSibling() {
        return null;
    }

    @Override
    public boolean isHaltExecution() {
        return false;
    }

    @Override
    public void setHaltExecution(boolean value) {

    }

    @Override
    public void resumeExecution(NodeVisitor nodeVisitor) {

    }

    /**
     * Builds a {@code ReturnStmt} statement
     *
     * @since 1.0.0
     */
    public static class ReplyStmtBuilder {
        Expression replyExpr;

        public ReplyStmtBuilder() {
        }

        public void setExpression(Expression expr) {
            replyExpr = expr;
        }

        public ReplyStmt build() {
            return new ReplyStmt(replyExpr);
        }
    }
}
