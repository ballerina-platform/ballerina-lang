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
define(['lodash', 'log', 'event_channel', '../../ast/module',
'./try-catch-statement-visitor',
'./try-statement-visitor',
'./catch-statement-visitor',
'./if-else-statement-visitor',
'./if-statement-visitor',
'./else-statement-visitor',
'./else-if-statement-visitor',
'./while-statement-visitor',
'./assignment-statement-visitor',
'./action-invocation-expression-visitor',
'./reply-statement-visitor',
'./logical-expression-visitor',
'./arithmetic-expression-visitor',
'./function-invocation-visitor'],
function (_, log, EventChannel, AST,
TryCatchStatementVisitor, TryStatementVisitor, CatchStatementVisitor, IfElseStatementVisitor, IfStatementVisitor,
ElseStatementVisitor, ElseIfStatementVisitor, WhileStatementVisitor, AssignmentStatementVisitor, ActionInvocationStatement,
ReplyStatementVisitor,LogicalExpressionVisitor, AssignmentExpressionVisitor, FunctionInvocationVisitor) {

    var StatementVisitorFactor = function () {
    };

    StatementVisitorFactor.prototype.getStatementVisitor = function (statement, parent) {
        if (statement instanceof AST.TryCatchStatement) {
            return new TryCatchStatementVisitor(parent);
        } else if (statement instanceof AST.TryStatement) {
            return new TryStatementVisitor(parent.getParent());
        } else if (statement instanceof AST.CatchStatement) {
            return new CatchStatementVisitor(parent.getParent());
        } else if (statement instanceof AST.IfElseStatement) {
            return new IfElseStatementVisitor(parent);
        } else if (statement instanceof AST.IfStatement) {
            return new IfStatementVisitor(parent.getParent());
        } else if (statement instanceof AST.ElseStatement) {
            return new ElseStatementVisitor(parent.getParent());
        } else if (statement instanceof AST.ElseIfStatement) {
            return new ElseIfStatementVisitor(parent.getParent());
        } else if (statement instanceof AST.WhileStatement) {
            return new WhileStatementVisitor(parent);
        } else if (statement instanceof AST.Assignment) {
            return new AssignmentStatementVisitor(parent);
        } else if (statement instanceof AST.ActionInvocationStatement) {
            return new ActionInvocationStatement(parent);
        } else if (statement instanceof AST.ReplyStatement) {
            return new ReplyStatementVisitor(parent);
        } else if (statement instanceof AST.LogicalExpression) {
            return new LogicalExpressionVisitor(parent);
        } else if (statement instanceof AST.ArithmeticExpression) {
            return new AssignmentExpressionVisitor(parent);
        }  else if (statement instanceof AST.FunctionInvocation) {
            return new FunctionInvocationVisitor(parent);
        }
    };

    return StatementVisitorFactor;
});