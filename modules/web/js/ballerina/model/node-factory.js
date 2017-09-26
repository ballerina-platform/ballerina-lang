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


import ActionNode from './tree/action-node';
import AnnotationNode from './tree/annotation-node';
import AnnotationAttachmentNode from './tree/annotation-attachment-node';
import AnnotationAttributeNode from './tree/annotation-attribute-node';
import CatchNode from './tree/catch-node';
import CompilationUnitNode from './tree/compilation-unit-node';
import ConnectorNode from './tree/connector-node';
import EnumNode from './tree/enum-node';
import FunctionNode from './tree/function-node';
import IdentifierNode from './tree/identifier-node';
import ImportNode from './tree/import-node';
import PackageNode from './tree/package-node';
import PackageDeclarationNode from './tree/package-declaration-node';
import ResourceNode from './tree/resource-node';
import RetryNode from './tree/retry-node';
import ServiceNode from './tree/service-node';
import StructNode from './tree/struct-node';
import VariableNode from './tree/variable-node';
import WorkerNode from './tree/worker-node';
import AnnotationAttachmentAttributeValueNode from './tree/annotation-attachment-attribute-value-node';
import ArrayLiteralExprNode from './tree/array-literal-expr-node';
import BinaryExprNode from './tree/binary-expr-node';
import InvocationNode from './tree/invocation-node';
import LiteralNode from './tree/literal-node';
import StringTemplateLiteralNode from './tree/string-template-literal-node';
import UnaryExprNode from './tree/unary-expr-node';
import AbortNode from './tree/abort-node';
import AssignmentNode from './tree/assignment-node';
import BlockNode from './tree/block-node';
import BreakNode from './tree/break-node';
import ContinueNode from './tree/continue-node';
import ExpressionStatementNode from './tree/expression-statement-node';
import ForkJoinNode from './tree/fork-join-node';
import IfNode from './tree/if-node';
import ReplyNode from './tree/reply-node';
import ReturnNode from './tree/return-node';
import CommentNode from './tree/comment-node';
import ThrowNode from './tree/throw-node';
import TransactionNode from './tree/transaction-node';
import TransformNode from './tree/transform-node';
import WhileNode from './tree/while-node';
import WorkerReceiveNode from './tree/worker-receive-node';
import WorkerSendNode from './tree/worker-send-node';
import ArrayTypeNode from './tree/array-type-node';
import ConstrainedTypeNode from './tree/constrained-type-node';
import FunctionTypeNode from './tree/function-type-node';
import UserDefinedTypeNode from './tree/user-defined-type-node';
import ValueTypeNode from './tree/value-type-node';

class NodeFactory {


    createAction(json){
        json.kind = 'Action';
        let node = new ActionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createAnnotation(json){
        json.kind = 'Annotation';
        let node = new AnnotationNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createAnnotationAttachment(json){
        json.kind = 'AnnotationAttachment';
        let node = new AnnotationAttachmentNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createAnnotationAttribute(json){
        json.kind = 'AnnotationAttribute';
        let node = new AnnotationAttributeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createCatch(json){
        json.kind = 'Catch';
        let node = new CatchNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createCompilationUnit(json){
        json.kind = 'CompilationUnit';
        let node = new CompilationUnitNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createConnector(json){
        json.kind = 'Connector';
        let node = new ConnectorNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createEnum(json){
        json.kind = 'Enum';
        let node = new EnumNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createFunction(json){
        json.kind = 'Function';
        let node = new FunctionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createIdentifier(json){
        json.kind = 'Identifier';
        let node = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createImport(json){
        json.kind = 'Import';
        let node = new ImportNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createPackage(json){
        json.kind = 'Package';
        let node = new PackageNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createPackageDeclaration(json){
        json.kind = 'PackageDeclaration';
        let node = new PackageDeclarationNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createResource(json){
        json.kind = 'Resource';
        let node = new ResourceNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createRetry(json){
        json.kind = 'Retry';
        let node = new RetryNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createService(json){
        json.kind = 'Service';
        let node = new ServiceNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createStruct(json){
        json.kind = 'Struct';
        let node = new StructNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createVariable(json){
        json.kind = 'Variable';
        let node = new VariableNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createWorker(json){
        json.kind = 'Worker';
        let node = new WorkerNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createAnnotationAttachmentAttributeValue(json){
        json.kind = 'AnnotationAttachmentAttributeValue';
        let node = new AnnotationAttachmentAttributeValueNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createArrayLiteralExpr(json){
        json.kind = 'ArrayLiteralExpr';
        let node = new ArrayLiteralExprNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createBinaryExpr(json){
        json.kind = 'BinaryExpr';
        let node = new BinaryExprNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createInvocation(json){
        json.kind = 'Invocation';
        let node = new InvocationNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createLiteral(json){
        json.kind = 'Literal';
        let node = new LiteralNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createStringTemplateLiteral(json){
        json.kind = 'StringTemplateLiteral';
        let node = new StringTemplateLiteralNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createUnaryExpr(json){
        json.kind = 'UnaryExpr';
        let node = new UnaryExprNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createAbort(json){
        json.kind = 'Abort';
        let node = new AbortNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createAssignment(json){
        json.kind = 'Assignment';
        let node = new AssignmentNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createBlock(json){
        json.kind = 'Block';
        let node = new BlockNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createBreak(json){
        json.kind = 'Break';
        let node = new BreakNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createContinue(json){
        json.kind = 'Continue';
        let node = new ContinueNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createExpressionStatement(json){
        json.kind = 'ExpressionStatement';
        let node = new ExpressionStatementNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createForkJoin(json){
        json.kind = 'ForkJoin';
        let node = new ForkJoinNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createIf(json){
        json.kind = 'If';
        let node = new IfNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createReply(json){
        json.kind = 'Reply';
        let node = new ReplyNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createReturn(json){
        json.kind = 'Return';
        let node = new ReturnNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createComment(json){
        json.kind = 'Comment';
        let node = new CommentNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createThrow(json){
        json.kind = 'Throw';
        let node = new ThrowNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createTransaction(json){
        json.kind = 'Transaction';
        let node = new TransactionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createTransform(json){
        json.kind = 'Transform';
        let node = new TransformNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createWhile(json){
        json.kind = 'While';
        let node = new WhileNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createWorkerReceive(json){
        json.kind = 'WorkerReceive';
        let node = new WorkerReceiveNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createWorkerSend(json){
        json.kind = 'WorkerSend';
        let node = new WorkerSendNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createArrayType(json){
        json.kind = 'ArrayType';
        let node = new ArrayTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createConstrainedType(json){
        json.kind = 'ConstrainedType';
        let node = new ConstrainedTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createFunctionType(json){
        json.kind = 'FunctionType';
        let node = new FunctionTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createUserDefinedType(json){
        json.kind = 'UserDefinedType';
        let node = new UserDefinedTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }

    createValueType(json){
        json.kind = 'ValueType';
        let node = new ValueTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below. 
        return node;
    }


}

export default new NodeFactory();
