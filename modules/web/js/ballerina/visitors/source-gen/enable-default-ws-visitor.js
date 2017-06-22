/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import ASTNode from './../../ast/node';

/**
 * Constructor for the Enable Format Visitor
 * @param parent
 * @constructor
 */
class EnableDefaultWSVisitor {

    canVisit(node) {
        return node instanceof ASTNode;
    }

    beginVisit() {
        return true;
    }

    visit(node) {
        node.whiteSpace.useDefault = true;
        if (node.getFactory().isBinaryExpression(node)) {
            node.getRightExpression().accept(this);
            node.getLeftExpression().accept(this);
        } else if (node.getFactory().isActionInvocationExpression(node)) {
            node.getArguments().forEach((arg) => {
                arg.accept(this);
            });
        } else if (node.getFactory().isConnectorInitExpression(node)) {
            node.getArgs().forEach((arg) => {
                arg.accept(this);
            });
            node.getConnectorName().accept(this);
        } else if (node.getFactory().isCatchStatement(node)) {
            node.getParameter().accept(this);
        } else if (node.getFactory().isWorkerInvocationStatement(node)) {
            node.getExpressionList().forEach((expr) => {
                expr.accept(this);
            });
        } else if (node.getFactory().isWorkerReplyStatement(node)) {
            node.getExpressionList().forEach((expr) => {
                expr.accept(this);
            });
        }
    }

    endVisit() {
        return true;
    }
}

export default EnableDefaultWSVisitor;
