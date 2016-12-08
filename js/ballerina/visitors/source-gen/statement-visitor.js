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
define(['lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/module',
'./try-catch-statement-visitor', './if-else-statement-visitor'],
function (_, log, EventChannel, AbstractStatementSourceGenVisitor, AST, TryCatchStatementVisitor, IfElseStatementVisitor) {

    var StatementVisitor = function (parent) {
        AbstractStatementSourceGenVisitor.call(this, parent);
    };

    StatementVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    StatementVisitor.prototype.constructor = StatementVisitor;

    StatementVisitor.prototype.beginVisitStatement = function (statement){
        this.getParent().appendSource(this.getGeneratedSource());
    };

    StatementVisitor.prototype.visitStatement = function (statement) {
        if (statement instanceof AST.TryCatchStatement) {
            var tryCatchVisitor = new TryCatchStatementVisitor();
            statement.accept(tryCatchVisitor);
        } else if (statement instanceof AST.IfElseStatement) {
            var ifElseStatementVisitor = new IfElseStatementVisitor(this);
            statement.accept(ifElseStatementVisitor);
        }
    };

    StatementVisitor.prototype.beginVisitStatement = function (statement){
        this.getParent().appendSource(this.getGeneratedSource());
    };

    StatementVisitor.prototype.endVisitStatement = function (statement){
        this.getParent().appendSource(this.getGeneratedSource());
    };

    return StatementVisitor;
});