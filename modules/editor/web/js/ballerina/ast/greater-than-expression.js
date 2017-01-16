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
     * Constructor for GreaterThanExpression
     * @param {Object} args - Arguments to create the GreaterThanExpression
     * @constructor
     * @augments Expression
     */
    var GreaterThanExpression = function (args) {
        Expression.call(this, 'GreaterThanExpression');
    };

    GreaterThanExpression.prototype = Object.create(Expression.prototype);
    GreaterThanExpression.prototype.constructor = GreaterThanExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    GreaterThanExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateGreaterThanExpressionString(jsonNode));
    };

    /**
     * Generates the greater than expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of greater than expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    GreaterThanExpression.prototype.generateGreaterThanExpressionString = function (jsonNode) {
        var self = this;
        var greaterThanString = "";

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            //TODO : Need to remove this if/else ladder by delegating expression string calculation to child classes
            if (childJsonNode.type == "basic_literal_expression") {
                if(childJsonNode.basic_literal_type == "string") {
                    // Adding double quotes if it is a string.
                    greaterThanString += "\"" + childJsonNode.basic_literal_value + "\"";
                } else {
                    greaterThanString += childJsonNode.basic_literal_value;
                }
            } else if (childJsonNode.type == "variable_reference_expression") {
                greaterThanString += childJsonNode.variable_reference_name;
            }

            if (itr !== jsonNode.children.length - 1) {
                greaterThanString += " < ";
            }
        }
        return greaterThanString;
    };

    return GreaterThanExpression;
});
