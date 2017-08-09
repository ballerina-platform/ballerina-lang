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
import _ from 'lodash';
import * as DesignerDefaults from './../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';
import ASTFactory from './../../ast/ballerina-ast-factory';

/**
 * Dimension visitor class for Fork Join.
 *
 * @class ForkJoinStatementDimensionCalculatorVisitor
 * */
class ForkJoinStatementDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf ForkJoinStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf ForkJoinStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf ForkJoinStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Fork Join statement node.
     *
     * @memberOf ForkJoinStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        const viewState = node.getViewState();
        let containerW = DesignerDefaults.statement.width;
        const workers = node.filterChildren(child => ASTFactory.isWorkerDeclaration(child));
        const join = node.filterChildren(child => ASTFactory.isJoinStatement(child));
        const timeout = node.filterChildren(child => ASTFactory.isTimeoutStatement(child));
        const childWithMaxHeight = _.maxBy(workers, child => child.getViewState().bBox.h);

        const bodyH = (childWithMaxHeight ? childWithMaxHeight.getViewState().bBox.h : 0) +
            (DesignerDefaults.statement.gutter.v * 2) + DesignerDefaults.lifeLine.gutter.v +
            DesignerDefaults.statement.height;

        const bodyInnerH = (childWithMaxHeight ? childWithMaxHeight.getViewState().components.statementContainer.h : 0);

        _.forEach(workers, (child) => {
            child.viewState.bBox.h = bodyInnerH;
            child.viewState.components.statementContainer.h = bodyInnerH;
            child.viewState.components.workerScopeContainer.h = bodyH - DesignerDefaults.lifeLine.gutter.v;
        });

        _.forEach(workers, (child) => {
            containerW += (child.getViewState().components.workerScopeContainer.w
            + child.getViewState().components.workerScopeContainer.expansionW);
            const connectorChildren = child.filterChildren(innerChild => ASTFactory.isConnectorDeclaration(innerChild));
            if (connectorChildren.length > 0) {
                _.forEach(connectorChildren, (innerChild) => {
                    innerChild.viewState.components.statementContainer.h = bodyInnerH;
                   // ToDo inlcude a proper padding amount
                    containerW += DesignerDefaults.statement.padding.right;
                });
            }
        });

        const dropZoneHeight = DesignerDefaults.statement.gutter.v;
        let bodyW;
        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['drop-zone'].h = dropZoneHeight;

        let hOfLowerParts;
        if (join.length > 0 && timeout.length > 0) {
            const joinBBox = join[0].viewState.bBox;
            const joinStatementsBBox = join[0].viewState.components.statementContainer;
            const timeoutBBox = timeout[0].viewState.bBox;
            const timeoutStatementsBBox = timeout[0].viewState.components.statementContainer;
            if (joinBBox.h > timeoutBBox.h) {
                hOfLowerParts = joinBBox.h;
                timeoutBBox.h = hOfLowerParts;
                timeoutStatementsBBox.h = hOfLowerParts;
            } else {
                hOfLowerParts = timeoutBBox.h;
                joinBBox.h = hOfLowerParts;
                joinStatementsBBox.h = hOfLowerParts;
            }
            const halfW = Math.max(timeoutBBox.w, joinBBox.w, containerW / 2);

            joinBBox.w = halfW;
            joinStatementsBBox.w = halfW;
            timeoutBBox.w = halfW;
            timeoutStatementsBBox.w = halfW;
            bodyW = halfW * 2;
        } else if (join.length > 0) {
            const joinBBox = join[0].viewState.bBox;
            const joinStatementsBBox = join[0].viewState.components.statementContainer;
            hOfLowerParts = joinBBox.h;

            const newW = Math.max(joinBBox.w, containerW);
            joinBBox.w = newW;
            joinStatementsBBox.w = newW;
            bodyW = newW;
        } else {
            const exception = {
                message: 'Missing join in a fork statement.',
            };
            throw exception;
        }

        viewState.components.body = new SimpleBBox(0, 0, bodyW, bodyH);
        viewState.components.workers = new SimpleBBox(0, 0, containerW, bodyH);
        viewState.bBox.h = bodyH + dropZoneHeight + DesignerDefaults.blockStatement.heading.height + hOfLowerParts;
        viewState.bBox.w = bodyW;
    }
}

export default ForkJoinStatementDimensionCalculatorVisitor;
