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
import Expression from './expression';

/**
 * Constructor for BinaryExpression
 * @param {Object} args - Arguments to create the BinaryExpression
 * @constructor
 * @augments Expression
 */
class BinaryExpression extends Expression {
    constructor(args) {
        super('BinaryExpression');
        this._operator = _.get(args, 'operator');
    }

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.setExpression(this.generateExpressionFromJson(jsonNode), {doSilently: true});
    }

    /**
     * Generates the binary expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of binary expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    generateExpressionFromJson(jsonNode) {
        var self = this;
        var expString = "";
        if(!_.isNil(jsonNode.children[0]) && !_.isNil(jsonNode.children[1])){
            var leftExpression = self.getFactory().createFromJson(jsonNode.children[0]);
            var rightExpression = self.getFactory().createFromJson(jsonNode.children[1]);
            leftExpression.initFromJson(jsonNode.children[0]);
            rightExpression.initFromJson(jsonNode.children[1]);
            expString = leftExpression.getExpression() + " " + this.getOperator() + " " + rightExpression.getExpression();
        }
        this._expression = expString;
        return expString;
    }

    generateExpression() {
        return this._expression;
    }

    getOperator() {
        return this._operator;
    }
}

export default BinaryExpression;
