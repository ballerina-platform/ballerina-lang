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

        var GetActionStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        GetActionStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        GetActionStatementVisitor.prototype.constructor = GetActionStatementVisitor;

        GetActionStatementVisitor.prototype.canVisitGetActionStatement = function(getAction){
            return true;
        };

        GetActionStatementVisitor.prototype.beginVisitGetActionStatement = function(getAction){
            var self = getAction;
            this.appendSource(self._variableAccessor + "=" + self._connector._connectorType + "." +self._action +
                "(" +self._connector._connectorName +",\"" +self._path +"\"," +self._message +")");
            log.debug('Begin Visit get action Statement Definition');
        };

        GetActionStatementVisitor.prototype.visitGetActionStatement = function(getAction){
            log.debug('Visit get action Statement Definition');
        };

        GetActionStatementVisitor.prototype.endVisitGetActionStatement = function(getAction){
            this.appendSource(";\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.info('End Visit get action Statement Definition');
        };

        return GetActionStatementVisitor;
    });