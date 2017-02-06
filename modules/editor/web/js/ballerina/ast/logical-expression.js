/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
     * Class to represent an logical expression in ballerina.
     * @constructor
     */
    var LogicalExpression = function (args) {
        Statement.call(this, 'LogicalExpression');
        this._expression = 'a > b';
        this.type = "LogicalExpression";
    };

    LogicalExpression.prototype = Object.create(Statement.prototype);
    LogicalExpression.prototype.constructor = LogicalExpression;

    LogicalExpression.prototype.setExpression = function (expression, options) {
        this.setAttribute('_expression', expression, options);
    };

    LogicalExpression.prototype.getExpression = function () {
        return this._expression;
    };

    return LogicalExpression;
});
