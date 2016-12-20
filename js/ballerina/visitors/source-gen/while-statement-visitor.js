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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor'],
function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor) {

    var WhileStatementVisitor = function(parent){
        AbstractStatementSourceGenVisitor.call(this,parent);
    };

    WhileStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    WhileStatementVisitor.prototype.constructor = WhileStatementVisitor;

    WhileStatementVisitor.prototype.canVisitStatement = function(whileStatement){
        return true;
    };

    WhileStatementVisitor.prototype.beginVisitStatement = function(whileStatement){
        this.appendSource('While(' + whileStatement.getCondition() + '){');
        log.info('Begin Visit If Statement Definition');
    };

    WhileStatementVisitor.prototype.visitWhileStatement = function(whileStatement){
        log.info('Visit If Statement Definition');
    };

    WhileStatementVisitor.prototype.endVisitStatement = function(whileStatement){
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.info('End Visit If Statement Definition');
    };

    WhileStatementVisitor.prototype.visitStatement = function (statement) {
        var StatementVisitorFactory = require('./statement-visitor-factory');
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    };

    return WhileStatementVisitor;
});