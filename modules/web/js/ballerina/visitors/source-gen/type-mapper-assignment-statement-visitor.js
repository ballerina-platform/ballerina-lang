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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/assignment-statement'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, AssignmentStatement) {

        var TypeMapperAssignmentStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        TypeMapperAssignmentStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        TypeMapperAssignmentStatementVisitor.prototype.constructor = TypeMapperAssignmentStatementVisitor;

        TypeMapperAssignmentStatementVisitor.prototype.canVisitAssignmentStatement = function(assignmentStatement){
            return assignmentStatement instanceof AssignmentStatement;
        };

        TypeMapperAssignmentStatementVisitor.prototype.beginVisitAssignmentStatement = function(assignmentStatement){
            log.debug('Begin Visit Type Mapper Assignment Statement');
        };

        TypeMapperAssignmentStatementVisitor.prototype.visitLeftOperandExpression = function(expression){
            var TypeMapperStatementVisitorFactory = require('./type-mapper-statement-visitor-factory');
            var statementVisitorFactory = new TypeMapperStatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(expression, this);
            expression.accept(statementVisitor);
        };

        TypeMapperAssignmentStatementVisitor.prototype.visitRightOperandExpression = function(expression){
            var TypeMapperStatementVisitorFactory = require('./type-mapper-statement-visitor-factory');
            var statementVisitorFactory = new TypeMapperStatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(expression, this);
            expression.accept(statementVisitor);
        };

        TypeMapperAssignmentStatementVisitor.prototype.endVisitAssignmentStatement = function(assignmentStatement){
            this.getParent().appendSource(this.getGeneratedSource() + ";\n");
            log.debug('End Visit Type Mapper Assignment Statement');
        };

        return TypeMapperAssignmentStatementVisitor;
    });