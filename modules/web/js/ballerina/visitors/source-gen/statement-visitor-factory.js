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
import _ from 'lodash';
import log from 'log';
import EventChannel from 'event_channel';
import AST from '../../ast/module';
import TryCatchStatementVisitor from './try-catch-statement-visitor';
import TryStatementVisitor from './try-statement-visitor';
import CatchStatementVisitor from './catch-statement-visitor';
import IfElseStatementVisitor from './if-else-statement-visitor';
import IfStatementVisitor from './if-statement-visitor';
import ElseStatementVisitor from './else-statement-visitor';
import ElseIfStatementVisitor from './else-if-statement-visitor';
import WhileStatementVisitor from './while-statement-visitor';
import AssignmentStatementVisitor from './assignment-statement-visitor';
import ActionInvocationStatementVisitor from './action-invocation-statement-visitor';
import ReplyStatementVisitor from './reply-statement-visitor';
import ReturnStatementVisitor from './return-statement-visitor';
import FunctionInvocationVisitor from './function-invocation-visitor';
import FunctionInvocationExpressionVisitor from './function-invocation-expression-visitor';
import AssignmentVisitor from './assignment-visitor';
import LeftOperandExpressionVisitor from './left-operand-expression-visitor';
import RightOperandExpressionVisitor from './right-operand-expression-visitor';
import VariableDefinitionStatement from './variable-definition-statement-visitor';
import WorkerInvocationStatementVisitor from './worker-invocation-statement-visitor';
import WorkerReplyStatementVisitor from './worker-reply-statement-visitor';
import BreakStatementVisitor from './break-statement-visitor';
import ThrowStatementVisitor from './throw-statement-visitor';
import CommentStatementVisitor from './comment-statement-visitor';

class StatementVisitorFactor {
    getStatementVisitor(statement, parent) {
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
            return new AssignmentStatementVisitor(parent);
        } else if (statement instanceof AST.ReplyStatement) {
            return new ReplyStatementVisitor(parent);
        } else if (statement instanceof AST.ReturnStatement) {
            return new ReturnStatementVisitor(parent);
        } else if (statement instanceof AST.FunctionInvocation) {
            return new FunctionInvocationVisitor(parent);
        } else if(statement instanceof AST.Assignment){
            return new AssignmentVisitor(parent);
        } else if (statement instanceof AST.LeftOperandExpression) {
            return new LeftOperandExpressionVisitor(parent);
        } else if (statement instanceof AST.RightOperandExpression) {
            return new RightOperandExpressionVisitor(parent);
        } else if (statement instanceof AST.VariableDefinitionStatement) {
            return new VariableDefinitionStatement(parent);
        } else if (statement instanceof AST.WorkerInvocationStatement) {
            return new WorkerInvocationStatementVisitor(parent);
        }  else if (statement instanceof AST.WorkerReplyStatement) {
            return new WorkerReplyStatementVisitor(parent);
        } else if (statement instanceof AST.ActionInvocationStatement) {
            return new ActionInvocationStatementVisitor(parent);
        } else if (statement instanceof AST.BreakStatement) {
            return new BreakStatementVisitor(parent);
        } else if (statement instanceof AST.ThrowStatement) {
            return new ThrowStatementVisitor(parent);
        } else if (statement instanceof AST.CommentStatement) {
            return new CommentStatementVisitor(parent);
        }
    }
}

export default StatementVisitorFactor;

