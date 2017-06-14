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
 * Class for return statement in ballerina.
 * @param expression zero or many expressions for a return statement.
 * @constructor
 */
class ReturnStatement extends Statement {
    constructor(args) {
        super();
        this._expression = _.get(args, 'expression', '');
        this.type = 'ReturnStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '',
            3: '\n',
        };
    }

    setExpression(expression, options) {
        if (!_.isNil(expression)) {
            this.setAttribute('_expression', expression, options);
        } else {
            log.error('Cannot set undefined to the return statement.');
        }
    }

    canBeAChildOf(node) {
        return this.getFactory().isFunctionDefinition(node) ||
               this.getFactory().isConnectorAction(node) ||
            this.getFactory().isStatement(node);
    }

    getReturnExpression() {
        return `return${this.getWSRegion(1)}${this.getExpression()}`;
    }

    getExpression() {
        return this._expression;
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        const self = this;
        let expression = '';

        for (let itr = 0; itr < jsonNode.children.length; itr++) {
            const childJsonNode = jsonNode.children[itr];
            const child = self.getFactory().createFromJson(childJsonNode);
            child.initFromJson(childJsonNode);
            expression += child.getExpressionString();

            if (itr !== jsonNode.children.length - 1) {
                expression += ',';
            }
        }
        this.setExpression(expression, { doSilently: true });
    }
}

export default ReturnStatement;
