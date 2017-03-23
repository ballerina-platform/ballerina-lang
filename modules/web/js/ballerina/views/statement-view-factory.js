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
import AST from '../ast/module';
import TryCatchStatementView from './try-catch-statement-view';
import TryStatementView from './try-statement-view';
import CatchStatementView from './catch-statement-view';
import IfElseStatementView from './if-else-statement-view';
import IfStatementView from './if-statement-view';
import ElseStatementView from './else-statement-view';
import ElseIfStatementView from './else-if-statement-view';
import AssignmentStatementView from './assignment-view';
import FunctionInvocationStatementView from './function-invocation-view';
import ActionInvocationStatementView from './action-invocation-statement-view';
import WhileStatementView from './while-statement-view';
import ReplyStatementView from './reply-statement-view';
import ReturnStatement from './return-statement-view';
import VariableDefinitionStatementView from './variable-definition-statement-view';
import WorkerInvocationView from './worker-invocation-view';
import WorkerReplyStatementView from './worker-reply-statement-view';
import BreakStatementView from './break-statement-view';
import ThrowStatementView from './throw-statement-view';

class StatementViewFactory {
    getStatementView(args) {
        var statement  = _.get(args, 'model');
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
        } else if (statement instanceof AST.WhileStatement) {
            return new WhileStatementView(args);
        } else if (statement instanceof AST.ActionInvocationStatement) {
            return new ActionInvocationStatementView(args);
        } else if (statement instanceof AST.ReplyStatement) {
            return new ReplyStatementView(args);
        } else if (statement instanceof AST.ReturnStatement) {
            return new ReturnStatement(args);
        } else if (statement instanceof AST.BreakStatement) {
            return new BreakStatementView(args);
        } else if (statement instanceof AST.AssignmentStatement){
            // TODO : This logic needs to be refactored.
            var children  = _.get(statement, 'children');
            var assignmentStatement = undefined;
            _.each(children, function (statementChild) {
                if(AST.BallerinaASTFactory.isRightOperandExpression(statementChild)) {
                    var operands  = _.get(statementChild, 'children');
                    _.each(operands, function (child) {
                        if (AST.BallerinaASTFactory.isActionInvocationExpression(child)) {
                            _.set(args, 'model', statement);
                            assignmentStatement = new ActionInvocationStatementView(args);
                        }
                    });
                }
            });
            if (_.isUndefined(assignmentStatement)) {
                _.set(args, 'model', statement);
                //If Operand type exist for the statement add it to the left operand expression.
                var leftOperandExpression = children[0].getOperandType() ?
                    children[0].getOperandType() + ' ' + children[0].getLeftOperandExpressionString() :
                    children[0].getLeftOperandExpressionString();

                _.get(args, 'model').setStatementString(leftOperandExpression + ' = '
                    + children[1].getRightOperandExpressionString());
                assignmentStatement = new AssignmentStatementView(args);
            }
            return assignmentStatement;
        } else if (statement instanceof AST.VariableDefinitionStatement) {
            var variableStatement = undefined;
            _.each(statement.getChildren(), function (statementChild) {
                if(AST.BallerinaASTFactory.isActionInvocationExpression(statementChild)) {
                    variableStatement = new ActionInvocationStatementView(args);
                }
            });
            if (_.isUndefined(variableStatement)) {
                variableStatement = new VariableDefinitionStatementView(args);
            }
            return variableStatement;
        } else if (statement instanceof AST.WorkerInvocationStatement) {
            return new WorkerInvocationView(args);
        } else if (statement instanceof AST.WorkerReplyStatement) {
            return new WorkerReplyStatementView(args);
        } else if (statement instanceof AST.ThrowStatement) {
            return new ThrowStatementView(args);
        }
    }
}

export default StatementViewFactory;
    
