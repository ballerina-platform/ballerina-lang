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
     * Constructor for EqualExpression
     * @param {Object} args - Arguments to create the EqualExpression
     * @constructor
     * @augments Expression
     */
    var EqualExpression = function (args) {
        Expression.call(this, 'EqualExpression');
    };

    EqualExpression.prototype = Object.create(Expression.prototype);
    EqualExpression.prototype.constructor = EqualExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    EqualExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateEqualExpressionString(jsonNode));
    };

    /**
     * Generates the equal expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of equal expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    EqualExpression.prototype.generateEqualExpressionString = function (jsonNode) {
        var self = this;
        var equalString = "";

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            //TODO : Need to remove this if/else ladder by delegating expression string calculation to child classes
            if (childJsonNode.type == "basic_literal_expression") {
                if(childJsonNode.basic_literal_type == "string") {
                    // Adding double quotes if it is a string.
                    equalString += "\"" + childJsonNode.basic_literal_value + "\"";
                } else {
                    equalString += childJsonNode.basic_literal_value;
                }
            } else if (childJsonNode.type == "variable_reference_expression") {
                equalString += childJsonNode.variable_reference_name;
            }

            if (itr !== jsonNode.children.length - 1) {
                equalString += " = ";
            }
        }
        return equalString;
    };

    return EqualExpression;
});
