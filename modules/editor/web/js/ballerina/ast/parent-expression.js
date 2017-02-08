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
     * Constructor for VariableReferenceExpression
     * @param {Object} args - Arguments to create the VariableReferenceExpression
     * @constructor
     */
    var ParentExpression = function (args) {
        this._parentExpression = _.get(args, 'parentExpressionName');
        Expression.call(this, 'parentExpressionName');
    };

    ParentExpression.prototype = Object.create(Expression.prototype);
    ParentExpression.prototype.constructor = ParentExpression;

    /**
     * Setter for parent expression
     * @param ParentExpression
     */
    ParentExpression.prototype.setParentExpressionName = function (parentExpression, options) {
        this.setAttribute('_parentExpression', parentExpression, options);
    };

    /**
     * Getter for ParentExpression
     * @returns ParentExpression
     */
    ParentExpression.prototype.getParentExpression = function () {
        return this._variableReferenceName;
    };

    ParentExpression.prototype.generateExpression = function () {
        this._expression = this.getParentExpression()
    };

    return ParentExpression;
});
