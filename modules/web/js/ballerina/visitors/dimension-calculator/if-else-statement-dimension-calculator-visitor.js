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

/**
 * Dimension visitor class for if else statement.
 *
 * @class IfElseStatementDimensionCalculatorVisitor
 * */
class IfElseStatementDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf IfElseStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf IfElseStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf IfElseStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - IfElse statement node.
     *
     * @memberOf IfElseStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        const viewState = node.getViewState();
        let statementWidth = 0;
        let statementHeight = 0;
        const sortedChildren = _.sortBy(node.getChildren(), child => child.getViewState().bBox.w);
        const sortedChildrenfromConnectors = _.sortBy(node.getChildren(),
            child => child.getViewState().bBox.expansionW);
        if (sortedChildren.length <= 0) {
            const exception = {
                message: 'Invalid number of children for if-else statement',
            };
            throw exception;
        }
        const childWithMaxWidth = sortedChildren[sortedChildren.length - 1];
        statementWidth = childWithMaxWidth.getViewState().bBox.w;
        const childWithMaxConnectorWidth = sortedChildrenfromConnectors[sortedChildrenfromConnectors.length - 1];
        const maxConnectorWidth = childWithMaxConnectorWidth.getViewState().bBox.expansionW;

        let previous;
        _.forEach(node.getChildren(), (child) => {
            /**
             * Re adjust the width of all the other children
             */
            if (child.id !== childWithMaxWidth.id) {
                child.getViewState().components.statementContainer.w =
                    childWithMaxWidth.getViewState().components.statementContainer.w;
                child.getViewState().bBox.w = childWithMaxWidth.getViewState().bBox.w;
            }
            if (child.getViewState().bBox.expansionW < maxConnectorWidth) {
                child.getViewState().bBox.expansionW = maxConnectorWidth;
            }
            statementHeight += child.getViewState().bBox.h + child.viewState.offSet;

            // Here instead of adding offSet to drop zone we will assign it to previous block statement -
            // since else and if-else do not have a drop zone above.
            if (previous) {
                previous.viewState.bBox.h += child.viewState.offSet;
            }
            previous = child;
        });

        const dropZoneHeight = DesignerDefaults.statement.gutter.v + node.viewState.offSet;
        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['drop-zone'].h = dropZoneHeight;
        viewState.components['block-header'] = new SimpleBBox();
        viewState.components['block-header'].h = DesignerDefaults.blockStatement.heading.height;
        viewState.components['block-header'].setOpaque(true);

        viewState.bBox.h = statementHeight + dropZoneHeight;
        viewState.bBox.w = statementWidth;
        viewState.bBox.expansionW = maxConnectorWidth;
    }
}

export default IfElseStatementDimensionCalculatorVisitor;
