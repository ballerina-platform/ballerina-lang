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
     * Constructor for LeftOperandExpression
     * @param {Object} args - Arguments to create the LeftOperandExpression
     * @constructor
     */
    var LeftOperandExpression = function (args) {
        Statement.call(this, 'LeftOperandExpression');
        this._operand_type = _.get(args, "operandType", "");
        this._left_operand_expression_string = _.get(args, "variableReferenceName");
    };

    LeftOperandExpression.prototype = Object.create(Statement.prototype);
    LeftOperandExpression.prototype.constructor = LeftOperandExpression;

    /**
     * Get Variable Reference Name
     * @returns {undefined|string}
     */
    LeftOperandExpression.prototype.getLeftOperandExpressionString = function () {
        if (!_.isEmpty(this._operand_type)) {
            return this._operand_type + " " + this._left_operand_expression_string;
        }
        return this._left_operand_expression_string;
    };

    /**
     * Set Variable Reference Name
     * @param {string} leftOperandExpStr left operand expression string
     * @param {Object} options
     */
    LeftOperandExpression.prototype.setLeftOperandExpressionString = function (leftOperandExpStr, options) {
        if (leftOperandExpStr.trim().split(" ").length > 1) {
            this.setAttribute('_operand_type', leftOperandExpStr.trim().split(" ")[0], options);
            this.setAttribute('_left_operand_expression_string', leftOperandExpStr.trim().split(" ")[1], options);
        } else {
            this.setAttribute('_left_operand_expression_string', leftOperandExpStr, options);
        }
    };

    LeftOperandExpression.prototype.setLeftOperandType = function (operandType, options) {
        this.setAttribute('_operand_type', operandType.trim(), options);
    };

    /**
     * setting parameters from json
     * @param jsonNode
     */
    LeftOperandExpression.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return LeftOperandExpression;
});
