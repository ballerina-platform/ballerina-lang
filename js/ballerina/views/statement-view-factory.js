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
define(['lodash', 'log', 'event_channel', '../ast/module', './try-catch-statement-view', './try-statement-view',
        './catch-statement-view', './if-else-statement-view', './if-statement-view', './else-statement-view', './else-if-statement-view', './assignment-view', './function-invocation-view','./get-action-statement-view', './while-statement-view'],
    function (_, log, EventChannel, AST, TryCatchStatementView, TryStatementView, CatchStatementView,
              IfElseStatementView, IfStatementView, ElseStatementView, ElseIfStatementView, AssignmentStatementView, FunctionInvocationStatementView, GetActionStatementView, WhileStatementView) {

        var StatementViewFactory = function () {
        };

        StatementViewFactory.prototype.getStatementView = function (args) {
            var statement  = _.get(args, "model");
            if (statement instanceof AST.TryCatchStatement) {
                return new TryCatchStatementView(args);
            } else if (statement instanceof AST.TryStatement) {
                return new TryStatementView(args);
            } else if (statement instanceof AST.CatchStatement) {
                return new CatchStatementView(args);
            } else if (statement instanceof AST.IfElseStatement) {
                return new IfElseStatementView(args);
            } else if (statement instanceof AST.IfStatement) {
                return new IfStatementView(args);
            } else if (statement instanceof AST.ElseStatement) {
                return new ElseStatementView(args);
            }  else if (statement instanceof AST.ElseIfStatement) {
                return new ElseIfStatementView(args);
            } else if (statement instanceof AST.Assignment) {
                return new AssignmentStatementView(args);
            } else if (statement instanceof AST.FunctionInvocation) {
                return new FunctionInvocationStatementView(args);
            } else if (statement instanceof AST.GetActionStatement) {
                return new GetActionStatementView(args);
            } else if (statement instanceof AST.WhileStatement) {
                return new WhileStatementView(args);
            }
        };

        StatementViewFactory.prototype.isGetActionStatement = function(statement){
            if (statement instanceof AST.GetActionStatement){
                return true;
            }
        };
        return StatementViewFactory;
    });