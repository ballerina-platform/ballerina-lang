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

/**
 * Position visitor class for Transaction Aborted Statement.
 *
 * @class TransactionAbortedStatementPositionCalcVisitor
 * */
class TransactionAbortedStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf TransactionAbortedStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit TransactionAbortedStatementPositionCalcVisitor');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Transaction Aborted Statement node.
     *
     * @memberOf TransactionAbortedStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit TransactionAbortedStatementPositionCalcVisitor');
        const viewState = node.getViewState();
        const bBox = viewState.bBox;
        const parent = node.getParent();
        const parentViewState = parent.getViewState();
        const parentStatementContainer = parentViewState.components.statementContainer;
        const parentStatements = parent.filterChildren(child =>
        ASTFactory.isStatement(child) || ASTFactory.isExpression(child));
        const currentIndex = _.findIndex(parentStatements, node);
        let y;

        if (parentStatementContainer.w < bBox.w) {
            const exception = {
                message: 'Invalid statement container width found, statement width should be ' +
                'greater than or equal to statement/ statement width ',
            };
            throw exception;
        }
        const x = parentStatementContainer.x + ((parentStatementContainer.w - bBox.w) / 2);
        if (currentIndex === 0) {
            y = parentStatementContainer.y;
        } else if (currentIndex > 0) {
            y = parentStatements[currentIndex - 1].getViewState().bBox.getBottom();
        } else {
            const exception = {
                message: `Invalid Index found for ${node.getType()}`,
            };
            throw exception;
        }

        bBox.x = x;
        bBox.y = y;
    }

    /**
     * visit the visitor.
     *
     * @memberOf TransactionAbortedStatementPositionCalcVisitor
     * */
    visit() {
        log.debug('visit TransactionAbortedStatementPositionCalcVisitor');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf TransactionAbortedStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit TransactionAbortedStatementPositionCalcVisitor');
    }
}

export default TransactionAbortedStatementPositionCalcVisitor;
