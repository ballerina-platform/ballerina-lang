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

    var IfStatementVisitor = function(parent){
        AbstractStatementSourceGenVisitor.call(this,parent);
    };

    IfStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    IfStatementVisitor.prototype.constructor = IfStatementVisitor;

    IfStatementVisitor.prototype.canVisitIfStatement = function(ifStatement){
        return true;
    };

    IfStatementVisitor.prototype.beginVisitIfStatement = function(ifStatement){
        this.appendSource('if(' + ifStatement.getCondition() + '){');
        log.debug('Begin Visit If Statement Definition');
    };

    IfStatementVisitor.prototype.visitIfStatement = function(ifStatement){
        log.debug('Visit If Statement Definition');
    };

    IfStatementVisitor.prototype.endVisitIfStatement = function(ifStatement){
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit If Statement Definition');
    };

    IfStatementVisitor.prototype.visitStatement = function (statement) {
        var StatementVisitorFactory = require('./statement-visitor-factory');
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    };

    return IfStatementVisitor;
});