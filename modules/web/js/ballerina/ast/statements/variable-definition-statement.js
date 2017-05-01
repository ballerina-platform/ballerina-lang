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
import _ from 'lodash';
import Statement from './statement';
import CommonUtils from '../../utils/common-utils';
import VariableDeclaration from './../variable-declaration';

/**
 * Class to represent an Variable Definition statement.
 * @param {Object} [args] - Arguments for creating a variable definition statement.
 * @param {string} [args.leftExpression] - The left hand expression.
 * @param {string|undefined} [args.rightExpression] - The right hand expression.
 * @constructor
 * @augments Statement
 */
class VariableDefinitionStatement extends Statement {
    constructor(args) {
        super('VariableDefinitionStatement');
        this._leftExpression = _.get(args, 'leftExpression', "int i");
        this._rightExpression = _.get(args, 'rightExpression');
    }

    /**
     * Get the left expression
     * @return {string} _leftExpression - Left expression
     */
    getLeftExpression() {
        return this._leftExpression;
    }

    /**
     * Get the right expression
     * @return {string} _rightExpression - Right expression
     */
    getRightExpression() {
        return this._rightExpression;
    }

    /**
     * Get the variable definition statement string
     * @return {string} - Variable definition expression string
     */
    getStatementString() {
        var variableDefinitionStatementString;
        if (_.isNil(this._rightExpression) || _.isEmpty(this._rightExpression)) {
            variableDefinitionStatementString = this._leftExpression;
        } else {
            variableDefinitionStatementString = this._leftExpression + " = " + this._rightExpression;
        }
        return variableDefinitionStatementString;
    }

    /**
     * Set the left expression
     * @param {string} leftExpression - Left expression
     */
    setLeftExpression(leftExpression) {
        this.setAttribute("_leftExpression", leftExpression.trim());
    }

    /**
     * Set the right expression
     * @param {string} rightExpression - Right expression
     */
    setRightExpression(rightExpression) {
        this.setAttribute("_rightExpression", rightExpression.trim());
    }

    /**
     * Gets the ballerina type of the variable definition statement.
     * @return {string} - The ballerina type.
     */
    getBType() {
        return (this._leftExpression.split(" ")[0]).trim();
    }

    /**
     * Gets the identifier of the variable definition statement.
     * @return {string} - The identifier.
     */
    getIdentifier() {
        return (this._leftExpression.split(" ")[1]).trim();
    }

    /**
     * Gets the identifier of the variable definition statement.
     * @return {string} - The identifier.
     */
    getValue() {
        return this._rightExpression;
    }

    setIdentifier(identifier) {
        this.setLeftExpression(this.getBType() + " " + identifier);
    }

    setBType(bType) {
        this.setLeftExpression(bType + " " + this.getIdentifier());
    }

    setValue(value) {
        this.setRightExpression(value);
    }

    /**
     * Set the variable definition expression string
     * @param {string} variableDefinitionStatementString - variable definition expression string
     */
    setStatementString(variableDefinitionStatementString) {
        var equalIndex = _.indexOf(variableDefinitionStatementString, '=');
        if (equalIndex === -1) {
            var leftOperand = variableDefinitionStatementString;
        } else {
            var leftOperand = variableDefinitionStatementString.substring(0, equalIndex);
            var rightOperand = variableDefinitionStatementString.substring(equalIndex + 1);
        }

        this.setLeftExpression(!_.isNil(leftOperand) ? leftOperand.trim() : "");
        this.setRightExpression(!_.isNil(rightOperand) ? rightOperand.trim() : "");
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
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
    }

    initFromJson(jsonNode) {
        var self = this;

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });

        if (!_.isNil(this.getChildren()[0])){
            this.setLeftExpression(this.getChildren()[0].getExpression());
        }
        if (!_.isNil(this.getChildren()[1])) {
            this.setRightExpression(this.getChildren()[1].getExpression());
        }
    }
}

export default VariableDefinitionStatement;

