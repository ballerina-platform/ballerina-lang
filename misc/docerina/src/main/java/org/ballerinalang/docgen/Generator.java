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

import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
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
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.generator.model.Annotation;
import org.ballerinalang.docgen.generator.model.BAbstractObject;
import org.ballerinalang.docgen.generator.model.BClass;
import org.ballerinalang.docgen.generator.model.BType;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.Constant;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.Error;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.Type;
import org.ballerinalang.docgen.generator.model.Variable;

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

    /**
     * Generate/Set the module constructs model(docerina model) when the syntax tree for the module is given.
     *
     * @param module  module constructs model to fill.
     * @param syntaxTree syntax tree of the document.
     * @param semanticModel semantic model
     * @param fileName document name.
     * @return whether the module has any public constructs.
     */
    public static boolean setModuleFromSyntaxTree(Module module, SyntaxTree syntaxTree,
                                                  BallerinaSemanticModel semanticModel, String fileName) {

        boolean hasPublicConstructs = false;
        if (syntaxTree.containsModulePart()) {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            for (Node node: modulePartNode.members()) {
                if (node.kind().equals(SyntaxKind.TYPE_DEFINITION)) {
                    TypeDefinitionNode typeDefinition = (TypeDefinitionNode) node;
                    if (typeDefinition.visibilityQualifier().isPresent() && typeDefinition.visibilityQualifier().get()
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD) || isTypePram(typeDefinition.metadata())) {
                        if (typeDefinition.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                            hasPublicConstructs = true;
                            module.records.add(getRecordTypeModel(typeDefinition, semanticModel, fileName));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.abstractObjects.add(getAbstractObjectModel(typeDefinition, semanticModel, fileName));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.UNION_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.types.add(getUnionTypeModel(typeDefinition, semanticModel, fileName));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                            hasPublicConstructs = true;
                            module.types.add(getUnionTypeModel(typeDefinition, semanticModel, fileName));
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
                                        semanticModel, fileName);
                            }
                            module.errors.add(new Error(typeDefinition.typeName().text(),
                                    getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition
                                    .metadata()), type));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.TUPLE_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.types.add(getTupleTypeModel(typeDefinition, semanticModel, fileName));
                        } else if (typeDefinition.typeDescriptor().kind() == SyntaxKind.INTERSECTION_TYPE_DESC) {
                            hasPublicConstructs = true;
                            module.types.add(getIntersectionTypeModel(typeDefinition, semanticModel, fileName));
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
                        BClass cls = getClassModel((ClassDefinitionNode) node, semanticModel, fileName);
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
                    module.functions.add(getFunctionModel((FunctionDefinitionNode) node, semanticModel, fileName));
                } else if (node.kind() == SyntaxKind.CONST_DECLARATION && ((ConstantDeclarationNode) node)
                        .visibilityQualifier().isPresent() && ((ConstantDeclarationNode) node).visibilityQualifier()
                        .get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.constants.add(getConstantTypeModel((ConstantDeclarationNode) node, semanticModel, fileName));
                } else if (node.kind() == SyntaxKind.ANNOTATION_DECLARATION && ((AnnotationDeclarationNode) node)
                        .visibilityQualifier().isPresent() && ((AnnotationDeclarationNode) node)
                        .visibilityQualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.annotations.add(getAnnotationModel((AnnotationDeclarationNode) node, semanticModel,
                            fileName));
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

    public static Annotation getAnnotationModel(AnnotationDeclarationNode annotationDeclaration,
                                                BallerinaSemanticModel semanticModel, String fileName) {
        String annotationName = annotationDeclaration.annotationTag().text();
        StringJoiner attachPointJoiner = new StringJoiner(", ");
        for (int i = 0; i < annotationDeclaration.attachPoints().size(); i++) {
            AnnotationAttachPointNode annotationAttachPointNode = (AnnotationAttachPointNode) annotationDeclaration
                    .attachPoints().get(i);
            attachPointJoiner.add(annotationAttachPointNode.toString());
        }

        Type dataType = annotationDeclaration.typeDescriptor().isPresent() ? Type.fromNode(annotationDeclaration.
                typeDescriptor().get(), semanticModel, fileName) : null;
        return new Annotation(annotationName, getDocFromMetadata(annotationDeclaration.metadata()),
                isDeprecated(annotationDeclaration.metadata()), dataType, attachPointJoiner.toString());
    }

    public static Constant getConstantTypeModel(ConstantDeclarationNode constantNode,
                                                BallerinaSemanticModel semanticModel, String fileName) {
        String constantName = constantNode.variableName().text();
        String value = constantNode.initializer().toString();
        String desc = getDocFromMetadata(constantNode.metadata());
        Type type;
        if (constantNode.typeDescriptor().isPresent()) {
            type = Type.fromNode(constantNode.typeDescriptor().get(), semanticModel, fileName);
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
                                                  BallerinaSemanticModel semanticModel, String fileName) {
        List<Type> memberTypes = new ArrayList<>();
        IntersectionTypeDescriptorNode typeDescriptor = (IntersectionTypeDescriptorNode) typeDefinition
                .typeDescriptor();
        memberTypes.add(Type.fromNode(typeDescriptor.leftTypeDesc(), semanticModel, fileName));
        memberTypes.add(Type.fromNode(typeDescriptor.rightTypeDesc(), semanticModel, fileName));
        BType bType = new BType(typeDefinition.typeName().text(),
                getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition.metadata()), memberTypes);
        bType.isIntersectionType = true;
        return bType;
    }

    private static BType getTupleTypeModel(TypeDefinitionNode typeDefinition,
                                           BallerinaSemanticModel semanticModel, String fileName) {
        List<Type> memberTypes = new ArrayList<>();
        TupleTypeDescriptorNode typeDescriptor = (TupleTypeDescriptorNode) typeDefinition.typeDescriptor();
        memberTypes.addAll(typeDescriptor.memberTypeDesc().stream().map(type ->
                Type.fromNode(type, semanticModel, fileName)).collect(Collectors.toList()));
        BType bType = new BType(typeDefinition.typeName().text(),
                getDocFromMetadata(typeDefinition.metadata()), isDeprecated(typeDefinition.metadata()), memberTypes);
        bType.isTuple = true;
        return bType;
    }

    private static BType getUnionTypeModel(TypeDefinitionNode typeDefinition,
                                           BallerinaSemanticModel semanticModel, String fileName) {
        List<Type> memberTypes = new ArrayList<>();
        Node typeDescriptor = typeDefinition.typeDescriptor();
        while (typeDescriptor.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
            UnionTypeDescriptorNode unionType = (UnionTypeDescriptorNode) typeDescriptor;
            memberTypes.add(Type.fromNode(unionType.leftTypeDesc(), semanticModel, fileName));
            typeDescriptor = unionType.rightTypeDesc();
        }
        memberTypes.add(Type.fromNode(typeDescriptor, semanticModel, fileName));
        BType bType = new BType(typeDefinition.typeName().text(), getDocFromMetadata(typeDefinition.metadata()),
                isDeprecated(typeDefinition.metadata()), memberTypes);
        bType.isAnonymousUnionType = true;
        return bType;
    }

    private static BClass getClassModel(ClassDefinitionNode classDefinitionNode, BallerinaSemanticModel semanticModel,
                                        String fileName) {
        List<Function> functions = new ArrayList<>();
        String name = classDefinitionNode.className().text();
        String description = getDocFromMetadata(classDefinitionNode.metadata());
        boolean isDeprecated = isDeprecated(classDefinitionNode.metadata());

        List<DefaultableVariable> fields = getDefaultableVariableList(classDefinitionNode.members(),
                classDefinitionNode.metadata(), semanticModel, fileName);

        for (Node member: classDefinitionNode.members()) {
            if (member instanceof FunctionDefinitionNode && containsToken(((FunctionDefinitionNode) member)
                    .qualifierList(), SyntaxKind.PUBLIC_KEYWORD)) {
                functions.add(getFunctionModel((FunctionDefinitionNode) member, semanticModel, fileName));
            }
        }

        if (containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
            return new Client(name, description, isDeprecated, fields, functions);
        } else if (containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.LISTENER_KEYWORD)
                || name.equals("Listener")) {
            return new Listener(name, description, isDeprecated, fields, functions);
        } else {
            return new BClass(name, description, isDeprecated, fields, functions);
        }
    }

    private static BAbstractObject getAbstractObjectModel(TypeDefinitionNode typeDefinition,
                                                          BallerinaSemanticModel semanticModel, String fileName) {
        List<Function> functions = new ArrayList<>();
        String name = typeDefinition.typeName().text();
        String description = getDocFromMetadata(typeDefinition.metadata());
        ObjectTypeDescriptorNode typeDescriptorNode = (ObjectTypeDescriptorNode) typeDefinition.typeDescriptor();
        boolean isDeprecated = isDeprecated(typeDefinition.metadata());

        List<DefaultableVariable> fields = getDefaultableVariableList(typeDescriptorNode.members(),
                typeDefinition.metadata(), semanticModel, fileName);

        for (Node member: typeDescriptorNode.members()) {
            if (member instanceof MethodDeclarationNode) {
                MethodDeclarationNode methodNode = (MethodDeclarationNode) member;
                if (containsToken(methodNode.qualifierList(), SyntaxKind.PUBLIC_KEYWORD)) {
                    String methodName = methodNode.methodName().text();

                    List<Variable> returnParams = new ArrayList<>();
                    FunctionSignatureNode methodSignature = methodNode.methodSignature();

                    // Iterate through the parameters
                    List<DefaultableVariable> parameters = new ArrayList<>(getDefaultableVariableList(methodSignature
                                    .parameters(), methodNode.metadata(), semanticModel, fileName));

                    // return params
                    if (methodSignature.returnTypeDesc().isPresent()) {
                        ReturnTypeDescriptorNode returnType = methodSignature.returnTypeDesc().get();
                        Type type = Type.fromNode(returnType.type(), semanticModel, fileName);
                        returnParams.add(new Variable(EMPTY_STRING, getParameterDocFromMetadataList(RETURN_PARAM_NAME,
                                methodNode.metadata()), false, type));
                    }

                    boolean isRemote = containsToken(methodNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD);

                    functions.add(new Function(methodName, getDocFromMetadata(methodNode.metadata()),
                            isRemote, false, isDeprecated(methodNode.metadata()),
                            containsToken(methodNode.qualifierList(), SyntaxKind.ISOLATED_KEYWORD), parameters,
                            returnParams));
                }
            }
        }
        return new BAbstractObject(name, description, isDeprecated, fields, functions);
    }

    private static Function getFunctionModel(FunctionDefinitionNode functionDefinitionNode,
                                             BallerinaSemanticModel semanticModel, String fileName) {
        String functionName = functionDefinitionNode.functionName().text();

        List<DefaultableVariable> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();

        // Iterate through the parameters
        parameters.addAll(getDefaultableVariableList(functionSignature.parameters(),
                functionDefinitionNode.metadata(), semanticModel, fileName));

        // return params
        if (functionSignature.returnTypeDesc().isPresent()) {
            ReturnTypeDescriptorNode returnType = functionSignature.returnTypeDesc().get();
            Type type = Type.fromNode(returnType.type(), semanticModel, fileName);
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
                                             BallerinaSemanticModel semanticModel, String fileName) {
        String recordName = recordTypeDefinition.typeName().text();
        List<DefaultableVariable> fields = getDefaultableVariableList(((RecordTypeDescriptorNode) recordTypeDefinition
                .typeDescriptor()).fields(), recordTypeDefinition.metadata(), semanticModel, fileName);
        boolean isClosed = ((((RecordTypeDescriptorNode) recordTypeDefinition.typeDescriptor()).bodyStartDelimiter()))
                .kind().equals(SyntaxKind.OPEN_BRACE_PIPE_TOKEN);
        return new Record(recordName, getDocFromMetadata(recordTypeDefinition.metadata()),
                isDeprecated(recordTypeDefinition.metadata()), isClosed, fields);
    }

    public static List<DefaultableVariable> getDefaultableVariableList(NodeList nodeList,
                                                                       Optional<MetadataNode> optionalMetadataNode,
                                                                       BallerinaSemanticModel semanticModel,
                                                                       String fileName) {
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
                Type type = Type.fromNode(recordField.typeName(), semanticModel, fileName);
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
                Type type = Type.fromNode(recordField.typeName(), semanticModel, fileName);
                DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc, false, type,
                        "");
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
                    Type type = Type.fromNode(objectField.typeName(), semanticModel, fileName);
                    DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc,
                            isDeprecated(objectField.metadata()), type, defaultValue);
                    variables.add(defaultableVariable);
                }
            } else if (node instanceof RequiredParameterNode) {
                RequiredParameterNode requiredParameter = (RequiredParameterNode) node;
                String paramName = requiredParameter.paramName().isPresent() ?
                        requiredParameter.paramName().get().text() : "";
                Type type = Type.fromNode(requiredParameter.typeName(), semanticModel, fileName);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), isDeprecated(requiredParameter.annotations()), type, ""));
            } else if (node instanceof DefaultableParameterNode) {
                DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) node;
                String paramName = defaultableParameter.paramName().isPresent() ?
                        defaultableParameter.paramName().get().text() : "";
                Type type = Type.fromNode(defaultableParameter.typeName(), semanticModel, fileName);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), isDeprecated(defaultableParameter.annotations()),
                        type, defaultableParameter.expression().toString()));
            } else if (node instanceof RestParameterNode) {
                RestParameterNode restParameter = (RestParameterNode) node;
                String paramName = restParameter.paramName().isPresent() ?
                        restParameter.paramName().get().text() : "";
                Type type = new Type(paramName);
                type.isRestParam = true;
                type.elementType = Type.fromNode(restParameter.typeName(), semanticModel, fileName);
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
                    doc += getDocString(((MarkdownDocumentationLineNode) docLine).documentElements());
                } else {
                    break;
                }
            }
            return BallerinaDocUtils.mdToHtml(doc, false);
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
            return BallerinaDocUtils.mdToHtml(parameterDoc.toString(), false);
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
        return doc.toString();
    }
}
