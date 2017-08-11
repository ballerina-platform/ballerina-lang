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
import * as DesignerDefaults from './../../configs/designer-defaults';
import * as PositioningUtils from './utils';
import ASTFactory from './../../ast/ballerina-ast-factory';
/**
 * Position visitor class for Function Definition.
 *
 * @class FunctionDefinitionPositionCalcVisitor
 * */
class FunctionDefinitionPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf FunctionDefinitionPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit FunctionDefinitionPositionCalc');
        return true;
    }

    /**
     *
     * @param {ASTNode} node
     * @private
     */
    static findParent(node) {
        const p = node.getParent();
        if (!p) {
            return null;
        }
        if (ASTFactory.isAssignmentStatement(p) || ASTFactory.isVariableDefinitionStatement(p)) {
            return p;
        }
        return this.findParent(p);
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Function Definition node.
     *
     * @memberOf FunctionDefinitionPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit FunctionDefinitionPositionCalc');

        // populate panel BBox positions.
        PositioningUtils.populateOuterPanelDecoratorBBoxPosition(node);
        const viewState = node.getViewState();
        const statementContainer = viewState.components.statementContainer;
        const workerScopeContainer = viewState.components.workerScopeContainer;
        // If more than one worker is present, then draw the worker scope container boundary around the workers
        if ((node.filterChildren(node.getFactory().isWorkerDeclaration)).length >= 1) {
            workerScopeContainer.x = viewState.components.body.x + DesignerDefaults.innerPanel.body.padding.left;
            workerScopeContainer.y = viewState.components.body.y + (DesignerDefaults.innerPanel.body.padding.top / 2);
        }
        if (node.isLambda()) {
            const parent = this.constructor.findParent(node);
            const lambdaChildren = parent.getLambdaChildren();
            const i = lambdaChildren.indexOf(node);
            const parentViewState = parent.getViewState();

            if (i > 0) {
                viewState.bBox.y = lambdaChildren[i - 1].getViewState().bBox.getBottom();
            } else {
                viewState.bBox.y = parentViewState.bBox.getTop() + DesignerDefaults.statement.height
                    + DesignerDefaults.statement.gutter.v;
            }

            viewState.bBox.x = parentViewState.bBox.x;
            viewState.components.body.y = viewState.bBox.y;
            viewState.components.body.x = viewState.bBox.x;
            if (node.filterChildren(node.getFactory().isConnectorDeclaration).length > 0) {
                statementContainer.x = viewState.bBox.x + DesignerDefaults.innerPanel.body.padding.left;
            } else {
                statementContainer.x = viewState.bBox.x + (viewState.bBox.w - statementContainer.w) / 2;
            }
        } else {
            statementContainer.x = viewState.components.body.x + DesignerDefaults.innerPanel.body.padding.left;
        }
        statementContainer.y = viewState.components.body.y + DesignerDefaults.innerPanel.body.padding.top
            + DesignerDefaults.lifeLine.head.height;
        // populate panel heading positioning.
        PositioningUtils.populatePanelHeadingPositioning(node, this.createPositionForTitleNode);
    }

    /**
     * visit the visitor.
     *
     * @memberOf FunctionDefinitionPositionCalcVisitor
     * */
    visit() {
        log.debug('visit FunctionDefinitionPositionCalc');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf FunctionDefinitionPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit FunctionDefinitionPositionCalc');
    }

    /**
     * Sets positioning for a parameter.
     *
     * @param {object} parameter - The resource parameter node.
     * @param {number} x - The x position
     * @param {number} y - The y position
     * @return {number} The x position of the next parameter node.
     *
     * @memberOf FunctionDefinitionPositionCalc
     */
    createPositionForTitleNode(parameter, x, y) {
        const viewState = parameter.getViewState();
        // Positioning the parameter
        viewState.bBox.x = x;
        viewState.bBox.y = y;

        // Positioning the delete icon
        viewState.components.deleteIcon.x = x + viewState.w;
        viewState.components.deleteIcon.y = y;

        return viewState.components.deleteIcon.x + viewState.components.deleteIcon.w;
    }
}

export default FunctionDefinitionPositionCalcVisitor;
