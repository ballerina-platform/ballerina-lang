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
import org.ballerinalang.docgen.model.AnnotationDoc;
import org.ballerinalang.docgen.model.Documentable;
import org.ballerinalang.docgen.model.EndpointDoc;
import org.ballerinalang.docgen.model.EnumDoc;
import org.ballerinalang.docgen.model.Field;
import org.ballerinalang.docgen.model.FunctionDoc;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.Link;
import org.ballerinalang.docgen.model.ObjectDoc;
import org.ballerinalang.docgen.model.PackageName;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.PrimitiveTypeDoc;
import org.ballerinalang.docgen.model.RecordDoc;
import org.ballerinalang.docgen.model.StaticCaption;
import org.ballerinalang.docgen.model.Variable;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_INIT;
import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_REGISTER;

/**
 * Generates the Page objects for bal packages.
 */
public class Generator {

    private static final Predicate<BLangFunction> IS_CALLER_ACTIONS =
            s -> s.name.value.equals(Names.EP_SPI_GET_CALLER_ACTIONS.value);
    private static final String EMPTY_STRING = "";

    /**
     * Generate the page when the bal package is passed.
     *
     * @param balPackage  The current package.
     * @param packages    List of available packages.
     * @param description package description.
     * @param primitives  list of primitives.
     * @return A page model for the current package.
     */
    public static Page generatePage(BLangPackage balPackage, List<Link> packages, String description, List<Link>
            primitives) {

        //TODO orgName is not properly set from the ballerina core, hence this work-around
        String currentPackageName = BallerinaDocDataHolder.getInstance().getOrgName() + balPackage.packageID.getName
                ().getValue();

        ArrayList<Documentable> records = new ArrayList<>();
        ArrayList<Documentable> objects = new ArrayList<>();
        ArrayList<Documentable> endpoints = new ArrayList<>();
        ArrayList<Documentable> union = new ArrayList<>();
        // Check for type definitions in the package
        for (BLangTypeDefinition typeDefinition : balPackage.getTypeDefinitions()) {
            if (typeDefinition.getFlags().contains(Flag.PUBLIC)) {
                addDocForNode(typeDefinition, records, objects, endpoints, union);
            }
        }
        List<Documentable> documentables = new ArrayList<>(records);

        // Check for functions in the package
        for (BLangFunction function : balPackage.getFunctions()) {
            if (function.getFlags().contains(Flag.PUBLIC) && !function.getFlags().contains(Flag.ATTACHED)) {
                if (function.getReceiver() != null) {
                    for (Documentable parentDocumentable : documentables) {
                        TypeNode langType = function.getReceiver().getTypeNode();
                        String typeName = (langType instanceof BLangUserDefinedType ? ((BLangUserDefinedType)
                                langType).typeName.value : langType.toString());

                        if (typeName.equals(parentDocumentable.name)) {
                            parentDocumentable.children.add(createDocForNode(function));
                        }
                    }
                } else {
                    // If there's no receiver type i.e. no struct binding to the function
                    documentables.add(createDocForNode(function));
                }
            }
        }

        documentables.addAll(objects);
        documentables.addAll(endpoints);
        documentables.addAll(union);

        // Check for annotations
        for (BLangAnnotation annotation : balPackage.getAnnotations()) {
            if (annotation.getFlags().contains(Flag.PUBLIC)) {
                documentables.add(createDocForNode(annotation));
            }
        }
        // Check for global variables
        for (BLangVariable var : balPackage.getGlobalVariables()) {
            if (var.getFlags().contains(Flag.PUBLIC)) {
                documentables.add(createDocForNode(var));
            }
        }

        // Create the links to select which page or package is active
        List<Link> links = new ArrayList<>();
        PackageName packageNameHeading = null;
        for (Link pkgLink : packages) {
            if (pkgLink.caption.value.equals(currentPackageName)) {
                packageNameHeading = (PackageName) pkgLink.caption;
                links.add(new Link(pkgLink.caption, pkgLink.href, true));
            } else {
                links.add(new Link(pkgLink.caption, pkgLink.href, false));
            }
        }

        return new Page(description, packageNameHeading, documentables, links, primitives);
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

                        primitiveTypeDoc.children.add(createDocForNode(function));
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
     * Create documentation for type definitions.
     *
     * @param typeDefinition ballerina type definition node.
     * @param records        list to put resulting records
     * @param objects        list to put resulting obj
     * @param endpoints      list to put resulting ep
     * @param union          list to put resulting unions
     */
    public static void addDocForNode(BLangTypeDefinition typeDefinition, ArrayList<Documentable> records,
                                     ArrayList<Documentable> objects, ArrayList<Documentable> endpoints,
                                     ArrayList<Documentable> union) {
        String typeName = typeDefinition.getName().getValue();
        BLangType typeNode = typeDefinition.typeNode;
        NodeKind kind = typeNode.getKind();
        boolean added = false;
        if (kind == NodeKind.OBJECT_TYPE) {
            BLangObjectTypeNode objectType = (BLangObjectTypeNode) typeNode;
            addDocForType(objectType, typeDefinition, objects, endpoints);
            added = true;
        } else if (kind == NodeKind.FINITE_TYPE_NODE) {
            BLangFiniteTypeNode enumNode = (BLangFiniteTypeNode) typeNode;
            String values = enumNode.getValueSet().stream()
                    .map(Object::toString)
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.joining(" | "));
            union.add(new EnumDoc(typeName, description(typeDefinition), new ArrayList<>(), values));
            added = true;
        } else if (kind == NodeKind.RECORD_TYPE) {
            records.add(createDocForType(typeDefinition, (BLangRecordTypeNode) typeNode, typeName));
            added = true;
        } else if (kind == NodeKind.UNION_TYPE_NODE) {
            List<BLangType> memberTypeNodes = ((BLangUnionTypeNode) typeNode).memberTypeNodes;
            String values = memberTypeNodes.stream()
                    .map(Object::toString)
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.joining(" | "));
            union.add(new EnumDoc(typeName, description(typeDefinition), new ArrayList<>(), values));
            added = true;
        } else if (kind == NodeKind.USER_DEFINED_TYPE) {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) typeNode;
            String value = userDefinedType.typeName.value;
            union.add(new EnumDoc(typeName, description(typeDefinition), new ArrayList<>(), value));
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
    public static AnnotationDoc createDocForNode(BLangAnnotation annotationNode) {
        String annotationName = annotationNode.getName().getValue();
        String dataType = "-", href = EMPTY_STRING;
        if (annotationNode.typeNode != null) {
            dataType = getTypeName(annotationNode.typeNode);
            href = extractLink(annotationNode.typeNode);
        }
        String attachments = annotationNode.attachPoints.stream()
                .map(AttachPoint::getValue)
                .collect(Collectors.joining(", "));

        return new AnnotationDoc(annotationName, description(annotationNode), dataType, href, attachments);
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
                return extractLink(union.memberTypes);
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
    public static GlobalVariableDoc createDocForNode(BLangVariable bLangVariable) {
        String globalVarName = bLangVariable.getName().getValue();
        String dataType = getTypeName(bLangVariable.getTypeNode());
        String desc = description(bLangVariable);
        String href = extractLink(bLangVariable.getTypeNode());
        return new GlobalVariableDoc(globalVarName, desc, new ArrayList<>(), dataType, href);
    }

    private static Field getVariableForType(String name, BType param) {
        BTypeSymbol type = param.tsymbol;
        if (type != null && type.type != null) {
            return new Field(name, type.type.toString(), EMPTY_STRING, EMPTY_STRING, extractLink(type.type));
        } else {
            return new Field(name, param.toString(), EMPTY_STRING, EMPTY_STRING, extractLink(param));
        }
    }

    private static FunctionDoc createDocForType(BInvokableSymbol invokable) {
        String name = invokable.name.value;
        name = name.substring(name.indexOf('.') + 1);
        List<Field> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        // Iterate through the parameters
        for (BVarSymbol param : invokable.getParameters()) {
            Field variable = getVariableForType(param.name.toString(), param.type);
            parameters.add(variable);
        }

        for (BVarSymbol param : invokable.getDefaultableParameters()) {
            Field variable = getVariableForType(param.name.toString(), param.type);
            parameters.add(variable);
        }
        if (null != invokable.retType) {
            returnParams.add(getVariableForType(EMPTY_STRING, invokable.retType));
        } else if (invokable.type instanceof BInvokableType) {
            BInvokableType invokableType = (BInvokableType) invokable.type;
            returnParams.add(getVariableForType(EMPTY_STRING, invokableType.retType));
        }
    
        return new FunctionDoc(name, invokable.markdownDocumentation.description, new ArrayList<>(), parameters,
                returnParams);
    }

    /**
     * Create documentation for functions.
     *
     * @param functionNode ballerina function node.
     * @return documentation for functions.
     */
    public static FunctionDoc createDocForNode(BLangFunction functionNode) {
        String functionName = functionNode.getName().value;
        List<Field> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        // Iterate through the parameters
        if (functionNode.getParameters().size() > 0) {
            for (BLangVariable param : functionNode.getParameters()) {
                Field variable = getVariable(functionNode, param);
                parameters.add(variable);
            }
        }
        // defaultable params
        if (functionNode.getDefaultableParameters().size() > 0) {
            for (BLangVariableDef variableDef : functionNode.getDefaultableParameters()) {
                BLangVariable param = variableDef.getVariable();
                Field variable = getVariable(functionNode, param);
                parameters.add(variable);
            }
        }
        // rest params
        if (functionNode.getRestParameters() != null) {
            VariableNode restParameter = functionNode.getRestParameters();
            if (restParameter instanceof BLangVariable) {
                BLangVariable param = (BLangVariable) restParameter;
                Field variable = getVariable(functionNode, param);
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
                Variable variable = new Variable(EMPTY_STRING, dataType, desc, href);
                returnParams.add(variable);
            }

        }

        //TODO: gen using symbol createDocForType(functionNode.symbol)
        return new FunctionDoc(functionName, description(functionNode), new ArrayList<>(), parameters, returnParams);
    }

    private static Field getVariable(BLangFunction functionNode, BLangVariable param) {
        String dataType = type(param);
        String desc = paramAnnotation(functionNode, param);
        String href = param.typeNode != null ? extractLink(param.typeNode) : extractLink(param.type);
        String defaultValue = EMPTY_STRING;
        if (null != param.getInitialExpression()) {
            defaultValue = param.getInitialExpression().toString();
        }
        return new Field(param.getName().value, dataType, desc, defaultValue, href);
    }

    /**
     * Create documentation for records.
     *
     * @param recordType ballerina record node.
     * @param structName struct name.
     * @return documentation of the record.
     */
    private static RecordDoc createDocForType(BLangTypeDefinition typeDefinition, BLangRecordTypeNode recordType,
                                              String structName) {
        // Check if its an anonymous struct
        if (recordType.isAnonymous) {
            structName = "Anonymous Record " + structName.substring(structName.lastIndexOf('$') + 1);
        }
        BLangMarkdownDocumentation documentationNode = typeDefinition.getMarkdownDocumentationAttachment();
        List<Field> fields = getFields(recordType, recordType.fields, documentationNode);
        String documentationText = documentationNode == null ? null : documentationNode.getDocumentation();
        return new RecordDoc(structName, documentationText, new ArrayList<>(), fields);
    }

    private static List<Field> getFields(BLangNode node, List<BLangVariable> allFields,
                                         BLangMarkdownDocumentation documentation) {
        List<Field> fields = new ArrayList<>();
        for (BLangVariable param : allFields) {
            if (param.getFlags().contains(Flag.PUBLIC)) {
                String name = param.getName().value;
                String dataType = type(param);
                String desc = fieldAnnotation(node, param);
                desc = desc.isEmpty() ? findDescFromList(name, documentation) : desc;

                String defaultValue = EMPTY_STRING;
                if (null != param.getInitialExpression()) {
                    defaultValue = param.getInitialExpression().toString();
                }
                String href = extractLink(param.getTypeNode());
                Field variable = new Field(name, dataType, desc, defaultValue, href);
                fields.add(variable);
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

    private static void addDocForType(BLangObjectTypeNode objectType,
                                      BLangTypeDefinition parent,
                                      ArrayList<Documentable> objects,
                                      ArrayList<Documentable> endpoints) {
        List<Documentable> functions = new ArrayList<>();
        String name = parent.getName().getValue();
        String description = description(parent);

        List<Field> fields = getFields(parent, objectType.fields, parent.getMarkdownDocumentationAttachment());
        boolean hasConstructor = false;

        if (objectType.initFunction != null) {
            BLangFunction constructor = objectType.initFunction;
            if (constructor.flagSet.contains(Flag.PUBLIC)) {
                FunctionDoc initFunction = createDocForNode(constructor);
                // if it's the default constructor, we don't need to document
                if (initFunction.parameters.size() > 0) {
                    hasConstructor = true;
                    functions.add(initFunction);
                }
            }
        }

        //Iterate through the functions of the connectors
        if (objectType.getFunctions().size() > 0) {
            for (BLangFunction action : objectType.getFunctions()) {
                if (action.flagSet.contains(Flag.PUBLIC)) {
                    functions.add(createDocForNode(action));
                }
            }
        }

        if (isEndpoint(objectType)) {
            Optional<BLangFunction> callerActions = objectType.functions.stream().filter(IS_CALLER_ACTIONS).findAny();
            if (callerActions.isPresent()) {
                BObjectTypeSymbol retrunType = (BObjectTypeSymbol) callerActions.get().returnTypeNode.type.tsymbol;
                functions = retrunType.attachedFuncs.stream()
                        .filter(c -> !c.funcName.value.equals("new"))
                        .map(c -> createDocForType(c.symbol))
                        .collect(Collectors.toList());
            } else {
                functions = new ArrayList<>();
            }
            endpoints.add(createEndpointObject(objectType, name, description, functions, fields, hasConstructor));
        } else {
            objects.add(createNonEndpointObject(objectType, name, description, functions, fields, hasConstructor));
        }

    }

    private static boolean isEndpoint(BLangObjectTypeNode objectType) {
        boolean hasAction = false;
        boolean hasInit = false;
        boolean hasReg = false;
        for (BLangFunction function : objectType.functions) {
            String name = function.name.value;
            if (name.equals(Names.EP_SPI_GET_CALLER_ACTIONS.value)) {
                hasAction = true;
            } else if (name.equals(EP_SPI_INIT.value)) {
                hasInit = true;
            } else if (name.equals(EP_SPI_REGISTER.value)) {
                hasReg = true;
            }
        }
        return (hasInit && hasReg) || (hasInit && hasAction);
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
    private static String type(final BLangVariable bLangVariable) {
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
    private static String paramAnnotation(BLangNode node, BLangVariable param) {
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
        if (!(param instanceof BLangVariable)) {
            return getParameterDocumentation(node, subName);
        }
        BLangVariable paramVariable = (BLangVariable) param;
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
