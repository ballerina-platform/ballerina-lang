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
define(['require','lodash', 'log', 'event_channel', './abstract-source-gen-visitor', '../../ast/module'],
    function(require, _, log, EventChannel, AbstractSourceGenVisitor, AST) {

        var VariableDeclarationVisitor = function(parent){
            AbstractSourceGenVisitor.call(this,parent);
        };

        VariableDeclarationVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        VariableDeclarationVisitor.prototype.constructor = VariableDeclarationVisitor;

        VariableDeclarationVisitor.prototype.canVisitVariableDeclaration = function (variableDeclaration) {
            return true;
        };

        VariableDeclarationVisitor.prototype.beginVisitVariableDeclaration = function (variableDeclaration) {
            this.appendSource(variableDeclaration.getType() + " " +variableDeclaration.getIdentifier());
            log.debug('Begin Visit Variable Declaration');
        };

        VariableDeclarationVisitor.prototype.visitVariableDeclaration = function (variableDeclaration) {
            log.debug('Visit Variable Declaration');
        };

        VariableDeclarationVisitor.prototype.endVisitVariableDeclaration = function (variableDeclaration) {
            this.appendSource(";\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Variable Declaration');
        };
        
        return VariableDeclarationVisitor;
    });