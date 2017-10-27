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
import SimpleBBox from './../../../model/view/simple-bounding-box';
import * as DesignerDefaults from './designer-defaults';
import { panel as defaultPanel} from './designer-defaults';
import TreeUtil from './../../../model/tree-util';

class ErrorRenderingUtil {



    /**
     * Calculate error position of Action nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForActionNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Annotation nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of AnnotationAttachment nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttachmentNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of AnnotationAttribute nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttributeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Catch nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForCatchNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of CompilationUnit nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForCompilationUnitNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Connector nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForConnectorNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Enum nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForEnumNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Function nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForFunctionNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Identifier nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForIdentifierNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Import nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForImportNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Package nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForPackageNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of PackageDeclaration nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForPackageDeclarationNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of RecordLiteralKeyValue nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForRecordLiteralKeyValueNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Resource nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForResourceNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Retry nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForRetryNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Service nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForServiceNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Struct nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForStructNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Variable nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForVariableNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Worker nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWorkerNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Xmlns nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlnsNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of AnnotationAttachmentAttribute nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttachmentAttributeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of AnnotationAttachmentAttributeValue nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttachmentAttributeValueNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ArrayLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForArrayLiteralExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of BinaryExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBinaryExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ConnectorInitExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForConnectorInitExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of FieldBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForFieldBasedAccessExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of IndexBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForIndexBasedAccessExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Invocation nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForInvocationNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Lambda nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForLambdaNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Literal nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForLiteralNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of RecordLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForRecordLiteralExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of SimpleVariableRef nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForSimpleVariableRefNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of StringTemplateLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForStringTemplateLiteralNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of TernaryExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTernaryExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of TypeCastExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTypeCastExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of TypeConversionExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTypeConversionExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of UnaryExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForUnaryExprNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlQname nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlQnameNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlAttribute nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlAttributeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlQuotedString nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlQuotedStringNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlElementLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlElementLiteralNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlTextLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlTextLiteralNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlCommentLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlCommentLiteralNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of XmlPiLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlPiLiteralNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Abort nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAbortNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Assignment nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAssignmentNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Block nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBlockNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Break nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBreakNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Next nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForNextNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForExpressionStatementNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ForkJoin nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForForkJoinNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of If nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForIfNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Reply nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForReplyNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Return nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForReturnNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Comment nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForCommentNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Throw nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForThrowNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Transaction nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTransactionNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Transform nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTransformNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of Try nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTryNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of VariableDef nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForVariableDefNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of While nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWhileNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of WorkerReceive nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWorkerReceiveNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of WorkerSend nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWorkerSendNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ArrayType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForArrayTypeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of BuiltInRefType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBuiltInRefTypeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ConstrainedType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForConstrainedTypeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of FunctionType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForFunctionTypeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of UserDefinedType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForUserDefinedTypeNode(node) {
        // Not implemented.
    }



    /**
     * Calculate error position of ValueType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForValueTypeNode(node) {
        // Not implemented.
    }



}

export default ErrorRenderingUtil;
