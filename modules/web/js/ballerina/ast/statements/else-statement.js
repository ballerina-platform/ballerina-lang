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
import Statement from './statement';
import ASTFactory from '../ast-factory.js';

/**
 * Class for else node in ballerina.
 */
class ElseStatement extends Statement {
    /**
     * Constructor for Else statement
     */
    constructor() {
        super();
        this.type = 'ElseStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
            3: '\n',
        };
    }
    /**
     * initialize AssignmentStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        _.each(jsonNode.children, (childNode) => {
            let child;
            // FIXME Keeping existing fragile  logic to detect connector declaration as
            // it is for now. We should refactor this
            if (childNode.type === 'variable_definition_statement' &&
                !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = ASTFactory.createConnectorDeclaration();
            } else {
                child = ASTFactory.createFromJson(childNode);
            }
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return ASTFactory.isConnectorDeclaration(node)
            || ASTFactory.isVariableDeclaration(node)
            || ASTFactory.isWorkerDeclaration(node)
            || ASTFactory.isStatement(node);
    }
}

export default ElseStatement;
