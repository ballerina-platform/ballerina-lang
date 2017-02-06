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
     * Class to represent an assignment in ballerina.
     * @constructor
     */
    var Assignment = function (args) {
        Statement.call(this, 'Assignment');
        this._variableAccessor = _.get(args, 'accessor', 'var1');
        this._expression = 'a = b';
    };

    Assignment.prototype = Object.create(Statement.prototype);
    Assignment.prototype.constructor = Assignment;

    Assignment.prototype.setVariableAccessor = function (accessor, options) {
        this.setAttribute('_variableAccessor', accessor, options);
    };

    Assignment.prototype.setExpression = function (expression, options) {
        this.setAttribute('_expression', expression, options);
    };

    Assignment.prototype.getVariableAccessor = function () {
        return this._variableAccessor;
    };

    Assignment.prototype.getExpression = function () {
        return this._expression;
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    Assignment.prototype.initFromJson = function (jsonNode) {
        this.setExpression('a = b', {doSilently: true});
    };

    return Assignment;
});
