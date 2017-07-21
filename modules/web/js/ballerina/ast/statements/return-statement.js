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
import log from 'log';
import Statement from './statement';
import FragmentUtils from './../../utils/fragment-utils';
import EnableDefaultWSVisitor from './../../visitors/source-gen/enable-default-ws-visitor';

/**
 * Class for return statement in ballerina.
 */
class ReturnStatement extends Statement {
    /**
     * Constructor for return statement
     * @param {object} args zero or many expressions for a return statement.
     * @constructor
     */
    constructor() {
        super();
        this.type = 'ReturnStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '',
            3: '\n',
        };
    }

   /**
     * Set the statement from the statement string
     * @param {string} stmtString statement string
     * @param {function} callback function
     * @returns {void}
     */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'return_statement')) {
                this.initFromJson(parsedJson);
            } else if (_.has(parsedJson, 'type')) {
                // user may want to change the statement type
                const newNode = this.getFactory().createFromJson(parsedJson);
                if (this.getFactory().isStatement(newNode)) {
                    // somebody changed the type of statement
                    const parent = this.getParent();
                    const index = parent.getIndexOfChild(this);
                    parent.removeChild(this, true);
                    parent.addChild(newNode, index, true, true);
                    newNode.initFromJson(parsedJson);
                    nodeToFireEvent = newNode;
                }
            } else {
                log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            }

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modify Assignment Statement',
                context: nodeToFireEvent,
            });
        } else {
            log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }

    /**
     * Check whether, Can be a child of the given node
     * @param {ASTNode} node ast node to be check against
     * @return {boolean} can be a child of status
     */
    canBeAChildOf(node) {
        return this.getFactory().isFunctionDefinition(node) ||
               this.getFactory().isConnectorAction(node) ||
            this.getFactory().isStatement(node);
    }

    /**
     * Get the return statement string
     * @return {string} return statement string
     */
    getStatementString() {
        let expressionString = '';
        this.children.forEach((child, index) => {
            if (index !== 0) {
                expressionString += ',';
                expressionString += (child.whiteSpace.useDefault ? ' ' : child.getWSRegion(0));
            }
            expressionString += child.getExpressionString();
        });
        return 'return' + this.getWSRegion(1) + expressionString;
    }

    /**
     * initialize from json
     * @param {object} jsonNode json node which is from the statement initialized
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this.children.length = 0;
        jsonNode.children.forEach((childJsonNode) => {
            const child = this.getFactory().createFromJson(childJsonNode);
            child.initFromJson(childJsonNode);
            this.addChild(child, undefined, true, true);
        });
    }
}

export default ReturnStatement;
