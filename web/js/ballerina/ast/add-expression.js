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
     * Constructor for AddExpression
     * @param {Object} args - Arguments to create the AddExpression
     * @constructor
     * @augments Expression
     */
    var AddExpression = function (args) {
        Expression.call(this, 'AddExpression');
    };

    AddExpression.prototype = Object.create(Expression.prototype);
    AddExpression.prototype.constructor = AddExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    AddExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateAddExpressionString(jsonNode));
    };

    /**
     * Generates the add expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of add expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    AddExpression.prototype.generateAddExpressionString = function (jsonNode) {
        var self = this;
        var addString = "";

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            //TODO : Need to remove this if/else ladder by delegating expression string calculation to child classes
            if (childJsonNode.type == "basic_literal_expression") {
                if(childJsonNode.basic_literal_type == "string") {
                    // Adding double quotes if it is a string.
                    addString += "\"" + childJsonNode.basic_literal_value + "\"";
                } else {
                    addString += childJsonNode.basic_literal_value;
                }
            } else if (childJsonNode.type == "variable_reference_expression") {
                addString += childJsonNode.variable_reference_name;
            } else {
                var child = self.getFactory().createFromJson(childJsonNode);
                child.initFromJson(childJsonNode);
                addString += child.getExpression();
            }

            if (itr !== jsonNode.children.length - 1) {
                addString += " + ";
            }
        }
        return addString;
    };

    return AddExpression;
});
