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

    var ThrowStatementVisitor = function(parent){
        AbstractStatementSourceGenVisitor.call(this, parent);
    };

    ThrowStatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    ThrowStatementVisitor.prototype.constructor = ThrowStatementVisitor;

    ThrowStatementVisitor.prototype.canVisitThrowStatement = function(throwStatement){
        return true;
    };

    ThrowStatementVisitor.prototype.beginVisitThrowStatement = function(throwStatement){
        this.appendSource('throw ');
        log.debug('Begin Visit Throw Statement Definition');
    };

    ThrowStatementVisitor.prototype.visitThrowStatement = function(throwStatement){
        log.debug('Visit Throw Statement Definition');
    };

    ThrowStatementVisitor.prototype.endVisitThrowStatement = function(throwStatement){
        this.appendSource(throwStatement.getChildren()[0].getExpression() + ";\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Throw Statement Definition');
    };

    return ThrowStatementVisitor;
});