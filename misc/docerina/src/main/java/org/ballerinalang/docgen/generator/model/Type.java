/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.docgen.generator.model;

import com.google.gson.annotations.Expose;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MemberTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.generator.model.types.FunctionType;
import org.ballerinalang.docgen.generator.model.types.ObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a Ballerina Type.
 */
public class Type {
    @Expose
    public String orgName;
    @Expose
    public String moduleName;
    @Expose
    public String version;
    @Expose
    public String name;
    @Expose
    public String description;
    @Expose
    public String category;
    @Expose
    public boolean isAnonymousUnionType;
    @Expose
    public boolean isInclusion;
    @Expose
    public boolean isArrayType;
    @Expose
    public boolean isNullable;
    @Expose
    public boolean isTuple;
    @Expose
    public boolean isIntersectionType;
    @Expose
    public boolean isParenthesisedType;
    @Expose
    public boolean isTypeDesc;
    @Expose
    public boolean isRestParam;
    @Expose
    public boolean isDeprecated;
    @Expose
    public boolean isPublic;
    @Expose
    public boolean generateUserDefinedTypeLink = true;
    @Expose
    public List<Type> memberTypes = new ArrayList<>();
    @Expose
    public int arrayDimensions;
    @Expose
    public Type elementType;
    @Expose
    public Type constraint;

    private static final Logger log = LoggerFactory.getLogger(Type.class);

    private static final String CATEGORY_NOT_FOUND = "not_found";
    private static final String CATEGORY_INLINE_RECORD = "inline_record";
    private static final String CATEGORY_INLINE_CLOSED_RECORD = "inline_closed_record";

    public Type() {
    }

    public static Type fromNode(Node node, SemanticModel semanticModel) {
        Type type = new Type();
        if (node instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode simpleNameReferenceNode = (SimpleNameReferenceNode) node;
            type.name = simpleNameReferenceNode.name().text();
            type.category = "reference";
            Optional<Symbol> symbol = Optional.empty();
            try {
                symbol = semanticModel.symbol(node);
            } catch (NullPointerException nullException) {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    log.error("Symbol find threw null pointer in : Line range:" + node.lineRange());
                }
            }
            if (symbol != null && symbol.isPresent()) {
                if (symbol.get() instanceof TypeReferenceTypeSymbol &&
                        !Type.isPublic(((TypeReferenceTypeSymbol) symbol.get()).definition())) {
                    type = fromSemanticSymbol(symbol.get(), Optional.empty(), null, false);
                } else {
                    resolveSymbolMetaData(type, symbol.get());
                }
            } else {
                type.generateUserDefinedTypeLink = false;
            }
        } else if (node instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode qualifiedNameReferenceNode = (QualifiedNameReferenceNode) node;
            type.moduleName = qualifiedNameReferenceNode.modulePrefix().text();
            type.category = "reference";
            type.name = qualifiedNameReferenceNode.identifier().text();
            Optional<Symbol> symbol = Optional.empty();
            try {
                symbol = semanticModel.symbol(node);
            } catch (NullPointerException nullException) {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    log.error("Symbol find threw null pointer in : Line range:" + node.lineRange());
                }
            }
            if (symbol != null && symbol.isPresent()) {
                if (symbol.get() instanceof TypeReferenceTypeSymbol &&
                        !Type.isPublic(((TypeReferenceTypeSymbol) symbol.get()).definition())) {
                    type = fromSemanticSymbol(symbol.get(), Optional.empty(), null, false);
                } else {
                    resolveSymbolMetaData(type, symbol.get());
                }
            } else {
                type.generateUserDefinedTypeLink = false;
            }
        } else if (node instanceof TypeReferenceNode) {
            Optional<Symbol> symbol = Optional.empty();
            try {
                symbol = semanticModel.symbol(node);
            } catch (NullPointerException nullException) {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    log.error("Symbol find threw null pointer in : Line range:" + node.lineRange());
                }
            }
            if (symbol != null && symbol.isPresent()) {
                type = fromSemanticSymbol(symbol.get(), Optional.empty(), null, true);
            }
        } else if (node instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode = (BuiltinSimpleNameReferenceNode) node;
            type.name = builtinSimpleNameReferenceNode.name().text();
            type.category = "builtin";
        } else if (node instanceof NilTypeDescriptorNode) {
            type.name = node.toString();
            type.category = "builtin";
        } else if (node instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) node;
            type.isArrayType = true;
            type.arrayDimensions = 1;
            type.elementType = fromNode(arrayTypeDescriptorNode.memberTypeDesc(), semanticModel);
        } else if (node instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode optionalTypeDescriptorNode = (OptionalTypeDescriptorNode) node;
            type = fromNode(optionalTypeDescriptorNode.typeDescriptor(), semanticModel);
            type.isNullable = true;
        } else if (node instanceof UnionTypeDescriptorNode) {
            type.isAnonymousUnionType = true;
            addUnionMemberTypes(node, semanticModel, type.memberTypes);
        } else if (node instanceof IntersectionTypeDescriptorNode) {
            type.isIntersectionType = true;
            addIntersectionMemberTypes(node, semanticModel, type.memberTypes);
        } else if (node instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode recordNode = (RecordTypeDescriptorNode) node;
            List<Type> fields = new ArrayList<>();
            recordNode.fields().forEach(node1 -> {
                if (node1 instanceof RecordFieldWithDefaultValueNode) {
                    RecordFieldWithDefaultValueNode recordField = (RecordFieldWithDefaultValueNode) node1;
                    Type defField = new Type();
                    defField.name = recordField.fieldName().text();
                    defField.elementType = fromNode(recordField.typeName(), semanticModel);
                    fields.add(defField);
                } else if (node1 instanceof RecordFieldNode) {
                    RecordFieldNode recordField = (RecordFieldNode) node1;
                    Type recField = new Type();
                    recField.name = recordField.fieldName().text();
                    recField.elementType = fromNode(recordField.typeName(), semanticModel);
                    fields.add(recField);
                }
            });
            if (recordNode.recordRestDescriptor().isPresent()) {
                Type restField = new Type();
                restField.elementType = fromNode(recordNode.recordRestDescriptor().get(), semanticModel);
                fields.add(restField);
            }
            type.category = (recordNode.bodyStartDelimiter()).kind().equals(SyntaxKind.OPEN_BRACE_PIPE_TOKEN) &&
                    recordNode.recordRestDescriptor().isEmpty() ?
                    CATEGORY_INLINE_CLOSED_RECORD : CATEGORY_INLINE_RECORD;
            type.memberTypes = fields;
        } else if (node instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode streamNode = (StreamTypeDescriptorNode) node;
            StreamTypeParamsNode streamParams = streamNode.streamTypeParamsNode().isPresent() ?
                    (StreamTypeParamsNode) streamNode.streamTypeParamsNode().get() : null;
            type.name = streamNode.streamKeywordToken().text();
            type.category = "stream";
            if (streamParams != null) {
                type.memberTypes.add(fromNode(streamParams.leftTypeDescNode(), semanticModel));
                if (streamParams.rightTypeDescNode().isPresent()) {
                    type.memberTypes.add(fromNode(streamParams.rightTypeDescNode().get(), semanticModel));
                }
            }
        } else if (node instanceof FunctionTypeDescriptorNode) {
            FunctionType functionType = new FunctionType();
            functionType.isLambda = true;
            FunctionTypeDescriptorNode functionDescNode = (FunctionTypeDescriptorNode) node;
            functionType.isIsolated =
                    Generator.containsToken(functionDescNode.qualifierList(), SyntaxKind.ISOLATED_KEYWORD);
            if (functionDescNode.functionSignature().isPresent()) {
                FunctionSignatureNode functionSignature = functionDescNode.functionSignature().get();
                List<DefaultableVariable> variables =
                        Generator.getDefaultableVariableList(functionSignature.parameters(), Optional.empty(),
                                semanticModel);
                functionType.paramTypes.addAll(variables.stream().map((defaultableVariable) -> defaultableVariable.type)
                        .collect(Collectors.toList()));
                if (functionSignature.returnTypeDesc().isPresent()) {
                    ReturnTypeDescriptorNode returnType = functionSignature.returnTypeDesc().get();
                    functionType.returnType = Type.fromNode(returnType.type(), semanticModel);
                }
            }
            type = functionType;
        } else if (node instanceof MapTypeDescriptorNode) {
            MapTypeDescriptorNode mapTypeDesc = (MapTypeDescriptorNode) node;
                type.name = "map";
                type.category = "map";
                type.constraint = fromNode(mapTypeDesc.mapTypeParamsNode().typeNode(), semanticModel);
        } else if (node instanceof ParameterizedTypeDescriptorNode) {
            ParameterizedTypeDescriptorNode parameterizedTypeNode = (ParameterizedTypeDescriptorNode) node;
            SyntaxKind typeKind = node.kind();
            if (typeKind == SyntaxKind.ERROR_TYPE_DESC || typeKind == SyntaxKind.XML_TYPE_DESC) {
                type.name = parameterizedTypeNode.keywordToken().text();
                type.category = "builtin";
            } else if (typeKind == SyntaxKind.FUTURE_TYPE_DESC) {
                type.category = "future";
                type.elementType = parameterizedTypeNode.typeParamNode().map(typeParameterNode ->
                        Type.fromNode(typeParameterNode.typeNode(), semanticModel)).orElse(null);
            } else if (typeKind == SyntaxKind.TYPEDESC_TYPE_DESC) {
                type.elementType = parameterizedTypeNode.typeParamNode().map(typeParameterNode ->
                        Type.fromNode(typeParameterNode.typeNode(), semanticModel)).orElse(null);
                type.isTypeDesc = true;
            }
        } else if (node instanceof ObjectTypeDescriptorNode) {
            ObjectTypeDescriptorNode objectType = (ObjectTypeDescriptorNode) node;
            type.name = objectType.toString();
            type.category = "other";
            type.generateUserDefinedTypeLink = false;
        } else if (node instanceof SingletonTypeDescriptorNode) {
            SingletonTypeDescriptorNode singletonTypeDesc = (SingletonTypeDescriptorNode) node;
            type.name = singletonTypeDesc.simpleContExprNode().toString();
            type.category = "other";
            type.generateUserDefinedTypeLink = false;
        } else if (node instanceof ParenthesisedTypeDescriptorNode) {
            ParenthesisedTypeDescriptorNode parenthesisedNode = (ParenthesisedTypeDescriptorNode) node;
            type.elementType = fromNode(parenthesisedNode.typedesc(), semanticModel);
            type.isParenthesisedType = true;
        } else if (node instanceof TupleTypeDescriptorNode) {
            TupleTypeDescriptorNode typeDescriptor = (TupleTypeDescriptorNode) node;
            type.memberTypes.addAll(typeDescriptor.memberTypeDesc().stream().map(memberType ->
                    Type.fromNode(memberType, semanticModel)).collect(Collectors.toList()));
            type.isTuple = true;
        } else if (node instanceof MemberTypeDescriptorNode) {
            MemberTypeDescriptorNode member = (MemberTypeDescriptorNode) node;
            type = fromNode(member.typeDescriptor(), semanticModel);
        } else if (node instanceof RecordRestDescriptorNode) {
            RecordRestDescriptorNode recordRestDescriptorNode = (RecordRestDescriptorNode) node;
            type.isRestParam = true;
            type.elementType = fromNode(recordRestDescriptorNode.typeName(), semanticModel);
        } else {
            type.name = node.toSourceCode();
            type.generateUserDefinedTypeLink = false;
            type.category = "UNKNOWN";
        }
        return type;
    }

    public static Type fromSemanticSymbol(Symbol symbol, Optional<Documentation> documentation,
                                          TypeReferenceTypeSymbol parentTypeRefSymbol, boolean isTypeInclusion) {
        Type type = new Type();
        if (symbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) symbol;
            Symbol typeDefinition = typeReferenceTypeSymbol.definition();

            // The following condition is required to avoid stackoverflow error, caused by cyclic defintions,
            // ex:- https://github.com/ballerina-platform/ballerina-lang/blob/
            // a1691ea0a24d1f66db5f5e3f1b67202d38128d4a/langlib/jballerina.java/src/main/ballerina/error.bal#L19
            if (!typeReferenceTypeSymbol.equals(parentTypeRefSymbol) &&
                    (!Type.isPublic(typeReferenceTypeSymbol.definition()) || isTypeInclusion)) {
                // Further process type details
                if (typeDefinition instanceof TypeDefinitionSymbol) {
                    type = fromSemanticSymbol(typeReferenceTypeSymbol.typeDescriptor(),
                            ((TypeDefinitionSymbol) typeDefinition).documentation(), typeReferenceTypeSymbol,
                            false);
                } else if (typeDefinition instanceof ClassSymbol) {
                    type = fromSemanticSymbol(typeReferenceTypeSymbol.typeDescriptor(),
                            ((ClassSymbol) typeDefinition).documentation(), typeReferenceTypeSymbol,
                            false);
                }
            }
            if (Type.isPublic(typeReferenceTypeSymbol.definition())) {
                resolveSymbolMetaData(type, symbol);
                type.isPublic = true;
            }
            type.name = typeReferenceTypeSymbol.getName().isPresent() ? typeReferenceTypeSymbol.getName().get() : null;
        } else if (symbol instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) symbol;
            Type recordType = type;
            recordType.name = recordTypeSymbol.getName().isPresent() ? recordTypeSymbol.getName().get() : "";
            recordType.description = documentation.isPresent() && documentation.get().description().isPresent() ?
                    documentation.get().description().get() : "";
            recordTypeSymbol.fieldDescriptors().forEach((name, field) -> {
                Type recField = new Type();
                recField.name = name;
                recField.description = documentation.isPresent() ? documentation.get().parameterMap().get(name) : "";
                recField.elementType = fromSemanticSymbol(field.typeDescriptor(), documentation, parentTypeRefSymbol,
                        isTypeInclusion);
                recordType.memberTypes.add(recField);
            });
            recordTypeSymbol.restTypeDescriptor().ifPresent(typeSymbol -> {
                Type restField = new Type();
                restField.isRestParam = true;
                restField.elementType = fromSemanticSymbol(typeSymbol, documentation, parentTypeRefSymbol,
                        isTypeInclusion);
                restField.description = Generator.REST_FIELD_DESCRIPTION;
                Type recField = new Type();
                recField.elementType = restField;
                recordType.memberTypes.add(recField);
            });
            recordType.category = CATEGORY_INLINE_RECORD;
        } else if (symbol instanceof ObjectTypeSymbol) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) symbol;
            ObjectType objType = new ObjectType();
            objType.name = objectTypeSymbol.getName().isPresent() ? objectTypeSymbol.getName().get() : "";
            objectTypeSymbol.fieldDescriptors().forEach((name, field) -> {
                Type objField = new Type();
                objField.name = name;
                objField.description = documentation.isPresent() ? documentation.get().parameterMap().get(name) : "";
                objField.elementType = fromSemanticSymbol(field.typeDescriptor(), documentation, parentTypeRefSymbol,
                        isTypeInclusion);
                objType.memberTypes.add(objField);
            });
            List<FunctionType> functionTypes = new ArrayList<>();
            objectTypeSymbol.methods().forEach((methodName, methodSymbol) -> {
                FunctionType functionType = new FunctionType();
                functionType.name = methodName;
                functionType.description = methodSymbol.documentation().isPresent() &&
                        methodSymbol.documentation().get().description().isPresent() ?
                        methodSymbol.documentation().get().description().get() : null;
                functionType.category = "included_function";
                functionType.isIsolated = methodSymbol.qualifiers().contains(Qualifier.ISOLATED);
                functionType.isRemote = methodSymbol.qualifiers().contains(Qualifier.REMOTE);
                functionType.isExtern = methodSymbol.external();
                methodSymbol.typeDescriptor().params().ifPresent(parameterSymbols -> {
                    parameterSymbols.forEach(parameterSymbol -> {
                        Type paramType = new Type();
                        paramType.name = parameterSymbol.getName().isPresent() ? parameterSymbol.getName().get() : "";
                        paramType.elementType = fromSemanticSymbol(parameterSymbol.typeDescriptor(),
                                methodSymbol.documentation(), parentTypeRefSymbol, isTypeInclusion);
                        paramType.isDeprecated = parameterSymbol.annotations().stream()
                                .anyMatch(annotationSymbol -> annotationSymbol.getName().get().equals("deprecated"));
                        functionType.paramTypes.add(paramType);
                    });
                if (methodSymbol.typeDescriptor().restParam().isPresent()) {
                    ParameterSymbol restParam = methodSymbol.typeDescriptor().restParam().get();
                    Type restType = fromSemanticSymbol(restParam, methodSymbol.documentation(), parentTypeRefSymbol,
                            isTypeInclusion);
                    restType.isDeprecated = restParam.annotations().stream()
                            .anyMatch(annotationSymbol -> annotationSymbol.getName().get().equals("deprecated"));
                    functionType.paramTypes.add(restType);
                }
                if (methodSymbol.typeDescriptor().returnTypeDescriptor().isPresent()) {
                    Type returnType = fromSemanticSymbol(methodSymbol.typeDescriptor().returnTypeDescriptor().get(),
                            methodSymbol.documentation(), parentTypeRefSymbol, isTypeInclusion);
                    functionType.returnType = returnType;
                }
                });
                functionTypes.add(functionType);
            });
            objType.functionTypes = functionTypes;
            type = objType;
        } else if (symbol instanceof MapTypeSymbol) {
            MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) symbol;
            type.name = "map";
            type.category = "map";
            type.constraint = fromSemanticSymbol(mapTypeSymbol.typeParam(), documentation, parentTypeRefSymbol,
                    isTypeInclusion);
        } else if (symbol instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionSymbol = (UnionTypeSymbol) symbol;
            type.isAnonymousUnionType = true;
            List<Type> unionMembers = new ArrayList<>();
            unionSymbol.memberTypeDescriptors().forEach(typeSymbol -> unionMembers.add(fromSemanticSymbol(typeSymbol,
                    documentation, parentTypeRefSymbol, isTypeInclusion)));
            type.memberTypes = unionMembers;
        } else if (symbol instanceof IntersectionTypeSymbol) {
            IntersectionTypeSymbol intersectionSymbol = (IntersectionTypeSymbol) symbol;
            type.isIntersectionType = true;
            List<Type> intersectionMembers = new ArrayList<>();
            intersectionSymbol.memberTypeDescriptors().forEach(typeSymbol -> intersectionMembers.add(
                    fromSemanticSymbol(typeSymbol, documentation, parentTypeRefSymbol, isTypeInclusion)));
            type.memberTypes = intersectionMembers;
        } else if (symbol instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            type.isArrayType = true;
            type.arrayDimensions = 1;
            type.elementType = fromSemanticSymbol(arrayTypeSymbol.memberTypeDescriptor(), documentation,
                    parentTypeRefSymbol, isTypeInclusion);
        } else if (symbol instanceof TypeSymbol) {
            type.category = "builtin";
            type.name = ((TypeSymbol) symbol).signature();
        }
        return type;
    }

    public static void addUnionMemberTypes(Node node, SemanticModel semanticModel, List<Type> members) {
        if (node instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode unionTypeNode = (UnionTypeDescriptorNode) node;
            addUnionMemberTypes(unionTypeNode.leftTypeDesc(), semanticModel, members);
            addUnionMemberTypes(unionTypeNode.rightTypeDesc(), semanticModel, members);
            return;
        }
        members.add(fromNode(node, semanticModel));
    }

    public static void addIntersectionMemberTypes(Node node, SemanticModel semanticModel, List<Type> members) {
        if (node instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode intersectionTypeNode = (IntersectionTypeDescriptorNode) node;
            addIntersectionMemberTypes(intersectionTypeNode.leftTypeDesc(), semanticModel, members);
            addIntersectionMemberTypes(intersectionTypeNode.rightTypeDesc(), semanticModel, members);
            return;
        }
        members.add(fromNode(node, semanticModel));
    }

    public static void resolveSymbolMetaData(Type type, Symbol symbol) {
        ModuleID moduleID = symbol.getModule().isPresent() ? symbol.getModule().get().id() : null;

        if (moduleID != null) {
            type.moduleName = moduleID.moduleName();
            type.orgName = moduleID.orgName();
            type.version = moduleID.version();
        } else {
            type.moduleName = "UNK_MOD";
            type.orgName = "UNK_ORG";
            type.version = "UNK_VER";
        }

        if (symbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeSymbol = (TypeReferenceTypeSymbol) symbol;
            if (typeSymbol.definition().kind().equals(SymbolKind.ENUM)) {
                type.category = "enums";
            } else if (typeSymbol.typeDescriptor() != null) {
                type.category = getTypeCategory(typeSymbol.typeDescriptor());
            }
        } else if (symbol instanceof ConstantSymbol) {
            type.category = "constants";
        } else if (symbol instanceof VariableSymbol) {
            VariableSymbol variableSymbol = (VariableSymbol) symbol;
            if (variableSymbol.typeDescriptor() != null) {
                type.category = getTypeCategory(variableSymbol.typeDescriptor());
            }
        } else if (symbol instanceof TypeDefinitionSymbol) {
            TypeDefinitionSymbol typeDefSymbol = (TypeDefinitionSymbol) symbol;
            if (typeDefSymbol.typeDescriptor() != null) {
                type.category = getTypeCategory(typeDefSymbol.typeDescriptor());
            }
        }
        if (type.category.equals(CATEGORY_NOT_FOUND)) {
            type.generateUserDefinedTypeLink = false;
        }
    }

    public static String getTypeCategory(TypeSymbol typeDescriptor) {
        if (typeDescriptor.kind().equals(SymbolKind.TYPE) && typeDescriptor.typeKind() != null) {
            if (typeDescriptor.typeKind().equals(TypeDescKind.RECORD)) {
                return "records";
            } else if (typeDescriptor.typeKind().equals(TypeDescKind.OBJECT)) {
                return "objectTypes";
            } else if (typeDescriptor.typeKind().equals(TypeDescKind.ERROR)) {
                return "errors";
            } else if (typeDescriptor.typeKind().equals(TypeDescKind.UNION)) {
                if (((UnionTypeSymbol) typeDescriptor).memberTypeDescriptors().stream().allMatch(typeSymbol -> {
                    if (typeSymbol.typeKind().equals((TypeDescKind.TYPE_REFERENCE))) {
                        return getTypeCategory(typeSymbol).equals("errors");
                    } else {
                        return typeSymbol.typeKind().equals(TypeDescKind.ERROR);
                    }
                })) {
                    return "errors";
                } else {
                    return "types";
                }
            } else if (typeDescriptor.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                return getTypeCategory(((TypeReferenceTypeSymbol) typeDescriptor).typeDescriptor());
            } else if (typeDescriptor.typeKind().equals(TypeDescKind.DECIMAL) ||
                    typeDescriptor.typeKind().isXMLType() ||
                    typeDescriptor.typeKind().equals(TypeDescKind.FUNCTION) ||
                    typeDescriptor.typeKind().isStringType() ||
                    typeDescriptor.typeKind().isIntegerType()) {
                return "types";
            } else if (typeDescriptor.typeKind().equals(TypeDescKind.INTERSECTION)) {
                return getIntersectionTypeCategory((IntersectionTypeSymbol) typeDescriptor);
            }
        } else if (typeDescriptor.kind().equals(SymbolKind.CLASS)) {
            Qualifiable classSymbol = (Qualifiable) typeDescriptor;
            if (classSymbol.qualifiers().contains(Qualifier.CLIENT)) {
                return "clients";
            } else if (classSymbol.qualifiers().contains(Qualifier.LISTENER) ||
                    "Listener".equals(typeDescriptor.getName().orElse(null))) {
                return "listeners";
            } else {
                return "classes";
            }
        }
        return CATEGORY_NOT_FOUND;
    }

    public static String getIntersectionTypeCategory(IntersectionTypeSymbol intersectionTypeSymbol) {
        for (TypeSymbol memberType : intersectionTypeSymbol.memberTypeDescriptors()) {
            String category = getTypeCategory(memberType);
            if (!category.equals("not_found")) {
                return category;
            }
        }
        return "types";
    }

    public Type(String name) {
        this.name = name;
        this.category = "builtin";
    }

    public Type(String name, String description, boolean isDeprecated) {
        this.name = name;
        this.description = description;
        this.isDeprecated = isDeprecated;
    }

    private static boolean isPublic(Symbol typeDefinition) {
        return typeDefinition instanceof TypeDefinitionSymbol &&
                ((TypeDefinitionSymbol) typeDefinition).qualifiers().contains(Qualifier.PUBLIC) ||
                typeDefinition instanceof ClassSymbol &&
                        ((ClassSymbol) typeDefinition).qualifiers().contains(Qualifier.PUBLIC);
    }

}
