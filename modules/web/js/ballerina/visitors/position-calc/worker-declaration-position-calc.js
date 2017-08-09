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
import WorkerDeclaration from './../../ast/worker-declaration';
import * as DesignerDefaults from './../../configs/designer-defaults';

/**
 * Position visitor class for Worker Declaration.
 *
 * @class WorkerDeclarationPositionCalcVisitor
 * */
class WorkerDeclarationPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf WorkerDeclarationPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit WorkerDeclarationPositionCalcVisitor');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Worker Declaration node.
     *
     * @memberOf WorkerDeclarationPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit WorkerDeclarationPositionCalcVisitor');
        const viewState = node.getViewState();
        const bBox = viewState.bBox;
        const parent = node.getParent();
        const isInFork = ASTFactory.isForkJoinStatement(parent);
        const parentViewState = parent.getViewState();
        const workers = _.filter(parent.getChildren(), child => child instanceof WorkerDeclaration);
        const workerIndex = _.findIndex(workers, node);
        let x;
        let connectors;
        let totalConnectorWidth = 0;

        if (workerIndex === 0) {
            /**
             * Always the first worker should place after the default worker lifeline. If in a case like fork-join
             * we keep the first worker right next to the parent statement boundary.
             */

            if (parentViewState.components.statementContainer) {
                if (ASTFactory.isFunctionDefinition(parent) || ASTFactory.isResourceDefinition(parent) ||
                ASTFactory.isConnectorAction(parent)) {
                    connectors = _.filter(parent.getChildren(), child => ASTFactory.isConnectorDeclaration(child));
                        /**
                         * Due to the model order of the ast, when worker declaration visits, position visitor for
                         * the parent's statements have not been calculated. so we need to use the width's of the parent
                         * statement container to get the x position
                         */
                    if (connectors.length > 0) {
                        _.forEach(connectors, (connector) => {
                            totalConnectorWidth += (connector.getViewState().components.statementContainer.w +
                                connector.getViewState().components.statementContainer.expansionW);
                        });
                        totalConnectorWidth += ((connectors.length + 1) * DesignerDefaults.connectorDeclaration.padding);
                    }
                    x = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h +
                            parentViewState.components.statementContainer.w + DesignerDefaults.lifeLine.gutter.h +
                            parentViewState.components.statementContainer.expansionW + totalConnectorWidth;
                }
            } else if (isInFork) {
                x = parentViewState.components.body.x + DesignerDefaults.lifeLine.gutter.h +
                    ((parentViewState.bBox.w - parentViewState.components.workers.w) / 2);
            } else {
                x = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h;
            }
        } else if (workerIndex > 0) {
            const previousWorker = workers[workerIndex - 1];
            const previousStatementContainer = previousWorker.getViewState().components.statementContainer;

            connectors = _.filter(previousWorker.getChildren(), child => ASTFactory.isConnectorDeclaration(child));
            if (connectors.length > 0) {
                _.forEach(connectors, (connector) => {
                    totalConnectorWidth += (connector.getViewState().components.statementContainer.w +
                    connector.getViewState().components.statementContainer.expansionW);
                });
                totalConnectorWidth += ((connectors.length + 1) * DesignerDefaults.connectorDeclaration.padding);
            }

            x = previousStatementContainer.getRight() + previousWorker.getViewState().components.statementContainer.expansionW +
                    totalConnectorWidth + (isInFork ? DesignerDefaults.fork.lifeLineGutterH :
                    DesignerDefaults.lifeLine.gutter.h);
        } else {
            const exception = {
                message: 'Invalid index found for Worker Declaration',
            };
            throw exception;
        }
        const y = parentViewState.components.body.getTop() + DesignerDefaults.innerPanel.body.padding.top;

        viewState.components.workerScopeContainer.x = x;
        viewState.components.workerScopeContainer.y = y - (DesignerDefaults.canvas.padding.top / 2);
        bBox.x = x + DesignerDefaults.lifeLine.gutter.h;
        bBox.y = y + DesignerDefaults.lifeLine.gutter.v;
        viewState.components.statementContainer.x = x;
        viewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;
    }

    /**
     * visit the visitor.
     *
     * @memberOf WorkerDeclarationPositionCalcVisitor
     * */
    visit() {
        log.debug('visit WorkerDeclarationPositionCalcVisitor');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf WorkerDeclarationPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit WorkerDeclarationPositionCalcVisitor');
    }
}

export default WorkerDeclarationPositionCalcVisitor;
