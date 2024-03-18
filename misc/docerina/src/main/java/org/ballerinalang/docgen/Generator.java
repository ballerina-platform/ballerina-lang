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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
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
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.Annotation;
import org.ballerinalang.docgen.generator.model.AnnotationAttachment;
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
import org.ballerinalang.docgen.generator.model.FunctionKind;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.MapType;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.TableType;
import org.ballerinalang.docgen.generator.model.Type;
import org.ballerinalang.docgen.generator.model.Variable;
import org.ballerinalang.docgen.generator.model.types.FunctionType;
import org.ballerinalang.docgen.generator.model.types.ObjectType;

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
    public static final String REST_FIELD_DESCRIPTION = "Rest field";
    public static final String LISTENER_START_METHOD_NAME = "'start";
    public static final String LISTENER_ATTACH_METHOD_NAME = "attach";
    public static final String LISTENER_DETACH_METHOD_NAME = "detach";
    public static final String LISTENER_IMMEDIATE_STOP_METHOD_NAME = "immediateStop";
    public static final String LISTENER_GRACEFUL_STOP_METHOD_NAME = "gracefulStop";
    public static final String DOC_HEADER_PREFIX = "# ";

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
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD) ||
                            isTypePramOrBuiltinSubtype(typeDefinition.metadata())) {
                        hasPublicConstructs = addTypeDefinition(typeDefinition, module, semanticModel);
                    }
                } else if (node.kind() == SyntaxKind.CLASS_DEFINITION) {
                    ClassDefinitionNode classDefinition = (ClassDefinitionNode) node;
                    if (classDefinition.visibilityQualifier().isPresent() && classDefinition.visibilityQualifier().get()
                            .kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                        hasPublicConstructs = true;
                        BClass cls = getClassModel((ClassDefinitionNode) node, semanticModel, module);
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
                    module.functions.add(getFunctionModel((FunctionDefinitionNode) node, semanticModel, module));
                } else if (node.kind() == SyntaxKind.CONST_DECLARATION && ((ConstantDeclarationNode) node)
                        .visibilityQualifier().isPresent() && ((ConstantDeclarationNode) node).visibilityQualifier()
                        .get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.constants.add(getConstantTypeModel((ConstantDeclarationNode) node, semanticModel, module));
                } else if (node.kind() == SyntaxKind.ANNOTATION_DECLARATION && ((AnnotationDeclarationNode) node)
                        .visibilityQualifier().isPresent() && ((AnnotationDeclarationNode) node)
                        .visibilityQualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    hasPublicConstructs = true;
                    module.annotations.add(getAnnotationModel((AnnotationDeclarationNode) node, semanticModel, module));
                } else if (node.kind() == SyntaxKind.ENUM_DECLARATION &&
                        ((EnumDeclarationNode) node).qualifier().isPresent() &&
                        ((EnumDeclarationNode) node).qualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    module.enums.add(getEnumModel((EnumDeclarationNode) node));
                } else if (node.kind() == SyntaxKind.MODULE_VAR_DECL &&
                        ((ModuleVariableDeclarationNode) node).visibilityQualifier().isPresent() &&
                        ((ModuleVariableDeclarationNode) node).visibilityQualifier().get().kind()
                                .equals(SyntaxKind.PUBLIC_KEYWORD)) {
                    DefaultableVariable defaultableVariable = getModuleVariable((ModuleVariableDeclarationNode) node,
                            semanticModel, module);
                    if (containsToken(((ModuleVariableDeclarationNode) node).qualifiers(),
                            SyntaxKind.CONFIGURABLE_KEYWORD)) {
                        module.configurables.add(defaultableVariable);
                    } else {
                        module.variables.add(defaultableVariable);
                    }
                }
            }
        }
        return hasPublicConstructs;
    }

    public static boolean addTypeDefinition(TypeDefinitionNode typeDefinition, Module module, SemanticModel
            semanticModel) {

        String typeName = typeDefinition.typeName().text();
        Optional<MetadataNode> metaDataNode = typeDefinition.metadata();
        SyntaxKind syntaxKind = typeDefinition.typeDescriptor().kind();

        if (syntaxKind.equals(SyntaxKind.RECORD_TYPE_DESC)) {
            module.records.add(getRecordTypeModel((RecordTypeDescriptorNode) typeDefinition.typeDescriptor(),
                    typeName, metaDataNode, semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.OBJECT_TYPE_DESC)) {
            ObjectTypeDescriptorNode objectTypeDescriptorNode =
                    (ObjectTypeDescriptorNode) typeDefinition.typeDescriptor();
            BObjectType bObj = getObjectTypeModel(objectTypeDescriptorNode,
                    typeName, metaDataNode, semanticModel, module);
            if (containsToken(objectTypeDescriptorNode.objectTypeQualifiers(), SyntaxKind.SERVICE_KEYWORD)) {
                module.serviceTypes.add(bObj);
            } else {
                module.objectTypes.add(bObj);
            }
        } else if (syntaxKind.equals(SyntaxKind.UNION_TYPE_DESC)) {
            Type unionType = Type.fromNode(typeDefinition.typeDescriptor(), semanticModel, module);
            if (unionType.memberTypes.stream().allMatch(type ->
                    (type.category != null && type.category.equals("errors")) ||
                            (type.category != null && type.category.equals("builtin")) &&
                                    type.name.equals("error"))) {
                module.errors.add(new Error(typeName, getDocFromMetadata(metaDataNode),
                        getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode),
                        Type.fromNode(typeDefinition.typeDescriptor(), semanticModel, module)));
            } else {
                module.unionTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(),
                        typeName, metaDataNode, semanticModel, module));
            }
        } else if (syntaxKind.equals(SyntaxKind.SIMPLE_NAME_REFERENCE) ||
                syntaxKind.equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
            Type refType = Type.fromNode(typeDefinition.typeDescriptor(), semanticModel, module);
            if (refType.category.equals("errors")) {
                module.errors.add(new Error(typeName, getDocFromMetadata(metaDataNode),
                        getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode), refType));
            } else {
                module.simpleNameReferenceTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName,
                        metaDataNode, semanticModel, module));
            }
        } else if (syntaxKind.equals(SyntaxKind.DISTINCT_TYPE_DESC) &&
                ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor().kind()
                        == SyntaxKind.ERROR_TYPE_DESC) {
            Type detailType = null;
            ParameterizedTypeDescriptorNode parameterizedTypeDescNode = (ParameterizedTypeDescriptorNode)
                    ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor();
            if (parameterizedTypeDescNode.typeParamNode().isPresent()) {
                detailType = Type.fromNode(parameterizedTypeDescNode.typeParamNode().get().typeNode(), semanticModel,
                        module);
            }
            Error err = new Error(typeName, getDocFromMetadata(metaDataNode),
                    getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode), detailType);
            err.isDistinct = true;
            module.errors.add(err);
        } else if (syntaxKind.equals(SyntaxKind.DISTINCT_TYPE_DESC) &&
                ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor().kind()
                        == SyntaxKind.OBJECT_TYPE_DESC) {
            ObjectTypeDescriptorNode objectTypeDescriptorNode = (ObjectTypeDescriptorNode)
                    ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor();
            BObjectType bObj = getObjectTypeModel((ObjectTypeDescriptorNode)
                            ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor(), typeName,
                    metaDataNode, semanticModel, module);
            bObj.isDistinct = true;
            if (containsToken(objectTypeDescriptorNode.objectTypeQualifiers(), SyntaxKind.SERVICE_KEYWORD)) {
                module.serviceTypes.add(bObj);
            } else {
                module.objectTypes.add(bObj);
            }
        } else if (syntaxKind.equals(SyntaxKind.DISTINCT_TYPE_DESC) &&
                ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor().kind()
                        == SyntaxKind.PARENTHESISED_TYPE_DESC) {
            ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode = (ParenthesisedTypeDescriptorNode)
                    ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor();
            Type detailType = Type.fromNode(parenthesisedTypeDescriptorNode, semanticModel, module);
            Error err = new Error(typeName, getDocFromMetadata(metaDataNode),
                    getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode), detailType);
            err.isDistinct = true;
            module.errors.add(err);
        } else if (syntaxKind.equals(SyntaxKind.DISTINCT_TYPE_DESC) &&
                ((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor())).typeDescriptor().kind()
                        == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            Type refType = Type.fromNode(((DistinctTypeDescriptorNode) (typeDefinition.typeDescriptor()))
                    .typeDescriptor(), semanticModel, module);
            if (refType.category.equals("errors")) {
                Error err = new Error(typeName, getDocFromMetadata(metaDataNode),
                        getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode), refType);
                err.isDistinct = true;
                module.errors.add(err);
            } else {
                List<Type> memberTypes = new ArrayList<>();
                memberTypes.add(refType);
                BType bType = new BType(typeName, getDocFromMetadata(metaDataNode),
                        getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode), memberTypes);
                bType.isAnonymousUnionType = true;
                module.types.add(bType);
            }
        } else if (syntaxKind.equals(SyntaxKind.ERROR_TYPE_DESC)) {
            ParameterizedTypeDescriptorNode parameterizedTypeDescNode =
                    (ParameterizedTypeDescriptorNode) typeDefinition.typeDescriptor();
            Type type = null;
            if (parameterizedTypeDescNode.typeParamNode().isPresent()) {
                type = Type.fromNode(parameterizedTypeDescNode.typeParamNode().get().typeNode(),
                        semanticModel, module);
            }
            module.errors.add(new Error(typeName, getDocFromMetadata(metaDataNode),
                    getDescSectionsDocFromMetaDataList(metaDataNode), isDeprecated(metaDataNode), type));
        } else if (syntaxKind.equals(SyntaxKind.TUPLE_TYPE_DESC)) {
            module.tupleTypes.add(getTupleTypeModel((TupleTypeDescriptorNode) typeDefinition.typeDescriptor(),
                    typeName, metaDataNode, semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.TABLE_TYPE_DESC)) {
            module.tableTypes.add(getTableTypeModel((TableTypeDescriptorNode) typeDefinition.typeDescriptor(),
                    typeName, metaDataNode, semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.MAP_TYPE_DESC)) {
            module.mapTypes.add(getMapTypeModel((MapTypeDescriptorNode) typeDefinition.typeDescriptor(),
                    typeName, metaDataNode, semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.INTERSECTION_TYPE_DESC)) {
            addIntersectionTypeModel((IntersectionTypeDescriptorNode) typeDefinition.typeDescriptor(), typeName,
                    metaDataNode, semanticModel, module);
        } else if (syntaxKind.equals(SyntaxKind.TYPEDESC_TYPE_DESC)) {
            module.typeDescriptorTypes.add(getTypeDescModel((ParameterizedTypeDescriptorNode) typeDefinition.
                            typeDescriptor(), typeName, metaDataNode, semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.INT_TYPE_DESC)) {
            module.integerTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.DECIMAL_TYPE_DESC)) {
            module.decimalTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.XML_TYPE_DESC)) {
            module.xmlTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.FUNCTION_TYPE_DESC)) {
            module.functionTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.ANYDATA_TYPE_DESC)) {
            module.anyDataTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.STRING_TYPE_DESC)) {
            module.stringTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.ANY_TYPE_DESC)) {
            module.anyTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.ARRAY_TYPE_DESC)) {
            module.arrayTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else if (syntaxKind.equals(SyntaxKind.STREAM_TYPE_DESC)) {
            module.streamTypes.add(getUnionTypeModel(typeDefinition.typeDescriptor(), typeName, metaDataNode,
                    semanticModel, module));
        } else {
            return false;
        }
        return true;
        // TODO: handle value type nodes
        // TODO: handle built in ref type
        // TODO: handle constrained types
    }

    public static boolean containsToken(NodeList<Token> nodeList, SyntaxKind kind) {
        for (Node node: nodeList) {
            if (node.kind() == kind) {
                return true;
            }
        }
        return false;
    }

    public static DefaultableVariable getModuleVariable(ModuleVariableDeclarationNode moduleVariableNode,
                                                        SemanticModel semanticModel, Module module) {
        String name = moduleVariableNode.typedBindingPattern().bindingPattern().toSourceCode().replace(" ", "");
        String doc = getDocFromMetadata(moduleVariableNode.metadata());
        String defaultValue = moduleVariableNode.initializer().isPresent() ?
                moduleVariableNode.initializer().get().toSourceCode() : "";
        Type type = Type.fromNode(moduleVariableNode.typedBindingPattern().typeDescriptor(), semanticModel, module);
        return new DefaultableVariable(name, doc, false, type, defaultValue);
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
                List<String> descSections = getDescSectionsDocFromMetaDataList(enumDeclaration.metadata());
                members.add(new Construct(memberName, doc, descSections, false));
            }
        });
        return new Enum(enumName, getDocFromMetadata(enumDeclaration.metadata()),
                getDescSectionsDocFromMetaDataList(enumDeclaration.metadata()),
                isDeprecated(enumDeclaration.metadata()), members);
    }

    public static Annotation getAnnotationModel(AnnotationDeclarationNode annotationDeclaration,
                                                SemanticModel semanticModel, Module module) {
        String annotationName = annotationDeclaration.annotationTag().text();
        StringJoiner attachPointJoiner = new StringJoiner(", ");
        for (int i = 0; i < annotationDeclaration.attachPoints().size(); i++) {
            AnnotationAttachPointNode annotationAttachPointNode = (AnnotationAttachPointNode) annotationDeclaration
                    .attachPoints().get(i);
            attachPointJoiner.add(annotationAttachPointNode.toString());
        }

        Type dataType = annotationDeclaration.typeDescriptor().isPresent() ? Type.fromNode(annotationDeclaration.
                typeDescriptor().get(), semanticModel, module) : null;
        return new Annotation(annotationName, getDocFromMetadata(annotationDeclaration.metadata()),
                getDescSectionsDocFromMetaDataList(annotationDeclaration.metadata()),
                isDeprecated(annotationDeclaration.metadata()), dataType, attachPointJoiner.toString());
    }

    public static Constant getConstantTypeModel(ConstantDeclarationNode constantNode,
                                                SemanticModel semanticModel, Module module) {
        String constantName = constantNode.variableName().text();
        String value = constantNode.initializer().toString();
        String desc = getDocFromMetadata(constantNode.metadata());
        List<String> descriptionSections = getDescSectionsDocFromMetaDataList(constantNode.metadata());
        Type type;
        if (constantNode.typeDescriptor().isPresent()) {
            type = Type.fromNode(constantNode.typeDescriptor().get(), semanticModel, module);
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
        return new Constant(constantName, desc, descriptionSections, isDeprecated(constantNode.metadata()), type,
                value);
    }

    private static void addIntersectionTypeModel(IntersectionTypeDescriptorNode typeDescriptor, String typeName,
                                                 Optional<MetadataNode> optionalMetadataNode,
                                                 SemanticModel semanticModel, Module module) {
        boolean isReadonly = typeDescriptor.leftTypeDesc().kind() == SyntaxKind.READONLY_TYPE_DESC ||
                typeDescriptor.rightTypeDesc().kind() == SyntaxKind.READONLY_TYPE_DESC;
        if (isReadonly) {
            Node typeDef = typeDescriptor.leftTypeDesc().kind() == SyntaxKind.READONLY_TYPE_DESC ?
                    typeDescriptor.rightTypeDesc() : typeDescriptor.leftTypeDesc();
            if (typeDef.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                Record record = getRecordTypeModel((RecordTypeDescriptorNode) typeDef, typeName, optionalMetadataNode,
                        semanticModel, module);
                record.isReadOnly = true;
                module.records.add(record);
                return;
            } else if (typeDef.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                BObjectType bObj = getObjectTypeModel((ObjectTypeDescriptorNode) typeDef, typeName,
                        optionalMetadataNode, semanticModel, module);
                bObj.isReadOnly = true;
                module.objectTypes.add(bObj);
                return;
            }
        }
        List<Type> memberTypes = new ArrayList<>();
        Type.addIntersectionMemberTypes(typeDescriptor, semanticModel, memberTypes, module);
        BType bType = new BType(typeName, getDocFromMetadata(optionalMetadataNode),
                getDescSectionsDocFromMetaDataList(optionalMetadataNode), isDeprecated(optionalMetadataNode),
                memberTypes);
        bType.isIntersectionType = true;
        module.intersectionTypes.add(bType);
    }

    private static BType getTupleTypeModel(TupleTypeDescriptorNode typeDescriptor, String tupleTypeName,
                                           Optional<MetadataNode> optionalMetadataNode, SemanticModel semanticModel,
                                           Module module) {
        List<Type> memberTypes = new ArrayList<>();
        memberTypes.addAll(typeDescriptor.memberTypeDesc().stream().map(type ->
                Type.fromNode(type, semanticModel, module)).collect(Collectors.toList()));
        BType bType = new BType(tupleTypeName, getDocFromMetadata(optionalMetadataNode),
                getDescSectionsDocFromMetaDataList(optionalMetadataNode), isDeprecated(optionalMetadataNode),
                memberTypes);
        bType.isTuple = true;
        return bType;
    }

    private static BType getTypeDescModel(ParameterizedTypeDescriptorNode typeDescriptor, String typeName,
                                          Optional<MetadataNode> optionalMetadataNode, SemanticModel semanticModel,
                                          Module module) {
        Type type = null;
        if (typeDescriptor.typeParamNode().isPresent()) {
            type = Type.fromNode(typeDescriptor.typeParamNode().get().typeNode(), semanticModel, module);
        }
        BType bType = new BType(typeName, getDocFromMetadata(optionalMetadataNode),
                getDescSectionsDocFromMetaDataList(optionalMetadataNode), isDeprecated(optionalMetadataNode), null);
        bType.isTypeDesc = true;
        bType.version = BallerinaDocGenerator.getBallerinaShortVersion();
        bType.elementType = type;
        return bType;
    }

    private static BType getUnionTypeModel(Node unionTypeDescriptor, String unionName,
                                           Optional<MetadataNode> optionalMetadataNode, SemanticModel semanticModel,
                                           Module module) {
        List<Type> memberTypes = new ArrayList<>();
        Type.addUnionMemberTypes(unionTypeDescriptor, semanticModel, memberTypes, module);
        BType bType = new BType(unionName, getDocFromMetadata(optionalMetadataNode),
                                getDescSectionsDocFromMetaDataList(optionalMetadataNode),
                                isDeprecated(optionalMetadataNode), memberTypes);
        bType.isAnonymousUnionType = true;
        return bType;
    }

    private static MapType getMapTypeModel(MapTypeDescriptorNode typeDescriptor, String typeName,
                                           Optional<MetadataNode> optionalMetadataNode, SemanticModel semanticModel,
                                           Module module) {
        Type type = typeDescriptor.mapTypeParamsNode().isMissing()
                ? null : Type.fromNode(typeDescriptor, semanticModel, module);

        return new MapType(typeName, getDocFromMetadata(optionalMetadataNode),
                getDescSectionsDocFromMetaDataList(optionalMetadataNode), isDeprecated(optionalMetadataNode), type);
    }

    private static TableType getTableTypeModel(TableTypeDescriptorNode typeDescriptor, String typeName,
                                           Optional<MetadataNode> optionalMetadataNode, SemanticModel semanticModel,
                                           Module module) {
        Type rowParameterType = typeDescriptor.rowTypeParameterNode().isMissing()
                ? null : Type.fromNode(typeDescriptor, semanticModel, module);
        Type keyConstraintType = typeDescriptor.keyConstraintNode().isEmpty()
                ? null : Type.fromNode(typeDescriptor.keyConstraintNode().get(), semanticModel, module);

        return new TableType(typeName, getDocFromMetadata(optionalMetadataNode),
                getDescSectionsDocFromMetaDataList(optionalMetadataNode), isDeprecated(optionalMetadataNode),
                rowParameterType, keyConstraintType);
    }

    private static BClass getClassModel(ClassDefinitionNode classDefinitionNode, SemanticModel semanticModel,
                                        Module module) {
        List<Function> classFunctions = new ArrayList<>();
        List<Function> includedFunctions = new ArrayList<>();
        String name = classDefinitionNode.className().text();
        String description = getDocFromMetadata(classDefinitionNode.metadata());
        List<String> descriptionSections = getDescSectionsDocFromMetaDataList(classDefinitionNode.metadata());
        boolean isDeprecated = isDeprecated(classDefinitionNode.metadata());
        boolean isReadOnly = containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.READONLY_KEYWORD);
        boolean isIsolated = containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.ISOLATED_KEYWORD);
        boolean isService = containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.SERVICE_KEYWORD);

        List<DefaultableVariable> fields = getDefaultableVariableList(classDefinitionNode.members(),
                classDefinitionNode.metadata(), semanticModel, module);

        for (Node member : classDefinitionNode.members()) {
            if (member instanceof FunctionDefinitionNode && (containsToken(((FunctionDefinitionNode) member)
                    .qualifierList(), SyntaxKind.PUBLIC_KEYWORD) || containsToken(((FunctionDefinitionNode) member)
                    .qualifierList(), SyntaxKind.REMOTE_KEYWORD) || containsToken(((FunctionDefinitionNode) member)
                    .qualifierList(), SyntaxKind.RESOURCE_KEYWORD))) {
                classFunctions.add(getFunctionModel((FunctionDefinitionNode) member, semanticModel, module));
            } else if (member instanceof TypeReferenceNode) {
                Type originType = Type.fromNode(member, semanticModel, module);
                if (originType instanceof ObjectType) {
                    includedFunctions.addAll(mapFunctionTypesToFunctions(((ObjectType) originType).functionTypes,
                            originType));
                }
            }
        }

        // Get functions that are not overridden
        List<Function> functions = includedFunctions.stream().filter(includedFunction ->
                classFunctions
                        .stream()
                        .noneMatch(objFunction -> objFunction.name.equals(includedFunction.name)))
                .collect(Collectors.toList());

        functions.addAll(classFunctions);

        if (containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
            return new Client(name, description, descriptionSections, isDeprecated, fields, functions, isReadOnly,
                    isIsolated, isService);
        } else if (containsToken(classDefinitionNode.classTypeQualifiers(), SyntaxKind.LISTENER_KEYWORD)
                || isListenerModel(functions)) {
            return new Listener(name, description, descriptionSections, isDeprecated, fields, functions, isReadOnly,
                    isIsolated, isService);
        } else {
            return new BClass(name, description, descriptionSections, isDeprecated, fields, functions, isReadOnly,
                    isIsolated, isService);
        }
    }

    private static boolean isListenerModel(List<Function> classFunctions) {
        boolean isStartIncluded = false;
        boolean isAttachIncluded = false;
        boolean isDetachIncluded = false;
        boolean isGracefulStopIncluded = false;
        boolean isImmediateStopIncluded = false;

        for (Function function : classFunctions) {
            if (function.name.equals(LISTENER_START_METHOD_NAME)) {
                isStartIncluded = true;
            } else if (function.name.equals(LISTENER_ATTACH_METHOD_NAME)) {
                isAttachIncluded = true;
            } else if (function.name.equals(LISTENER_DETACH_METHOD_NAME)) {
                isDetachIncluded = true;
            } else if (function.name.equals(LISTENER_GRACEFUL_STOP_METHOD_NAME)) {
                isGracefulStopIncluded = true;
            } else if (function.name.equals(LISTENER_IMMEDIATE_STOP_METHOD_NAME)) {
                isImmediateStopIncluded = true;
            }
        }

        return isStartIncluded && isAttachIncluded && isDetachIncluded && isGracefulStopIncluded &&
                isImmediateStopIncluded;
    }

    private static BObjectType getObjectTypeModel(ObjectTypeDescriptorNode typeDescriptorNode, String objectName,
                                                  Optional<MetadataNode> optionalMetadataNode,
                                                  SemanticModel semanticModel, Module module) {
        List<Function> objectFunctions = new ArrayList<>();
        List<Function> includedFunctions = new ArrayList<>();

        String description = getDocFromMetadata(optionalMetadataNode);
        List<String> descriptionSections = getDescSectionsDocFromMetaDataList(optionalMetadataNode);
        boolean isDeprecated = isDeprecated(optionalMetadataNode);

        List<DefaultableVariable> fields = getDefaultableVariableList(typeDescriptorNode.members(),
                optionalMetadataNode, semanticModel, module);

        for (Node member : typeDescriptorNode.members()) {
            if (member instanceof MethodDeclarationNode) {
                MethodDeclarationNode methodNode = (MethodDeclarationNode) member;
                if (containsToken(methodNode.qualifierList(), SyntaxKind.PUBLIC_KEYWORD) ||
                        containsToken(methodNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD) ||
                        containsToken(methodNode.qualifierList(), SyntaxKind.RESOURCE_KEYWORD)) {
                    String methodName = "";
                    String accessor = "";
                    String resourcePath = "";
                    if (methodNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                        accessor = methodNode.methodName().text();
                        resourcePath = methodNode.relativeResourcePath().stream().
                                collect(StringBuilder::new, (firstString,
                                                             secondString) -> firstString.append(secondString),
                                        (nodeA, nodeB) -> nodeA.append(nodeB)).toString();
                    } else {
                        methodName = methodNode.methodName().text();
                    }

                    List<Variable> returnParams = new ArrayList<>();
                    FunctionSignatureNode methodSignature = methodNode.methodSignature();

                    // Iterate through the parameters
                    List<DefaultableVariable> parameters = new ArrayList<>(getDefaultableVariableList(methodSignature
                                    .parameters(), methodNode.metadata(), semanticModel, module));

                    // return params
                    if (methodSignature.returnTypeDesc().isPresent()) {
                        ReturnTypeDescriptorNode returnType = methodSignature.returnTypeDesc().get();
                        Type type = Type.fromNode(returnType.type(), semanticModel, module);
                        returnParams.add(new Variable(EMPTY_STRING, getParameterDocFromMetadataList(RETURN_PARAM_NAME,
                                methodNode.metadata()), false, type));
                    }

                    FunctionKind functionKind;
                    if (containsToken(methodNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD)) {
                        functionKind = FunctionKind.REMOTE;
                    } else if (containsToken(methodNode.qualifierList(), SyntaxKind.RESOURCE_KEYWORD)) {
                        functionKind = FunctionKind.RESOURCE;
                    } else {
                        functionKind = FunctionKind.OTHER;
                    }

                    objectFunctions.add(new Function(methodName, accessor, resourcePath,
                            getDocFromMetadata(methodNode.metadata()),
                            getDescSectionsDocFromMetaDataList(methodNode.metadata()), functionKind, false,
                            isDeprecated(methodNode.metadata()), containsToken(methodNode.qualifierList(),
                            SyntaxKind.ISOLATED_KEYWORD), parameters, returnParams));
                }
            } else if (member instanceof TypeReferenceNode) {
                Type originType = Type.fromNode(member, semanticModel, module);
                if (originType instanceof ObjectType) {
                    includedFunctions.addAll(mapFunctionTypesToFunctions(((ObjectType) originType).functionTypes,
                            originType));
                }
            }
        }

        // Get functions that are not overridden
        List<Function> functions = includedFunctions.stream().filter(includedFunction ->
                objectFunctions
                        .stream()
                        .noneMatch(objFunction -> objFunction.name.equals(includedFunction.name)))
                .collect(Collectors.toList());

        functions.addAll(objectFunctions);

        return new BObjectType(objectName, description, descriptionSections, isDeprecated, fields, functions);
    }

    private static List<Function> mapFunctionTypesToFunctions(List<FunctionType> functionTypes, Type originType) {
        List<Function> functions = new ArrayList<>();
        for (FunctionType functionType : functionTypes) {
            List<DefaultableVariable> parameters = new ArrayList<>(functionType.paramTypes.stream()
                    .map(type -> new DefaultableVariable(type.name, type.description, false,
                            type.elementType, "")).collect(Collectors.toList()));

            List<Variable> returnParameters = new ArrayList<>();
            if (functionType.returnType != null) {
                returnParameters.add(new Variable(EMPTY_STRING, functionType.returnType.description,
                        functionType.returnType.isDeprecated, functionType.returnType));
            }

            Function function = new Function(functionType.name, functionType.accessor, functionType.resourcePath,
                    functionType.description, functionType.descriptionSections, functionType.functionKind,
                    functionType.isExtern, functionType.isDeprecated, functionType.isIsolated, parameters,
                    returnParameters);
            function.inclusionType = originType.isPublic ? originType : null;
            functions.add(function);
        }
        return functions;
    }

    private static Function getFunctionModel(FunctionDefinitionNode functionDefinitionNode,
                                             SemanticModel semanticModel, Module module) {
        String functionName = "";
        String accessor = "";
        String resourcePath = "";
        if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
            accessor = functionDefinitionNode.functionName().text();
            resourcePath = functionDefinitionNode.relativeResourcePath().stream().
                    collect(StringBuilder::new, (firstString, secondString) -> firstString.append(secondString),
                            (nodeA, nodeB) -> nodeA.append(nodeB)).toString();
        } else {
            functionName = functionDefinitionNode.functionName().text();
        }

        List<DefaultableVariable> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();

        // Iterate through the parameters
        parameters.addAll(getDefaultableVariableList(functionSignature.parameters(),
                functionDefinitionNode.metadata(), semanticModel, module));

        List<AnnotationAttachment> annotationAttachments = extractAnnotationAttachmentsFromMetadataNode(semanticModel,
                functionDefinitionNode.metadata());
        // return params
        if (functionSignature.returnTypeDesc().isPresent()) {
            ReturnTypeDescriptorNode returnType = functionSignature.returnTypeDesc().get();
            Type type = Type.fromNode(returnType.type(), semanticModel, module);
            returnParams.add(new Variable(EMPTY_STRING, getParameterDocFromMetadataList(RETURN_PARAM_NAME,
                    functionDefinitionNode.metadata()), false, type));
        }

        boolean isExtern = functionDefinitionNode.functionBody() instanceof ExternalFunctionBodyNode;
        FunctionKind functionKind;
        if (containsToken(functionDefinitionNode.qualifierList(), SyntaxKind.REMOTE_KEYWORD)) {
            functionKind = FunctionKind.REMOTE;
        } else if (containsToken(functionDefinitionNode.qualifierList(), SyntaxKind.RESOURCE_KEYWORD)) {
            functionKind = FunctionKind.RESOURCE;
        } else {
            functionKind = FunctionKind.OTHER;
        }

        return new Function(functionName, accessor, resourcePath, getDocFromMetadata(functionDefinitionNode.metadata()),
                getDescSectionsDocFromMetaDataList(functionDefinitionNode.metadata()),
                functionKind, isExtern, isDeprecated(functionDefinitionNode.metadata()),
                containsToken(functionDefinitionNode.qualifierList(), SyntaxKind.ISOLATED_KEYWORD), parameters,
                returnParams, annotationAttachments);
    }

    private static Record getRecordTypeModel(RecordTypeDescriptorNode recordTypeDesc, String recordName,
                                             Optional<MetadataNode> optionalMetadataNode,
                                             SemanticModel semanticModel, Module module) {

        List<DefaultableVariable> fields = getDefaultableVariableList(recordTypeDesc.fields(),
                optionalMetadataNode, semanticModel, module);
        boolean isClosed = (recordTypeDesc.bodyStartDelimiter()).kind().equals(SyntaxKind.OPEN_BRACE_PIPE_TOKEN);
        if (recordTypeDesc.recordRestDescriptor().isPresent()) {
            isClosed = false;
            DefaultableVariable restVariable = new DefaultableVariable("", REST_FIELD_DESCRIPTION,
                    false, Type.fromNode(recordTypeDesc.recordRestDescriptor().get(), semanticModel, module), "");
            fields.add(restVariable);
        }
        return new Record(recordName, getDocFromMetadata(optionalMetadataNode),
                          getDescSectionsDocFromMetaDataList(optionalMetadataNode),
                          isDeprecated(optionalMetadataNode), isClosed, fields);
    }

    public static List<DefaultableVariable> getDefaultableVariableList(NodeList nodeList,
                                                                       Optional<MetadataNode> optionalMetadataNode,
                                                                       SemanticModel semanticModel, Module module) {
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
                Type type = Type.fromNode(recordField.typeName(), semanticModel, module);
                DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc, false, type,
                        defaultValue, extractAnnotationAttachmentsFromMetadataNode(semanticModel,
                        recordField.metadata()));
                if (recordField.readonlyKeyword().isPresent()) {
                    defaultableVariable.isReadOnly = true;
                }
                variables.add(defaultableVariable);
            } else if (node instanceof RecordFieldNode) {
                RecordFieldNode recordField = (RecordFieldNode) node;
                String name = recordField.fieldName().text();
                String doc = getDocFromMetadata(recordField.metadata());
                if (doc.equals("")) {
                    doc = getParameterDocFromMetadataList(name, optionalMetadataNode);
                }
                Type type = Type.fromNode(recordField.typeName(), semanticModel, module);
                type.isNullable = recordField.questionMarkToken().isPresent();
                DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc,
                        isDeprecated(recordField.metadata()), type, "",
                        extractAnnotationAttachmentsFromMetadataNode(semanticModel, recordField.metadata()));
                if (recordField.readonlyKeyword().isPresent()) {
                    defaultableVariable.isReadOnly = true;
                }
                variables.add(defaultableVariable);
            } else if (node instanceof TypeReferenceNode) {
                Type originType = Type.fromNode(node, semanticModel, module);
                if (!originType.isPublic) {
                    variables.addAll(originType.memberTypes.stream()
                            .map(type -> new DefaultableVariable(type.name, type.description, false,
                                    type.elementType, "")).collect(Collectors.toList()));
                } else if (originType.memberTypes.size() > 0) {
                    variables.add(new DefaultableVariable(originType));
                }
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
                    Type type = Type.fromNode(objectField.typeName(), semanticModel, module);
                    DefaultableVariable defaultableVariable = new DefaultableVariable(name, doc,
                            isDeprecated(objectField.metadata()), type, defaultValue,
                            extractAnnotationAttachmentsFromMetadataNode(semanticModel, objectField.metadata()));
                    variables.add(defaultableVariable);
                }
            } else if (node instanceof RequiredParameterNode) {
                RequiredParameterNode requiredParameter = (RequiredParameterNode) node;
                String paramName = requiredParameter.paramName().isPresent() ?
                        requiredParameter.paramName().get().text() : "";
                Type type = Type.fromNode(requiredParameter.typeName(), semanticModel, module);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), isDeprecated(requiredParameter.annotations()), type, ""));
            } else if (node instanceof DefaultableParameterNode) {
                DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) node;
                String paramName = defaultableParameter.paramName().isPresent() ?
                        defaultableParameter.paramName().get().text() : "";
                Type type = Type.fromNode(defaultableParameter.typeName(), semanticModel, module);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), isDeprecated(defaultableParameter.annotations()),
                        type, defaultableParameter.expression().toString(),
                        extractAnnotationAttachmentsFromAnnotations(semanticModel,
                                defaultableParameter.annotations())));
            } else if (node instanceof RestParameterNode) {
                RestParameterNode restParameter = (RestParameterNode) node;
                String paramName = restParameter.paramName().isPresent() ?
                        restParameter.paramName().get().text() : "";
                Type type = new Type(paramName);
                type.isRestParam = true;
                type.elementType = Type.fromNode(restParameter.typeName(), semanticModel, module);
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), false, type, ""));
            } else if (node instanceof IncludedRecordParameterNode) {
                IncludedRecordParameterNode includedRecord = (IncludedRecordParameterNode) node;
                String paramName = includedRecord.paramName().isPresent() ?
                        includedRecord.paramName().get().text() : "";
                Type type = Type.fromNode(includedRecord.typeName(), semanticModel, module);
                type.isInclusion = true;
                variables.add(new DefaultableVariable(paramName, getParameterDocFromMetadataList(paramName,
                        optionalMetadataNode), false, type, ""));
            }

        }
        return variables;
    }

    private static List<AnnotationAttachment> extractAnnotationAttachmentsFromMetadataNode(SemanticModel semanticModel,
                                                 Optional<MetadataNode> metadata) {
        List<AnnotationAttachment> annotationAttachments = new ArrayList<>();
        metadata.ifPresent(metadataNode -> metadataNode.annotations().forEach(annotationNode -> {
            Symbol symbol = semanticModel.symbol(annotationNode).orElse(null);
            if (symbol == null) {
                return;
            }
            final ModuleID id = symbol.getModule().get().id();
            annotationAttachments.add(
                    new AnnotationAttachment(symbol.getName().orElse(""), "", null, false,
                            id.orgName(), id.moduleName(), id.version()));
        }));
        return annotationAttachments;
    }

    private static List<AnnotationAttachment> extractAnnotationAttachmentsFromAnnotations(SemanticModel semanticModel
            , NodeList<AnnotationNode> annotations) {
        List<AnnotationAttachment> annotationAttachments = new ArrayList<>();
        annotations.forEach(annotationNode -> {
            Symbol symbol = semanticModel.symbol(annotationNode).orElse(null);
            if (symbol == null) {
                return;
            }
            final ModuleID id = symbol.getModule().get().id();
            annotationAttachments.add(
                    new AnnotationAttachment(symbol.getName().orElse(""), "", null, false,
                            id.orgName(), id.moduleName(), id.version()));
        });
        return annotationAttachments;
    }

    private static boolean isTypePramOrBuiltinSubtype(Optional<MetadataNode> metadataNode) {
        if (metadataNode.isEmpty()) {
            return false;
        }
        return metadataNode.get().annotations().stream().anyMatch(annotationNode ->
                annotationNode.toString().contains("@typeParam") ||
                        annotationNode.toString().contains("@builtinSubtype"));
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
        StringBuilder doc = new StringBuilder();
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        if (docLines != null) {
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownDocumentationLineNode) {
                    String docLineString = getDocLineString(((MarkdownDocumentationLineNode) docLine).
                            documentElements());
                    if (docLineString.startsWith(DOC_HEADER_PREFIX)) {
                        break;
                    }
                    doc.append(!((MarkdownDocumentationLineNode) docLine).documentElements().isEmpty() ?
                            getDocLineString(((MarkdownDocumentationLineNode) docLine).documentElements()) : "\n");
                } else if (docLine instanceof MarkdownCodeBlockNode) {
                    doc.append(getDocCodeBlockString((MarkdownCodeBlockNode) docLine));
                } else {
                    break;
                }
            }
        }

        return doc.toString();
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
                        parameterDoc.append(getDocLineString(((MarkdownParameterDocumentationLineNode) docLine)
                                .documentElements()));
                        lookForMoreLines = true;
                    } else {
                        lookForMoreLines = false;
                    }
                } else if (lookForMoreLines && docLine instanceof MarkdownDocumentationLineNode) {
                    String docLineString = getDocLineString(((MarkdownDocumentationLineNode) docLine)
                            .documentElements());
                    if (!docLineString.isEmpty()) {
                        parameterDoc.append(docLineString);
                    } else {
                        lookForMoreLines = false;
                    }
                }
            }
        }

        return parameterDoc.toString();
    }

    private static List<String> getDescSectionsDocFromMetaDataList(Optional<MetadataNode> optionalMetadataNode) {
        if (optionalMetadataNode.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> descSections = new ArrayList<>();
        MarkdownDocumentationNode docLines = optionalMetadataNode.get().documentationString().isPresent() ?
                (MarkdownDocumentationNode) optionalMetadataNode.get().documentationString().get() : null;
        if (docLines != null) {
            StringBuilder sectionDoc = new StringBuilder();
            boolean lookForMoreLines = false;
            for (Node docLine : docLines.documentationLines()) {
                if (docLine instanceof MarkdownDocumentationLineNode) {
                    String docLineString = getDocLineString(((MarkdownDocumentationLineNode) docLine)
                            .documentElements());
                    if (!docLineString.isEmpty()) {
                        if (docLineString.startsWith(DOC_HEADER_PREFIX)) {
                            sectionDoc = new StringBuilder();
                            sectionDoc.append(docLineString);
                            lookForMoreLines = true;
                        } else if (lookForMoreLines) {
                            sectionDoc.append(docLineString);
                        }
                    } else {
                        if (sectionDoc != null && !sectionDoc.toString().isEmpty()) {
                            descSections.add(sectionDoc.toString());
                            sectionDoc = null;
                        }
                        lookForMoreLines = false;
                    }
                } else {
                    if (sectionDoc != null && !sectionDoc.toString().isEmpty()) {
                        descSections.add(sectionDoc.toString());
                        sectionDoc = null;
                    }
                    lookForMoreLines = false;
                }
            }
            if (sectionDoc != null && !sectionDoc.toString().isEmpty()) {
                descSections.add(sectionDoc.toString());
            }
        }

        return descSections;
    }

    private static String getDocLineString(NodeList<Node> documentElements) {
        if (documentElements.isEmpty()) {
            return "";
        }
        StringBuilder doc = new StringBuilder();
        for (Node docNode : documentElements) {
            doc.append(docNode.toString());
        }

        return doc.toString();
    }

    private static String getDocCodeBlockString(MarkdownCodeBlockNode markdownCodeBlockNode) {
        StringBuilder doc = new StringBuilder();

        doc.append(markdownCodeBlockNode.startBacktick().toString());
        markdownCodeBlockNode.langAttribute().ifPresent(langAttribute -> doc.append(langAttribute));

        for (MarkdownCodeLineNode codeLineNode : markdownCodeBlockNode.codeLines()) {
            doc.append(codeLineNode.codeDescription().toString());
        }

        doc.append(markdownCodeBlockNode.endBacktick().toString());
        return doc.toString();
    }
}
