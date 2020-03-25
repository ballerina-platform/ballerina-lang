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

import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.generator.model.Annotation;
import org.ballerinalang.docgen.generator.model.Client;
import org.ballerinalang.docgen.generator.model.Constant;
import org.ballerinalang.docgen.generator.model.DefaultableVarible;
import org.ballerinalang.docgen.generator.model.Error;
import org.ballerinalang.docgen.generator.model.FiniteType;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Listener;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Object;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.Type;
import org.ballerinalang.docgen.generator.model.UnionType;
import org.ballerinalang.docgen.generator.model.Variable;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Generates the Page objects for bal packages.
 */
public class Generator {

    private static final String EMPTY_STRING = "";

    /**
     * Generate the module constructs model when the bal module is given.
     *
     * @param module  module model to fill.
     * @param balPackage module tree.
     */
    public static void generateModuleConstructs(Module module, BLangPackage balPackage) {

        // Check for type definitions in the package
        for (BLangTypeDefinition typeDefinition : balPackage.getTypeDefinitions()) {
            if (typeDefinition.getFlags().contains(Flag.PUBLIC)) {
                createTypeDefModels(typeDefinition, module);
            }
        }

        // Check for functions in the package
        for (BLangFunction function : balPackage.getFunctions()) {
            if (function.getFlags().contains(Flag.PUBLIC) && !function.getFlags().contains(Flag.ATTACHED)) {
                module.functions.add(createDocForFunction(function, module));
            }
        }

        // Check for annotations
        for (BLangAnnotation annotation : balPackage.getAnnotations()) {
            if (annotation.getFlags().contains(Flag.PUBLIC)) {
                module.annotations.add(createDocForAnnotation(annotation, module));
            }
        }

        // Check for constants.
        for (BLangConstant constant : balPackage.getConstants()) {
            if (constant.getFlags().contains(Flag.PUBLIC)) {
                module.constants.add(createDocForConstant(constant, module));
            }
        }
    }

    /**
     * Create documentation models for type definitions.
     *
     * @param typeDefinition ballerina type definition node.
     * @param module Module model.
     */
    public static void createTypeDefModels(BLangTypeDefinition typeDefinition, Module module) {
        String typeName = typeDefinition.getName().getValue();
        BLangType typeNode = typeDefinition.typeNode;
        NodeKind kind = typeNode.getKind();
        boolean added = false;
        if (kind == NodeKind.OBJECT_TYPE) {
            BLangObjectTypeNode objectType = (BLangObjectTypeNode) typeNode;
            addDocForObjectType(objectType, typeDefinition, module);
            added = true;
        } else if (kind == NodeKind.FINITE_TYPE_NODE) {
            BLangFiniteTypeNode enumNode = (BLangFiniteTypeNode) typeNode;
            List<String> values = enumNode.getValueSet().stream()
                    .map(java.lang.Object::toString)
                    .collect(Collectors.toList());
            module.finiteTypes.add(new FiniteType(typeName, description(typeDefinition), values));
            added = true;
        } else if (kind == NodeKind.RECORD_TYPE) {
            BLangRecordTypeNode recordNode = (BLangRecordTypeNode) typeNode;
            if (recordNode.isAnonymous) {
                return;
            }
            addDocForRecordType(typeDefinition, recordNode, module);
            added = true;
        } else if (kind == NodeKind.UNION_TYPE_NODE) {
            List<BLangType> memberTypeNodes = ((BLangUnionTypeNode) typeNode).memberTypeNodes;
            List<Type> memberTypes = memberTypeNodes.stream()
                    .map(type -> Type.fromTypeNode(type, module.id))
                    .collect(Collectors.toList());
            module.unionTypes.add(new UnionType(typeName, description(typeDefinition), memberTypes));
            added = true;
        } else if (kind == NodeKind.USER_DEFINED_TYPE) {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) typeNode;
            module.unionTypes.add(new UnionType(typeName, description(typeDefinition),
                    Collections.singletonList(Type.fromTypeNode(userDefinedType, module.id))));
            added = true;
        } else if (kind == NodeKind.VALUE_TYPE) {
            // TODO: handle value type nodes
            added = true;
        } else if (kind == NodeKind.TUPLE_TYPE_NODE) {
            // TODO: handle tuple type nodes
            added = true;
        } else if (kind == NodeKind.ERROR_TYPE) {
            BLangErrorType errorType = (BLangErrorType) typeNode;
            Type detailType = errorType.detailType != null ? Type.fromTypeNode(errorType.detailType, module.id) : null;
            module.errors.add(new Error(errorType.type.tsymbol.name.value, description(typeDefinition), detailType));
            added = true;
        } else if (kind == NodeKind.FUNCTION_TYPE) {
            // TODO: handle function type nodes
            added = true;
        }
        if (!added) {
            throw new UnsupportedOperationException("Type def not supported for " + kind);
        }
    }

    /**
     * Create documentation for annotations.
     *
     * @param annotationNode ballerina annotation node.
     * @param module Module instance                      
     * @return documentation for annotation.
     */
    public static Annotation createDocForAnnotation(BLangAnnotation annotationNode, Module module) {
        String annotationName = annotationNode.getName().getValue();
        String attachments = annotationNode.getAttachPoints().stream()
                .map(attachPoint -> attachPoint.point.getValue())
                .collect(Collectors.joining(", "));
        Type dataType = annotationNode.typeNode != null ? Type.fromTypeNode(annotationNode.typeNode, module.id) : null;
        return new Annotation(annotationName, description(annotationNode), dataType, attachments);
    }

    public static Constant createDocForConstant(BLangConstant constant, Module module) {
        String constantName = constant.getName().getValue();
        java.lang.Object value = constant.symbol.value;
        String desc = description(constant);
        BLangType typeNode = constant.typeNode != null ? constant.typeNode : constant.associatedTypeDefinition.typeNode;
        return new Constant(constantName, desc, Type.fromTypeNode(typeNode, module.id), value.toString());
    }

    /**
     * Create documentation for functions.
     *
     * @param functionNode ballerina function node.
     * @param module Module instance                    
     * @return Function documentation model.
     */
    public static Function createDocForFunction(BLangFunction functionNode, Module module) {
        String functionName = functionNode.getName().value;
        List<DefaultableVarible> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        // Iterate through the parameters
        if (functionNode.getParameters().size() > 0) {
            for (BLangSimpleVariable param : functionNode.getParameters()) {
                DefaultableVarible variable = getVariable(functionNode, param, module);
                parameters.add(variable);
            }
        }
        // rest params
        if (functionNode.getRestParameters() != null) {
            SimpleVariableNode restParameter = functionNode.getRestParameters();
            if (restParameter instanceof BLangSimpleVariable) {
                BLangSimpleVariable param = (BLangSimpleVariable) restParameter;
                DefaultableVarible variable = getVariable(functionNode, param, module);
                parameters.add(variable);
            }
        }

        // return params
        if (functionNode.getReturnTypeNode() != null) {
            BLangType returnType = functionNode.getReturnTypeNode();
            String dataType = getTypeName(returnType);
            if (!dataType.equals("null")) {
                String desc = returnParamAnnotation(functionNode);
                Variable variable = new Variable(EMPTY_STRING, desc, Type.fromTypeNode(returnType, module.id));
                returnParams.add(variable);
            }

        }

        return new Function(functionName, description(functionNode),
                        functionNode.getFlags().contains(Flag.REMOTE),
                        functionNode.getFlags().contains(Flag.NATIVE),
                        parameters, returnParams);
    }

    private static DefaultableVarible getVariable(BLangFunction functionNode, BLangSimpleVariable param, Module mod) {
        String desc = paramAnnotation(functionNode, param);
        String defaultValue = EMPTY_STRING;
        if (null != param.getInitialExpression()) {
            defaultValue = param.getInitialExpression().toString();
        }
        return new DefaultableVarible(param.getName().value, desc, Type.fromTypeNode(param.typeNode, mod.id),
                defaultValue);
    }

    /**
     * Create documentation for records.
     *
     * @param typeDefinition struct name.
     * @param recordType ballerina record node.
     * @param module Module.
     *
     */
    private static void addDocForRecordType(BLangTypeDefinition typeDefinition, BLangRecordTypeNode recordType,
                                            Module module) {
        String recordName = typeDefinition.getName().getValue();
        // Check if its an anonymous struct
        if (recordType.isAnonymous) {
            recordName = "Anonymous Record " + recordName.substring(recordName.lastIndexOf('$') + 1);
        }
        BLangMarkdownDocumentation documentationNode = typeDefinition.getMarkdownDocumentationAttachment();
        List<DefaultableVarible> fields = getFields(recordType, recordType.fields, documentationNode, module);
        String documentationText = documentationNode == null ? null : documentationNode.getDocumentation();

        module.records.add(new Record(recordName, documentationText, fields));
    }

    private static List<DefaultableVarible> getFields(BLangNode node, List<BLangSimpleVariable> allFields,
                                         BLangMarkdownDocumentation documentation, Module module) {
        List<DefaultableVarible> fields = new ArrayList<>();
        for (BLangSimpleVariable param : allFields) {
            if (param.getFlags().contains(Flag.PUBLIC)) {
                String name = param.getName().value;
                String desc = fieldAnnotation(node, param);
                desc = desc.isEmpty() ? findDescFromList(name, documentation) : desc;

                String defaultValue = EMPTY_STRING;
                if (null != param.getInitialExpression()) {
                    defaultValue = param.getInitialExpression().toString();
                }
                DefaultableVarible field = new DefaultableVarible(name, desc,
                        Type.fromTypeNode(param.typeNode, module.id), defaultValue);
                fields.add(field);
            }
        }
        return fields;
    }

    private static String findDescFromList(String name, BLangMarkdownDocumentation documentation) {
        if (documentation == null) {
            return EMPTY_STRING;
        }
        Map<String, BLangMarkdownParameterDocumentation> parameterDocumentations =
                documentation.getParameterDocumentations();
        BLangMarkdownParameterDocumentation parameter = parameterDocumentations.get(name);
        if (parameter == null) {
            return EMPTY_STRING;
        }
        return BallerinaDocUtils.mdToHtml(parameter.getParameterDocumentation());
    }

    private static void addDocForObjectType(BLangObjectTypeNode objectType,
                                            BLangTypeDefinition parent,
                                            Module module) {
        List<Function> functions = new ArrayList<>();
        String name = parent.getName().getValue();
        String description = description(parent);

        List<DefaultableVarible> fields = getFields(parent, objectType.fields,
                    parent.getMarkdownDocumentationAttachment(), module);

        if (objectType.initFunction != null) {
            BLangFunction constructor = objectType.initFunction;
            if (constructor.flagSet.contains(Flag.PUBLIC)) {
                Function initFunction = createDocForFunction(constructor, module);
                // if it's the default constructor, we don't need to document
                if (initFunction.parameters.size() > 0) {
                    functions.add(initFunction);
                }
            }
        }

        // Iterate through the functions
        if (objectType.getFunctions().size() > 0) {
            for (BLangFunction function : objectType.getFunctions()) {
                if (function.flagSet.contains(Flag.PUBLIC)) {
                    functions.add(createDocForFunction(function, module));
                }
            }
        }

        if (isEndpoint(objectType)) {
            module.clients.add(new Client(name, description, fields, functions));
        } else if (isListener(objectType)) {
            module.listeners.add(new Listener(name, description, fields, functions));
        } else {
            module.objects.add(new Object(name, description, fields, functions));
        }
    }

    private static boolean isListener(BLangObjectTypeNode objectType) {
        AtomicBoolean isListener = new AtomicBoolean(false);
        objectType.typeRefs.forEach((type) -> {
            isListener.set((type instanceof BLangUserDefinedType)
                    && ((BLangUserDefinedType) type).typeName.value.equals("Listener"));
        });
        return isListener.get();
    }

    private static boolean isEndpoint(BLangObjectTypeNode objectType) {
        return objectType.flagSet.contains(Flag.CLIENT);
    }

    /**
     * Get the type name of the type node.
     *
     * @param bLangType a variable
     * @return type name.
     */
    private static String getTypeName(BLangType bLangType) {
        return (bLangType instanceof BLangUserDefinedType ?
                ((BLangUserDefinedType) bLangType).typeName.value : bLangType.toString());
    }

    /**
     * Get description annotation of the parameter.
     *
     * @param node  parent node.
     * @param param parameter.
     * @return description of the parameter.
     */
    private static String paramAnnotation(BLangNode node, BLangSimpleVariable param) {
        String subName = param.getName() == null ? param.type.tsymbol.name.value : param.getName().getValue();
        return getParameterDocumentation(node, subName);
    }

    /**
     * Get description annotation of the return parameter.
     *
     * @param node parent node.
     * @return description of the return parameter.
     */
    private static String returnParamAnnotation(BLangNode node) {
        if (!isDocumentAttached(node)) {
            return EMPTY_STRING;
        }
        BLangMarkdownDocumentation documentationAttachment =
                ((DocumentableNode) node).getMarkdownDocumentationAttachment();
        return BallerinaDocUtils.mdToHtml(documentationAttachment.getReturnParameterDocumentation());
    }

    /**
     * Get description annotation of the field.
     *
     * @param node  parent node.
     * @param param field.
     * @return description of the field.
     */
    private static String fieldAnnotation(BLangNode node, BLangNode param) {
        String subName = EMPTY_STRING;
        if (!(param instanceof BLangSimpleVariable)) {
            return getParameterDocumentation(node, subName);
        }
        BLangSimpleVariable paramVariable = (BLangSimpleVariable) param;
        subName = (paramVariable.getName() == null) ?
                paramVariable.type.tsymbol.name.value : paramVariable.getName().getValue();
        return getParameterDocumentation(node, subName);
    }

    /**
     * Get the description annotation of the node.
     *
     * @param node top level node.
     * @return description of the node.
     */
    private static String description(BLangNode node) {
        if (isDocumentAttached(node)) {
            BLangMarkdownDocumentation documentationAttachment =
                    ((DocumentableNode) node).getMarkdownDocumentationAttachment();
            return BallerinaDocUtils.mdToHtml(documentationAttachment.getDocumentation());
        }
        return EMPTY_STRING;
    }

    private static String getParameterDocumentation(BLangNode node, String subName) {
        if (!isDocumentAttached(node)) {
            return EMPTY_STRING;
        }
        BLangMarkdownDocumentation documentationAttachment =
                ((DocumentableNode) node).getMarkdownDocumentationAttachment();
        Map<String, BLangMarkdownParameterDocumentation> parameterDocumentations =
                documentationAttachment.getParameterDocumentations();
        if (parameterDocumentations == null || parameterDocumentations.isEmpty()) {
            return EMPTY_STRING;
        }
        BLangMarkdownParameterDocumentation documentation = parameterDocumentations.get(subName);
        if (documentation != null) {
            return BallerinaDocUtils.mdToHtml(documentation.getParameterDocumentation());
        }
        return EMPTY_STRING;
    }

    private static boolean isDocumentAttached(BLangNode node) {
        return node instanceof DocumentableNode
                && ((DocumentableNode) node).getMarkdownDocumentationAttachment() != null;
    }
}
