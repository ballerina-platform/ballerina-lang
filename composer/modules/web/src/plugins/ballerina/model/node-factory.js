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

import StatementNode from './tree/statement-node';
import ExpressionNode from './tree/expression-node';
import ActionNode from './tree/action-node';
import AnnotationNode from './tree/annotation-node';
import AnnotationAttachmentNode from './tree/annotation-attachment-node';
import AnnotationAttributeNode from './tree/annotation-attribute-node';
import CatchNode from './tree/catch-node';
import CompilationUnitNode from './tree/compilation-unit-node';
import ConnectorNode from './tree/connector-node';
import DeprecatedNode from './tree/deprecated-node';
import DocumentationNode from './tree/documentation-node';
import EndpointNode from './tree/endpoint-node';
import EnumNode from './tree/enum-node';
import EnumeratorNode from './tree/enumerator-node';
import FunctionNode from './tree/function-node';
import IdentifierNode from './tree/identifier-node';
import ImportNode from './tree/import-node';
import PackageNode from './tree/package-node';
import PackageDeclarationNode from './tree/package-declaration-node';
import RecordLiteralKeyValueNode from './tree/record-literal-key-value-node';
import ResourceNode from './tree/resource-node';
import ServiceNode from './tree/service-node';
import StructNode from './tree/struct-node';
import VariableNode from './tree/variable-node';
import WorkerNode from './tree/worker-node';
import XmlnsNode from './tree/xmlns-node';
import TransformerNode from './tree/transformer-node';
import DocumentationAttributeNode from './tree/documentation-attribute-node';
import AnnotationAttachmentAttributeNode from './tree/annotation-attachment-attribute-node';
import AnnotationAttachmentAttributeValueNode from './tree/annotation-attachment-attribute-value-node';
import ArrayLiteralExprNode from './tree/array-literal-expr-node';
import BinaryExprNode from './tree/binary-expr-node';
import TypeInitExprNode from './tree/type-init-expr-node';
import FieldBasedAccessExprNode from './tree/field-based-access-expr-node';
import IndexBasedAccessExprNode from './tree/index-based-access-expr-node';
import IntRangeExprNode from './tree/int-range-expr-node';
import InvocationNode from './tree/invocation-node';
import LambdaNode from './tree/lambda-node';
import LiteralNode from './tree/literal-node';
import RecordLiteralExprNode from './tree/record-literal-expr-node';
import SimpleVariableRefNode from './tree/simple-variable-ref-node';
import StringTemplateLiteralNode from './tree/string-template-literal-node';
import TernaryExprNode from './tree/ternary-expr-node';
import TypeofExpressionNode from './tree/typeof-expression-node';
import TypeCastExprNode from './tree/type-cast-expr-node';
import TypeConversionExprNode from './tree/type-conversion-expr-node';
import UnaryExprNode from './tree/unary-expr-node';
import XmlQnameNode from './tree/xml-qname-node';
import XmlAttributeNode from './tree/xml-attribute-node';
import XmlAttributeAccessExprNode from './tree/xml-attribute-access-expr-node';
import XmlQuotedStringNode from './tree/xml-quoted-string-node';
import XmlTextLiteralNode from './tree/xml-text-literal-node';
import XmlPiLiteralNode from './tree/xml-pi-literal-node';
import AbortNode from './tree/abort-node';
import AssignmentNode from './tree/assignment-node';
import BindNode from './tree/bind-node';
import BlockNode from './tree/block-node';
import BreakNode from './tree/break-node';
import NextNode from './tree/next-node';
import ExpressionStatementNode from './tree/expression-statement-node';
import ForeachNode from './tree/foreach-node';
import ForkJoinNode from './tree/fork-join-node';
import IfNode from './tree/if-node';
import ReplyNode from './tree/reply-node';
import ReturnNode from './tree/return-node';
import ThrowNode from './tree/throw-node';
import TransactionNode from './tree/transaction-node';
import TryNode from './tree/try-node';
import VariableDefNode from './tree/variable-def-node';
import WhileNode from './tree/while-node';
import LockNode from './tree/lock-node';
import WorkerReceiveNode from './tree/worker-receive-node';
import WorkerSendNode from './tree/worker-send-node';
import ArrayTypeNode from './tree/array-type-node';
import BuiltInRefTypeNode from './tree/built-in-ref-type-node';
import ConstrainedTypeNode from './tree/constrained-type-node';
import FunctionTypeNode from './tree/function-type-node';
import UserDefinedTypeNode from './tree/user-defined-type-node';
import EndpointTypeNode from './tree/endpoint-type-node';
import ValueTypeNode from './tree/value-type-node';
import VariableReferenceNode from './tree/variable-reference-node';

class NodeFactory {


    createAction(json = {}) {
        json.kind = 'Action';
        let node = new ActionNode();
        node.returnParameters = [];
        node.body = new BlockNode();
        node.workers = [];
        node.name = new IdentifierNode();
        node.parameters = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAnnotation(json = {}) {
        json.kind = 'Annotation';
        let node = new AnnotationNode();
        node.name = new IdentifierNode();
        node.attributes = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAnnotationAttachment(json = {}) {
        json.kind = 'AnnotationAttachment';
        let node = new AnnotationAttachmentNode();
        node.packageAlias = new IdentifierNode();
        node.annotationName = new IdentifierNode();
        node.attributes = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAnnotationAttribute(json = {}) {
        json.kind = 'AnnotationAttribute';
        let node = new AnnotationAttributeNode();
        node.initialExpression = new ExpressionNode();
        node.name = new IdentifierNode();
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createCatch(json = {}) {
        json.kind = 'Catch';
        let node = new CatchNode();
        node.body = new BlockNode();
        node.parameter = new VariableNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createCompilationUnit(json = {}) {
        json.kind = 'CompilationUnit';
        let node = new CompilationUnitNode();
        node.topLevelNodes = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createConnector(json = {}) {
        json.kind = 'Connector';
        let node = new ConnectorNode();
        node.filteredParameter = new VariableNode();
        node.variableDefs = [];
        node.initFunction = new FunctionNode();
        node.initAction = new ActionNode();
        node.name = new IdentifierNode();
        node.actions = [];
        node.parameters = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createDeprecated(json = {}) {
        json.kind = 'Deprecated';
        let node = new DeprecatedNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createDocumentation(json = {}) {
        json.kind = 'Documentation';
        let node = new DocumentationNode();
        node.attributes = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createEndpoint(json = {}) {
        json.kind = 'Endpoint';
        let node = new EndpointNode();
        node.endPointType = new EndpointTypeNode();
        node.configurationExpression = new ExpressionNode();
        node.name = new IdentifierNode();
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createEnum(json = {}) {
        json.kind = 'Enum';
        let node = new EnumNode();
        node.enumFields = [];
        node.name = new IdentifierNode();
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createEnumerator(json = {}) {
        json.kind = 'Enumerator';
        let node = new EnumeratorNode();
        node.enumerators = [];
        node.name = new IdentifierNode();
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createFunction(json = {}) {
        json.kind = 'Function';
        let node = new FunctionNode();
        node.receiver = new VariableNode();
        node.returnParameters = [];
        node.body = new BlockNode();
        node.workers = [];
        node.name = new IdentifierNode();
        node.parameters = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createIdentifier(json = {}) {
        json.kind = 'Identifier';
        let node = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createImport(json = {}) {
        json.kind = 'Import';
        let node = new ImportNode();
        node.packageVersion = new IdentifierNode();
        node.alias = new IdentifierNode();
        node.packageName = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createPackage(json = {}) {
        json.kind = 'Package';
        let node = new PackageNode();
        node.compilationUnits = [];
        node.imports = [];
        node.packageDeclaration = new PackageDeclarationNode();
        node.namespaceDeclarations = [];
        node.globalVariables = [];
        node.services = [];
        node.connectors = [];
        node.functions = [];
        node.structs = [];
        node.annotations = [];
        node.transformers = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createPackageDeclaration(json = {}) {
        json.kind = 'PackageDeclaration';
        let node = new PackageDeclarationNode();
        node.packageVersion = new IdentifierNode();
        node.packageName = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createRecordLiteralKeyValue(json = {}) {
        json.kind = 'RecordLiteralKeyValue';
        let node = new RecordLiteralKeyValueNode();
        node.value = new ExpressionNode();
        node.key = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createResource(json = {}) {
        json.kind = 'Resource';
        let node = new ResourceNode();
        node.returnParameters = [];
        node.body = new BlockNode();
        node.workers = [];
        node.name = new IdentifierNode();
        node.parameters = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createService(json = {}) {
        json.kind = 'Service';
        let node = new ServiceNode();
        node.initFunction = new FunctionNode();
        node.protocolPackageIdentifier = new IdentifierNode();
        node.variables = [];
        node.name = new IdentifierNode();
        node.resources = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createStruct(json = {}) {
        json.kind = 'Struct';
        let node = new StructNode();
        node.name = new IdentifierNode();
        node.fields = [];
        node.annotationAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createVariable(json = {}) {
        json.kind = 'Variable';
        let node = new VariableNode();
        node.typeNode = new TypeNode();
        node.initialExpression = new ExpressionNode();
        node.name = new IdentifierNode();
        node.annotationAttachments = [];
        node.documentationAttachments = [];
        node.deprecatedAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createWorker(json = {}) {
        json.kind = 'Worker';
        let node = new WorkerNode();
        node.returnParameters = [];
        node.body = new BlockNode();
        node.workers = [];
        node.endpointNodes = [];
        node.name = new IdentifierNode();
        node.parameters = [];
        node.annotationAttachments = [];
        node.documentationAttachments = [];
        node.deprecatedAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createXmlns(json = {}) {
        json.kind = 'Xmlns';
        let node = new XmlnsNode();
        node.prefix = new IdentifierNode();
        node.namespaceURI = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTransformer(json = {}) {
        json.kind = 'Transformer';
        let node = new TransformerNode();
        node.source = new VariableNode();
        node.returnParameters = [];
        node.body = new BlockNode();
        node.workers = [];
        node.endpointNodes = [];
        node.name = new IdentifierNode();
        node.parameters = [];
        node.annotationAttachments = [];
        node.documentationAttachments = [];
        node.deprecatedAttachments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createDocumentationAttribute(json = {}) {
        json.kind = 'DocumentationAttribute';
        let node = new DocumentationAttributeNode();
        node.documentationField = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAnnotationAttachmentAttribute(json = {}) {
        json.kind = 'AnnotationAttachmentAttribute';
        let node = new AnnotationAttachmentAttributeNode();
        node.value = this.createAnnotationAttachmentAttributeValue();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAnnotationAttachmentAttributeValue(json = {}) {
        json.kind = 'AnnotationAttachmentAttributeValue';
        let node = new AnnotationAttachmentAttributeValueNode();
        node.valueArray = [];
        node.value = new LiteralNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createArrayLiteralExpr(json = {}) {
        json.kind = 'ArrayLiteralExpr';
        let node = new ArrayLiteralExprNode();
        node.expressions = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createBinaryExpr(json = {}) {
        json.kind = 'BinaryExpr';
        let node = new BinaryExprNode();
        node.leftExpression = new ExpressionNode();
        node.rightExpression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTypeInitExpr(json = {}) {
        json.kind = 'TypeInitExpr';
        let node = new TypeInitExprNode();
        node.expressions = [];
        node.type = new EndpointTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createFieldBasedAccessExpr(json = {}) {
        json.kind = 'FieldBasedAccessExpr';
        let node = new FieldBasedAccessExprNode();
        node.expression = new VariableReferenceNode();
        node.fieldName = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createIndexBasedAccessExpr(json = {}) {
        json.kind = 'IndexBasedAccessExpr';
        let node = new IndexBasedAccessExprNode();
        node.index = new ExpressionNode();
        node.expression = new VariableReferenceNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createIntRangeExpr(json = {}) {
        json.kind = 'IntRangeExpr';
        let node = new IntRangeExprNode();
        node.startExpression = new ExpressionNode();
        node.endExpression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createInvocation(json = {}) {
        json.kind = 'Invocation';
        let node = new InvocationNode();
        node.packageAlias = new IdentifierNode();
        node.expression = new VariableReferenceNode();
        node.argumentExpressions = [];
        node.name = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createLambda(json = {}) {
        json.kind = 'Lambda';
        let node = new LambdaNode();
        node.functionNode = new FunctionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createLiteral(json = {}) {
        json.kind = 'Literal';
        let node = new LiteralNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createRecordLiteralExpr(json = {}) {
        json.kind = 'RecordLiteralExpr';
        let node = new RecordLiteralExprNode();
        node.keyValuePairs = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createSimpleVariableRef(json = {}) {
        json.kind = 'SimpleVariableRef';
        let node = new SimpleVariableRefNode();
        node.packageAlias = new IdentifierNode();
        node.variableName = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createStringTemplateLiteral(json = {}) {
        json.kind = 'StringTemplateLiteral';
        let node = new StringTemplateLiteralNode();
        node.expressions = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTernaryExpr(json = {}) {
        json.kind = 'TernaryExpr';
        let node = new TernaryExprNode();
        node.condition = new ExpressionNode();
        node.thenExpression = new ExpressionNode();
        node.elseExpression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTypeofExpression(json = {}) {
        json.kind = 'TypeofExpression';
        let node = new TypeofExpressionNode();
        node.typeNode = new TypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTypeCastExpr(json = {}) {
        json.kind = 'TypeCastExpr';
        let node = new TypeCastExprNode();
        node.expression = new ExpressionNode();
        node.typeNode = new TypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTypeConversionExpr(json = {}) {
        json.kind = 'TypeConversionExpr';
        let node = new TypeConversionExprNode();
        node.expression = new ExpressionNode();
        node.typeNode = new TypeNode();
        node.transformerInvocation = new InvocationNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createUnaryExpr(json = {}) {
        json.kind = 'UnaryExpr';
        let node = new UnaryExprNode();
        node.expression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createXmlQname(json = {}) {
        json.kind = 'XmlQname';
        let node = new XmlQnameNode();
        node.prefix = new IdentifierNode();
        node.localname = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createXmlAttribute(json = {}) {
        json.kind = 'XmlAttribute';
        let node = new XmlAttributeNode();
        node.name = new ExpressionNode();
        node.value = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createXmlAttributeAccessExpr(json = {}) {
        json.kind = 'XmlAttributeAccessExpr';
        let node = new XmlAttributeAccessExprNode();
        node.index = new ExpressionNode();
        node.expression = new VariableReferenceNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }


    createXmlQuotedString(json = {}) {
        json.kind = 'XmlQuotedString';
        let node = new XmlQuotedStringNode();
        node.textFragments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createXmlTextLiteral(json = {}) {
        json.kind = 'XmlTextLiteral';
        let node = new XmlTextLiteralNode();
        node.textFragments = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createXmlPiLiteral(json = {}) {
        json.kind = 'XmlPiLiteral';
        let node = new XmlPiLiteralNode();
        node.dataTextFragments = [];
        node.target = new LiteralNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAbort(json = {}) {
        json.kind = 'Abort';
        let node = new AbortNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createAssignment(json = {}) {
        json.kind = 'Assignment';
        let node = new AssignmentNode();
        node.variables = [];
        node.expression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createBind(json = {}) {
        json.kind = 'Bind';
        let node = new BindNode();
        node.variable = new ExpressionNode();
        node.expression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createBlock(json = {}) {
        json.kind = 'Block';
        let node = new BlockNode();
        node.statements = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createBreak(json = {}) {
        json.kind = 'Break';
        let node = new BreakNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createNext(json = {}) {
        json.kind = 'Next';
        let node = new NextNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createExpressionStatement(json = {}) {
        json.kind = 'ExpressionStatement';
        let node = new ExpressionStatementNode();
        node.expression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createForeach(json = {}) {
        json.kind = 'Foreach';
        let node = new ForeachNode();
        node.variables = [];
        node.body = new BlockNode();
        node.collection = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createForkJoin(json = {}) {
        json.kind = 'ForkJoin';
        let node = new ForkJoinNode();
        node.workers = [];
        node.joinBody = new BlockNode();
        node.joinedWorkerIdentifiers = [];
        node.timeOutExpression = new ExpressionNode();
        node.timeOutVariable = new VariableNode();
        node.timeoutBody = new BlockNode();
        node.joinResultVar = new VariableNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createIf(json = {}) {
        json.kind = 'If';
        let node = new IfNode();
        node.body = new BlockNode();
        node.condition = new ExpressionNode();
        node.elseStatement = new StatementNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createReply(json = {}) {
        json.kind = 'Reply';
        let node = new ReplyNode();
        node.expression = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createReturn(json = {}) {
        json.kind = 'Return';
        let node = new ReturnNode();
        node.expressions = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createThrow(json = {}) {
        json.kind = 'Throw';
        let node = new ThrowNode();
        node.expressions = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTransaction(json = {}) {
        json.kind = 'Transaction';
        let node = new TransactionNode();
        node.condition = new ExpressionNode();
        node.failedBody = new BlockNode();
        node.transactionBody = new BlockNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createTry(json = {}) {
        json.kind = 'Try';
        let node = new TryNode();
        node.body = new BlockNode();
        node.catchBlocks = [];
        node.finallyBody = new BlockNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createVariableDef(json = {}) {
        json.kind = 'VariableDef';
        let node = new VariableDefNode();
        node.variable = new VariableNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createWhile(json = {}) {
        json.kind = 'While';
        let node = new WhileNode();
        node.body = new BlockNode();
        node.condition = new ExpressionNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createLock(json = {}) {
        json.kind = 'Lock';
        let node = new LockNode();
        node.body = new BlockNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createWorkerReceive(json = {}) {
        json.kind = 'WorkerReceive';
        let node = new WorkerReceiveNode();
        node.expressions = [];
        node.workerName = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createWorkerSend(json = {}) {
        json.kind = 'WorkerSend';
        let node = new WorkerSendNode();
        node.expressions = [];
        node.workerName = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createArrayType(json = {}) {
        json.kind = 'ArrayType';
        let node = new ArrayTypeNode();
        node.elementType = new ValueTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createBuiltInRefType(json = {}) {
        json.kind = 'BuiltInRefType';
        let node = new BuiltInRefTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createConstrainedType(json = {}) {
        json.kind = 'ConstrainedType';
        let node = new ConstrainedTypeNode();
        node.constraint = new TypeNode();
        node.type = new TypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createFunctionType(json = {}) {
        json.kind = 'FunctionType';
        let node = new FunctionTypeNode();
        node.paramTypeNode = [];
        node.returnParamTypeNode = [];
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createUserDefinedType(json = {}) {
        json.kind = 'UserDefinedType';
        let node = new UserDefinedTypeNode();
        node.packageAlias = new IdentifierNode();
        node.typeName = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createEndpointType(json = {}) {
        json.kind = 'EndpointType';
        let node = new EndpointTypeNode();
        node.packageAlias = new IdentifierNode();
        node.typeName = new IdentifierNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }

    createValueType(json = {}) {
        json.kind = 'ValueType';
        let node = new ValueTypeNode();
        node = Object.assign(node, json);
        // Set any aditional default properties below.
        return node;
    }


}

export default new NodeFactory();
