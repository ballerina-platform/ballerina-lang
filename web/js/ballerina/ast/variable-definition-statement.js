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
define(['lodash', './statement'], function (_, Statement) {
    /**
     * Class to represent an Variable Definition statement.
     * @constructor
     */
    var VariableDefinitionStatement = function (args) {
        Statement.call(this, 'VariableDefinitionStatement');
        this._leftExpression = _.get(args, 'leftExpression', 'leftExpression');
        this._rightExpression = _.get(args, 'rightExpression', 'rightExpression');
        this._variableDefinitionStatementString = _.get(args, 'expressionString', 'int i = 0');
    };

    VariableDefinitionStatement.prototype = Object.create(Statement.prototype);
    VariableDefinitionStatement.prototype.constructor = VariableDefinitionStatement;

    /**
     * initialize VariableDefinitionStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    VariableDefinitionStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;

        // TODO: need to refactor based on the backend response
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    /**
     * Override the removeChild function
     * @param {ASTNode} child - child node
     */
    VariableDefinitionStatement.prototype.removeChild = function (child) {
        this.getParent().removeChild(this);
    };

    /**
     * Get the left expression
     * @return {string} _leftExpression - Left expression
     */
    VariableDefinitionStatement.prototype.getLeftExpression = function () {
        return this._leftExpression;
    };

    /**
     * Get the right expression
     * @return {string} _rightExpression - Right expression
     */
    VariableDefinitionStatement.prototype.getRightExpression = function () {
        return this._rightExpression;
    };

    /**
     * Get the variable definition statement string
     * @return {string} _variableDefinitionExpressionString - variable definition expression string
     */
    VariableDefinitionStatement.prototype.getVariableDefinitionStatementString = function () {
        return this._variableDefinitionStatementString;
    };

    /**
     * Set the left expression
     * @param {string} leftExpression - Left expression
     */
    VariableDefinitionStatement.prototype.setLeftExpression = function (leftExpression) {
        this._leftExpression = leftExpression;
    };

    /**
     * Set the right expression
     * @param {string} rightExpression - Right expression
     */
    VariableDefinitionStatement.prototype.setRightExpression = function (rightExpression) {
        this._rightExpression = rightExpression;
    };

    /**
     * Set the variable definition expression string
     * @param {string} variableDefinitionStatementString - variable definition expression string
     */
    VariableDefinitionStatement.prototype.setVariableDefinitionStatementString = function (variableDefinitionStatementString) {
        this._variableDefinitionStatementString = variableDefinitionStatementString;
        var tokens = this._variableDefinitionStatementString.split("=");
        this.setLeftExpression(!_.isNil(tokens[0]) ? tokens[0].trim() : "");
        this.setRightExpression(!_.isNil(tokens[1]) ? tokens[1].trim() : "");
    };

    return VariableDefinitionStatement;
});
