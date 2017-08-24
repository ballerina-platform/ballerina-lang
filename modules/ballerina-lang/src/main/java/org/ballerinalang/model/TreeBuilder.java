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

import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.ValueTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.List;

/**
 * This contains the functionality of building the nodes in the AST.
 * 
 * @since 0.94
 */
public class TreeBuilder {

    public static PackageNode createPackageNode() {
        return new BLangPackage();
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
    
    public static VariableDefinitionNode createVariableDefinitionNode() {
        return new BLangVariableDef();
    }
    
    public static ValueTypeNode createValueTypeNode() {
        return new BLangValueType();
    }
    
}
