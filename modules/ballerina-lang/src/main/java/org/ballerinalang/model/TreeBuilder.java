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

import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotAttributeNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.PackageDeclarationNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.model.tree.expressions.AnnotAttributeValueNode;
import org.ballerinalang.model.tree.expressions.ArrayLiteralNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.RecordTypeLiteralNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.ArrayTypeNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.tree.types.ConstrainedTypeNode;
import org.ballerinalang.model.tree.types.FunctionTypeNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.model.tree.types.ValueTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordTypeLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInReferenceType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * This contains the functionality of building the nodes in the AST.
 * 
 * @since 0.94
 */
public class TreeBuilder {

    public static CompilationUnitNode createCompilationUnit() {
        return new BLangCompilationUnit();
    }
    
    public static PackageNode createPackageNode() {
        return new BLangPackage();
    }
    
    public static PackageDeclarationNode createPackageDeclarationNode() {
        return new BLangPackageDeclaration();
    }
    
    public static IdentifierNode createIdentifierNode() {
        return new BLangIdentifier();
    }
    
    public static ImportPackageNode createImportPackageNode() {
        return new BLangImportPackage();
    }
    
    public static XMLNSDeclarationNode createXMLNSNode() {
        return new BLangXMLNS();
    }
    
    public static VariableNode createVariableNode() {
        return new BLangVariable();
    }
    
    public static FunctionNode createFunctionNode() {
        return new BLangFunction();
    }
    
    public static BlockNode createBlockNode() {
        return new BLangBlockStmt();
    }
    
    public static LiteralNode createLiteralExpression() {
        return new BLangLiteral();
    }

    public static ArrayLiteralNode createArrayLiteralNode(){
        return new BLangArrayLiteral();
    }

    public static RecordTypeLiteralNode createRecordTypeLiteralNode() {
        return new BLangRecordTypeLiteral();
    }

    public static VariableDefinitionNode createVariableDefinitionNode() {
        return new BLangVariableDef();
    }
    
    public static ValueTypeNode createValueTypeNode() {
        return new BLangValueType();
    }

    public static ArrayTypeNode createArrayTypeNode() {
        return new BLangArrayType();
    }

    public static UserDefinedTypeNode createUserDefinedTypeNode() {
        return new BLangUserDefinedType();
    }

    public static BuiltInReferenceTypeNode createBuiltInReferenceTypeNode() {
        return new BLangBuiltInReferenceType();
    }

    public static ConstrainedTypeNode createConstrainedTypeNode() {
        return new BLangConstrainedType();
    }

    public static FunctionTypeNode createFunctionTypeNode() {
        return new BLangFunctionTypeNode();
    }

    public static SimpleVariableReferenceNode createSimpleVariableReferenceNode() {
        return new BLangSimpleVariableReference();
    }

    public static StructNode createStructNode() {
        return new BLangStruct();
    }
    
    public static ConnectorNode createConnectorNode() {
        return new BLangConnector();
    }
    
    public static ActionNode createActionNode() {
        return new BLangAction();
    }

    public static AnnotationNode createAnnotationNode() {
        return new BLangAnnotation();
    }
    
    public static AnnotAttributeNode createAnnotationAttributeNode() {
        return new BLangAnnotAttribute();
    }
    
    public static AnnotAttributeValueNode createAnnotAttributeValueNode() {
        return new BLangAnnotAttributeValue();
    }
}
