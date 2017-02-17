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
define(['require','lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './statement-visitor-factory'],
    function(require, _, log, EventChannel, AbstractSourceGenVisitor, StatementVisitorFactory) {

        var BlockStatementVisitor = function(parent){
            AbstractSourceGenVisitor.call(this, parent);
        };

        BlockStatementVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        BlockStatementVisitor.prototype.constructor = BlockStatementVisitor;

        BlockStatementVisitor.prototype.canVisitBlockStatement = function(blockStatement){
            return true;
        };

        BlockStatementVisitor.prototype.beginBlockStatement = function(blockStatement){
            log.debug('Begin Visit Block Statement Definition');
        };

        BlockStatementVisitor.prototype.visitBlockStatement = function(blockStatement){
            log.debug('Visit Block Statement Definition');
        };

        BlockStatementVisitor.prototype.endVisitBlockStatement = function(blockStatement){
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Block Statement Definition');
        };

        BlockStatementVisitor.prototype.visitStatement = function (statement) {
           var statementVisitorFactory = new StatementVisitorFactory();
           var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
           statement.accept(statementVisitor);
       };

        return BlockStatementVisitor;
    });