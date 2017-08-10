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
import * as PositioningUtils from './utils';
import ASTFactory from './../../ast/ballerina-ast-factory';
import * as DesignerDefaults from './../../configs/designer-defaults';
import PositionCalculatorVisitor from '../position-calculator-visitor';

/**
 * Position visitor class for Assignment Statement.
 *
 * @class AssignmentStatementPositionCalcVisitor
 * */
class AssignmentStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf AssignmentStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit AssignmentStatementPositionCalc');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Assignment Statement node.
     *
     * @memberOf AssignmentStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('visit AssignmentStatementPositionCalc');
        PositioningUtils.getSimpleStatementPosition(node);
        if (ASTFactory.isConnectorInitExpression(node.getChildren()[1])) {
            this.calculateConnectorDeclarationPosition(node);
        }
    }

    /**
     * @param {AssignmentStatement} node
     * visit the visitor.
     *
     * @memberOf AssignmentStatementPositionCalcVisitor
     * */
    visit(node) {
        log.debug('visit AssignmentStatementPositionCalc');
        const child = node.getRightExpression();
        if (ASTFactory.isActionInvocationExpression(child) &&
            ASTFactory.isLambdaExpression(child.getConnectorExpression())) {
            child.getConnectorExpression().getLambdaFunction().accept(new PositionCalculatorVisitor());
        }
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf AssignmentStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit AssignmentStatementPositionCalc');
    }

    /**
     * Calculate the connector declaration position
     * @param {AssignmentStatement} node - assignment statement node
     */
    calculateConnectorDeclarationPosition(node) {
        const connectorDeclViewState = node.getViewState().connectorDeclViewState;
        const bBox = connectorDeclViewState.bBox;
        const parent = node.getParent();
        const parentViewState = parent.getViewState();
        const workers = _.filter(parent.getChildren(), child => ASTFactory.isWorkerDeclaration(child));
        const connectors = _.filter(parent.getChildren(), child => ASTFactory.isConnectorDeclaration(child)
        || (ASTFactory.isAssignmentStatement(child) && ASTFactory.isConnectorInitExpression(child.getChildren()[1])));
        const connectorIndex = _.findIndex(connectors, node);
        let x = -1;

        if (connectorIndex === 0) {
            if (workers.length > 0) {
                /**
                 * Due to the model order in ast, at the moment, workers and the parent's statement positioning have not
                 * calculated. Therefore we need to consider the widths of them to get the connector x position
                 */
                let totalWorkerStmtContainerWidth = 0;
                _.forEach(workers, (worker) => {
                    totalWorkerStmtContainerWidth += worker.getViewState().components.statementContainer.w;
                });
                x = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h +
                    parentViewState.components.statementContainer.w + totalWorkerStmtContainerWidth +
                    (DesignerDefaults.lifeLine.gutter.h * (workers.length + 1));
            } else {
                x = parentViewState.components.statementContainer.getRight() + DesignerDefaults.lifeLine.gutter.h;
            }
        } else if (connectorIndex > 0) {
            const previousConnector = connectors[connectorIndex - 1];
            const previousConViewState = ASTFactory.isAssignmentStatement(previousConnector)
                ? previousConnector.getViewState().connectorDeclViewState : previousConnector.getViewState();
            const previousStatementContainer = previousConViewState.components.statementContainer;
            x = previousStatementContainer.getRight() + DesignerDefaults.innerPanel.body.padding.left;
        }

        let y = parentViewState.components.body.getTop() + DesignerDefaults.innerPanel.body.padding.top;

        if (parentViewState.components.variablesPane) {
            y += (parentViewState.components.variablesPane.h + DesignerDefaults.panel.body.padding.top);
        }

        bBox.x = x;
        bBox.y = y;

        connectorDeclViewState.components.statementContainer.x = x;
        connectorDeclViewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;
    }
}

export default AssignmentStatementPositionCalcVisitor;
