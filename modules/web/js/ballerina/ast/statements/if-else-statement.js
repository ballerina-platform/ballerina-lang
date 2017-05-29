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
import _ from 'lodash';
import log from 'log';
import Statement from './statement';
import ElseStatement from './else-statement';
import ElseIfStatement from './else-if-statement';
import IfStatement from './if-statement';
import BallerinaASTFactory from './../ballerina-ast-factory';

/**
 * Class for if conditions in ballerina.
 * @constructor
 */

class IfElseStatement extends Statement {
    constructor(args) {
        super();

        var ifStatement = new IfStatement(args);
        this.addChild(ifStatement);
        this._ifStatement = ifStatement;

        this._elseIfStatements = [];
        this.type = "IfElseStatement";
    }

    getIfStatement() {
        return this._ifStatement;
    }

    getElseStatement() {
        return this._elseStatement;
    }

    getElseIfStatement() {
        return this._elseIfStatements;
    }

    setIfStatement(ifStatement, options) {
        this.setAttribute('_ifStatement', ifStatement, options);
    }

    /**
     * creates Else Statement
     * @param args
     */
    createElseStatement(args) {
        var newElseStatement = new ElseStatement(args);
        this._elseStatement = newElseStatement;
        this.addChild(newElseStatement);
        return newElseStatement;
    }

    /**
     * creates Else If Statement
     * @param args
     */
    createElseIfStatement(args) {
        var newElseIfStatement = new ElseIfStatement(args);
        this._elseIfStatements.push(newElseIfStatement);
        this.addChild(newElseIfStatement);
        return newElseIfStatement;
    }

    /**
     * Override the addChild method for ordering the child elements
     * @param {ASTNode} child
     * @param {number|undefined} index
     */
    addChild(child, index) {

        const lastElseIfIndex = _.findLastIndex(this.getChildren(), function (node) {
            return BallerinaASTFactory.isElseIfStatement(node);
        });

        const elseStatementIndex = _.findIndex(this.getChildren(), function (node) {
            return BallerinaASTFactory.isElseStatement(node);
        });

        if (BallerinaASTFactory.isElseIfStatement(child) && elseStatementIndex > -1) {
            index = elseStatementIndex;
        }

        Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
    }

    /**
     * initialize IfElseStatement from json object
     * @param {Object} jsonNode to initialize from
     * @param {Object} [jsonNode.if_statement] - If statement block
     * @param {Object} [jsonNode.else_statement] - Else statement block
     * @param {Object} [jsonNode.else_if_blocks] - Else If statement blocks
     */
    initFromJson(jsonNode) {

        var self = this;

        _.each(jsonNode.if_statement, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            if (self.getFactory().isIfCondition(child)) {
                child.initFromJson(childNode);
                self.getIfStatement().setCondition(child.getChildren()[0].getExpression());
            } else if(self.getFactory().isThenBody(child)){
                _.each(childNode.children, function (childNode) {
                    var child = undefined;
                    var childNodeTemp = undefined;
                    if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                        child = self.getFactory().createConnectorDeclaration();
                        childNodeTemp = childNode;
                    } else {
                        child = self.getFactory().createFromJson(childNode);
                        childNodeTemp = childNode;
                    }
                    self._ifStatement.addChild(child);
                    child.initFromJson(childNodeTemp);
                });
            }
        });

        _.each(jsonNode.else_if_blocks, function (elseIfNode) {
            let elseIfStatement = new ElseIfStatement('testCondition');
            self._elseIfStatements.push(elseIfStatement);
            self.addChild(elseIfStatement);
            _.each(elseIfNode.children, function (childNode) {
                var child = undefined;
                var childNodeTemp = undefined;
                //TODO : generalize this logic
                if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                    child = self.getFactory().createConnectorDeclaration();
                    childNodeTemp = childNode;
                } else {
                    child = self.getFactory().createFromJson(childNode);
                    childNodeTemp = childNode;
                }
                elseIfStatement.addChild(child);
                child.initFromJson(childNodeTemp);
            });
        });

        if(jsonNode.else_statement) {
            const elseStatement = new ElseStatement();
            this.addChild(elseStatement);
            this._elseStatement = elseStatement;
        }

        _.each(jsonNode.else_statement, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            //TODO : generalize this logic
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.getFactory().createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.getFactory().createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self._elseStatement.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    }
}

export default IfElseStatement;

