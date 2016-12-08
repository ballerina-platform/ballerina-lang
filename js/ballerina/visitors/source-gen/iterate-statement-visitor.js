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
define(['lodash', 'log', 'event_channel', './statement-definition-visitor'], function(_, log, EventChannel, StatementDefinitionVisitor) {

    var IterateStatementVisitor = function(){
        StatementDefinitionVisitor.call(this);
    };

    IterateStatementVisitor.prototype = Object.create(StatementDefinitionVisitor.prototype);
    IterateStatementVisitor.prototype.constructor = IterateStatementVisitor;

    IterateStatementVisitor.prototype.canVisitStatementDefinition = function(iterateStatement){
        return true;
    };

    IterateStatementVisitor.prototype.beginVisitStatementDefinition = function(iterateStatement){
        log.info('Begin Visit Iterate Statement Definition');
    };

    IterateStatementVisitor.prototype.visitStatementDefinition = function(iterateStatement){
        log.info('Visit Iterate Statement Definition');
    };

    IterateStatementVisitor.prototype.endVisitStatementDefinition = function(iterateStatement){
        log.info('End Visit Iterate Statement Definition');
    };

    return IterateStatementVisitor;
});