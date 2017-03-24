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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/statements/worker-reply-statement'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, WorkerReplyStatement) {

        var WorkerReplyStatementVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        WorkerReplyStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        WorkerReplyStatementVisitor.prototype.constructor = WorkerReplyStatementVisitor;

        WorkerReplyStatementVisitor.prototype.canVisitWorkerReplyStatement = function(workerReplyStatement){
            return workerReplyStatement instanceof WorkerReplyStatement;
        };

        WorkerReplyStatementVisitor.prototype.beginVisitWorkerReplyStatement = function(workerReplyStatement){
            this.appendSource(workerReplyStatement.getReplyStatement());
            log.debug('Begin Visit Worker Receive Statement');
        };

        WorkerReplyStatementVisitor.prototype.endVisitWorkerReplyStatement = function(workerReplyStatement){
            this.getParent().appendSource(this.getGeneratedSource() + ";\n");
            log.debug('End Visit Worker Receive Statement');
        };

        return WorkerReplyStatementVisitor;
    });
