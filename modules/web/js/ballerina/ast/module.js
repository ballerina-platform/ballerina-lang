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
define(['./ballerina-ast-factory', './ballerina-ast-root', './conditional-statement', './connector-declaration',
        './connector-definition', './constant-definition', './expression', './function-definition',
        './if-else-statement', './if-statement', './else-statement', './else-if-statement', './trycatch-statement',
        './try-statement', './catch-statement', './node', './reply-statement', './resource-definition',
        './return-statement', './service-definition', './statement', './throw-statement', './type-definition',
        './type-mapper-definition', './type-element', './variable-declaration', './while-statement',
        './worker-declaration', './package-definition', './import-declaration', './assignment',
        './assignment-statement', './function-invocation', './arithmetic-expression', './logical-expression',
        './action-invocation-expression', './ballerina-ast-deserializer', './function-invocation-expression',
        './left-operand-expression', './right-operand-expression', './connector-action', './struct-definition',
        './action-invocation-statement', './variable-definition-statement','./resource-parameter',
        './return-type','./worker-invoke','./worker-receive','./block-statement','./struct-field-access-expression',
        './variable-reference-expression','./reference-type-init-expression','./variable-definition', './break-statement',
        './comment-statement', './type-cast-expression'],
    function (BallerinaASTFactory, BallerinaASTRoot, ConditionalStatement, ConnectorDeclaration, ConnectorDefinition,
              ConstantDefinition, Expression, FunctionDefinition, IfElseStatement, IfStatement, ElseStatement,
              ElseIfStatement, TryCatchStatement, TryStatement, CatchStatement, ASTNode, ReplyStatement,
              ResourceDefinition, ReturnStatement, ServiceDefinition, Statement, ThrowStatement,
              TypeDefinition, TypeMapperDefinition, TypeElement, VariableDeclaration, WhileStatement,
              WorkerDeclaration, PackageDefinition, ImportDeclaration, Assignment, AssignmentStatement,
              FunctionInvocation, ArithmeticExpression, LogicalExpression, ActionInvocationExpression,
              BallerinaASTDeserializer, FunctionInvocationExpression, LeftOperandExpression, RightOperandExpression,
              ConnectorAction, StructDefinition, ActionInvocationStatement, VariableDefinitionStatement,
	          ResourceParameter,ReturnType,WorkerInvoke,WorkerReceive ,BlockStatement,StructFieldAccessExpression,
              VariableReferenceExpression, ReferenceTypeInitExpression, VariableDefinition, BreakStatement, CommentStatement,
              TypeCastExpression) {

        return  {
            BallerinaASTFactory: BallerinaASTFactory,
            BallerinaASTRoot: BallerinaASTRoot,
            ConditionalStatement: ConditionalStatement,
            ConnectorDeclaration: ConnectorDeclaration,
            ConnectorDefinition: ConnectorDefinition,
            ConstantDefinition: ConstantDefinition,
            Expression: Expression,
            FunctionDefinition: FunctionDefinition,
            IfElseStatement: IfElseStatement,
            IfStatement: IfStatement,
            ElseStatement: ElseStatement,
            ElseIfStatement: ElseIfStatement,
            TryCatchStatement: TryCatchStatement,
            TryStatement: TryStatement,
            CatchStatement: CatchStatement,
            ASTNode: ASTNode,
            ReplyStatement: ReplyStatement,
            ResourceDefinition: ResourceDefinition,
            ReturnStatement: ReturnStatement,
            ServiceDefinition: ServiceDefinition,
            Statement: Statement,
            ThrowStatement: ThrowStatement,
            TypeDefinition: TypeDefinition,
            TypeMapperDefinition: TypeMapperDefinition,
            TypeElement: TypeElement,
            VariableDeclaration: VariableDeclaration,
            WhileStatement: WhileStatement,
            WorkerDeclaration: WorkerDeclaration,
            PackageDefinition: PackageDefinition,
            ImportDeclaration: ImportDeclaration,
            Assignment: Assignment,
            AssignmentStatement: AssignmentStatement,
            LeftOperandExpression: LeftOperandExpression,
            RightOperandExpression: RightOperandExpression,
            FunctionInvocation: FunctionInvocation,
            FunctionInvocationExpression: FunctionInvocationExpression,
            ArithmeticExpression: ArithmeticExpression,
            LogicalExpression: LogicalExpression,
            ActionInvocationStatement: ActionInvocationStatement,
            ActionInvocationExpression: ActionInvocationExpression,
            BallerinaASTDeserializer : BallerinaASTDeserializer,
            ConnectorAction : ConnectorAction,
            StructDefinition : StructDefinition,
            VariableDefinitionStatement: VariableDefinitionStatement,
            ResourceParameter: ResourceParameter,
            ReturnType: ReturnType,
            WorkerInvoke: WorkerInvoke,
            WorkerReceive: WorkerReceive,
            BlockStatement: BlockStatement,
            StructFieldAccessExpression : StructFieldAccessExpression,
            VariableReferenceExpression : VariableReferenceExpression,
            ReferenceTypeInitExpression : ReferenceTypeInitExpression,
            VariableDefinition : VariableDefinition,
            BreakStatement : BreakStatement,
            CommentStatement : CommentStatement,
            TypeCastExpression : TypeCastExpression
        }
    });

