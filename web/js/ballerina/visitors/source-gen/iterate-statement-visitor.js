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
define(['lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor'], function(_, log, EventChannel, AbstractStatementSourceGenVisitor) {

    var IterateStatementVisitor = function(){
        AbstractStatementSourceGenVisitor.call(this);
    };

    IterateStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    IterateStatementVisitor.prototype.constructor = IterateStatementVisitor;

    IterateStatementVisitor.prototype.canVisitIterateStatement = function(iterateStatement){
        return true;
    };

    IterateStatementVisitor.prototype.beginVisitIterateStatement = function(iterateStatement){
        log.debug('Begin Visit Iterate Statement Definition');
    };

    IterateStatementVisitor.prototype.visitIterateStatement = function(iterateStatement){
        log.debug('Visit Iterate Statement Definition');
    };

    IterateStatementVisitor.prototype.endVisitIterateStatement = function(iterateStatement){
        log.debug('End Visit Iterate Statement Definition');
    };

    return IterateStatementVisitor;
});