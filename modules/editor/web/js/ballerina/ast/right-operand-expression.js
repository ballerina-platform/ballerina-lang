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
define(['lodash', './statement'], function(_, Statement){

    /**
     * Constructor for RightOperandExpression
     * @param {Object} args - Arguments to create the RightOperandExpression
     * @constructor
     */
    var RightOperandExpression = function (args) {
        Statement.call(this, 'RightOperandExpression');
        this._back_quate_enclosed_string = undefined;
    };

    RightOperandExpression.prototype = Object.create(Statement.prototype);
    RightOperandExpression.prototype.constructor = RightOperandExpression;

    /**
     * Get BackQuote String
     * @returns {undefined|string}
     */
    RightOperandExpression.prototype.getRightOperandExpressionString = function () {
        return this._back_quate_enclosed_string;
    };

    /**
     * Set Back Quote String value
     * @param {string} backQuoteStr
     */
    RightOperandExpression.prototype.setRightOperandExpressionString = function (backQuoteStr) {
        this._back_quate_enclosed_string = backQuoteStr;
    };

    /**
     * setting parameters from json
     * @param jsonNode
     */
    RightOperandExpression.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            // TODO: Handle this Properly
            if (childNode.type === 'back_quote_expression') {
                self.setRightOperandExpressionString('`' + childNode.back_quate_enclosed_string + '`');
            } else if (childNode.type === 'instance_creation_expression'){
                self.setRightOperandExpressionString("new " + childNode.instance_type);
            } else if (childNode.type === 'basic_literal_expression'){
                self.setRightOperandExpressionString('"' + childNode.basic_literal_value + '"');
            } else if(childNode.type === 'variable_reference_expression'){
                self.setRightOperandExpressionString(childNode.variable_reference_name);
            } else {
                var child = self.getFactory().createFromJson(childNode);
                // TODO: Need to handle the function expressions and statements differently. Need Refactor the bellow
                if (self.getFactory().isFunctionInvocationExpression(child) &&
                    !self.getFactory().isFunctionInvocationStatement(child.getParent())) {
                    var newParent = self.getFactory().createFunctionInvocationStatement();
                    newParent.addChild(child);
                    self.addChild(newParent);
                } else {
                    self.addChild(child);
                }
                child.initFromJson(childNode);
            }
        });
    };

    return RightOperandExpression;
});
