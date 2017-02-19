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
define(['lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor'], function(_, log, EventChannel, AbstractStatementSourceGenVisitor) {

    var CommentStatementVisitor = function(parent){
        AbstractStatementSourceGenVisitor.call(this, parent);
    };

    CommentStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    CommentStatementVisitor.prototype.constructor = CommentStatementVisitor;

    CommentStatementVisitor.prototype.canVisitCommentStatement = function(commentStatement){
        return true;
    };

    CommentStatementVisitor.prototype.beginVisitCommentStatement = function(commentStatement){
        log.debug('Begin Visit Comment Statement Definition');
    };

    CommentStatementVisitor.prototype.visitCommentStatement = function(commentStatement){
        log.debug('Visit Comment Statement Definition');
    };

    CommentStatementVisitor.prototype.endVisitCommentStatement = function(commentStatement){
        this.appendSource(commentStatement.getCommentString() + ";\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Comment Statement Definition');
    };

    return CommentStatementVisitor;
});