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
import { util } from './../sizing-utils';

/**
 * Dimension visitor class for Struct definition.
 *
 * @class StructDefinitionDimensionCalculatorVisitor
 * */
class StructDefinitionDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf StructDefinitionDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf StructDefinitionDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf StructDefinitionDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * calculate the dimension of children.
     *
     * @param {array} children - collection of children.
     * @param {object} components - components.
     * @param {object} bBox - bounding box.
     * @param {boolean} collapsed - true or false.
     *
     * @memberOf StructDefinitionDimensionCalculatorVisitor
     * */
    _calculateChildrenDimensions(children = [], components, bBox, collapsed) {
        children.forEach(() => {
            if (!collapsed) {
                bBox.h += DesignerDefaults.structDefinitionStatement.height;
            }
        });
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - struct definition node.
     *
     * @memberOf StructDefinitionDimensionCalculatorVisitor
     * */
    endVisit(node) {
        util.populateOuterPanelDecoratorBBox(node);
        const viewState = node.getViewState();

        const textWidth = util.getTextWidth(node.getStructName());
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;

        const { components } = viewState;
        if (!node.viewState.collapsed) {
            viewState.bBox.h += DesignerDefaults.panel.body.padding.top;
        }
        this._calculateChildrenDimensions(node.getChildren(), components, viewState.bBox, node.viewState.collapsed);
    }
}

export default StructDefinitionDimensionCalculatorVisitor;
