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
 * @param message expression of a reply statement.
 * @constructor
 */
class ReplyStatement extends Statement {
    constructor(args) {
        super();
        this._message = _.get(args, 'message') || '';
        this.type = "ReplyStatement";
    }

    setReplyMessage(message, options) {
        if (!_.isNil(message)) {
            this.setAttribute('_message', message, options);
        } else {
            log.error("Cannot set undefined to the reply statement.");
        }
    }

    getReplyMessage() {
        return this._message;
    }

    canBeAChildOf(node) {
        return this.getFactory().isResourceDefinition(node)
                || this.getFactory().isWorkerDeclaration(node)
                || this.getFactory().isStatement(node);
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        let {expression} = jsonNode;
        const child = jsonNode.children[0] || {};
        if (child.is_identifier_literal) {
            expression = `|${jsonNode.expression}|`;
        }
        this.setReplyMessage(expression, {doSilently: true});
    }

    getReplyExpression() {
        return `reply ${this.getReplyMessage()}`;
    }

    messageDrawTargetAllowed(target) {
        return this.getFactory().isResourceDefinition(target)
            || this.getFactory().isConnectorAction(target)
            || this.getFactory().isFunctionDefinition(target);
    }
}

export default ReplyStatement;
