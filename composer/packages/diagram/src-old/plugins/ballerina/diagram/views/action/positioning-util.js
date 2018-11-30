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

import DefaultPositioningUtil from '../default/positioning-util';
import TreeUtil from './../../../model/tree-util';

class PositioningUtil extends DefaultPositioningUtil {

    /**
     * Calculate position of Break nodes.
     *
     * @param {object} node Break object
     */
    positionBreakNode(node) {
        // do nothing.
    }

    /**
     * Calculate position of Next nodes.
     *
     * @param {object} node
     *
     */
    positionNextNode(node) {
        // do nothing
    }

    /**
     * Calculate position of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    positionExpressionStatementNode(node) {
        if (TreeUtil.statementIsInvocation(node) ||
            TreeUtil.statementIsClientResponder(node)) {
            super.positionExpressionStatementNode(node);
        }
    }

    /**
     * Calculate position of Return nodes.
     *
     * @param {object} node
     *
     */
    positionReturnNode(node) {
        super.positionReturnNode(node);
    }

    /**
     * Calculate position of Throw nodes.
     *
     * @param {object} node
     *
     */
    positionThrowNode(node) {
        // do nothing
    }

    /**
     * Calculate position of VariableDef nodes.
     *
     * @param {object} node
     *
     */
    positionVariableDefNode(node) {
        if (TreeUtil.statementIsInvocation(node) ||
            TreeUtil.statementIsClientResponder(node)) {
            super.positionVariableDefNode(node);
        }
    }
}

export default PositioningUtil;
