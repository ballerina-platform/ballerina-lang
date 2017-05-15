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
import AST from '../../ast/module';
import FunctionInvocationVisitor from './function-invocation-visitor';
import TransformAssignmentStatementVisitor from './transform-assignment-statement-visitor';
import TypeMapperFunctionInvocationExpressionVisitor from './type-mapper-function-invocation-expression-visitor';
import TypeMapperLeftOperandExpressionVisitor from './type-mapper-left-operand-expression-visitor';
import TypeMapperRightOperandExpressionVisitor from './type-mapper-right-operand-expression-visitor';
import TypeMapperVariableDefinitionStatement from './type-mapper-variable-definition-statement-visitor';

class TransformStatementVisitorFactory {
    getStatementVisitor(statement, parent) {
        if (statement instanceof AST.AssignmentStatement) {
            return new TransformAssignmentStatementVisitor(parent);
        } else if (statement instanceof AST.FunctionInvocation) {
            return new FunctionInvocationVisitor(parent);
        }else if (statement instanceof AST.FunctionInvocationExpression) {
            return new TypeMapperFunctionInvocationExpressionVisitor(parent);
        } else if (statement instanceof AST.LeftOperandExpression) {
            return new TypeMapperLeftOperandExpressionVisitor(parent);
        } else if (statement instanceof AST.RightOperandExpression) {
            return new TypeMapperRightOperandExpressionVisitor(parent);
        } else if (statement instanceof AST.VariableDefinitionStatement) {
            return new TypeMapperVariableDefinitionStatement(parent);
        }
    }
}

export default TransformStatementVisitorFactory;

