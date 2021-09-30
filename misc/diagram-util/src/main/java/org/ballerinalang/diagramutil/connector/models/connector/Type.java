/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaRecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.central.client.model.connector.BalType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.EnumType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ErrorType;
import org.ballerinalang.diagramutil.connector.models.connector.types.IntersectionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.diagramutil.connector.models.connector.types.StreamType;
import org.ballerinalang.diagramutil.connector.models.connector.types.UnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Type model.
 */
public class Type extends BalType {

    static ArrayList<String> parentSymbols = new ArrayList<String>();

    public static Type fromSyntaxNode(Node node, SemanticModel semanticModel) {
        Type type = null;
        if (node instanceof SimpleNameReferenceNode || node instanceof QualifiedNameReferenceNode) {
            Optional<Symbol> optSymbol = null;
            try {
                optSymbol = semanticModel.symbol(node);
            } catch (NullPointerException ignored) {
            }
            if (optSymbol != null && optSymbol.isPresent()) {
                Symbol symbol = optSymbol.get();
                type = fromSemanticSymbol(symbol);
                parentSymbols.clear();
            }
        } else if (node instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode = (BuiltinSimpleNameReferenceNode) node;
            type = new PrimitiveType(builtinSimpleNameReferenceNode.name().text());
        } else if (node instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode optionalTypeDescriptorNode = (OptionalTypeDescriptorNode) node;
            type = fromSyntaxNode(optionalTypeDescriptorNode.typeDescriptor(), semanticModel);
            type.optional = true;
            // todo: check syntax tree compatible version for ErrorTypeDescriptorNode
//        } else if (node instanceof ErrorTypeDescriptorNode) {
//            ErrorTypeDescriptorNode errorType = (ErrorTypeDescriptorNode) node;
//            type = new PrimitiveType(errorType.errorKeywordToken().text());
        } else if (node instanceof UnionTypeDescriptorNode) {
            UnionType unionType = new UnionType();
            flattenUnionNode(node, semanticModel, unionType.members);
            type = unionType;
        } else if (node instanceof IntersectionTypeDescriptorNode) {
            IntersectionType intersectionType = new IntersectionType();
            flattenIntersectionNode(node, semanticModel, intersectionType.members);
            type = intersectionType;
        } else if (node instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) node;
            type = new ArrayType(fromSyntaxNode(arrayTypeDescriptorNode.memberTypeDesc(), semanticModel));
        } else if (node instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode streamNode = (StreamTypeDescriptorNode) node;
            StreamTypeParamsNode streamParams = streamNode.streamTypeParamsNode().isPresent() ?
                    (StreamTypeParamsNode) streamNode.streamTypeParamsNode().get() : null;
            Type leftParam = null, rightParam = null;
            if (streamParams != null) {
                leftParam = fromSyntaxNode(streamParams.leftTypeDescNode(), semanticModel);
                if (streamParams.rightTypeDescNode().isPresent()) {
                    rightParam = fromSyntaxNode(streamParams.rightTypeDescNode().get(), semanticModel);
                }
            }
            type = new StreamType(leftParam, rightParam);
        } else if (node instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode recordNode = (RecordTypeDescriptorNode) node;
            List<Type> fields = new ArrayList<>();
            recordNode.fields().forEach(node1 -> fields.add(fromSyntaxNode(node1, semanticModel)));
            Type restType = recordNode.recordRestDescriptor().isPresent() ?
                    fromSyntaxNode(recordNode.recordRestDescriptor().get().typeName(), semanticModel) : null;
            type = new RecordType(fields, restType);
        } else if (node instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) node;
            type = fromSyntaxNode(recordField.typeName(), semanticModel);
            type.name = recordField.fieldName().text();
        } else {
            type = new PrimitiveType(node.toSourceCode());
        }
        return type;
    }

    public static void flattenUnionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode unionTypeNode = (UnionTypeDescriptorNode) node;
            flattenUnionNode(unionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(unionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        fields.add(fromSyntaxNode(node, semanticModel));
    }

    public static void flattenIntersectionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode intersectionTypeNode = (IntersectionTypeDescriptorNode) node;
            flattenUnionNode(intersectionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(intersectionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        fields.add(fromSyntaxNode(node, semanticModel));
    }

    public static Type fromSemanticSymbol(Symbol symbol) {
        Type type = null;

        if (symbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) symbol;
            if (typeReferenceTypeSymbol.definition().kind().equals(SymbolKind.ENUM)) {
                List<Type> fields = new ArrayList<>();
                ((UnionTypeSymbol) typeReferenceTypeSymbol.typeDescriptor()).memberTypeDescriptors()
                        .forEach(typeSymbol -> {
                            Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                            if (semanticSymbol != null) {
                                fields.add(semanticSymbol);
                            }
                        });
                type = new EnumType(fields);
            } else {
                type = fromSemanticSymbol(typeReferenceTypeSymbol.typeDescriptor());
            }
            if (type != null && symbol.getName().isPresent() && symbol.getModule().isPresent()) {
                ModuleID moduleID = symbol.getModule().get().id();
                type.typeInfo = new TypeInfo(symbol.getName().get(), moduleID.orgName(), moduleID.moduleName(),
                        null, moduleID.version());
                type.name = typeReferenceTypeSymbol.getName().isPresent() ? typeReferenceTypeSymbol.getName().get()
                        : null;
            }
        } else if (symbol instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) symbol;
            String symbolName = ((BallerinaRecordTypeSymbol) recordTypeSymbol).getBType().toString();
            if (parentSymbols.contains(symbolName)) {
                return null;
            } else if (!symbolName.contains("record {")) {
                // TODO: need to figure it out anonymous record types proper way
                parentSymbols.add(symbolName);
            }

            List<Type> fields = new ArrayList<>();
            recordTypeSymbol.fieldDescriptors().forEach((name, field) -> {
                Type subType = fromSemanticSymbol(field.typeDescriptor());
                if (subType != null) {
                    subType.name = name;
                    subType.optional = field.isOptional();
                    fields.add(subType);
                }
            });
            Type restType = recordTypeSymbol.restTypeDescriptor().isPresent() ?
                    fromSemanticSymbol(recordTypeSymbol.restTypeDescriptor().get()) : null;
            type = new RecordType(fields, restType);
        } else if (symbol instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            type = new ArrayType(fromSemanticSymbol(arrayTypeSymbol.memberTypeDescriptor()));
        } else if (symbol instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionSymbol = (UnionTypeSymbol) symbol;
            UnionType unionType = new UnionType();
            unionSymbol.memberTypeDescriptors().forEach(typeSymbol -> {
                Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                if (semanticSymbol != null) {
                    unionType.members.add(semanticSymbol);
                }
            });
            if (unionType.members.stream().allMatch(type1 -> type1 instanceof ErrorType)) {
                ErrorType errType = new ErrorType();
                errType.isErrorUnion = true;
                errType.errorUnion = unionType;
                type = errType;
            } else {
                type = unionType;
            }
        } else if (symbol instanceof ErrorTypeSymbol) {
            ErrorTypeSymbol errSymbol = (ErrorTypeSymbol) symbol;
            ErrorType errType = new ErrorType();
            if (errSymbol.detailTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                errType.detailType = fromSemanticSymbol(errSymbol.detailTypeDescriptor());
            }
            type = errType;
        } else if (symbol instanceof IntersectionTypeSymbol) {
            IntersectionTypeSymbol intersectionTypeSymbol = (IntersectionTypeSymbol) symbol;
            IntersectionType intersectionType = new IntersectionType();
            intersectionTypeSymbol.memberTypeDescriptors().forEach(typeSymbol -> {
                Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                if (semanticSymbol != null) {
                    intersectionType.members.add(semanticSymbol);
                }
            });
            type = intersectionType;
        } else if (symbol instanceof TypeSymbol) {
            type = new PrimitiveType(((TypeSymbol) symbol).signature());
        }
        return type;
    }

}
