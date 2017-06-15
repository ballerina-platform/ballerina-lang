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

/**
 * Class for reply statement in ballerina.
 */
class ReplyStatement extends Statement {
    /**
     * Constructor for reply statement
     * @param {object} args arguments for reply statement
     * @constructor
     */
    constructor(args) {
        super();
        this._message = _.get(args, 'message') || '';
        this.type = 'ReplyStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
        };
    }

    /**
     * Set reply message
     * @param {expression} message replying expression
     * @param {object} options set attribute options
     * @returns {void}
     */
    setReplyMessage(message, options) {
        if (!_.isNil(message)) {
            this.setAttribute('_message', message, options);
        } else {
            log.error('Cannot set undefined to the reply statement.');
        }
    }

    /**
     * Get replying expression
     * @return {expression} replying expression
     */
    getReplyMessage() {
        return this._message;
    }

    /**
     * Check whether, Can be a child of the given node
     * @param {ASTNode} node ast node to be check against
     * @return {boolean} can be a child of status
     */
    canBeAChildOf(node) {
        return this.getFactory().isResourceDefinition(node)
                || this.getFactory().isWorkerDeclaration(node)
                || this.getFactory().isStatement(node);
    }

    /**
     * initialize from json
     * @param {object} jsonNode json node
     * @returns {void}
     */
    initFromJson(jsonNode) {
        let { expression } = jsonNode;
        const child = jsonNode.children[0] || {};
        if (child.is_identifier_literal) {
            expression = `|${jsonNode.expression}|`;
        }
        this.setReplyMessage(expression, { doSilently: true });
    }

    /**
     * Get reply expression string
     * @return {string} reply expression string
     */
    getReplyExpression() {
        return `reply ${this.getReplyMessage()}`;
    }

    /**
     * Check Whether message drawing target is allowed for te given target
     * @param {ASTNode} target target node
     * @return {boolean} target allowed or not
     */
    messageDrawTargetAllowed(target) {
        return this.getFactory().isResourceDefinition(target)
            || this.getFactory().isConnectorAction(target)
            || this.getFactory().isFunctionDefinition(target);
    }
}

export default ReplyStatement;
