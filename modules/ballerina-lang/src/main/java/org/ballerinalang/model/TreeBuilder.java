/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.List;

/**
 * This contains the functionality of building the nodes in the AST.
 * 
 * @since 0.94
 */
public class TreeBuilder {

    @SuppressWarnings("unchecked")
    public static PackageNode createPackageNode(List<? extends IdentifierNode> nameComps, IdentifierNode version, 
            List<? extends ImportPackageNode> imports, List<? extends XMLNSDeclarationNode> xmlnsList,
            List<? extends VariableNode> globalVars, List<? extends ServiceNode> services,
            List<? extends ConnectorNode> connectors, List<? extends FunctionNode> functions,
            List<? extends StructNode> structs, List<? extends AnnotationNode> annotations) {
        return new BLangPackage((List<BLangIdentifier>) nameComps, (BLangIdentifier) version, 
                (List<BLangImportPackage>) imports, (List<BLangXMLNS>) xmlnsList, (List<BLangVariable>) globalVars, 
                (List<BLangService>) services, (List<BLangConnector>) connectors, (List<BLangFunction>) functions, 
                (List<BLangStruct>) structs, (List<BLangAnnotation>) annotations);
    }
    
    public static IdentifierNode createIdentifierNode(String value, boolean isLiteral) {
        return new BLangIdentifier(value, isLiteral);
    }
    
    @SuppressWarnings("unchecked")
    public static ImportPackageNode createImportPackageNode(List<? extends IdentifierNode> nameComps,
            IdentifierNode version, IdentifierNode alias) {
        return new BLangImportPackage((List<BLangIdentifier>) nameComps, 
                (BLangIdentifier) version, (BLangIdentifier) alias);
    }
    
    public static XMLNSDeclarationNode createXMLNSNode(IdentifierNode namespaceURI, IdentifierNode prefix) {
        return new BLangXMLNS((BLangIdentifier) namespaceURI, (BLangIdentifier) prefix);
    }
    
    @SuppressWarnings("unchecked")
    public static VariableNode createVariableNode(TypeNode type, IdentifierNode name, 
            ExpressionNode expr, long flags, List<? extends AnnotationAttachmentNode> annAttachments) {
        return new BLangVariable((BLangType) type, (BLangIdentifier) name, (BLangExpression) expr, flags, 
                (List<BLangAnnotationAttachment>) annAttachments);
    }
    
    @SuppressWarnings("unchecked")
    public static FunctionNode createFunctionNode(IdentifierNode name, List<? extends VariableNode> params, 
            List<? extends VariableNode> retParams, BlockNode body, long flags, 
            List<? extends AnnotationAttachmentNode> annAttachments) {
        return new BLangFunction((BLangIdentifier) name, (List<BLangVariable>) params, 
                (List<BLangVariable>) retParams, (BLangBlockStmt) body, flags, 
                (List<BLangAnnotationAttachment>) annAttachments);
    }
    
    @SuppressWarnings("unchecked")
    public static BlockNode createBlockNode(List<? extends StatementNode> statements) {
        return new BLangBlockStmt((List<BLangStatement>) statements);
    }
    
    public static LiteralNode createLiteralExpression(Object value) {
        return new BLangLiteral(value);
    }
    
}
