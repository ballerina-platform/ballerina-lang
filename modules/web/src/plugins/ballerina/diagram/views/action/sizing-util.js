/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import DefaultSizingUtil from '../default/sizing-util';
import TreeUtil from './../../../model/tree-util';

class SizingUtil extends DefaultSizingUtil {

    hideStatement(node) {
        if (!TreeUtil.statementIsInvocation(node)) {
            const viewState = node.viewState;
            viewState.hidden = true;
            viewState.bBox.h = 0;
            viewState.components = {};
        }
    }

    /**
     * Calculate dimention
     *  of Break nodes.
     *
     * @param {object} node
     */
    sizeBreakNode(node) {
        super.sizeBreakNode(node);
        this.hideStatement(node);
    }

    /**
     * Calculate dimention of Next nodes.
     *
     * @param {object} node
     *
     */
    sizeNextNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.hideStatement(node);
    }

    /**
     * Calculate dimention of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    sizeExpressionStatementNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.hideStatement(node);
    }

    /**
     * Calculate dimention of Return nodes.
     *
     * @param {object} node
     *
     */
    sizeReturnNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.hideStatement(node);
    }

    /**
     * Calculate dimention of Throw nodes.
     *
     * @param {object} node
     *
     */
    sizeThrowNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.hideStatement(node);
    }

    /**
     * Calculate dimention of VariableDef nodes.
     *
     * @param {object} node
     *
     */
    sizeVariableDefNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.hideStatement(node);
    }

}

export default SizingUtil;
