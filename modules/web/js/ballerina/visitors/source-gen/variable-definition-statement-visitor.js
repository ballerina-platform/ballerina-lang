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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/variable-definition-statement'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, VariableDefinitionStatement) {

        var VariableDefinitionStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        VariableDefinitionStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        VariableDefinitionStatementVisitor.prototype.constructor = VariableDefinitionStatementVisitor;

        VariableDefinitionStatementVisitor.prototype.canVisitVariableDefinitionStatement = function(variableDefinitionStatement){
            return variableDefinitionStatement instanceof VariableDefinitionStatement;
        };

        VariableDefinitionStatementVisitor.prototype.beginVisitVariableDefinitionStatement = function(variableDefinitionStatement){
            var constructedSource = variableDefinitionStatement.getStatementString();
            this.appendSource(constructedSource);
            log.debug('Begin Visit Variable Definition Statement');
        };

        VariableDefinitionStatementVisitor.prototype.endVisitVariableDefinitionStatement = function(variableDefinitionStatement){
            this.getParent().appendSource(this.getGeneratedSource() + ";\n");
            log.debug('End Visit Variable Definition Statement');
        };
        return VariableDefinitionStatementVisitor;
    });