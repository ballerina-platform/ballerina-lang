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

import com.google.gson.annotations.Expose;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.EnumType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ErrorType;
import org.ballerinalang.diagramutil.connector.models.connector.types.InclusionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.IntersectionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ObjectType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.diagramutil.connector.models.connector.types.StreamType;
import org.ballerinalang.diagramutil.connector.models.connector.types.UnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Type model.
 */
public class Type {

    static ArrayList<Integer> parentSymbols = new ArrayList<Integer>();
    @Expose
    public String name;
    @Expose
    public String typeName;
    @Expose
    public boolean optional;
    @Expose
    public TypeInfo typeInfo;
    @Expose
    public boolean defaultable;
    @Expose
    public String defaultValue;
    @Expose
    public Map<String, String> displayAnnotation;
    @Expose
    public String documentation;

    public static Optional<Type> fromSyntaxNode(Node node, SemanticModel semanticModel) {
        Optional<Type> type = Optional.empty();

        switch (node.kind()) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                Optional<Symbol> optSymbol = null;
                try {
                    optSymbol = semanticModel.symbol(node);
                } catch (NullPointerException ignored) {
                }
                if (optSymbol != null && optSymbol.isPresent()) {
                    Symbol symbol = optSymbol.get();
                    type = Optional.of(fromSemanticSymbol(symbol));
                    parentSymbols.clear();
                }
                break;
            case OPTIONAL_TYPE_DESC:
                OptionalTypeDescriptorNode optionalTypeDescriptorNode = (OptionalTypeDescriptorNode) node;
                type = fromSyntaxNode(optionalTypeDescriptorNode.typeDescriptor(), semanticModel);
                if (type.isPresent()) {
                    Type optionalType = type.get();
                    optionalType.optional = true;
                    type = Optional.of(optionalType);
                }
                break;
            case UNION_TYPE_DESC:
                UnionType unionType = new UnionType();
                flattenUnionNode(node, semanticModel, unionType.members);
                type = Optional.of(unionType);
                break;
            case INTERSECTION_TYPE_DESC:
                IntersectionType intersectionType = new IntersectionType();
                flattenIntersectionNode(node, semanticModel, intersectionType.members);
                type = Optional.of(intersectionType);
                break;
            case ARRAY_TYPE_DESC:
                ArrayTypeDescriptorNode arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) node;
                Optional<Type> syntaxNode = fromSyntaxNode(arrayTypeDescriptorNode.memberTypeDesc(), semanticModel);
                if (syntaxNode.isPresent()) {
                    type = Optional.of(new ArrayType(syntaxNode.get()));
                }
                break;
            case STREAM_TYPE_DESC:
                StreamTypeDescriptorNode streamNode = (StreamTypeDescriptorNode) node;
                StreamTypeParamsNode streamParams = streamNode.streamTypeParamsNode().isPresent() ?
                        (StreamTypeParamsNode) streamNode.streamTypeParamsNode().get() : null;
                Optional<Type> leftParam = Optional.empty();
                Optional<Type> rightParam = Optional.empty();
                if (streamParams != null) {
                    leftParam = fromSyntaxNode(streamParams.leftTypeDescNode(), semanticModel);
                    if (streamParams.rightTypeDescNode().isPresent()) {
                        rightParam = fromSyntaxNode(streamParams.rightTypeDescNode().get(), semanticModel);
                    }
                }
                type = Optional.of(new StreamType(leftParam, rightParam));
                break;
            case RECORD_TYPE_DESC:
                RecordTypeDescriptorNode recordNode = (RecordTypeDescriptorNode) node;
                List<Type> fields = new ArrayList<>();
                recordNode.fields().forEach(node1 -> {
                    Optional<Type> optionalType = fromSyntaxNode(node1, semanticModel);
                    if (optionalType.isPresent()) {
                        fields.add(optionalType.get());
                    }
                });

                Optional<Type> restType = recordNode.recordRestDescriptor().isPresent() ?
                        fromSyntaxNode(recordNode.recordRestDescriptor().get().typeName(), semanticModel) :
                        Optional.empty();
                type = Optional.of(new RecordType(fields, restType));
                break;
            case RECORD_FIELD:
                RecordFieldNode recordField = (RecordFieldNode) node;
                type = fromSyntaxNode(recordField.typeName(), semanticModel);
                if (type.isPresent()) {
                    Type recordType = type.get();
                    recordType.name = recordField.fieldName().text();
                    type = Optional.of(recordType);
                }
                break;
            // TODO: Check syntax tree compatible version for ErrorTypeDescriptorNode.
//            case ERROR_TYPE_DESC:
//                ErrorTypeDescriptorNode errorType = (ErrorTypeDescriptorNode) node;
//                type = new PrimitiveType(errorType.errorKeywordToken().text());
//                break;
            default:
                if (node instanceof BuiltinSimpleNameReferenceNode) {
                    BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode =
                            (BuiltinSimpleNameReferenceNode) node;
                    type = Optional.of(new PrimitiveType(builtinSimpleNameReferenceNode.name().text()));
                } else {
                    type = Optional.of(new PrimitiveType(node.toSourceCode()));
                }
                break;
        }

        return type;
    }

    public static void flattenUnionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node.kind() == SyntaxKind.UNION_TYPE_DESC) {
            UnionTypeDescriptorNode unionTypeNode = (UnionTypeDescriptorNode) node;
            flattenUnionNode(unionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(unionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        Optional<Type> optionalType = fromSyntaxNode(node, semanticModel);
        if (optionalType.isPresent()) {
            fields.add(optionalType.get());
        }
    }

    public static void flattenIntersectionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node.kind() == SyntaxKind.INTERSECTION_TYPE_DESC) {
            IntersectionTypeDescriptorNode intersectionTypeNode = (IntersectionTypeDescriptorNode) node;
            flattenUnionNode(intersectionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(intersectionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        Optional<Type> optionalType = fromSyntaxNode(node, semanticModel);
        if (optionalType.isPresent()) {
            fields.add(optionalType.get());
        }
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
            int signature = recordTypeSymbol.hashCode();
            if (parentSymbols.contains(signature)) {
                return null;
            }
            parentSymbols.add(signature);

            List<Type> fields = new ArrayList<>();
            recordTypeSymbol.fieldDescriptors().forEach((name, field) -> {
                Type subType = fromSemanticSymbol(field.typeDescriptor());
                if (subType != null) {
                    subType.name = name;
                    subType.optional = field.isOptional();
                    subType.defaultable = field.hasDefaultValue();
                    fields.add(subType);
                }
            });
            Type restType = recordTypeSymbol.restTypeDescriptor().isPresent() ?
                    fromSemanticSymbol(recordTypeSymbol.restTypeDescriptor().get()) : null;
            type = new RecordType(fields, restType);
            parentSymbols.remove(parentSymbols.size() - 1);
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
        } else if (symbol instanceof StreamTypeSymbol) {
            StreamTypeSymbol streamTypeSymbol = (StreamTypeSymbol) symbol;
            type = fromSemanticSymbol(streamTypeSymbol.typeParameter());
        } else if (symbol instanceof ObjectTypeSymbol) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) symbol;
            ObjectType objectType = new ObjectType();
            objectTypeSymbol.fieldDescriptors().forEach((typeName, typeSymbol) -> {
                Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                if (semanticSymbol != null) {
                    objectType.fields.add(semanticSymbol);
                }
            });
            objectTypeSymbol.typeInclusions().forEach(typeSymbol -> {
                Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                if (semanticSymbol != null) {
                    objectType.fields.add(new InclusionType(semanticSymbol));
                }
            });
            type = objectType;
        } else if (symbol instanceof RecordFieldSymbol) {
            RecordFieldSymbol recordFieldSymbol = (RecordFieldSymbol) symbol;
            type = fromSemanticSymbol(recordFieldSymbol.typeDescriptor());
        } else if (symbol instanceof ParameterSymbol) {
            ParameterSymbol parameterSymbol = (ParameterSymbol) symbol;
            type = fromSemanticSymbol(parameterSymbol.typeDescriptor());
            if (type != null) {
                type.defaultable = parameterSymbol.paramKind() == ParameterKind.DEFAULTABLE;
            }
        } else if (symbol instanceof VariableSymbol) {
            VariableSymbol variableSymbol = (VariableSymbol) symbol;
            type = fromSemanticSymbol(variableSymbol.typeDescriptor());
        } else if (symbol instanceof TypeSymbol) {
            type = new PrimitiveType(((TypeSymbol) symbol).signature());
        }
        return type;
    }

    public static void clearParentSymbols() {
        parentSymbols.clear();
    }

}
