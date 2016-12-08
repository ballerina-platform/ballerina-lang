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
define(['./ballerina-ast-factory', './ballerina-ast-root', './conditional-statement', './connection-declaration', './connector-definition',
    './constant-definition', './expression', './function-definition', './if-statement', './trycatch-statement', './try-statement', './catch-statement', './node', './reply-statement', './resource-definition',
    './return-statement', './service-definition', './statement', './throw-statement', './type-definition', './type-converter-definition', './type-element',
    './variable-declaration', './while-statement', './worker-declaration'],
    function (BallerinaASTFactory, BallerinaASTRoot, ConditionalStatement, ConnectionDeclaration, ConnectorDefinition, ConstantDefinition,
                Expression, FunctionDefinition, IfStatement, TryCatchStatement, TryStatement, CatchStatement, ASTNode, ReplyStatement, ResourceDefinition, ReturnStatement, ServiceDefinition,
                Statement, ThrowStatement, TypeConverterDefinition, TypeDefinition, TypeElement, VariableDeclaration, WhileStatement, WorkerDeclaration) {
        return  {
            BallerinaASTFactory: BallerinaASTFactory,
            BallerinaASTRoot: BallerinaASTRoot,
            ConditionalStatement: ConditionalStatement,
            ConnectionDeclaration: ConnectionDeclaration,
            ConnectorDefinition: ConnectorDefinition,
            ConstantDefinition: ConstantDefinition,
            Expression: Expression,
            FunctionDefinition: FunctionDefinition,
            IfStatement: IfStatement,
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
            TypeConverterDefinition: TypeConverterDefinition,
            TypeElement: TypeElement,
            VariableDeclaration: VariableDeclaration,
            WhileStatement: WhileStatement,
            WorkerDeclaration: WorkerDeclaration
        }
    });

