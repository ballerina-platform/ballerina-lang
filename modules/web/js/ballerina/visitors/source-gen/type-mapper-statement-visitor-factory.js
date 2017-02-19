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
define(['lodash', 'log', 'event_channel', '../../ast/module', './try-catch-statement-visitor',
        './try-statement-visitor', './catch-statement-visitor', './if-else-statement-visitor', './if-statement-visitor',
        './else-statement-visitor', './else-if-statement-visitor', './while-statement-visitor',
        './type-mapper-assignment-statement-visitor', './action-invocation-statement-visitor', './reply-statement-visitor',
        './logical-expression-visitor', './arithmetic-expression-visitor', './type-mapper-return-statement-visitor',
        './function-invocation-visitor', './type-mapper-function-invocation-expression-visitor', './assignment-visitor',
        './type-mapper-left-operand-expression-visitor', './type-mapper-right-operand-expression-visitor',
        './type-mapper-variable-definition-statement-visitor', './worker-invoke-visitor', './worker-receive-visitor',
        './break-statement-visitor', './throw-statement-visitor'],
function (_, log, EventChannel, AST, TryCatchStatementVisitor,
          TryStatementVisitor, CatchStatementVisitor, IfElseStatementVisitor, IfStatementVisitor,
          ElseStatementVisitor, ElseIfStatementVisitor, WhileStatementVisitor,
          TypeMapperAssignmentStatementVisitor, ActionInvocationStatementVisitor, ReplyStatementVisitor,
          LogicalExpressionVisitor, ArithmeticExpression, TypeMapperReturnStatementVisitor,
          FunctionInvocationVisitor, TypeMapperFunctionInvocationExpressionVisitor, AssignmentVisitor,
          TypeMapperLeftOperandExpressionVisitor, TypeMapperRightOperandExpressionVisitor,
          TypeMapperVariableDefinitionStatement, WorkerInvoke, WorkerReceive,
          BreakStatementVisitor, ThrowStatementVisitor) {

    var TypeMapperStatementVisitorFactory = function () {
    };

    TypeMapperStatementVisitorFactory.prototype.getStatementVisitor = function (statement, parent) {
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
        } else if (statement instanceof AST.AssignmentStatement) {
            return new TypeMapperAssignmentStatementVisitor(parent);
        } else if (statement instanceof AST.ReplyStatement) {
            return new ReplyStatementVisitor(parent);
        } else if (statement instanceof AST.ReturnStatement) {
            return new TypeMapperReturnStatementVisitor(parent);
        } else if (statement instanceof AST.LogicalExpression) {
            return new LogicalExpressionVisitor(parent);
        } else if (statement instanceof AST.FunctionInvocation) {
            return new FunctionInvocationVisitor(parent);
        }else if (statement instanceof AST.FunctionInvocationExpression) {
            return new TypeMapperFunctionInvocationExpressionVisitor(parent);
        } else if(statement instanceof AST.Assignment){
            return new AssignmentVisitor(parent);
        } else if (statement instanceof AST.LeftOperandExpression) {
            return new TypeMapperLeftOperandExpressionVisitor(parent);
        } else if (statement instanceof AST.RightOperandExpression) {
            return new TypeMapperRightOperandExpressionVisitor(parent);
        } else if (statement instanceof AST.VariableDefinitionStatement) {
            return new TypeMapperVariableDefinitionStatement(parent);
        } else if (statement instanceof AST.WorkerInvoke) {
            return new WorkerInvoke(parent);
        }  else if (statement instanceof AST.WorkerReceive) {
            return new WorkerReceive(parent);
        } else if (statement instanceof AST.ActionInvocationStatement) {
            return new ActionInvocationStatementVisitor(parent);
        } else if (statement instanceof AST.BreakStatement) {
            return new BreakStatementVisitor(parent);
        } else if (statement instanceof AST.ThrowStatement) {
            return new ThrowStatementVisitor(parent);
        }
    };

    return TypeMapperStatementVisitorFactory;
});
