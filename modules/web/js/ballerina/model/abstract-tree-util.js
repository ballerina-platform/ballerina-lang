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

class AbstractTreeUtil {


    isAbort(node) {
        return node.kind === 'Abort';
    }

    isAction(node) {
        return node.kind === 'Action';
    }

    isAnnotation(node) {
        return node.kind === 'Annotation';
    }

    isAnnotationAttribute(node) {
        return node.kind === 'AnnotationAttribute';
    }

    isAnnotationAttachment(node) {
        return node.kind === 'AnnotationAttachment';
    }

    isAnnotationAttachmentAttribute(node) {
        return node.kind === 'AnnotationAttachmentAttribute';
    }

    isAnnotationAttachmentAttributeValue(node) {
        return node.kind === 'AnnotationAttachmentAttributeValue';
    }

    isAssignment(node) {
        return node.kind === 'Assignment';
    }

    isBreak(node) {
        return node.kind === 'Break';
    }

    isBlock(node) {
        return node.kind === 'Block';
    }

    isCatch(node) {
        return node.kind === 'Catch';
    }

    isCompilationUnit(node) {
        return node.kind === 'CompilationUnit';
    }

    isComment(node) {
        return node.kind === 'Comment';
    }

    isConnector(node) {
        return node.kind === 'Connector';
    }

    isContinue(node) {
        return node.kind === 'Continue';
    }

    isEnum(node) {
        return node.kind === 'Enum';
    }

    isExpressionStatement(node) {
        return node.kind === 'ExpressionStatement';
    }

    isForkjoin(node) {
        return node.kind === 'Forkjoin';
    }

    isFunction(node) {
        return node.kind === 'Function';
    }

    isIdentifier(node) {
        return node.kind === 'Identifier';
    }

    isIf(node) {
        return node.kind === 'If';
    }

    isImport(node) {
        return node.kind === 'Import';
    }

    isLiteral(node) {
        return node.kind === 'Literal';
    }

    isPackage(node) {
        return node.kind === 'Package';
    }

    isPackageDeclaration(node) {
        return node.kind === 'PackageDeclaration';
    }

    isService(node) {
        return node.kind === 'Service';
    }

    isReply(node) {
        return node.kind === 'Reply';
    }

    isResource(node) {
        return node.kind === 'Resource';
    }

    isReturn(node) {
        return node.kind === 'Return';
    }

    isStruct(node) {
        return node.kind === 'Struct';
    }

    isThrow(node) {
        return node.kind === 'Throw';
    }

    isTransaction(node) {
        return node.kind === 'Transaction';
    }

    isTransform(node) {
        return node.kind === 'Transform';
    }

    isType(node) {
        return node.kind === 'Type';
    }

    isVariable(node) {
        return node.kind === 'Variable';
    }

    isWhile(node) {
        return node.kind === 'While';
    }

    isWorker(node) {
        return node.kind === 'Worker';
    }

    isWorkerSend(node) {
        return node.kind === 'WorkerSend';
    }

    isWorkerReceive(node) {
        return node.kind === 'WorkerReceive';
    }

    isInvocation(node) {
        return node.kind === 'Invocation';
    }

    isArrayLiteralExpr(node) {
        return node.kind === 'ArrayLiteralExpr';
    }

    isBinaryExpr(node) {
        return node.kind === 'BinaryExpr';
    }

    isUnaryExpr(node) {
        return node.kind === 'UnaryExpr';
    }

    isStringTemplateLiteral(node) {
        return node.kind === 'StringTemplateLiteral';
    }


}

export default AbstractTreeUtil;
