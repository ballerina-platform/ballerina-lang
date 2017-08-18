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
import { canvas, panel } from './../../configs/designer-defaults';

/**
 * Dimension visitor class for ballerina ast root.
 *
 * @class BallerinaASTRootDimensionCalculatorVisitor
 * */
class BallerinaASTRootDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf BallerinaASTRootDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visit the visitor.
     *
     * @memberOf BallerinaASTRootDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf BallerinaASTRootDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Ballerina AST Root node.
     *
     * @memberOf BallerinaASTRootDimensionCalculatorVisitor
     * */
    endVisit(node) {
        // get the visit state to set ast root dimentions.
        const viewState = node.getViewState();
        // set the state to 0 since we re-calculate
        viewState.bBox.h = 0;
        viewState.bBox.w = 0;

        // get the children of ast root
        const children = node.getChildren();

        // now lets iterate and calculate height and width.
        // height = sum of all the children height ( this is only for now it might change if we
        // have same level items in the future )
        // width = maximum with of the containing children.
        // @todo filter out children
        children.forEach((element, index) => {
            const childViewState = element.getViewState();
            viewState.bBox.h += childViewState.bBox.h;

            // add an extra gutter if there are more than one child.
            if (index !== 0) {
                viewState.bBox.h += panel.wrapper.gutter.h;
            }

            // if we find a child with a wider width we will assign that as the canvas width.
            if (viewState.bBox.w < childViewState.bBox.w) {
                viewState.bBox.w = childViewState.bBox.w;
            }
        }, this);

        // add canvas padding to the bBox
        viewState.bBox.h = viewState.bBox.h + canvas.padding.top + canvas.padding.bottom;
        viewState.bBox.w = viewState.bBox.w + canvas.padding.right + canvas.padding.left;
    }
}

export default BallerinaASTRootDimensionCalculatorVisitor;
