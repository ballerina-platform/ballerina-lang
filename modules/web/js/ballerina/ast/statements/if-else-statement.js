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
import Statement from './statement';
import ElseStatement from './else-statement';
import ElseIfStatement from './else-if-statement';
import IfStatement from './if-statement';
import BallerinaASTFactory from './../ballerina-ast-factory';

/**
 * Class for if conditions in ballerina.
 */
class IfElseStatement extends Statement {
    /**
     * Constructor for IfElseStatement
     * @param {object} args arguments for IfElseStatement
     * @constructor
     */
    constructor(args) {
        super('IfElseStatement');
        this.createIfStatement(args);
    }

    /**
     * Get the If statement
     * @return {IfStatement} if statement
     */
    getIfStatement() {
        const stmtArray = this.getChildrenOfType(this.getFactory().isIfStatement);
        return (!_.isNil(stmtArray) && !_.isEmpty(stmtArray))
                ? _.nth(stmtArray, 0) : undefined ;
    }

    /**
     * Get the Else statement
     * @return {ElseStatement} else statement
     */
    getElseStatement() {
        const stmtArray = this.getChildrenOfType(this.getFactory().isElseStatement);
        return (!_.isNil(stmtArray) && !_.isEmpty(stmtArray))
                ? _.nth(stmtArray, 0) : undefined ;
    }

    /**
     * Get the ElseIf statement
     * @return {ElseIfStatement} else if statement
     */
    getElseIfStatements() {
        return this.getChildrenOfType(this.getFactory().isElseIfStatement);
    }

    /**
     * creates If Statement
     * @param {object} args arguments for if-else statement
     * @returns {IfStatement} if else node
     */
    createIfStatement(args) {
        const newIfStmt = new IfStatement(args);
        this.addChild(newIfStmt);
        return newIfStmt;
    }

    /**
     * creates Else Statement
     * @param {object} args Else statement arguments
     * @returns {ElseStatement} else statement
     */
    createElseStatement(args) {
        const newElseStatement = this.getFactory().createElseStatement(args);
        this.addChild(newElseStatement);
        return newElseStatement;
    }

    /**
     * creates Else If Statement
     * @param {object} args arguments for elseif statement
     * @returns {ElseIfStatement} Else if Statement
     */
    createElseIfStatement(args) {
        const condition = this.getFactory().createBasicLiteralExpression({
            basicLiteralType: 'boolean',
            basicLiteralValue: true,
        });
        _.set(args, 'condition', condition);
        const newElseIfStatement = this.getFactory().createElseIfStatement(args);
        this.addChild(newElseIfStatement);
        return newElseIfStatement;
    }

    /**
     * Add the else statement
     * @param {ElseStatement} elseStatement else statement to add
     * @returns {void}
     */
    addElseStatement(elseStatement) {
        Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, elseStatement);
    }

    /**
     * Add else if statement
     * @param {ElseIfStatement} elseIfStatement else if statement to add
     * @param {number} index index where to be add
     * @returns {void}
     */
    addElseIfStatement(elseIfStatement, index) {
        const elseStatementIndex = _.findIndex(this.getChildren(), (node) => {
            return BallerinaASTFactory.isElseStatement(node);
        });

        Object.getPrototypeOf(this.constructor.prototype).addChild.call(
            this, elseIfStatement, index || elseStatementIndex);
    }

    /**
     * Override the addChild method for ordering the child elements
     * @param {ASTNode} child child to be added
     * @param {number} index index where to add the child
     * @param {boolean} ignoreTreeModifiedEvent whether ignore tree modified event
     * @param {boolean} ignoreChildAddedEvent whether ignore tree child added event
     * @param {boolean} generateId whether generate id
     * @returns {void}
     */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId) {
        const elseStatementIndex = _.findIndex(this.getChildren(), (node) => {
            return BallerinaASTFactory.isElseStatement(node);
        });

        if (BallerinaASTFactory.isElseIfStatement(child) && elseStatementIndex > -1) {
            index = elseStatementIndex;
        }

        Object.getPrototypeOf(this.constructor.prototype)
          .addChild.call(this, child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
    }

    /**
     * initialize IfElseStatement from json object
     * @param {Object} jsonNode to initialize from
     * @param {Object} [jsonNode.if_statement] - If statement
     * @param {Object} [jsonNode.else_statement] - Else statement
     * @param {Object} [jsonNode.else_if_statements] - Else If statements
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this.children.length = 0;
        // create if statement
        const ifStmtNode = jsonNode.if_statement;
        if (!_.isNil(ifStmtNode)) {
            const ifStatement = this.getFactory().createFromJson(ifStmtNode);
            this.addChild(ifStatement);
            ifStatement.initFromJson(ifStmtNode);
        }
        // create else statement
        const elseStmtNode = jsonNode.else_statement;
        if (!_.isNil(elseStmtNode)) {
            const elseStatement = this.getFactory().createFromJson(elseStmtNode);
            this.addChild(elseStatement);
            elseStatement.initFromJson(elseStmtNode);
        }

        // create else if statements
        if (!_.isNil(jsonNode.else_if_statements) && _.isArray(jsonNode.else_if_statements)) {
            _.each(jsonNode.else_if_statements, (elseIfStmtNode) => {
                if (!_.isNil(elseIfStmtNode)) {
                    const elseIfStatement = this.getFactory().createFromJson(elseIfStmtNode);
                    this.addChild(elseIfStatement);
                    elseIfStatement.initFromJson(elseIfStmtNode);
                }
            });
        }
    }
}

export default IfElseStatement;
