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

import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocDataHolder;
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
import org.ballerinalang.docgen.model.Documentable;
import org.ballerinalang.docgen.model.EndpointDoc;
import org.ballerinalang.docgen.model.Field;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.Link;
import org.ballerinalang.docgen.model.ObjectDoc;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.PrimitiveTypeDoc;
import org.ballerinalang.docgen.model.StaticCaption;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generates the Page objects for bal packages.
 */
public class Generator {

    private static final Predicate<BLangFunction> IS_REMOTE_FUNCTION = func -> func.getFlags().contains(Flag.REMOTE);
    private static final String EMPTY_STRING = "";

    /**
     * Generate the module constructs model when the bal module is given.
     *
     * @param module  module model to fill.
     * @param balPackage module tree.
     * @param moduleNames List of available modules in current Project.
     */
    public static void generateModuleConstructs(Module module, BLangPackage balPackage, List<String> moduleNames) {

        //TODO orgName is not properly set from the ballerina core, hence this work-around
        String currentPackageName = BallerinaDocDataHolder.getInstance().getOrgName() + balPackage.packageID.getName
                ().getValue();

        // Check for type definitions in the package
        for (BLangTypeDefinition typeDefinition : balPackage.getTypeDefinitions()) {
            if (typeDefinition.getFlags().contains(Flag.PUBLIC)) {
                createTypeDefModels(typeDefinition, module);
            }
        }

        // Check for functions in the package
        for (BLangFunction function : balPackage.getFunctions()) {
            if (function.getFlags().contains(Flag.PUBLIC) && !function.getFlags().contains(Flag.ATTACHED)) {
                if (function.getReceiver() != null) {
                    String value = function.getName().value;
//                    for (Documentable parentDocumentable : documentables) {
//                        TypeNode langType = function.getReceiver().getTypeNode();
//                        String typeName = (langType instanceof BLangUserDefinedType ? ((BLangUserDefinedType)
//                                langType).typeName.value : langType.toString());
//
//                        if (typeName.equals(parentDocumentable.name)) {
//                            parentDocumentable.children.add(createDocForFunction(function));
//                        }
//                    }
                } else {
                    // If there's no receiver type i.e. no struct binding to the function
//                    documentables.add(createDocForFunction(function));
                }
                module.functions.add(createDocForFunction(function));
            }
        }
//
        // Check for annotations
        for (BLangAnnotation annotation : balPackage.getAnnotations()) {
            if (annotation.getFlags().contains(Flag.PUBLIC)) {
                module.annotations.add(createDocForFunction(annotation));
            }
        }
//
        // Check for constants.
        for (BLangConstant constant : balPackage.getConstants()) {
            if (constant.getFlags().contains(Flag.PUBLIC)) {
                module.constants.add(createDocForConstant(constant));
            }
        }

        // Check for global variables
        for (BLangSimpleVariable var : balPackage.getGlobalVariables()) {
            if (var.getFlags().contains(Flag.PUBLIC)) {
                // documentables.add(createDocForFunction(var));
            }
        }
    }

    /**
     * Generate the page for primitive types.
     *
     * @param balPackage The ballerina/builtin package.
     * @param packages   List of available packages.
     * @param primitives list of primitives.
     * @return A page model for the primitive types.
     */
    public static Page generatePageForPrimitives(BLangPackage balPackage, List<Link> packages, List<Link> primitives) {
        ArrayList<Documentable> primitiveTypes = new ArrayList<>();
        List<String> descriptions = BallerinaDocUtils.loadPrimitivesDescriptions(false);

        for (Link primitiveType : primitives) {
            String type = primitiveType.caption.value;
            String desc = BallerinaDocUtils.getPrimitiveDescription(descriptions, type);
            primitiveTypes.add(new PrimitiveTypeDoc(type, !desc.isEmpty() ? BallerinaDocUtils.mdToHtml(desc) :
                    desc, new ArrayList<>()));
        }

        // Check for functions in the package
        if (balPackage.getFunctions().size() > 0) {
            for (BLangFunction function : balPackage.getFunctions()) {
                if (function.getFlags().contains(Flag.PUBLIC) && function.getReceiver() != null) {
                    TypeNode langType = function.getReceiver().getTypeNode();
                    if (!(langType instanceof BLangUserDefinedType)) {
                        // Check for primitives in ballerina/builtin
                        Optional<PrimitiveTypeDoc> existingPrimitiveType = primitiveTypes.stream().filter((doc) ->
                                doc instanceof PrimitiveTypeDoc && (((PrimitiveTypeDoc) doc)).name.equals(langType
                                        .toString())).map(doc -> (PrimitiveTypeDoc) doc).findFirst();

                        PrimitiveTypeDoc primitiveTypeDoc;
                        if (existingPrimitiveType.isPresent()) {
                            primitiveTypeDoc = existingPrimitiveType.get();
                        } else {
                            String desc = BallerinaDocUtils.getPrimitiveDescription(descriptions, langType.toString());
                            primitiveTypeDoc = new PrimitiveTypeDoc(langType.toString(), !desc.isEmpty() ?
                                    BallerinaDocUtils.mdToHtml(desc) : desc, new ArrayList<>());
                            primitiveTypes.add(primitiveTypeDoc);
                        }

                       // primitiveTypeDoc.children.add(createDocForFunction(function));
                    }
                }
            }
        }

        // Create the links to select which page or package is active
        List<Link> links = new ArrayList<>();
        for (Link pkgLink : packages) {
            if (BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_NAME.equals(pkgLink.caption.value)) {
                links.add(new Link(pkgLink.caption, pkgLink.href, true));
            } else {
                links.add(new Link(pkgLink.caption, pkgLink.href, false));
            }
        }

        StaticCaption primitivesPageHeading = new StaticCaption(BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_NAME);
        return new Page(null, primitivesPageHeading, primitiveTypes, links, primitives);
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
                    .map(Type::new)
                    .collect(Collectors.toList());
            module.unionTypes.add(new UnionType(typeName, description(typeDefinition), memberTypes));
            added = true;
        } else if (kind == NodeKind.USER_DEFINED_TYPE) {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) typeNode;
            module.unionTypes.add(new UnionType(typeName, description(typeDefinition),
                    Arrays.asList(Type.fromTypeNode(userDefinedType))));
            added = true;
        } else if (kind == NodeKind.ERROR_TYPE) {
            BLangErrorType errorType = (BLangErrorType) typeNode;
            module.errors.add(new Error(errorType.type.tsymbol.name.value, description(typeDefinition),
                    Type.fromTypeNode(errorType.detailType)));
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
     * @return documentation for annotation.
     */
    public static Annotation createDocForFunction(BLangAnnotation annotationNode) {
        String annotationName = annotationNode.getName().getValue();
        String attachments = annotationNode.attachPoints.stream()
                .map(AttachPoint::getValue)
                .collect(Collectors.joining(", "));
        Type dataType = annotationNode.typeNode != null ? Type.fromTypeNode(annotationNode.typeNode) : null;
        return new Annotation(annotationName, description(annotationNode), dataType, attachments);
    }

    private static String extractLink(Collection<BType> types) {
        return types.stream()
                .filter(t -> t.tag != TypeTags.NIL)
                .map(Generator::extractLink)
                .collect(Collectors.joining(","));
    }

    //TODO
    private static String extractLink(BLangType typeNode) {
        BType bType = typeNode.type;
        return extractLink(bType);
    }

    private static String extractLink(BType bType) {
        switch (bType.tag) {
            case TypeTags.UNION:
                BUnionType union = (BUnionType) bType;
                return extractLink(union.getMemberTypes());
            case TypeTags.TUPLE:
                BTupleType tuple = (BTupleType) bType;
                return extractLink(tuple.tupleTypes);
            case TypeTags.ARRAY:
                BArrayType array = (BArrayType) bType;
                return extractLink(array.eType);
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.NIL:
            case TypeTags.JSON:
                return BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_HREF + ".html#" + bType.tsymbol.getName().value;
        }

        if (bType.tsymbol == null || bType.tsymbol.pkgID == null) {
            return EMPTY_STRING;
        }

        String pkg = bType.tsymbol.pkgID.getName().getValue();
        String name = bType.tsymbol.getName().getValue();
        return pkg != null && !pkg.isEmpty() ? pkg + ".html#" + name : "#" + name;
    }

    /**
     * Create documentation for global variables.
     *
     * @param bLangVariable ballerina variable node.
     * @return documentation for global variables.
     */
    public static GlobalVariableDoc createDocForFunction(BLangSimpleVariable bLangVariable) {
        String globalVarName = bLangVariable.getName().getValue();
        String dataType = getTypeName(bLangVariable.getTypeNode());
        String desc = description(bLangVariable);
        String href = extractLink(bLangVariable.getTypeNode());
        return new GlobalVariableDoc(globalVarName, desc, new ArrayList<>(), dataType, href);
    }

    public static Constant createDocForConstant(BLangConstant constant) {
        String constantName = constant.getName().getValue();
        java.lang.Object value = constant.value;
        String desc = description(constant);
        BLangType typeNode = constant.typeNode != null ? constant.typeNode : constant.associatedTypeDefinition.typeNode;
        return new Constant(constantName, desc, Type.fromTypeNode(typeNode), value.toString());
    }

    /**
     * Create documentation for functions.
     *
     * @param functionNode ballerina function node.
     * @return Function documentation model.
     */
    public static Function createDocForFunction(BLangFunction functionNode) {
        String functionName = functionNode.getName().value;
        List<DefaultableVarible> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        // Iterate through the parameters
        if (functionNode.getParameters().size() > 0) {
            for (BLangSimpleVariable param : functionNode.getParameters()) {
                DefaultableVarible variable = getVariable(functionNode, param);
                parameters.add(variable);
            }
        }
        // defaultable params
        if (functionNode.getDefaultableParameters().size() > 0) {
            for (BLangSimpleVariableDef variableDef : functionNode.getDefaultableParameters()) {
                BLangSimpleVariable param = variableDef.getVariable();
                DefaultableVarible variable = getVariable(functionNode, param);
                parameters.add(variable);
            }
        }
        // rest params
        if (functionNode.getRestParameters() != null) {
            SimpleVariableNode restParameter = functionNode.getRestParameters();
            if (restParameter instanceof BLangSimpleVariable) {
                BLangSimpleVariable param = (BLangSimpleVariable) restParameter;
                DefaultableVarible variable = getVariable(functionNode, param);
                parameters.add(variable);
            }
        }

        // return params
        if (functionNode.getReturnTypeNode() != null) {
            BLangType returnType = functionNode.getReturnTypeNode();
            String dataType = getTypeName(returnType);
            if (!dataType.equals("null")) {
                String desc = returnParamAnnotation(functionNode);
                String href = extractLink(returnType);
                Variable variable = new Variable(EMPTY_STRING, desc, Type.fromTypeNode(returnType));
                returnParams.add(variable);
            }

        }

        return new Function(functionName,description(functionNode),
                        functionNode.getFlags().contains(Flag.REMOTE),
                        functionNode.getFlags().contains(Flag.NATIVE),
                        parameters, returnParams);
    }

    private static DefaultableVarible getVariable(BLangFunction functionNode, BLangSimpleVariable param) {
        String dataType = type(param);
        String desc = paramAnnotation(functionNode, param);
        String href = param.typeNode != null ? extractLink(param.typeNode) : extractLink(param.type);
        String defaultValue = EMPTY_STRING;
        if (null != param.getInitialExpression()) {
            defaultValue = param.getInitialExpression().toString();
        }
        return new DefaultableVarible(param.getName().value, desc, Type.fromTypeNode(param.typeNode), defaultValue);
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
        List<DefaultableVarible> fields = getFields(recordType, recordType.fields, documentationNode);
        String documentationText = documentationNode == null ? null : documentationNode.getDocumentation();

        module.records.add(new Record(recordName, documentationText, fields));
    }

    private static List<DefaultableVarible> getFields(BLangNode node, List<BLangSimpleVariable> allFields,
                                         BLangMarkdownDocumentation documentation) {
        List<DefaultableVarible> fields = new ArrayList<>();
        for (BLangSimpleVariable param : allFields) {
            if (param.getFlags().contains(Flag.PUBLIC)) {
                String name = param.getName().value;
                String dataType = type(param);
                String desc = fieldAnnotation(node, param);
                desc = desc.isEmpty() ? findDescFromList(name, documentation) : desc;

                String defaultValue = EMPTY_STRING;
                if (null != param.getInitialExpression()) {
                    defaultValue = param.getInitialExpression().toString();
                }
                DefaultableVarible field = new DefaultableVarible(name, desc, Type.fromTypeNode(param.typeNode),
                        defaultValue);
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
                    parent.getMarkdownDocumentationAttachment());

        if (objectType.initFunction != null) {
            BLangFunction constructor = objectType.initFunction;
            if (constructor.flagSet.contains(Flag.PUBLIC)) {
                Function initFunction = createDocForFunction(constructor);
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
                    functions.add(createDocForFunction(function));
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
                    && ((BLangUserDefinedType) type).typeName.value.equals("AbstractListener"));
        });
        return isListener.get();
    }

    private static boolean isEndpoint(BLangObjectTypeNode objectType) {
        return objectType.flagSet.contains(Flag.CLIENT);
    }

    private static ObjectDoc createNonEndpointObject(BLangObjectTypeNode objectType, String name,
                                                     String description, List<Documentable> functions,
                                                     List<Field> fields, boolean hasConstructor) {
        return new ObjectDoc(name, description, functions, fields, hasConstructor);
    }

    private static EndpointDoc createEndpointObject(BLangObjectTypeNode objectType, String name,
                                                    String description, List<Documentable> functions,
                                                    List<Field> fields, boolean hasConstructor) {
        return new EndpointDoc(name, description, functions, fields, functions, true, false);
    }


    /**
     * Get the type of the variable.
     *
     * @param bLangVariable a varibale
     * @return data type of the variable.
     */
    private static String type(final BLangSimpleVariable bLangVariable) {
        return bLangVariable.type != null ? bLangVariable.type.toString() : "null";
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
