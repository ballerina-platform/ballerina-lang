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
define(['lodash', './statement', '../utils/common-utils', './variable-declaration'],
    function (_, Statement, CommonUtils, VariableDeclaration) {

    /**
     * Class to represent an Variable Definition statement.
     * @param {Object} [args] - Arguments for creating a variable definition statement.
     * @param {string} [args.leftExpression] - The left hand expression.
     * @param {string|undefined} [args.rightExpression] - The right hand expression.
     * @constructor
     * @augments Statement
     */
    var VariableDefinitionStatement = function (args) {
        Statement.call(this, 'VariableDefinitionStatement');
        this._leftExpression = _.get(args, 'leftExpression', "string str");
        this._rightExpression = _.get(args, 'rightExpression');
    };

    VariableDefinitionStatement.prototype = Object.create(Statement.prototype);
    VariableDefinitionStatement.prototype.constructor = VariableDefinitionStatement;

    /**
     * initialize VariableDefinitionStatement from json object
        this._leftExpression = _.get(args, 'leftExpression');
        this._rightExpression = _.get(args, 'rightExpression');
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
     * @return {string} - Variable definition expression string
     */
    VariableDefinitionStatement.prototype.getStatementString = function () {
        var variableDefinitionStatementString;
        if(_.isNil(this._rightExpression) || _.isEmpty(this._rightExpression)){
            variableDefinitionStatementString = this._leftExpression;
        }else {
            variableDefinitionStatementString = this._leftExpression + " = " + this._rightExpression;
        }
        return variableDefinitionStatementString;
    };

    /**
     * Set the left expression
     * @param {string} leftExpression - Left expression
     */
    VariableDefinitionStatement.prototype.setLeftExpression = function (leftExpression) {
        this.setAttribute("_leftExpression", leftExpression.trim());
    };

    /**
     * Set the right expression
     * @param {string} rightExpression - Right expression
     */
    VariableDefinitionStatement.prototype.setRightExpression = function (rightExpression) {
        this.setAttribute("_rightExpression", rightExpression.trim());
    };

    /**
     * Gets the ballerina type of the variable definition statement.
     * @return {string} - The ballerina type.
     */
    VariableDefinitionStatement.prototype.getBType = function() {
        return (this._leftExpression.split(" ")[0]).trim();
    };

    /**
     * Gets the identifier of the variable definition statement.
     * @return {string} - The identifier.
     */
    VariableDefinitionStatement.prototype.getIdentifier = function() {
        return (this._leftExpression.split(" ")[1]).trim();
    };

    VariableDefinitionStatement.prototype.setIdentifier = function(identifier) {
        this.setLeftExpression(this.getBType() + " " + identifier);
    };

    /**
     * Set the variable definition expression string
     * @param {string} variableDefinitionStatementString - variable definition expression string
     */
    VariableDefinitionStatement.prototype.setStatementString = function (variableDefinitionStatementString) {
        var tokens = variableDefinitionStatementString.split("=");
        this.setLeftExpression(!_.isNil(tokens[0]) ? tokens[0].trim() : "");
        this.setRightExpression(!_.isNil(tokens[1]) ? tokens[1].trim() : "");
    };

    /**
     * @inheritDoc
     * @override
     */
    VariableDefinitionStatement.prototype.generateUniqueIdentifiers = function () {
        if (this.getFactory().isResourceDefinition(this.parent) || this.getFactory().isConnectorAction(this.parent)) {
            CommonUtils.generateUniqueIdentifier({
                node: this,
                attributes: [{
                    checkEvenIfDefined: true,
                    defaultValue: "i",
                    setter: this.setIdentifier,
                    getter: this.getIdentifier,
                    parents: [{
                        // resource/connector action definition
                        node: this.parent,
                        getChildrenFunc: this.parent.getVariableDefinitionStatements,
                        getter: this.getIdentifier
                    }, {
                        // service/connector definition
                        node: this.parent.parent,
                        getChildrenFunc: this.parent.parent.getVariableDefinitionStatements,
                        getter: this.getIdentifier
                    }, {
                        // ballerina-ast-root definition
                        node: this.parent.parent.parent,
                        getChildrenFunc: this.parent.parent.parent.getConstantDefinitions,
                        getter: VariableDeclaration.prototype.getIdentifier
                    }]
                }]
            });
        }
    };

    VariableDefinitionStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return VariableDefinitionStatement;
});
