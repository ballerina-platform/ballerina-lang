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
     * Constructor for InstanceCreationExpression
     * @param {Object} args - Arguments to create the InstanceCreationExpression
     * @param {Object} args.typeName - Type of the instance creation.
     * @constructor
     * @augments Expression
     */
    var InstanceCreationExpression = function (args) {
        Expression.call(this, 'InstanceCreationExpression');
        this._typeName = _.get(args, 'typeName', 'newType');
    };

    InstanceCreationExpression.prototype = Object.create(Expression.prototype);
    InstanceCreationExpression.prototype.constructor = InstanceCreationExpression;

    InstanceCreationExpression.prototype.setTypeName = function (typeName, options) {
        this.setAttribute('_typeName', typeName, options);
    };

    InstanceCreationExpression.prototype.getTypeName = function () {
        return this._typeName;
    };

    /**
     * initialize BackQuoteExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.instance_type] - instance type
     */
    InstanceCreationExpression.prototype.initFromJson = function (jsonNode) {
        this.setTypeName(jsonNode.instance_type, {doSilently: true});
        this.setExpression(this.generateExpression(), {doSilently: true});
    };

    InstanceCreationExpression.prototype.generateExpression = function () {
        this._expression = 'new ' + this.getTypeName();
    };

    return InstanceCreationExpression;
});
