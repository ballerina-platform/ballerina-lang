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
    var ReferenceTypeInitExpression = function (args) {
        Expression.call(this, 'ReferenceTypeInitExpression');
    };

    ReferenceTypeInitExpression.prototype = Object.create(Expression.prototype);
    ReferenceTypeInitExpression.prototype.constructor = ReferenceTypeInitExpression;

    /**
     * initialize ReferenceTypeInitExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    ReferenceTypeInitExpression.prototype.initFromJson = function (jsonNode) {
        var self = this;
        var generateExpression = '';
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
            generateExpression +=child.getExpression() + ",";
        });
        this.setExpression("{" + (generateExpression.substring(0, generateExpression.length-1)) + "}",{doSilently: true});
    };

    ReferenceTypeInitExpression.prototype.generateExpression = function () {
        this._expression = this.getVariableReferenceName()
    };
    return ReferenceTypeInitExpression;
});