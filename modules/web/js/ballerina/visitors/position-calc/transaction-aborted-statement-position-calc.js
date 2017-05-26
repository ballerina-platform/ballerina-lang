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
import log from 'log';
import _ from 'lodash';
import ASTFactory from './../../ast/ballerina-ast-factory';

class TransactionAbortedStatementPositionCalcVisitor {
    canVisit(node) {
        log.debug('can visit TransactionAbortedStatementPositionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('begin visit TransactionAbortedStatementPositionCalcVisitor');
        let viewState = node.getViewState();
        let bBox = viewState.bBox;
        let parent = node.getParent();
        let parentViewState = parent.getViewState();
        let parentStatementContainer = parentViewState.components.statementContainer;
        let parentStatements = parent.filterChildren(function (child) {
            return ASTFactory.isStatement(child) || ASTFactory.isExpression(child);
        });
        let currentIndex = _.findIndex(parentStatements, node);
        let x, y;

        if (parentStatementContainer.w < bBox.w) {
            throw 'Invalid statement container width found, statement width should be greater than or equal to ' +
            'statement/ statement width ';
        }
        x = parentStatementContainer.x + (parentStatementContainer.w - bBox.w) / 2;
        if (currentIndex === 0) {
            y = parentStatementContainer.y;
        } else if (currentIndex > 0) {
            y = parentStatements[currentIndex - 1].getViewState().bBox.getBottom();
        } else {
            throw 'Invalid Index found for ' + node.getType();
        }

        bBox.x = x;
        bBox.y = y;
    }

    visit(node) {
        log.debug('visit TransactionAbortedStatementPositionCalcVisitor');
    }

    endVisit(node) {
        log.debug('end visit TransactionAbortedStatementPositionCalcVisitor');
    }
}

export default TransactionAbortedStatementPositionCalcVisitor;