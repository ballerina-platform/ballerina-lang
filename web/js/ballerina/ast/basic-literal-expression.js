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
     * Constructor for BackQuoteExpression
     * @param {Object} args - Arguments to create the BackQuoteExpression
     * @constructor
     */
    var BasicLiteralExpression = function (args) {
        Expression.call(this, 'BasicLiteralExpression');
        this._basicLiteralType = _.get(args, 'basicLiteralType', '');
        this._basicLiteralValue = _.get(args, 'basicLiteralValue', '');
    };

    BasicLiteralExpression.prototype = Object.create(Expression.prototype);
    BasicLiteralExpression.prototype.constructor = BasicLiteralExpression;

    /**
     * setting parameters from json
     * @param jsonNode
     */
    BasicLiteralExpression.prototype.initFromJson = function (jsonNode) {
        this._basicLiteralType = jsonNode.basic_literal_type;
        this._basicLiteralValue = jsonNode.basic_literal_value;
        this.setExpression(this.generateExpression(), {doSilently: true});
    };

    BasicLiteralExpression.prototype.generateExpression = function () {
        if (this._basicLiteralType == "string") {
            // Adding double quotes if it is a string.
            this._expression = "\"" + this.escapeEscapeChars(this._basicLiteralValue) + "\"";
        } else {
            this._expression = this._basicLiteralValue;
        }
    };

    BasicLiteralExpression.prototype.escapeEscapeChars = function(stringVal){
        return stringVal.replace(/"/g, "\\\"")
                        .replace(/\n/g, "\\n")
                        .replace(/\r/g, "\\r")
                        .replace(/\t/g, "\\t");
    };

    return BasicLiteralExpression;
});
