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
import { panel } from './../../configs/designer-defaults';

/**
 * Position visitor class for Ballerina AST Root.
 *
 * @class BallerinaASTRootPositionCalcVisitor
 * */
class BallerinaASTRootPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf BallerinaASTRootPositionCalcVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Ballerina AST Root node.
     *
     * @memberOf BallerinaASTRootPositionCalcVisitor
     * */
    beginVisit(node) {
        // here we need to re adjust panel width to match the screen.
        const children = node.getChildren();
        const defaultContainerWidth = node.getViewState().container.width - (panel.wrapper.gutter.h * 2);
        const highestWidthOfChildren = node.getViewState().bBox.w - (panel.wrapper.gutter.h * 2);
        const minWidth = highestWidthOfChildren > defaultContainerWidth ?
            highestWidthOfChildren : defaultContainerWidth;

        children.forEach((element) => {
            const viewState = element.getViewState();
            if (viewState.bBox.w < minWidth) {
                viewState.bBox.w = minWidth;
            }
        }, this);

        // lets adjust the canvas width and height
        if (node.getViewState().container.width > node.viewState.bBox.w) {
            node.viewState.bBox.w = node.getViewState().container.width;
        }
        if (node.getViewState().container.height > node.viewState.bBox.h) {
            node.viewState.bBox.h = node.getViewState().container.height;
        }
    }

    /**
     * visit the visitor.
     *
     * @memberOf BallerinaASTRootPositionCalcVisitor
     * */
    visit() {
        log.debug('Visit BallerinaASTRoot');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf BallerinaASTRootPositionCalcVisitor
     * */
    endVisit() {
        log.debug('End Visit BallerinaASTRoot');
    }
}

export default BallerinaASTRootPositionCalcVisitor;

