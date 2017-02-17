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
define(['require', 'lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor'], function (require, _, log, EventChannel, AbstractStatementSourceGenVisitor) {

    var CatchStatementVisitor = function (parent) {
        AbstractStatementSourceGenVisitor.call(this, parent);
    };

    CatchStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    CatchStatementVisitor.prototype.constructor = CatchStatementVisitor;

    CatchStatementVisitor.prototype.canVisitCatchStatement = function (catchStatement) {
        return true;
    };

    CatchStatementVisitor.prototype.beginVisitCatchStatement = function (catchStatement) {
        /**
         * set the configuration start for the catch statement
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        this.appendSource('catch('+ catchStatement.getParameter() + '){');
        log.debug('Begin Visit Catch Statement');
    };

    CatchStatementVisitor.prototype.visitCatchStatement = function (catchStatement) {
        log.debug('Visit Catch Statement');
    };

    CatchStatementVisitor.prototype.endVisitCatchStatement = function (catchStatement) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Catch Statement');
    };

    CatchStatementVisitor.prototype.visitStatement = function (statement) {
        var StatementVisitorFactory = require('./statement-visitor-factory');
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    };

    return CatchStatementVisitor;
});