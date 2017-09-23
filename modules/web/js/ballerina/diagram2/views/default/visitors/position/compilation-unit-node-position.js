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
import { panel } from './../../designer-defaults';
import TreeUtil from './../../../../../model/tree-util';

/**
 * Position Calculater for CompilationUnit.
 *
 * @class CompilationUnitPositionVisitor
 * */
class CompilationUnitPositionVisitor {

    /**
     * begin visit.
     *
     * @param {Node} node.
     *
     * @memberOf CompilationUnitPositionVisitor
     * */
    beginVisit(node) {
        let width = 0;
        let height = 0;
        const viewState = node.viewState;

        const children = node.filterTopLevelNodes((child) => {
            return TreeUtil.isFunction(child) || TreeUtil.isService(child);
        });

        children.forEach((child) => {
            if (width <= child.viewState.bBox.w) {
                width = child.viewState.bBox.w;
            }
            child.viewState.bBox.x = panel.wrapper.gutter.v;
            child.viewState.bBox.y = height + panel.wrapper.gutter.h;
            height = panel.wrapper.gutter.h + child.viewState.bBox.h + height;
        });

        height = (height > node.viewState.container.height) ? height : node.viewState.container.height;
        width = (width > node.viewState.container.width) ? width : node.viewState.container.width;

        children.forEach((child) => {
            child.viewState.bBox.w = width - (panel.wrapper.gutter.h * 2);
        });

        node.viewState.bBox.h = height;
        node.viewState.bBox.w = width;
    }

    /**
     * visit the visitor at the end.
     *
     * @param {Node} node.
     *
     * @memberOf CompilationUnitPositionVisitor
     * */
    endVisit(node) {
        
    }
}

export default CompilationUnitPositionVisitor;
