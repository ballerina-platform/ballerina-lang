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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/worker-invoke'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor, WorkerInvoke) {

        var WorkerInvokeVisitor = function(parent){
            AbstractStatementSourceGenVisitor.call(this,parent);
        };

        WorkerInvokeVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        WorkerInvokeVisitor.prototype.constructor = WorkerInvokeVisitor;

        WorkerInvokeVisitor.prototype.canVisitWorkerInvoke = function(workerInvoke){
            return workerInvoke instanceof WorkerInvoke;
        };

        WorkerInvokeVisitor.prototype.beginVisitWorkerInvoke = function(workerInvoke){
            this.appendSource(workerInvoke.getInvokeStatement());
            log.debug('Begin Visit Worker Invoke Statement');
        };

        WorkerInvokeVisitor.prototype.endVisitWorkerInvoke = function(workerInvoke){
            this.getParent().appendSource(this.getGeneratedSource() + ";\n");
            log.debug('End Visit Worker Invoke Statement');
        };

        return WorkerInvokeVisitor;
    });
