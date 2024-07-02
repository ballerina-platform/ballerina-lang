/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.datamapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Visitor to extract Record Type Structure information.
 */
public class DataMapperNodeVisitor extends NodeVisitor {
    public final HashMap<String, String> restFields;
    public final HashMap<String, Map<String, RecordFieldSymbol>> spreadFields;
    public final ArrayList<String> specificFieldList;
    private final String rhsSymbolName;
    private SemanticModel model;

    public DataMapperNodeVisitor(String rhsSymbolName) {
        this.restFields = new HashMap<>();
        this.spreadFields = new HashMap<>();
        this.rhsSymbolName = rhsSymbolName;
        this.specificFieldList = new ArrayList<>();
    }

    public void setModel(SemanticModel model) {
        this.model = model;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode.initializer().isEmpty()) {
            return;
        }
        if (variableDeclarationNode.initializer().get().kind() != SyntaxKind.MAPPING_CONSTRUCTOR) {
            return;
        }
        String rightSymbolName = "";
        TypeDescriptorNode typeDescriptorNode = variableDeclarationNode.typedBindingPattern().typeDescriptor();
        if (typeDescriptorNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            rightSymbolName = ((SimpleNameReferenceNode) typeDescriptorNode).name().text();
        } else if (typeDescriptorNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            rightSymbolName = ((QualifiedNameReferenceNode) typeDescriptorNode).identifier().text();
        }

        if (rightSymbolName.isEmpty()) {
            return;
        }

        if (this.rhsSymbolName.equals(rightSymbolName)) {
            MappingConstructorExpressionNode mappingConstructorExpressionNode =
                    (MappingConstructorExpressionNode) variableDeclarationNode.initializer().get();
            SeparatedNodeList<MappingFieldNode> fields = mappingConstructorExpressionNode.fields();
            for (MappingFieldNode field : fields) {
                if (field.kind() == SyntaxKind.SPECIFIC_FIELD) {
                    SpecificFieldNode specificFieldNode = (SpecificFieldNode) field;
                    if (specificFieldNode.fieldName().kind() == SyntaxKind.STRING_LITERAL) {
                        String fieldName = ((BasicLiteralNode) specificFieldNode.fieldName()).literalToken().text();
                        fieldName = fieldName.replace("\"", "");
                        Optional<Symbol> symbol = this.model.symbol(variableDeclarationNode);
                        if (symbol.isPresent()) {
                            TypeSymbol typeSymbol = ((VariableSymbol) symbol.get()).typeDescriptor();
                            typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
                            String restFieldType = ((RecordTypeSymbol) typeSymbol).restTypeDescriptor().get().
                                    typeKind().getName();
                            this.restFields.put(fieldName, restFieldType);
                        }
                    } else if (specificFieldNode.fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                        String fieldName = ((IdentifierToken) specificFieldNode.fieldName()).text();
                        specificFieldList.add(fieldName);
                    }
                } else if (field.kind() == SyntaxKind.SPREAD_FIELD) {
                    SpreadFieldNode field1 = (SpreadFieldNode) field;
                    Optional<Symbol> symbol = this.model.symbol(field1.valueExpr());
                    if (symbol.isPresent()) {
                        TypeSymbol typeSymbol = ((VariableSymbol) symbol.get()).typeDescriptor();
                        typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
                        if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
                            Map<String, RecordFieldSymbol> fieldSymbolMap =
                                    ((RecordTypeSymbol) typeSymbol).fieldDescriptors();
                            if (symbol.get().getName().isPresent()) {
                                this.spreadFields.put(symbol.get().getName().get(), fieldSymbolMap);
                            }
                        }
                    }
                }
            }
        }
    }
}
