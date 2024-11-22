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
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.EnumType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ErrorType;
import org.ballerinalang.diagramutil.connector.models.connector.types.InclusionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.IntersectionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.MapType;
import org.ballerinalang.diagramutil.connector.models.connector.types.ObjectType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.diagramutil.connector.models.connector.types.StreamType;
import org.ballerinalang.diagramutil.connector.models.connector.types.TableType;
import org.ballerinalang.diagramutil.connector.models.connector.types.UnionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Type model.
 */
public class Type {

    private static final Map<String, VisitedType> visitedTypeMap = new HashMap<>();

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
    @Expose
    public boolean isRestType;

    public Type() {
    }

    public Type(String name, String typeName, boolean optional, TypeInfo typeInfo, boolean defaultable,
                String defaultValue, Map<String, String> displayAnnotation, String documentation) {
        this.name = name;
        this.typeName = typeName;
        this.optional = optional;
        this.typeInfo = typeInfo;
        this.defaultable = defaultable;
        this.defaultValue = defaultValue;
        this.displayAnnotation = displayAnnotation;
        this.documentation = documentation;
    }

    public static void clearVisitedTypeMap() {
        visitedTypeMap.clear();
    }

    public static Optional<Type> fromSyntaxNode(Node node, SemanticModel semanticModel) {
        Optional<Type> type = Optional.empty();

        switch (node.kind()) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                Optional<Symbol> optSymbol = Optional.empty();
                try {
                    optSymbol = semanticModel.symbol(node);
                } catch (NullPointerException ignored) {
                }
                if (optSymbol != null && optSymbol.isPresent()) {
                    Symbol symbol = optSymbol.get();
                    type = Optional.of(fromSemanticSymbol(symbol));
                    clearVisitedTypeMap();
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
                    optionalType.ifPresent(fields::add);
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
            case MAP_TYPE_DESC:
                MapTypeDescriptorNode mapNode = (MapTypeDescriptorNode) node;
                Optional<Type> mapStNode = fromSyntaxNode(mapNode.mapTypeParamsNode().typeNode(), semanticModel);
                if (mapStNode.isPresent()) {
                    type = Optional.of(new MapType(mapStNode.get()));
                }
                break;
            case TABLE_TYPE_DESC:
                TableTypeDescriptorNode tableTypeNode = (TableTypeDescriptorNode) node;
                Optional<Symbol> optTableTypeSymbol = Optional.empty();
                TableTypeSymbol tableTypeSymbol = null;
                List<String> keySpecifiers = null;
                try {
                    optTableTypeSymbol = semanticModel.symbol(tableTypeNode);
                } catch (NullPointerException ignored) {
                }
                if (optTableTypeSymbol != null && optTableTypeSymbol.isPresent()) {
                    tableTypeSymbol = (TableTypeSymbol) optTableTypeSymbol.get();
                }
                if (tableTypeSymbol != null) {
                    keySpecifiers = tableTypeSymbol.keySpecifiers();
                }
                if (tableTypeNode.keyConstraintNode().isEmpty()) {
                    break;
                }
                Node keyConstraint = tableTypeNode.keyConstraintNode().get();
                Optional<Type> tableStNode = fromSyntaxNode(tableTypeNode.rowTypeParameterNode(), semanticModel);
                Optional<Type> constraintStNode = fromSyntaxNode(keyConstraint, semanticModel);
                if (tableStNode.isPresent() && constraintStNode.isPresent()) {
                    type = Optional.of(new TableType(tableStNode.get(), keySpecifiers, constraintStNode.get()));
                }
                break;
            default:
                if (node instanceof BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
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
        optionalType.ifPresent(fields::add);
    }

    public static void flattenIntersectionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node.kind() == SyntaxKind.INTERSECTION_TYPE_DESC) {
            IntersectionTypeDescriptorNode intersectionTypeNode = (IntersectionTypeDescriptorNode) node;
            flattenUnionNode(intersectionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(intersectionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        Optional<Type> optionalType = fromSyntaxNode(node, semanticModel);
        optionalType.ifPresent(fields::add);
    }

    public static VisitedType getVisitedType(String typeName) {
        if (visitedTypeMap.containsKey(typeName)) {
            return visitedTypeMap.get(typeName);
        }
        return null;
    }

    public static void completeVisitedTypeEntry(String typeName, Type typeNode) {
        VisitedType visitedType = visitedTypeMap.get(typeName);
        visitedType.setCompleted(true);
        visitedType.setTypeNode(typeNode);
    }

    public static Type fromSemanticSymbol(Symbol symbol) {
        Type type = null;
        if (symbol instanceof TypeReferenceTypeSymbol typeReferenceTypeSymbol) {
            type = getEnumType(typeReferenceTypeSymbol, symbol);
        } else if (symbol instanceof RecordTypeSymbol recordTypeSymbol) {
            String typeName = String.valueOf(recordTypeSymbol.hashCode());
            VisitedType visitedType = getVisitedType(typeName);
            if (visitedType != null) {
                return getAlreadyVisitedType(symbol, typeName, visitedType, false);
            } else {
                if (typeName.contains("record {")) {
                    type = getRecordType(recordTypeSymbol);
                } else {
                    visitedTypeMap.put(typeName, new VisitedType());
                    type = getRecordType(recordTypeSymbol);
                    completeVisitedTypeEntry(typeName, type);
                }
            }
        } else if (symbol instanceof ArrayTypeSymbol arrayTypeSymbol) {
            type = new ArrayType(fromSemanticSymbol(arrayTypeSymbol.memberTypeDescriptor()));
        } else if (symbol instanceof MapTypeSymbol mapTypeSymbol) {
            type = new MapType(fromSemanticSymbol(mapTypeSymbol.typeParam()));
        } else if (symbol instanceof TableTypeSymbol tableTypeSymbol) {
            TypeSymbol keyConstraint = null;
            if (tableTypeSymbol.keyConstraintTypeParameter().isPresent()) {
                keyConstraint = tableTypeSymbol.keyConstraintTypeParameter().get();
            }
            type = new TableType(fromSemanticSymbol(tableTypeSymbol.rowTypeParameter()),
                    tableTypeSymbol.keySpecifiers(), fromSemanticSymbol(keyConstraint));
        } else if (symbol instanceof UnionTypeSymbol unionSymbol) {
            String typeName = String.valueOf(unionSymbol.hashCode());
            VisitedType visitedType = getVisitedType(typeName);
            if (visitedType != null) {
                return getAlreadyVisitedType(symbol, typeName, visitedType, true);
            } else {
                visitedTypeMap.put(typeName, new VisitedType());
                type = getUnionType(unionSymbol);
                completeVisitedTypeEntry(typeName, type);
            }
        } else if (symbol instanceof ErrorTypeSymbol errSymbol) {
            ErrorType errType = new ErrorType();
            if (errSymbol.detailTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                errType.detailType = fromSemanticSymbol(errSymbol.detailTypeDescriptor());
            }
            type = errType;
        } else if (symbol instanceof IntersectionTypeSymbol intersectionTypeSymbol) {
            String typeName = String.valueOf(intersectionTypeSymbol.hashCode());
            VisitedType visitedType = getVisitedType(typeName);
            if (visitedType != null) {
                return getAlreadyVisitedType(symbol, typeName, visitedType, false);
            } else {
                visitedTypeMap.put(typeName, new VisitedType());
                type = getIntersectionType(intersectionTypeSymbol);
                completeVisitedTypeEntry(typeName, type);
            }
        } else if (symbol instanceof StreamTypeSymbol streamTypeSymbol) {
            type = getStreamType(streamTypeSymbol);
        } else if (symbol instanceof ObjectTypeSymbol objectTypeSymbol) {
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
        } else if (symbol instanceof RecordFieldSymbol recordFieldSymbol) {
            type = fromSemanticSymbol(recordFieldSymbol.typeDescriptor());
        } else if (symbol instanceof ParameterSymbol parameterSymbol) {
            type = fromSemanticSymbol(parameterSymbol.typeDescriptor());
            if (type != null) {
                type.defaultable = parameterSymbol.paramKind() == ParameterKind.DEFAULTABLE;
            }
        } else if (symbol instanceof VariableSymbol variableSymbol) {
            type = fromSemanticSymbol(variableSymbol.typeDescriptor());
        } else if (symbol instanceof TypeSymbol typeSymbol) {
            String typeName = typeSymbol.signature();
            if (typeName.startsWith("\"") && typeName.endsWith("\"")) {
                typeName = typeName.substring(1, typeName.length() - 1);
            }
            type = new PrimitiveType(typeName);
        }
        return type;
    }

    private static Type getAlreadyVisitedType(Symbol symbol, String typeName, VisitedType visitedType,
                                              boolean getClone) {
        if (visitedType.isCompleted()) {
            Type existingType = visitedType.getTypeNode();
            if (getClone) {
                if (existingType instanceof UnionType unionType) {
                    return new UnionType(unionType);
                }
                return new Type(existingType.getName(), existingType.getTypeName(), existingType.isOptional(),
                        existingType.getTypeInfo(), existingType.isDefaultable(), existingType.getDefaultValue(),
                        existingType.getDisplayAnnotation(), existingType.getDocumentation());
            }
            if (existingType instanceof RecordType recordType) {
                return new RecordType(recordType);
            }
            return existingType;
        } else {
            Type type = new Type();
            setTypeInfo(typeName, symbol, type);
            return type;
        }
    }

    private static Type getIntersectionType(IntersectionTypeSymbol intersectionTypeSymbol) {
        Type type;
        IntersectionType intersectionType = new IntersectionType();
        intersectionTypeSymbol.memberTypeDescriptors().forEach(typeSymbol -> {
            Type semanticSymbol = fromSemanticSymbol(typeSymbol);
            if (semanticSymbol != null) {
                intersectionType.members.add(semanticSymbol);
            }
        });

        type = intersectionType;
        return type;
    }

    private static Type getUnionType(UnionTypeSymbol unionSymbol) {
        Type type;
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
        return type;
    }

    private static Type getRecordType(RecordTypeSymbol recordTypeSymbol) {
        Type type;
        List<Type> fields = new ArrayList<>();
        recordTypeSymbol.fieldDescriptors().forEach((name, field) -> {
            Type subType = fromSemanticSymbol(field.typeDescriptor());
            if (subType != null) {
                subType.setName(name);
                subType.setOptional(field.isOptional());
                subType.setDefaultable(field.hasDefaultValue());
                fields.add(subType);
            }
        });
        Type restType = recordTypeSymbol.restTypeDescriptor().isPresent() ?
                fromSemanticSymbol(recordTypeSymbol.restTypeDescriptor().get()) : null;
        type = new RecordType(fields, restType);
        return type;
    }

    private static Type getEnumType(TypeReferenceTypeSymbol typeReferenceTypeSymbol, Symbol symbol) {
        Type type;
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
        setTypeInfo(typeReferenceTypeSymbol.getName().isPresent() ? typeReferenceTypeSymbol.getName().get()
                : null, symbol, type);
        return type;
    }

    private static Type getStreamType(StreamTypeSymbol streamSymbol) {
        Type leftType = fromSemanticSymbol(streamSymbol.typeParameter());
        Type rightType = fromSemanticSymbol(streamSymbol.completionValueTypeParameter());
        return new StreamType(leftType, rightType);
    }

    public static void clearParentSymbols() {
        clearVisitedTypeMap();
    }

    private static void setTypeInfo(String typeName, Symbol symbol, Type type) {
        if (type != null && symbol.getName().isPresent() && symbol.getModule().isPresent()) {
            ModuleID moduleID = symbol.getModule().get().id();
            type.typeInfo = new TypeInfo(symbol.getName().get(), moduleID.orgName(), moduleID.moduleName(),
                    null, moduleID.version());
            type.name = typeName;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public boolean isDefaultable() {
        return defaultable;
    }

    public void setDefaultable(boolean defaultable) {
        this.defaultable = defaultable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Map<String, String> getDisplayAnnotation() {
        return displayAnnotation;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public void setRestType(boolean restType) {
        isRestType = restType;
    }
}
