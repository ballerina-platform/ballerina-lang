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

    var TryStatementVisitor = function (parent) {
        AbstractStatementSourceGenVisitor.call(this, parent);
    };

    TryStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    TryStatementVisitor.prototype.constructor = TryStatementVisitor;

    TryStatementVisitor.prototype.canVisitTryStatement = function (tryStatement) {
        return true;
    };

    TryStatementVisitor.prototype.beginVisitTryStatement = function (tryStatement) {
        /**
         * set the configuration start for the try statement
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        this.appendSource('try {');
        log.debug('Begin Visit Try Statement');
    };

    TryStatementVisitor.prototype.visitTryStatement = function (tryStatement) {
        log.debug('Visit Try Statement');
    };

    TryStatementVisitor.prototype.endVisitTryStatement = function (tryStatement) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Try Statement');
    };

    TryStatementVisitor.prototype.visitStatement = function(statement){
        var StatementVisitorFactory = require('./statement-visitor-factory');
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    };

    return TryStatementVisitor;
});