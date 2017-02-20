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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/function-invocation'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, FunctionInvocation) {

        var FunctionInvocationVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        FunctionInvocationVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        FunctionInvocationVisitor.prototype.constructor = FunctionInvocationVisitor;

        FunctionInvocationVisitor.prototype.canVisitFuncInvocationStatement = function(functionInvocation){
            return true;
        };

        FunctionInvocationVisitor.prototype.visitFuncInvocationExpression = function (functionInvocation) {
            this.appendSource(functionInvocation.getFunctionalExpression());
        };

        FunctionInvocationVisitor.prototype.endVisitFuncInvocationStatement = function(functionInvocation){
            this.appendSource(";\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Function Invocation Statement');
        };

        return FunctionInvocationVisitor;
    });