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
import * as DesignerDefaults from './../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

class WorkerDeclarationDimensionCalculatorVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
    }

    visit(node) {
    }

    endVisit(node) {
        const viewState = node.getViewState();
        const components = {};

        components.statementContainer = new SimpleBBox();
        const statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        const statementContainerWidthPadding = DesignerDefaults.statementContainer.padding.left +
            DesignerDefaults.statementContainer.padding.right;
        let statementWidth = DesignerDefaults.lifeLine.width + statementContainerWidthPadding;
        let statementHeight = 0;

        _.forEach(statementChildren, (child) => {
            statementHeight += child.viewState.bBox.h + DesignerDefaults.statement.gutter.v;
            if ((child.viewState.bBox.w + statementContainerWidthPadding) > statementWidth) {
                statementWidth = child.viewState.bBox.w + statementContainerWidthPadding;
            }
        });

        /**
         * We add an extra gap to the statement container height, in order to maintain the gap between the
         * last statement's bottom margin and the default worker bottom rect's top margin
         */
        statementHeight += DesignerDefaults.statement.gutter.v * 2;

        viewState.bBox.h = statementHeight + DesignerDefaults.lifeLine.head.height * 2;
        viewState.bBox.w = statementWidth;

        components.statementContainer.h = statementHeight;
        components.statementContainer.w = statementWidth;

        viewState.components = components;
    }
}

export default WorkerDeclarationDimensionCalculatorVisitor;
