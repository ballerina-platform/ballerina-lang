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
import log from 'log';
import Statement from './statement';
import FragmentUtils from '../../utils/fragment-utils';
import EnableDefaultWSVisitor from '../../visitors/source-gen/enable-default-ws-visitor';

/**
 * Class for retry statement in ballerina.
 * */
class RetryStatement extends Statement {
    /**
     * Constructor for retry statement.
     * @param {object} args - arguments for creating retry statement.
     * @constructor
     * */
    constructor(args) {
        super(args);
        this.type = 'RetryStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
        };
    }

    /**
     * Set the statement from the statement string.
     * @param {string} stmtString - statement string.
     * @param {function} callback - callback function.
     * */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createTransactionFailedFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            this.viewState.source = null;
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'retry_statement')) {
                this.initFromJson(parsedJson);
            } else if (_.has(parsedJson, 'type')) {
                const newNode = this.getFactory().createFromJson(parsedJson);
                if (this.getFactory().isStatement(newNode)) {
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
                callback({
                    isValid: true,
                });
            }
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modified Retry Statement',
                context: nodeToFireEvent,
            });
        } else {
            log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            if (_.isFunction(callback)) {
                callback({
                    isValid: false,
                    response: parsedJson,
                });
            }
        }
    }

    /**
     * Check whether can be a child of the given node.
     * @param {ASTNode} node ast node to be check against.
     * @return {boolean} can be a child of status.
     * */
    canBeAChildOf(node) {
        return this.getFactory().isFailedStatement(node);
    }

    /**
     * Get the statement string.
     * @return {string} retry statement string.
     * */
    getStatementString() {
        if (this.viewState.source) {
            return this.viewState.source.replace(/;\s*$/, '');
        }
        let expressionString = 'retry';
        if (this.getChildren().length !== 0) {
            const child = this.getChildren()[0];
            expressionString += (child.whiteSpace.useDefault ? ' ' : child.getWSRegion(0));
            expressionString += child.getExpressionString();
        } else {
            expressionString += `${this.getWSRegion(1)}3`;
        }
        return expressionString;
    }

    /**
     * Initialize from the json
     * @param {object} jsonNode - json node for the retry statement.
     * */
    initFromJson(jsonNode) {
        this.children.length = 0;
        jsonNode.children.forEach((childJsonNode) => {
            const child = this.getFactory().createFromJson(childJsonNode);
            child.initFromJson(childJsonNode);
            this.addChild(child, undefined, true, true);
        });
    }
}

export default RetryStatement;
