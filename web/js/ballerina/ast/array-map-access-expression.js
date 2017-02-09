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
     * Constructor for ArrayMapAccessExpression
     * @param {Object} args - Arguments to create the ArrayMapAccessExpression
     * @constructor
     * @augments Expression
     */
    var ArrayMapAccessExpression = function (args) {
        Expression.call(this, 'ArrayMapAccessExpression');
    };

    ArrayMapAccessExpression.prototype = Object.create(Expression.prototype);
    ArrayMapAccessExpression.prototype.constructor = ArrayMapAccessExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    ArrayMapAccessExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateArrayMapAccessExpressionString(jsonNode), {doSilently: true});
    };

    /**
     * Generates the array map access expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of array map access expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    ArrayMapAccessExpression.prototype.generateArrayMapAccessExpressionString = function (jsonNode) {
        var self = this;
        var indexString = "";

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            var child = self.getFactory().createFromJson(childJsonNode);
            child.initFromJson(childJsonNode);
            indexString = child.getExpression();
        }
        return jsonNode.array_map_access_expression_name + "[" + indexString + "]";
    };

    return ArrayMapAccessExpression;
});
