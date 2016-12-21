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

        var ActionInvocationExpressionVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        ActionInvocationExpressionVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        ActionInvocationExpressionVisitor.prototype.constructor = ActionInvocationExpressionVisitor;

        ActionInvocationExpressionVisitor.prototype.canVisitStatement = function(action){
            return action instanceof ActionInvocation;
        };

        ActionInvocationExpressionVisitor.prototype.beginVisitStatement = function(action){
            var self = action;
            // TODO: till the model of the connector setting is done through the invocation arrow draw, connector type & connectorName is set to default
            // this.appendSource(self._variableAccessor + "=" + self._connector._connectorType + "." +self._action +
            //     "(" +self._connector._connectorName +",\"" +self._path +"\"," +self._message +")");
            this.appendSource(self._variableAccessor + "=http:HttpConnector." +self._action +
                "(" + 'nyseEP' +",\"" +self._path +"\"," +self._message +")");
            log.debug('Begin Visit action invocation expression');
        };

        ActionInvocationExpressionVisitor.prototype.endVisitStatement = function(action){
            this.appendSource(";\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.info('End Visit action Invocation Expression');
        };

        return ActionInvocationExpressionVisitor;
    });