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
define(['lodash', 'log', './statement'], function (_, log, Statement) {

    /**
     * Class for return statement in ballerina.
     * @param expression zero or many expressions for a return statement.
     * @constructor
     */
    var ReturnStatement = function (args) {
        Statement.call(this);
        this._expression = _.get(args, 'expression', '');
        this.type = "ReturnStatement";
    };

    ReturnStatement.prototype = Object.create(Statement.prototype);
    ReturnStatement.prototype.constructor = ReturnStatement;

    ReturnStatement.prototype.setExpression = function (expression, options) {
        if (!_.isNil(expression)) {
            this.setAttribute('_expression', expression, options);
        } else {
            log.error("Cannot set undefined to the return statement.");
        }
    };

    ReturnStatement.prototype.canBeAChildOf = function (node) {
        return this.getFactory().isFunctionDefinition(node) ||
               this.getFactory().isConnectorAction(node) ||
            this.getFactory().isStatement(node);
    };

    ReturnStatement.prototype.getReturnExpression = function () {
        return "return " + this.getExpression();
    };

    ReturnStatement.prototype.getExpression = function () {
        return this._expression;
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    ReturnStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        var expression = "";

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            var child = self.getFactory().createFromJson(childJsonNode);
            child.initFromJson(childJsonNode);
            expression += child.getExpression();

            if (itr !== jsonNode.children.length - 1) {
                expression += " , ";
            }
        }
        this.setExpression(expression, {doSilently: true});
    };

    return ReturnStatement;
});
