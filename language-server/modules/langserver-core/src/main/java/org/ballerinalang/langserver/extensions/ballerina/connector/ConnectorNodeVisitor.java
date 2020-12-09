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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.runtime.api.types.RemoteFunctionType;
import java.util.Optional;
import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.ObjectType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common node visitor to override and remove assertion errors from BLangNodeVisitor methods.
 */
public class ConnectorNodeVisitor extends NodeVisitor {

    private final String name;
//    private Map<PackageID, BLangImportPackage> packageMap = new HashMap<>();
    private final boolean found = false;
    private final List<ClassDefinitionNode> connectors;
    private final Map<String, TypeDefinitionNode> records;
//    private boolean inClient = false;

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
        if (!found) {
            for (int i = 0; i < modulePartNode.members().size(); i++) {
                modulePartNode.members().get(i).accept(this);
            }
        }
    }

    public void visit(RemoteFunctionType remoteFunctionType) {
        boolean x=false;
    }

//    public void visit(FunctionDefinitionNode functionDefinitionNode) {
//        if (inClient) {
//            boolean x = false;
//            functionDefinitionNode.childEntries().stream().filter(e->e.name().equals("qualifierList")).findFirst().get().node().get().internalNode().firstToken().text();
//        }
//    }

    public void visit(ClassDefinitionNode classDefinitionNode) {
        String fileName = classDefinitionNode.syntaxTree().filePath();
        Optional<TypeSymbol> typeSymbol = this.semanticModel.type(fileName, classDefinitionNode.className().lineRange());

        if (typeSymbol.isPresent()) {
            TypeSymbol rawType = getRawType(typeSymbol.get());
            if (rawType.typeKind() == TypeDescKind.OBJECT) {
                ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol)rawType;

                boolean isClient = objectTypeSymbol.typeQualifiers().contains(ObjectTypeSymbol.TypeQualifier.CLIENT);
                if (isClient) {
                    this.connectors.add(classDefinitionNode);
//                    this.inClient = true;
//                    for (int i = 0; i < classDefinitionNode.children().size(); i++) {
//                        classDefinitionNode.children().get(i).accept(this);
//                    }
//                    this.inClient = false;

                }

            }
        }
//        ((ObjectTypeSymbol)getRawType((this.semanticModel.type(classDefinitionNode.syntaxTree().filePath(), classDefinitionNode.className().lineRange())).get()) ).typeQualifiers();
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        if (!found) {
            String fileName = typeDefinitionNode.syntaxTree().filePath();
            Optional<TypeSymbol> typeSymbol = this.semanticModel.type(fileName, typeDefinitionNode.typeDescriptor().lineRange());
//            Optional<Symbol> symbol = this.semanticModel.symbol(typeDefinitionNode.syntaxTree().filePath(), typeDefinitionNode.typeName().lineRange().startLine());

            if (typeSymbol.isPresent()) {
                TypeSymbol rawType = getRawType(typeSymbol.get());
                String typeName = typeSymbol.get().name();
                if (rawType.typeKind() == TypeDescKind.RECORD || rawType.typeKind() == TypeDescKind.UNION) {
                    this.records.put(typeName, typeDefinitionNode);
                }
            }

        }
    }

    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        return typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE
                ? ((TypeReferenceTypeSymbol) typeDescriptor).typeDescriptor() : typeDescriptor;
    }
//
//    @Override
//    public void visit(BLangTypeDefinition typeDefinition) {
//        if (typeDefinition.getTypeNode() instanceof BLangObjectTypeNode) {
//            if (((BLangObjectTypeNode) typeDefinition.getTypeNode()).flagSet.contains(Flag.CLIENT)) {
//                this.connectors.add(typeDefinition);
//                typeDefinition.getTypeNode().accept(this);
//            }
//
//        } else if (typeDefinition.getTypeNode() instanceof BLangRecordTypeNode) {
//            this.records.put(((BLangRecordTypeNode) typeDefinition.getTypeNode()).symbol.type.toString(),
//                    typeDefinition);
//        } else if (typeDefinition.getTypeNode() instanceof BLangUnionTypeNode) {
//            this.records.put(((BLangUnionTypeNode) typeDefinition.getTypeNode()).type.toString(),
//                    typeDefinition);
//        }
//    }


}
