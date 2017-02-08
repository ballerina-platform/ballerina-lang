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
     * Constructor for TypeCastingExpression
     * @param {Object} args - Arguments to create the TypeCastingExpression
     * @constructor
     * @augments Expression
     */
    var TypeCastingExpression = function (args) {
        Expression.call(this, 'TypeCastingExpression');
        this._targetType = _.get(args, 'targetType');
    };

    TypeCastingExpression.prototype = Object.create(Expression.prototype);
    TypeCastingExpression.prototype.constructor = TypeCastingExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    TypeCastingExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateExpressionString(jsonNode), {doSilently: true});
    };

    /**
     * Generates the type casting expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of type casting expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    TypeCastingExpression.prototype.generateExpressionString = function (jsonNode) {
        var self = this;
        var expString = "";
        var targetType = jsonNode.children[1].target_type;
        var child = self.getFactory().createFromJson(jsonNode.children[0]);
        child.initFromJson(jsonNode.children[0]);
        var castingExpression = jsonNode.children[1].target_type;
        expString += "(" + targetType + ")" + child.getExpression();
        return expString;
    };

    TypeCastingExpression.prototype.getOperator = function (){
        return this._operator;
    };

    return TypeCastingExpression;
});