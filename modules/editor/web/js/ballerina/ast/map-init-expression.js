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
    var MapInitExpression = function (args) {
        Expression.call(this, 'MapInitExpression');
    };

    MapInitExpression.prototype = Object.create(Expression.prototype);
    MapInitExpression.prototype.constructor = MapInitExpression;

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    MapInitExpression.prototype.initFromJson = function (jsonNode) {
        this.setExpression(this.generateMapInitExpressionString(jsonNode), {doSilently: true});
    };

    /**
     * Generates the map init expression as a string.
     * @param {Object} jsonNode - A node explaining the structure of map init expression.
     * @return {string} - Arguments as a string.
     * @private
     */
    MapInitExpression.prototype.generateMapInitExpressionString = function (jsonNode) {
        var self = this;
        var indexString = "";

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            var key = childJsonNode.key;
            var valueExpression = childJsonNode.expression[0];
            //Adding double quotes to key
            indexString += "\"" + childJsonNode.key + "\"" + ":";
            if (valueExpression.type == "basic_literal_expression") {
                if(valueExpression.basic_literal_type == "string") {
                    // Adding double quotes if it is a string.
                    indexString += "\"" + valueExpression.basic_literal_value + "\"";
                } else {
                    indexString += valueExpression.basic_literal_value;
                }
            }
            indexString += ",";
        }
        return "{" + indexString.substring(0, indexString.length-1) + "}";
    };
    return MapInitExpression;
});
