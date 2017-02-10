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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/action-invocation-expression'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, ActionInvocation) {

        var ActionInvocationStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        ActionInvocationStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        ActionInvocationStatementVisitor.prototype.constructor = ActionInvocationStatementVisitor;

        ActionInvocationStatementVisitor.prototype.canVisitActionInvocationExpression = function(actionInvocationExpression){
            return true;
        };

        ActionInvocationStatementVisitor.prototype.beginVisitActionInvocationExpression = function(actionInvocationExpression){
            this.appendSource(actionInvocationExpression.getExpression());
            log.debug('Begin Visit action invocation expression');
        };

        ActionInvocationStatementVisitor.prototype.endVisitActionInvocationExpression = function(action){
            this.getParent().appendSource(this.getGeneratedSource() + ";");
            log.debug('End Visit action Invocation Expression');
        };

        return ActionInvocationStatementVisitor;
    });