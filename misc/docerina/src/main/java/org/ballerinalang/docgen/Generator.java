/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.docgen;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.TypedescTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.generator.model.Annotation;
import org.ballerinalang.docgen.generator.model.BClass;
import org.ballerinalang.docgen.generator.model.BObjectType;
import org.ballerinalang.docgen.generator.model.BType;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.Constant;
import org.ballerinalang.docgen.generator.model.Construct;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.Enum;
import org.ballerinalang.docgen.generator.model.Error;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.Type;
import org.ballerinalang.docgen.generator.model.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Generates the Page bClasses for bal packages.
 */
public class Generator {

    private static final String EMPTY_STRING = "";
    private static final String RETURN_PARAM_NAME = "return";
    private static final Logger log = LoggerFactory.getLogger(BallerinaDocGenerator.class);

    /**
     * Generate/Set the module constructs model(docerina model) when the syntax tree for the module is given.
     *
     * @param module  module constructs model to fill.
     * @param syntaxTree syntax tree of the document.
     * @param semanticModel semantic model
     * @return whether the module has any public constructs.
     */
    public static boolean setModuleFromSyntaxTree(Module module, SyntaxTree syntaxTree,
                                                  SemanticModel semanticModel) {

        boolean hasPublicConstructs = false;
        if (syntaxTree.containsModulePart()) {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            for (Node node : modulePartNode.members()) {
                if (node.kind().equals(SyntaxKind.TYPE_DEFINITION)) {
                    TypeDefinitionNode typeDefinition = (TypeDefinitionNode) node;
                    if (typeDefinition.visibilityQualifier().isPresent() && typeDefinition.visibilityQualifier().get()
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD) || isTypePram(typeDefinition.metadata())) {
                        if (typeDefinition.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                            hasPublicConstructs = true;
                            module.records.add(getRecordTypeModel(typeDefinition, semanticModel));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.objectTypes.add(getObjectTypeModel(typeDefinition, semanticModel));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.UNION_TYPE_DESC) {
                            hasPublicConstructs = true;
                            Type unionType = Type.fromNode(typeDefinition.typeDescriptor(), semanticModel);
                            if (unionType.memberTypes.stream().allMatch(type -> type.category.equals("errors") ||
                                    (type.category.equals("builtin") && type.name.equals("error")))) {
                                module.errors.add(new Error(typeDefinition.typeName().text(),
                                        getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition
                                        .metadata()), Type.fromNode(typeDefinition.typeDescriptor(), semanticModel)));
                            } else {
                                module.types.add(getUnionTypeModel(typeDefinition, semanticModel));
                            }
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                            hasPublicConstructs = true;
                            Type refType = Type.fromNode(typeDefinition.typeDescriptor(), semanticModel);
                            if (refType.category.equals("errors")) {
                                module.errors.add(new Error(typeDefinition.typeName().text(),
                                        getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition
                                        .metadata()), refType));
                            } else {
                                module.types.add(getUnionTypeModel(typeDefinition, semanticModel));
                            }
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.DISTINCT_TYPE_DESC &&
                                ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor().kind()
                                        == SyntaxKind.ERROR_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.errors.add(new Error(typeDefinition.typeName().text(),
                                    getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition
                                    .metadata()), null));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.ERROR_TYPE_DESC) {
                            hasPublicConstructs = true;
                            ErrorTypeDescriptorNode errorTypeDescriptor = (ErrorTypeDescriptorNode)
                                    typeDefinition.typeDescriptor();
                            Type type = null;
                            if (errorTypeDescriptor.errorTypeParamsNode().isPresent()) {
                                type = Type.fromNode(errorTypeDescriptor.errorTypeParamsNode().get().parameter(),
                                        semanticModel);
                            }
                            module.errors.add(new Error(typeDefinition.typeName().text(),
                                    getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition
                                    .metadata()), type));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.TUPLE_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.types.add(getTupleTypeModel(typeDefinition, semanticModel));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.INTERSECTION_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.types.add(getIntersectionTypeModel(typeDefinition, semanticModel));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.TYPEDESC_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.types.add(getTypeDescModel(typeDefinition, semanticModel));
                        }
                        // TODO: handle value type nodes
                        // TODO: handle function type nodes
                        // TODO: handle built in ref type
                        // TODO: handle constrained types
                    }
                } else if (node.kind() == SyntaxKind.CLASS_DEFINITION) {
                    ClassDefinitionNode classDefinition = (ClassDefinitionNode) node;
                    if (classDefinition.visibilityQualifier().isPresent() && classDefinition.visibilityQualifier().get()
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                        hasPublicConstructs = true;
                        BClass cls = getClassModel((ClassDefinitionNode) node, semanticModel);
                        if (cls instanceof Client) {
                            module.clients.add((Client) cls);
                        } else if (cls instanceof Listener) {
                            module.listeners.add((Listener) cls);
                        } else {
                            module.classes.add(cls);
                        }
                    }
                } else if (node.kind() == SyntaxKind.FUNCTION_DEFINITION &&
                        containsToken(((FunctionDefinitionNode) node).qualifierList(), SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.functions.add(getFunctionModel((FunctionDefinitionNode) node, semanticModel));
                } else if (node.kind() == SyntaxKind.CONST_DECLARATION && ((ConstantDeclarationNode) node)
                        .visibilityQualifier().isPresent() && ((ConstantDeclarationNode) node).visibilityQualifier()
                        .get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.constants.add(getConstantTypeModel((ConstantDeclarationNode) node, semanticModel));
                } else if (node.kind() == SyntaxKind.ANNOTATION_DECLARATION && ((AnnotationDeclarationNode) node)
                        .visibilityQualifier().isPresent() && ((AnnotationDeclarationNode) node)
                        .visibilityQualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.annotations.add(getAnnotationModel((AnnotationDeclarationNode) node, semanticModel));
                } else if (node.kind() == SyntaxKind.ENUM_DECLARATION &&
                        ((EnumDeclarationNode) node).qualifier().isPresent() &&
                        ((EnumDeclarationNode) node).qualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    module.enums.add(getEnumModel((EnumDeclarationNode) node));
                }
            }
        }
        return hasPublicConstructs;
    }

    private static boolean containsToken(NodeList<Token> nodeList, SyntaxKind kind) {
        for (Node node: nodeList) {
            if (node.kind() == kind) {
                return true;
            }
        }
        return false;
    }

    public static Enum getEnumModel(EnumDeclarationNode enumDeclaration) {
        String enumName = enumDeclaration.identifier().text();
        List<Construct> members = new ArrayList<>();
        enumDeclaration.enumMemberList().forEach(node -> {
            if (node.kind().equals(SyntaxKind.ENUM_MEMBER)) {
                EnumMemberNode enumMemberNode = (EnumMemberNode) node;
                String memberName = enumMemberNode.identifier().text();
                String doc = getDocFromMetadata(enumMemberNode.metadata());
                if (doc.equals("")) {
                    doc = getParameterDocFromMetadataList(memberName, enumDeclaration.metadata());
                }
                members.add(new Construct(memberName, doc, false));
            }
        });
        return new Enum(enumName, getDocFromMetadata(enumDeclaration.metadata()),
                isDeprecated(enumDeclaration.metadata()), members);
    }

    public static Annotation getAnnotationModel(AnnotationDeclarationNode annotationDeclaration,
                                                SemanticModel semanticModel) {
        String annotationName = annotationDeclaration.annotationTag().text();
        StringJoiner attachPointJoiner = new StringJoiner(", ");
        for (int i = 0; i < annotationDeclaration.attachPoints().size(); i++) {
            AnnotationAttachPointNode annotationAttachPointNode = (AnnotationAttachPointNode) annotationDeclaration
                    .attachPoints().get(i);
            attachPointJoiner.add(annotationAttachPointNode.toString());
        }

        Type dataType = annotationDeclaration.typeDescriptor().isPresent() ? Type.fromNode(annotationDeclaration.
                typeDescriptor().get(), semanticModel) : null;
        return new Annotation(annotationName, getDocFromMetadata(annotationDeclaration.metadata()),
                isDeprecated(annotationDeclaration.metadata()), dataType, attachPointJoiner.toString());
    }

    public static Constant getConstantTypeModel(ConstantDeclarationNode constantNode,
                                                SemanticModel semanticModel) {
        String constantName = constantNode.variableName().text();
        String value = constantNode.initializer().toString();
        String desc = getDocFromMetadata(constantNode.metadata());
        Type type;
        if (constantNode.typeDescriptor().isPresent()) {
            type = Type.fromNode(constantNode.typeDescriptor().get(), semanticModel);
        } else {
            String dataType = "";
            if (constantNode.initializer().kind() == SyntaxKind.STRING_LITERAL) {
                dataType = "string";
            } else if (constantNode.initializer().kind() == SyntaxKind.BOOLEAN_LITERAL) {
                dataType = "boolean";
            } else if (constantNode.initializer().kind() == SyntaxKind.NUMERIC_LITERAL) {
                if (((BasicLiteralNode) constantNode.initializer()).literalToken().kind()
                        .equals(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN)) {
                    dataType = "int";
                } else if (((BasicLiteralNode) constantNode.initializer()).literalToken().kind()
                        .equals(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)) {
                    dataType = "float";
                }
            }
            type = new Type(dataType);
        }
        return new Constant(constantName, desc, isDeprecated(constantNode.metadata()), type, value);
    }

    private static BType getIntersectionTypeModel(TypeDefinitionNode typeDefinition,
                                                  SemanticModel semanticModel) {
        List<Type> memberTypes = new ArrayList<>();
        IntersectionTypeDescriptorNode typeDescriptor = (IntersectionTypeDescriptorNode) typeDefinition
                .typeDescriptor();
        memberTypes.add(Type.fromNode(typeDescriptor.leftTypeDesc(), semanticModel));
        memberTypes.add(Type.fromNode(typeDescriptor.rightTypeDesc(), semanticModel));
        BType bType = new BType(typeDefinition.typeName().text(),
                getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition.metadata()), memberTypes);
        bType.isIntersectionType = true;
        return bType;
    }

    private static BType getTupleTypeModel(TypeDefinitionNode typeDefinition,
                                           SemanticModel semanticModel) {
        List<Type> memberTypes = new ArrayList<>();
        TupleTypeDescriptorNode typeDescriptor = (TupleTypeDescriptorNode) typeDefinition.typeDescriptor();
        memberTypes.addAll(typeDescriptor.memberTypeDesc().stream().map(type ->
                Type.fromNode(type, semanticModel)).collect(Collectors.toList()));
        BType bType = new BType(typeDefinition.typeName().text(),
                getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition.metadata()), memberTypes);
        bType.isTuple = true;
        return bType;
    }

    private static BType getTypeDescModel(TypeDefinitionNode typeDefinition,
                                           SemanticModel semanticModel) {
        TypedescTypeDescriptorNode typeDescriptor = (TypedescTypeDescriptorNode) typeDefinition.typeDescriptor();
        Type type = null;
        if (typeDescriptor.typedescTypeParamsNode().isPresent()) {
            type = Type.fromNode(typeDescriptor.typedescTypeParamsNode().get().typeNode(), semanticModel);
        }
        BType bType = new BType(typeDefinition.typeName().text(),
                getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition.metadata()), null);
        bType.isTypeDesc = true;
        bType.elementType = type;
        return bType;
    }

    private static BType getUnionTypeModel(TypeDefinitionNode typeDefinition,
                                           SemanticModel semanticModel) {
        List<Type> memberTypes = new ArrayList<>();
        Node typeDescriptor = typeDefinition.typeDescriptor();
        while (typeDescriptor.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
            UnionTypeDescriptorNode unionType = (UnionTypeDescriptorNode) typeDescriptor;
            memberTypes.add(Type.fromNode(unionType.leftTypeDesc(), semanticModel));
            typeDescriptor = unionType.rightTypeDesc();
        }
        memberTypes.add(Type.fromNode(typeDescriptor, semanticModel));
        BType bType = new BType(typeDefinition.typeName().text(), getDocFromMetadata(typeDefinition.metadata()),
                                isDeprecated(typeDefinition.metadata()), memberTypes);
        bType.isAnonymousUnionType = true;
        return bType;
    }

    private static BClass getClassModel(ClassDefinitionNode classDefinitionNode, SemanticModel semanticModel) {
        List<Function> functions = new ArrayList<>();
        String name = classDefinitionNode.className().text();
        String description = getDocFromMetadata(classDefinitionNode.metadata());
        boolean isDeprecated = isDeprecated(classDefinitionNode.metadata());
        boolean isReadOnly = containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.READONLY_KEYWORD);
        boolean isIsolated = containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.ISOLATED_KEYWORD);

        List<DefaultableVariable> fields = getDefaultableVariableList(classDefinitionNode.members(),
                classDefinitionNode.metadata(), semanticModel);

        for (Node member : classDefinitionNode.members()) {
            if (member instanceof FunctionDefinitionNode && (containsToken(((FunctionDefinitionNode) member)
                    .qualifierList(), SyntaxKind.PUBLIC_KEYWORD) || containsToken(((FunctionDefinitionNode) member)
                    .qualifierList(), SyntaxKind.REMOTE_KEYWORD))) {
                functions.add(getFunctionModel((FunctionDefinitionNode) member, semanticModel));
            } else if (member instanceof TypeReferenceNode) {
                Type originType = Type.fromNode(((TypeReferenceNode) member).typeName(), semanticModel);
                TypeSymbol typeSymbol = null;
                try {
                    Optional<Symbol> symbol = semanticModel.symbol(member);
                    typeSymbol = ((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor();
                } catch (NullPointerException nullException) {
                    if (BallerinaDocUtils.isDebugEnabled()) {
                        log.error("Symbol find threw null pointer in Line range:" +
                                ((TypeReferenceNode) member).typeName().lineRange());
                    }
                }
                functions.addAll(getInclusionFunctions(typeSymbol, originType, classDefinitionNode.members()));
            }
        }

        if (containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
            return new Client(name, description, isDeprecated, fields, functions, isReadOnly, isIsolated);
        } else if (containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.LISTENER_KEYWORD)
                || name.equals("Listener")) {
            return new Listener(name, description, isDeprecated, fields, functions, isReadOnly, isIsolated);
        } else {
            return new BClass(name, description, isDeprecated, fields, functions, isReadOnly, isIsolated);
        }
    }

    private static BObjectType getObjectTypeModel(TypeDefinitionNode typeDefinition,
                                                  SemanticModel semanticModel) {
        List<Function> functions = new ArrayList<>();
        String name = typeDefinition.typeName().text();
        String description = getDocFromMetadata(typeDefinition.metadata());
        ObjectTypeDescriptorNode typeDescriptorNode = (ObjectTypeDescriptorNode) typeDefinition.typeDescriptor();
        boolean isDeprecated = isDeprecated(typeDefinition.metadata());

        List<DefaultableVariable> fields = getDefaultableVariableList(typeDescriptorNode.members(),
                typeDefinition.metadata(), semanticModel);

        for (Node member : typeDescriptorNode.members()) {
            if (member instanceof MethodDeclarationNode) {
                MethodDeclarationNode methodNode = (MethodDeclarationNode) member;
                if (containsToken(methodNode.qualifierList(), SyntaxKind.PUBLIC_KEYWORD) ||
                        containsToken(methodNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD)) {
                    String methodName = methodNode.methodName().text();

                    List<Variable> returnParams = new ArrayList<>();
                    FunctionSignatureNode methodSignature = methodNode.methodSignature();

                    // Iterate through the parameters
                    List<DefaultableVariable> parameters = new ArrayList<>(getDefaultableVariableList(methodSignature
                                    .parameters(), methodNode.metadata(), semanticModel));

                    // return params
                    if (methodSignature.returnTypeDesc().isPresent()) {
                        ReturnTypeDescriptorNode returnType = methodSignature.returnTypeDesc().get();
                        Type type = Type.fromNode(returnType.type(), semanticModel);
                        returnParams.add(new Variable(EMPTY_STRING, getParameterDocFromMetadataList(RETURN_PARAM_NAME,
                                methodNode.metadata()), false, type));
                    }

                    boolean isRemote = containsToken(methodNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD);

                    functions.add(new Function(methodName, getDocFromMetadata(methodNode.metadata()),
                            isRemote, false, isDeprecated(methodNode.metadata()),
                            containsToken(methodNode.qualifierList(), SyntaxKind.ISOLATED_KEYWORD), parameters,
                            returnParams));
                }
            } else if (member instanceof TypeReferenceNode) {
                Type originType = Type.fromNode(((TypeReferenceNode) member).typeName(), semanticModel);
                TypeSymbol typeSymbol = null;
                try {
                    Optional<Symbol> symbol = semanticModel.symbol(member);
                    typeSymbol = ((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor();
                } catch (NullPointerException nullException) {
                    if (BallerinaDocUtils.isDebugEnabled()) {
                        log.error("Symbol find threw null pointer in Line range:" +
                                ((TypeReferenceNode) member).typeName().lineRange());
                    }
                }
                functions.addAll(getInclusionFunctions(typeSymbol, originType, typeDescriptorNode.members()));
            }
        }
        return new BObjectType(name, description, isDeprecated, fields, functions);
    }

    // TODO: Revisit this. This probably can be written in a much simpler way.
    private static List<Function> getInclusionFunctions(TypeSymbol typeSymbol, Type originType,
                                                        NodeList<Node> members) {
        List<Function> functions = new ArrayList<>();
        if (typeSymbol instanceof ObjectTypeSymbol) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) typeSymbol;
            objectTypeSymbol.methods().values().forEach(methodSymbol -> {
                String methodName = methodSymbol.getName().get();
                // Check if the inclusion function is overridden
                if (members.stream().anyMatch(node -> {
                    if (node instanceof MethodDeclarationNode && ((MethodDeclarationNode) node).methodName()
                            .text().equals(methodName)) {
                        return true;
                    } else if (node instanceof FunctionDefinitionNode && ((FunctionDefinitionNode) node).functionName()
                            .text().equals(methodName)) {
                        return true;
                    } else {
                        return false;
                    }
                })) {
                    return;
                }

                List<DefaultableVariable> parameters = new ArrayList<>();
                List<Variable> returnParams = new ArrayList<>();

                methodSymbol.typeDescriptor().parameters().forEach(parameterSymbol -> {
                    boolean parameterDeprecated = parameterSymbol.annotations().stream()
                            .anyMatch(annotationSymbol -> annotationSymbol.getName().get().equals("deprecated"));
                    Type type = new Type(parameterSymbol.typeDescriptor().signature());
                    Type.resolveSymbol(type, parameterSymbol.typeDescriptor());
                    parameters.add(new DefaultableVariable(parameterSymbol.getName().isPresent() ?
                            parameterSymbol.getName().get() : "", "", parameterDeprecated, type, ""));
                });

                if (methodSymbol.typeDescriptor().restParam().isPresent()) {
                    ParameterSymbol restParam = methodSymbol.typeDescriptor().restParam().get();
                    boolean parameterDeprecated = restParam.annotations().stream()
                            .anyMatch(annotationSymbol -> annotationSymbol.getName().get().equals("deprecated"));
                    Type type = new Type(restParam.getName().isPresent() ? restParam.getName().get() : "");
                    type.isRestParam = true;
                    Type elemType = new Type(restParam.typeDescriptor().signature());
                    Type.resolveSymbol(elemType, restParam.typeDescriptor());
                    type.elementType = elemType;
                    parameters.add(new DefaultableVariable(restParam.getName().isPresent() ?
                            restParam.getName().get() : "", "", parameterDeprecated, type, ""));
                }

                if (methodSymbol.typeDescriptor().returnTypeDescriptor().isPresent()) {
                    Type type = new Type(methodSymbol.typeDescriptor().returnTypeDescriptor().get()
                            .signature());
                    Type.resolveSymbol(type, methodSymbol.typeDescriptor().returnTypeDescriptor().get());
                    returnParams.add(new Variable(EMPTY_STRING, "", false, type));
                }

                Function function = new Function(methodName, "",
                        methodSymbol.qualifiers().contains(Qualifier.REMOTE), methodSymbol.external(),
                        methodSymbol.deprecated(), methodSymbol.qualifiers().contains(Qualifier.ISOLATED),
                        parameters, returnParams);
                function.inclusionType = originType;
                functions.add(function);
            });
        }
        return functions;
    }

    private static Function getFunctionModel(FunctionDefinitionNode functionDefinitionNode,
                                             SemanticModel semanticModel) {
        String functionName = functionDefinitionNode.functionName().text();

        List<DefaultableVariable> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();

        // Iterate through the parameters
        parameters.addAll(getDefaultableVariableList(functionSignature.parameters(),
                                                     functionDefinitionNode.metadata(), semanticModel));

        // return params
        if (functionSignature.returnTypeDesc().isPresent()) {
            ReturnTypeDescriptorNode returnType = functionSignature.returnTypeDesc().get();
            Type type = Type.fromNode(returnType.type(), semanticModel);
            returnParams.add(new Variable(EMPTY_STRING, getParameterDocFromMetadataList(RETURN_PARAM_NAME,
                    functionDefinitionNode.metadata()), false, type));
        }

        boolean isExtern = functionDefinitionNode.functionBody() instanceof ExternalFunctionBodyNode;
        boolean isRemote = containsToken(functionDefinitionNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD);

        return new Function(functionName, getDocFromMetadata(functionDefinitionNode.metadata()),
                isRemote, isExtern, isDeprecated(functionDefinitionNode.metadata()),
                containsToken(functionDefinitionNode.qualifierList(), SyntaxKind.ISOLATED_KEYWORD), parameters,
                returnParams);
    }

    private static Record getRecordTypeModel(TypeDefinitionNode recordTypeDefinition,
                                             SemanticModel semanticModel) {
        String recordName = recordTypeDefinition.typeName().text();
        List<DefaultableVariable> fields = getDefaultableVariableList(((RecordTypeDescriptorNode) recordTypeDefinition
                .typeDescriptor()).fields(), recordTypeDefinition.metadata(), semanticModel);
        boolean isClosed = ((((RecordTypeDescriptorNode) recordTypeDefinition.typeDescriptor()).bodyStartDelimiter()))
                .kind().equals(SyntaxKind.OPEN_BRACE_PIPE_TOKEN);
        return new Record(recordName, getDocFromMetadata(recordTypeDefinition.metadata()),
                          isDeprecated(recordTypeDefinition.metadata()), isClosed, fields);
    }

    public static List<DefaultableVariable> getDefaultableVariableList(NodeList nodeList,
                                                                       Optional<MetadataNode> optionalMetadataNode,
                                                                       SemanticModel semanticModel) {
        List<DefaultableVariable> variables = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if (node instanceof RecordFieldWithDefaultValueNode) {
                RecordFieldWithDefaultValueNode recordField = (RecordFieldWithDefaultValueNode) node;
                String name = recordField.fieldName().text();
                String doc = getDocFromMetadata(recordField.metadata());
                if (doc.equals("")) {
                    doc = getParameterDocFromMetadataList(name, optionalMetadataNode);
                }
                String defaultValue = recordField.expression().toString();
                Type type = Type.fromNode(recordField.typeName(), semanticModel);
                type.isReadOnly = recordField.readonlyKeyword().isPresent();
                DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc, false, type,
                        defaultValue);
                variables.add(defaultableVariable);
            } else if (node instanceof RecordFieldNode) {
                RecordFieldNode recordField = (RecordFieldNode) node;
                String name = recordField.fieldName().text();
                String doc = getDocFromMetadata(recordField.metadata());
                if (doc.equals("")) {
                    doc = getParameterDocFromMetadataList(name, optionalMetadataNode);
                }
                Type type = Type.fromNode(recordField.typeName(), semanticModel);
                type.isNullable = recordField.questionMarkToken().isPresent();
                type.isReadOnly = recordField.readonlyKeyword().isPresent();
                DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc, false, type,
                        "");
                variables.add(defaultableVariable);
            } else if (node instanceof TypeReferenceNode) {
                Type originType = Type.fromNode(((TypeReferenceNode) node).typeName(), semanticModel);
                DefaultableVariable defaultableVariable = new DefaultableVariable(originType.name, "",
                        false, null, "");
                defaultableVariable.inclusionType = originType;
                TypeSymbol typeSymbol = null;
                try {
                    Optional<Symbol> symbol = semanticModel.symbol(node);
                    typeSymbol = ((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor();
                } catch (NullPointerException nullException) {
                    if (BallerinaDocUtils.isDebugEnabled()) {
                        log.error("Symbol find threw null pointer in Line range:" +
                                ((TypeReferenceNode) node).typeName().lineRange());
                    }
                }
                if (typeSymbol instanceof RecordTypeSymbol) {
                    RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeSymbol;
                    recordTypeSymbol.fieldDescriptors().forEach((name, field) -> {
                        Type type = new Type(name);
                        Type elemType;
                        String typeName;
                        if (field.typeDescriptor() instanceof TypeReferenceTypeSymbol) {
                            typeName = field.typeDescriptor().getName().get();
                        } else {
                            typeName = field.typeDescriptor().signature();
                        }
                        elemType = new Type(typeName);
                        Type.resolveSymbol(elemType, field.typeDescriptor());
                        type.elementType = elemType;
                        originType.memberTypes.add(type);
                    });
                } else if (typeSymbol instanceof ObjectTypeSymbol) {
                    ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) typeSymbol;
                    objectTypeSymbol.fieldDescriptors().forEach((name, field) -> {
                        Type type = new Type(name);
                        Type elemType;
                        String typeName;
                        if (field.typeDescriptor() instanceof TypeReferenceTypeSymbol) {
                            typeName = field.typeDescriptor().getName().get();
                        } else {
                            typeName = field.typeDescriptor().signature();
                        }
                        elemType = new Type(typeName);
                        Type.resolveSymbol(elemType, field.typeDescriptor());
                        type.elementType = elemType;
                        originType.memberTypes.add(type);
                    });
                }
                defaultableVariable.type = originType;
                variables.add(defaultableVariable);
            } else if (node instanceof ObjectFieldNode) {
                ObjectFieldNode objectField = (ObjectFieldNode) node;
                if (objectField.visibilityQualifier().isPresent() && objectField.visibilityQualifier().get().kind()
                        .equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    String name = objectField.fieldName().text();
                    String doc = getDocFromMetadata(objectField.metadata());
                    if (doc.equals("")) {
                        doc = getParameterDocFromMetadataList(name, optionalMetadataNode);
                    }
                    String defaultValue;
                    if (objectField.expression().isPresent()) {
                        defaultValue = objectField.expression().get().toString();
                    } else {
                        defaultValue = "";
                    }
                    Type type = Type.fromNode(objectField.typeName(), semanticModel);
                    DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc,
                            isDeprecated(objectField.metadata()), type, defaultValue);
                    variables.add(defaultableVariable);
                }
            } else if (node instanceof RequiredParameterNode) {
                RequiredParameterNode requiredParameter = (RequiredParameterNode) node;
                String paramName = requiredParameter.paramName().isPresent() ?
                        requiredParameter.paramName().get().text() : "";
                Type type = Type.fromNode(requiredParameter.typeName(), semanticModel);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), isDeprecated(requiredParameter.annotations()), type, ""));
            } else if (node instanceof DefaultableParameterNode) {
                DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) node;
                String paramName = defaultableParameter.paramName().isPresent() ?
                        defaultableParameter.paramName().get().text() : "";
                Type type = Type.fromNode(defaultableParameter.typeName(), semanticModel);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), isDeprecated(defaultableParameter.annotations()),
                        type, defaultableParameter.expression().toString()));
            } else if (node instanceof RestParameterNode) {
                RestParameterNode restParameter = (RestParameterNode) node;
                String paramName = restParameter.paramName().isPresent() ?
                        restParameter.paramName().get().text() : "";
                Type type = new Type(paramName);
                type.isRestParam = true;
                type.elementType = Type.fromNode(restParameter.typeName(), semanticModel);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), false, type, ""));
            }

        }
        return variables;
    }

    private static boolean isTypePram(Optional<MetadataNode> metadataNode) {
        if (metadataNode.isEmpty()) {
            return false;
        }
        NodeList<AnnotationNode> annotations = metadataNode.get().annotations();
        for (AnnotationNode annotationNode : annotations) {
            if (annotationNode.toString().contains("@typeParam")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDeprecated(NodeList<AnnotationNode> annotations) {
        for (AnnotationNode annotationNode : annotations) {
            if (annotationNode.toString().contains("@deprecated")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDeprecated(Optional<MetadataNode> metadataNode) {
        if (!metadataNode.isPresent()) {
            return false;
        }
        MetadataNode metaData = metadataNode.get();
        for (AnnotationNode annotationNode : metaData.annotations()) {
            if (annotationNode.toString().contains("@deprecated")) {
                return true;
            }
        }
        return false;
    }

    private static String getDocFromMetadata(Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return "";
        }
        String doc = "";
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        if (docLines != null) {
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownDocumentationLineNode) {
                    doc += !((MarkdownDocumentationLineNode) docLine).documentElements().isEmpty() ?
                            getDocString(((MarkdownDocumentationLineNode) docLine).documentElements()) : "\n";
                } else {
                    break;
                }
            }
            return doc;
        } else {
            return "";
        }
    }

    private static String getParameterDocFromMetadataList(String parameterName,
                                                          Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return "";
        }
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        StringBuilder parameterDoc = new StringBuilder();
        if (docLines != null) {
            boolean lookForMoreLines = false;
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownParameterDocumentationLineNode) {
                    if (((MarkdownParameterDocumentationLineNode) docLine).parameterName().text()
                            .equals(parameterName)) {
                        parameterDoc.append(getDocString(((MarkdownParameterDocumentationLineNode) docLine)
                                .documentElements()));
                        lookForMoreLines = true;
                    } else {
                        lookForMoreLines = false;
                    }
                } else if (lookForMoreLines && docLine instanceof MarkdownDocumentationLineNode) {
                    parameterDoc.append(getDocString(((MarkdownDocumentationLineNode) docLine).documentElements()));
                }
            }
            return parameterDoc.toString();
        } else {
            return "";
        }
    }

    private static String getDocString(NodeList<Node> documentElements) {
        if (documentElements.isEmpty()) {
            return "";
        }
        StringBuilder doc = new StringBuilder();
        for (Node docNode : documentElements) {
            doc.append(docNode.toString());
        }
        if (doc.toString().startsWith(" ")) {
            return doc.substring(1);
        }
        return doc.toString();
    }
}
