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
import ASTFactory from '../../ast/ballerina-ast-factory';
import TryCatchStatementVisitor from './try-catch-statement-visitor';
import TryStatementVisitor from './try-statement-visitor';
import CatchStatementVisitor from './catch-statement-visitor';
import IfElseStatementVisitor from './if-else-statement-visitor';
import IfStatementVisitor from './if-statement-visitor';
import ElseStatementVisitor from './else-statement-visitor';
import ElseIfStatementVisitor from './else-if-statement-visitor';
import WhileStatementVisitor from './while-statement-visitor';
import TypeMapperAssignmentStatementVisitor from './type-mapper-assignment-statement-visitor';
import ActionInvocationStatementVisitor from './action-invocation-statement-visitor';
import ReplyStatementVisitor from './reply-statement-visitor';
import TypeMapperReturnStatementVisitor from './type-mapper-return-statement-visitor';
import FunctionInvocationVisitor from './function-invocation-visitor';
import TypeMapperFunctionInvocationExpressionVisitor from './type-mapper-function-invocation-expression-visitor';
import TypeMapperLeftOperandExpressionVisitor from './type-mapper-left-operand-expression-visitor';
import TypeMapperRightOperandExpressionVisitor from './type-mapper-right-operand-expression-visitor';
import TypeMapperVariableDefinitionStatement from './type-mapper-variable-definition-statement-visitor';
import WorkerInvocationStatement from './worker-invocation-statement-visitor';
import WorkerReplyStatement from './worker-reply-statement-visitor';
import BreakStatementVisitor from './break-statement-visitor';
import ThrowStatementVisitor from './throw-statement-visitor';

class TypeMapperStatementVisitorFactory {
    getStatementVisitor(statement, parent) {
        if (ASTFactory.isTryCatchStatement(statement)) {
            return new TryCatchStatementVisitor(parent);
        } else if (ASTFactory.isTryStatement(statement)) {
            return new TryStatementVisitor(parent.getParent());
        } else if (ASTFactory.isCatchStatement(statement)) {
            return new CatchStatementVisitor(parent.getParent());
        } else if (ASTFactory.isIfElseStatement(statement)) {
            return new IfElseStatementVisitor(parent);
        } else if (ASTFactory.isIfStatement(statement)) {
            return new IfStatementVisitor(parent.getParent());
        } else if (ASTFactory.isElseStatement(statement)) {
            return new ElseStatementVisitor(parent.getParent());
        } else if (ASTFactory.isElseIfStatement(statement)) {
            return new ElseIfStatementVisitor(parent.getParent());
        } else if (ASTFactory.isWhileStatement(statement)) {
            return new WhileStatementVisitor(parent);
        } else if (ASTFactory.isAssignmentStatement(statement)) {
            return new TypeMapperAssignmentStatementVisitor(parent);
        } else if (ASTFactory.isReplyStatement(statement)) {
            return new ReplyStatementVisitor(parent);
        } else if (ASTFactory.isReturnStatement(statement)) {
            return new TypeMapperReturnStatementVisitor(parent);
        } else if (ASTFactory.isFunctionInvocation(statement)) {
            return new FunctionInvocationVisitor(parent);
        }else if (ASTFactory.isFunctionInvocationExpression(statement)) {
            return new TypeMapperFunctionInvocationExpressionVisitor(parent);
        } else if (ASTFactory.isLeftOperandExpression(statement)) {
            return new TypeMapperLeftOperandExpressionVisitor(parent);
        } else if (ASTFactory.isRightOperandExpression(statement)) {
            return new TypeMapperRightOperandExpressionVisitor(parent);
        } else if (ASTFactory.isVariableDefinitionStatement(statement)) {
            return new TypeMapperVariableDefinitionStatement(parent);
        } else if (ASTFactory.isWorkerInvocationStatement(statement)) {
            return new WorkerInvocationStatement(parent);
        }  else if (ASTFactory.isWorkerReplyStatement(statement)) {
            return new WorkerReplyStatement(parent);
        } else if (ASTFactory.isActionInvocationStatement(statement)) {
            return new ActionInvocationStatementVisitor(parent);
        } else if (ASTFactory.isBreakStatement(statement)) {
            return new BreakStatementVisitor(parent);
        } else if (ASTFactory.isThrowStatement(statement)) {
            return new ThrowStatementVisitor(parent);
        }
    }
}

export default TypeMapperStatementVisitorFactory;
