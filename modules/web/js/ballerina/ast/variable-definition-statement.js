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
        var equalIndex = _.indexOf(variableDefinitionStatementString, '=');
        var leftOperand = variableDefinitionStatementString.substring(0, equalIndex);
        var rightOperand = variableDefinitionStatementString.substring(equalIndex + 1);
        this.setLeftExpression(!_.isNil(leftOperand) ? leftOperand.trim() : "");
        this.setRightExpression(!_.isNil(rightOperand) ? rightOperand.trim() : "");
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
        var lhs = jsonNode.children[0];
        var rhs = jsonNode.children[1];
        if (lhs.type === 'left_operand_expression') {
            if (!_.isNil(lhs.children[0].variable_def_options)) {
                /**
                 * Sample1: message m = 'messageValue';
                 * Sample2: http:HTTPConnector connector = .....
                 *          <packageName>:<> <variable reference>
                 */
                var expressionValue = (!_.isNil(lhs.children[0].variable_def_options.package_name) ?
                    lhs.children[0].variable_def_options.package_name + ":" : "")
                    + lhs.children[0].variable_def_options.type_name
                    + " " + lhs.children[0].variable_reference_name;
                this.setLeftExpression(expressionValue);
            }
        } else {


        if (!_.isNil(lhs.variable_def_options)) {
            /**
             * Sample1: message m = 'messageValue';
             * Sample2: http:HTTPConnector connector = .....
             *          <packageName>:<> <variable reference>
             */
            var expressionValue = (!_.isNil(lhs.variable_def_options.package_name) ?
                lhs.variable_def_options.package_name + ":" : "")
                + lhs.variable_def_options.type_name
                + " " + lhs.variable_reference_name;
            this.setLeftExpression(expressionValue);
        }
    }

        if (!_.isNil(rhs)) {
            var rightExpressionChild = self.getFactory().createFromJson(rhs);
            self.addChild(rightExpressionChild);
            rightExpressionChild.initFromJson(rhs);
            if (self.getFactory().isRightOperandExpression(rightExpressionChild)) {
                this.setRightExpression(rightExpressionChild.getChildren()[0].getExpression());
            } else {
                this.setRightExpression(rightExpressionChild.getExpression());
            }
        }
    };

    return VariableDefinitionStatement;
});
