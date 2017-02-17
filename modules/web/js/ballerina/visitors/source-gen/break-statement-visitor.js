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
define(['lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/break-statement',
        './expression-visitor-factory'],
    function(_, log, EventChannel, AbstractStatementSourceGenVisitor, BreakStatement, ExpressionVisitorFactory) {

        var BreakStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this, parent);
        };

        BreakStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        BreakStatementVisitor.prototype.constructor = BreakStatementVisitor;

        BreakStatementVisitor.prototype.canVisitBreakStatement = function(breakStatement){
            return breakStatement instanceof BreakStatement;
        };

        BreakStatementVisitor.prototype.beginVisitBreakStatement = function(breakStatement){
            /**
             * set the configuration start for the reply statement definition language construct
             * If we need to add additional parameters which are dynamically added to the configuration start
             * that particular source generation has to be constructed here
             */
            this.appendSource(breakStatement.getStatement());
            log.debug('Begin Visit Break Statement Definition');
        };

        BreakStatementVisitor.prototype.visitBreakStatement = function(breakStatement){
            log.debug('Visit Break Statement Definition');
        };

        BreakStatementVisitor.prototype.endVisitBreakStatement = function(breakStatement){
            this.appendSource(";\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Break Statement Definition');
        };

        return BreakStatementVisitor;
    });