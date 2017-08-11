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
import Statement from './statement';

/**
 * Class for Abort statement
 */
class AbortStatement extends Statement {
    /**
     * Constructor for abort statement
     */
    constructor() {
        super();
        this.type = 'AbortStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: '\n',
        };
    }

    /**
     * Define what type of nodes that this node can be added as a child.
     * @param {ASTNode} node - Parent node that this node becoming a child of.
     * @return {boolean} true|false.
     * */
    canBeAChildOf(node) {
        let canBeChildOf = this.getFactory().isTransactionStatement(node);
        let nodeToBeParent = node;
        while (!canBeChildOf && !_.isEmpty(nodeToBeParent.getParent())) {
            canBeChildOf = this.getFactory().isTransactionStatement(nodeToBeParent);
            nodeToBeParent = nodeToBeParent.getParent();
        }
        return canBeChildOf;
    }

    /**
     * initialize the node from the node json object.
     * @returns {void}
     */
    initFromJson() {
        // No Children Available to Iterate Through.
    }

    /**
     * Get the statement string.
     * @return {string} statement string
     * @override
     * */
    getStatementString() {
        return 'abort';
    }
}

export default AbortStatement;
