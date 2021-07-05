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
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
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
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean isLambda;
    @Expose
    public boolean isIsolated;
    @Expose
    public boolean isDeprecated;
    @Expose
    public boolean generateUserDefinedTypeLink = true;
    @Expose
    public List<Type> memberTypes = new ArrayList<>();
    @Expose
    public List<Type> paramTypes = new ArrayList<>();
    @Expose
    public int arrayDimensions;
    @Expose
    public Type elementType;
    @Expose
    public Type returnType;
    @Expose
    public Type constraint;

    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);
    private static final String ballerinaShotVersion = BallerinaDocGenerator.getBallerinaShortVersion();

    private Type() {
    }

    public static Type fromNode(Node node, SemanticModel semanticModel) {
        Type type = new Type();
        if (node instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode simpleNameReferenceNode = (SimpleNameReferenceNode) node;
            type.name = simpleNameReferenceNode.name().text();
            type.category = "reference";
            Optional<Symbol> symbol = null;
            try {
                symbol = semanticModel.symbol(node);
            } catch (NullPointerException nullException) {
                if (BallerinaDocUtils.isDebugEnabled()) {
                    log.error("Symbol find threw null pointer in : Line range:" + node.lineRange());
                }
            }
            if (symbol != null && symbol.isPresent()) {
                resolveSymbol(type, symbol.get());
            } else {
                type.generateUserDefinedTypeLink = false;
            }
        } else if (node instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode qualifiedNameReferenceNode = (QualifiedNameReferenceNode) node;
            type.moduleName = qualifiedNameReferenceNode.modulePrefix().text();
            type.category = "reference";
            type.name = qualifiedNameReferenceNode.identifier().text();
            Optional<Symbol> symbol = null;
            try {
                symbol = semanticModel.symbol(node);
            } catch (NullPointerException nullException) {
                System.out.print(Arrays.toString(nullException.getStackTrace()));
            }
            if (symbol != null && symbol.isPresent()) {
                resolveSymbol(type, symbol.get());
            } else {
                type.generateUserDefinedTypeLink = false;
            }
        } else if (node instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode = (BuiltinSimpleNameReferenceNode) node;
            type.name = builtinSimpleNameReferenceNode.name().text();
            type.version = ballerinaShotVersion;
            type.category = "builtin";
        } else if (node instanceof NilTypeDescriptorNode) {
            type.name = node.toString();
            type.version = ballerinaShotVersion;
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
            type.category = (recordNode.bodyStartDelimiter()).kind().equals(SyntaxKind.OPEN_BRACE_PIPE_TOKEN) ?
                    "inline_closed_record" : "inline_record";
            type.memberTypes = fields;
        } else if (node instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode streamNode = (StreamTypeDescriptorNode) node;
            StreamTypeParamsNode streamParams = streamNode.streamTypeParamsNode().isPresent() ?
                    (StreamTypeParamsNode) streamNode.streamTypeParamsNode().get() : null;
            type.name = streamNode.streamKeywordToken().text();
            type.category = "stream";
            type.version = ballerinaShotVersion;
            if (streamParams != null) {
                type.memberTypes.add(fromNode(streamParams.leftTypeDescNode(), semanticModel));
                if (streamParams.rightTypeDescNode().isPresent()) {
                    type.memberTypes.add(fromNode(streamParams.rightTypeDescNode().get(), semanticModel));
                }
            }
        } else if (node instanceof FunctionTypeDescriptorNode) {
            type.isLambda = true;
            FunctionTypeDescriptorNode functionDescNode = (FunctionTypeDescriptorNode) node;
            type.isIsolated = Generator.containsToken(functionDescNode.qualifierList(), SyntaxKind.ISOLATED_KEYWORD);
            if (functionDescNode.functionSignature().isPresent()) {
                FunctionSignatureNode functionSignature = functionDescNode.functionSignature().get();
                List<DefaultableVariable> variables =
                        Generator.getDefaultableVariableList(functionSignature.parameters(), Optional.empty(),
                                semanticModel);
                type.paramTypes.addAll(variables.stream().map((defaultableVariable) -> defaultableVariable.type)
                        .collect(Collectors.toList()));
                if (functionSignature.returnTypeDesc().isPresent()) {
                    ReturnTypeDescriptorNode returnType = functionSignature.returnTypeDesc().get();
                    type.returnType = Type.fromNode(returnType.type(), semanticModel);
                }
            }
        } else if (node instanceof MapTypeDescriptorNode) {
            MapTypeDescriptorNode mapTypeDesc = (MapTypeDescriptorNode) node;
                type.name = "map";
                type.category = "map";
                type.version = ballerinaShotVersion;
                type.constraint = fromNode(mapTypeDesc.mapTypeParamsNode().typeNode(), semanticModel);
        } else if (node instanceof ParameterizedTypeDescriptorNode) {
            ParameterizedTypeDescriptorNode parameterizedTypeNode = (ParameterizedTypeDescriptorNode) node;
            SyntaxKind typeKind = node.kind();
            if (typeKind == SyntaxKind.ERROR_TYPE_DESC || typeKind == SyntaxKind.XML_TYPE_DESC) {
                type.name = parameterizedTypeNode.keywordToken().text();
                type.version = ballerinaShotVersion;
                type.category = "builtin";
            } else if (typeKind == SyntaxKind.FUTURE_TYPE_DESC) {
                type.category = "future";
                type.elementType = parameterizedTypeNode.typeParamNode().map(typeParameterNode ->
                        Type.fromNode(typeParameterNode.typeNode(), semanticModel)).orElse(null);
            } else if (typeKind == SyntaxKind.TYPEDESC_TYPE_DESC) {
                type.elementType = parameterizedTypeNode.typeParamNode().map(typeParameterNode ->
                        Type.fromNode(typeParameterNode.typeNode(), semanticModel)).orElse(null);
                type.isTypeDesc = true;
                type.version = ballerinaShotVersion;
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

    public static void resolveSymbol(Type type, Symbol symbol) {
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
    }

    public static String getTypeCategory(TypeSymbol typeDescriptor) {
        if (typeDescriptor.kind().equals(SymbolKind.TYPE)) {
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

        return "not_found";
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
        this.version = ballerinaShotVersion;
        this.category = "builtin";
    }

    public Type(String name, String description, boolean isDeprecated) {
        this.name = name;
        this.description = description;
        this.isDeprecated = isDeprecated;
    }

}
