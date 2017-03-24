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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor) {

        /**
         * Constructor for Function invocation expression visitor
         * @param {ASTNode} parent - parent node
         * @constructor
         */
        var ActionInvocationExpressionVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        ActionInvocationExpressionVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        ActionInvocationExpressionVisitor.prototype.constructor = ActionInvocationExpressionVisitor;

        ActionInvocationExpressionVisitor.prototype.canVisitActionInvocationExpression = function(){
            return true;
        };

        ActionInvocationExpressionVisitor.prototype.beginVisitActionInvocationExpression = function(actionInvocation){
        };

        ActionInvocationExpressionVisitor.prototype.visitActionInvocationExpression = function(actionInvocation){
            var source = actionInvocation.getExpression();
            this.appendSource(source);
        };

        ActionInvocationExpressionVisitor.prototype.endVisitActionInvocationExpression = function(){
            this.getParent().appendSource(this.getGeneratedSource());
        };

        return FunctionInvocationExpressionVisitor;
    });