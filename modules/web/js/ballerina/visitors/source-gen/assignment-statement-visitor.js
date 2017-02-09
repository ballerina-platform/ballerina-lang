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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/assignment-statement'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, AssignmentStatement) {

        var AssignmentStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        AssignmentStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        AssignmentStatementVisitor.prototype.constructor = AssignmentStatementVisitor;

        AssignmentStatementVisitor.prototype.canVisitAssignmentStatement = function(assignmentStatement){
            return assignmentStatement instanceof AssignmentStatement;
        };

        AssignmentStatementVisitor.prototype.beginVisitAssignmentStatement = function(assignmentStatement){
            log.debug('Begin Visit Assignment Statement');
        };

        AssignmentStatementVisitor.prototype.visitLeftOperandExpression = function(expression){
            var StatementVisitorFactory = require('./statement-visitor-factory');
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(expression, this);
            expression.accept(statementVisitor);
        };

        AssignmentStatementVisitor.prototype.visitRightOperandExpression = function(expression){
            var StatementVisitorFactory = require('./statement-visitor-factory');
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(expression, this);
            expression.accept(statementVisitor);
        };

        AssignmentStatementVisitor.prototype.endVisitAssignmentStatement = function(assignmentStatement){
            this.getParent().appendSource(this.getGeneratedSource() + ";\n");
            log.debug('End Visit Assignment Statement');
        };

        return AssignmentStatementVisitor;
    });