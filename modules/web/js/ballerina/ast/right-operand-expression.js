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
define(['lodash', './statement'], function (_, Statement) {

    /**
     * Constructor for RightOperandExpression
     * @param {Object} args - Arguments to create the RightOperandExpression
     * @constructor
     */
    var RightOperandExpression = function (args) {
        Statement.call(this, 'RightOperandExpression');
        this._right_operand_expression_string = undefined;
    };

    RightOperandExpression.prototype = Object.create(Statement.prototype);
    RightOperandExpression.prototype.constructor = RightOperandExpression;

    /**
     * Get Right Operand Expression String
     * @returns {string} - The expression
     */
    RightOperandExpression.prototype.getRightOperandExpressionString = function () {
        return this._right_operand_expression_string;
    };

    /**
     * Set Right Operand Expression String
     * @param {string} rightOperandExpStr - The expression
     */
    RightOperandExpression.prototype.setRightOperandExpressionString = function (rightOperandExpStr, options) {
        this.setAttribute('_right_operand_expression_string', rightOperandExpStr, options);
    };

    /**
     * Override the removeChild function
     * @param {ASTNode} child - child node
     */
    RightOperandExpression.prototype.removeChild = function (child) {
        this.getParent().removeChild(this);
    };

    /**
     * setting parameters from json
     * @param jsonNode
     */
    RightOperandExpression.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
            self.setRightOperandExpressionString(child.getExpression(), {doSilently: true});
        });
    };

    return RightOperandExpression;
});
