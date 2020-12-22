/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.connector;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common node visitor to override and remove assertion errors from BLangNodeVisitor methods.
 */
public class ConnectorNodeVisitor extends NodeVisitor {

    private final String name;
    private final List<ClassDefinitionNode> connectors;
    private final Map<String, TypeDefinitionNode> records;

    private final SemanticModel semanticModel;

    public ConnectorNodeVisitor(String name, SemanticModel semanticModel) {
        this.name = name;
        this.semanticModel = semanticModel;
        this.connectors = new ArrayList<>();
        this.records = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public List<ClassDefinitionNode> getConnectors() {
        return connectors;
    }

    public Map<String, TypeDefinitionNode> getRecords() {
        return records;
    }

    public void visit(ModulePartNode modulePartNode) {
        for (int i = 0; i < modulePartNode.members().size(); i++) {
            modulePartNode.members().get(i).accept(this);
        }
    }

    public void visit(ClassDefinitionNode classDefinitionNode) {
        String fileName = classDefinitionNode.syntaxTree().filePath();
        Optional<TypeSymbol> typeSymbol = this.semanticModel.type(fileName,
                classDefinitionNode.className().lineRange());

        if (typeSymbol.isPresent()) {
            TypeSymbol rawType = getRawType(typeSymbol.get());
            if (rawType.typeKind() == TypeDescKind.OBJECT) {
                ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;

                boolean isClient = objectTypeSymbol.qualifiers().contains(Qualifier.CLIENT);
                if (isClient) {
                    this.connectors.add(classDefinitionNode);
                }

            }
        }
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        String fileName = typeDefinitionNode.syntaxTree().filePath();
        Optional<Symbol> typeSymbol = this.semanticModel.symbol(fileName,
                typeDefinitionNode.typeName().lineRange().startLine());

        if (typeSymbol.isPresent() && typeSymbol.get() instanceof TypeDefinitionSymbol) {
            TypeSymbol rawType = getRawType(((TypeDefinitionSymbol) typeSymbol.get()).typeDescriptor());
            String typeName = String.format("%s:%s", typeSymbol.get().moduleID().toString(), typeSymbol.get().name());
            if (rawType.typeKind() == TypeDescKind.RECORD || rawType.typeKind() == TypeDescKind.UNION) {
                this.records.put(typeName, typeDefinitionNode);
            }
        }
    }

    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        return typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE
                ? ((TypeReferenceTypeSymbol) typeDescriptor).typeDescriptor() : typeDescriptor;
    }
}
