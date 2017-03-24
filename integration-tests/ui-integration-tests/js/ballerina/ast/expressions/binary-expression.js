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
define(['lodash', './expression'], function (_, Expression) {

    /**
     * Constructor for BinaryExpression
     * @param {Object} args - Arguments to create the BinaryExpression
     * @constructor
     * @augments Expression
     */
    var BinaryExpression = function (args) {
        Expression.call(this, 'BinaryExpression');
        this._operator = _.get(args, 'operator');
    };

    BinaryExpression.prototype = Object.create(Expression.prototype);
    BinaryExpression.prototype.constructor = BinaryExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    BinaryExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateExpressionString(jsonNode), {doSilently: true});
    };

    /**
     * Generates the binary expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of binary expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    BinaryExpression.prototype.generateExpressionString = function (jsonNode) {
        var self = this;
        var expString = "";
        if(!_.isNil(jsonNode.children[0]) && !_.isNil(jsonNode.children[1])){
            var leftExpression = self.getFactory().createFromJson(jsonNode.children[0]);
            var rightExpression = self.getFactory().createFromJson(jsonNode.children[1]);
            leftExpression.initFromJson(jsonNode.children[0]);
            rightExpression.initFromJson(jsonNode.children[1]);
            expString = leftExpression.getExpression() + " " + this.getOperator() + " " + rightExpression.getExpression();
        }
        return expString;
    };

    BinaryExpression.prototype.getOperator = function (){
        return this._operator;
    };

    return BinaryExpression;
});