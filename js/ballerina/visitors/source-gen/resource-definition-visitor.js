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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor'],
    function(_, log, EventChannel, AbstractSourceGenVisitor) {

        /**
         * @param parent
         * @constructor
         */
        var ResourceDefinitionVisitor = function (parent) {
            AbstractSourceGenVisitor.call(this, parent);
        };

        ResourceDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        ResourceDefinitionVisitor.prototype.constructor = ResourceDefinitionVisitor;

        ResourceDefinitionVisitor.prototype.canVisitResourceDefinition = function(resourceDefinition){
            return true;
        };

        ResourceDefinitionVisitor.prototype.beginVisitResourceDefinition = function(resourceDefinition){
            /**
             * set the configuration start for the resource definition language construct
             * If we need to add additional parameters which are dynamically added to the configuration start
             * that particular source generation has to be constructed here
             */
            this.appendSource(resourceDefinition.getConfigStart());
            log.info('Begin Visit ResourceDefinition');
        };

        ResourceDefinitionVisitor.prototype.visitResourceDefinition = function(resourceDefinition){
            log.info('Visit ResourceDefinition');
        };

        ResourceDefinitionVisitor.prototype.endVisitResourceDefinition = function(resourceDefinition){
            this.appendSource(resourceDefinition.getConfigEnd() + "\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.info('End Visit ResourceDefinition');
        };

        ResourceDefinitionVisitor.prototype.visitStatement = function(statementDefinition){
            var statementDefinitionVisitor;
            //routing to correct statement type
            if(statementDefinition instanceof IfStatement){
                statementDefinitionVisitor = new IfStatementVisitor();
            } else if(statementDefinition instanceof WhileStatement){
                statementDefinitionVisitor = new IterateStatementVisitor();
            } else if(statementDefinition instanceof TryCatchStatement){
                statementDefinitionVisitor = new TryCatchStatementVisitor();
            } else if(statementDefinition instanceof ReplyStatement){
                statementDefinitionVisitor = new ReplyStatementVisitor();
            }
            statementDefinition.accept(statementDefinitionVisitor);
        };

        return ResourceDefinitionVisitor;
    });