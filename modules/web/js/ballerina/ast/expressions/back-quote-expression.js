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
    var BackQuoteExpression = function (args) {
        Expression.call(this, 'BackQuoteExpression');
        this._backQuoteEnclosedString = _.get(args, 'backQuoteEnclosedString', '');
    };

    BackQuoteExpression.prototype = Object.create(Expression.prototype);
    BackQuoteExpression.prototype.constructor = BackQuoteExpression;

    /**
     * Setter for BackQuoteEnclosedString
     * @param backQuoteEnclosedString
     */
    BackQuoteExpression.prototype.setBackQuoteEnclosedString = function (backQuoteEnclosedString, options) {
        this.setAttribute('_backQuoteEnclosedString', backQuoteEnclosedString, options);
    };

    /**
     * Getter for BackQuoteEnclosedString
     * @returns backQuoteEnclosedString
     */
    BackQuoteExpression.prototype.getBackQuoteEnclosedString = function () {
        return this._backQuoteEnclosedString;
    };

    /**
     * initialize BackQuoteExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.back_quote_enclosed_string] - back quote enclosed string
     */
    BackQuoteExpression.prototype.initFromJson = function (jsonNode) {
        this.setBackQuoteEnclosedString(jsonNode.back_quote_enclosed_string, {doSilently: true});
        this.setExpression(this.generateExpression(), {doSilently: true});
    };

    BackQuoteExpression.prototype.generateExpression = function () {
        this._expression = '`' + this.getBackQuoteEnclosedString() + '`';
    };

    return BackQuoteExpression;
});
