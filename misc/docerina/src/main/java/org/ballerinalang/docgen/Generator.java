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
import org.ballerinalang.docgen.model.Field;
import org.ballerinalang.docgen.model.FunctionDoc;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.Link;
import org.ballerinalang.docgen.model.PackageName;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.PrimitiveTypeDoc;
import org.ballerinalang.docgen.model.RecordDoc;
import org.ballerinalang.docgen.model.StaticCaption;
import org.ballerinalang.docgen.model.TypeDefinitionDoc;
import org.ballerinalang.docgen.model.Variable;
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.DocumentationAttributeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates the Page objects for bal packages.
 */
public class Generator {
    private static final String ANONYMOUS_STRUCT = "$anonRecord$";

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
        List<Documentable> documentables = new ArrayList<>();
        List<BLangObject> visitedObjects;

        //TODO orgName is not properly set from the ballerina core, hence this work-around
        String currentPackageName = BallerinaDocDataHolder.getInstance().getOrgName() + balPackage.packageID.getName
                ().getValue();

        // Check for records in the package
        if (balPackage.getRecords().size() > 0) {
            for (BLangRecord record : balPackage.getRecords()) {
                if (record.getFlags().contains(Flag.PUBLIC)) {
                    documentables.add(createDocForNode(record));
                }
            }
        }
        // Check for functions in the package
        if (balPackage.getFunctions().size() > 0) {
            for (BLangFunction function : balPackage.getFunctions()) {
                if (function.getFlags().contains(Flag.PUBLIC) && !function.getFlags().contains(Flag.ATTACHED)) {
                    if (function.getReceiver() != null) {
                        if (documentables.size() > 0) {
                            for (Documentable parentDocumentable : documentables) {
                                TypeNode langType = function.getReceiver().getTypeNode();
                                String typeName = (langType instanceof BLangUserDefinedType ? ((BLangUserDefinedType)
                                        langType).typeName.value : langType.toString());

                                if (typeName.equals(parentDocumentable.name)) {
                                    parentDocumentable.children.add(createDocForNode(function));
                                }
                            }
                        }
                    } else {
                        // If there's no receiver type i.e. no struct binding to the function
                        documentables.add(createDocForNode(function));
                    }
                }
            }
        }

        // endpoints
        visitedObjects = balPackage.getObjects().stream().filter(bLangObject -> {
            if (!bLangObject.getDocumentationAttachments().isEmpty()) {
                if (!((DocumentableNode) bLangObject).getDocumentationAttachments().isEmpty()) {
                    DocumentationNode bLangDocumentation = ((DocumentableNode) bLangObject)
                            .getDocumentationAttachments().get(0);
                    for (DocumentationAttributeNode attribute : bLangDocumentation.getAttributes()) {
                        if (attribute instanceof BLangDocumentationAttribute) {
                            BLangDocumentationAttribute docAttribute = (BLangDocumentationAttribute) attribute;
                            if (docAttribute.docTag == DocTag.ENDPOINT) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());

        // endpoint actions
        List<EndpointDoc> endpoints = visitedObjects.stream().map(obj -> {
            BLangIdentifier epName = obj.getName();
            Optional<BLangFunction> getClientOptional = obj.getFunctions().stream().filter(bLangFunction ->
                    bLangFunction.getName().getValue().equals(Names.EP_SPI_GET_CALLER_ACTIONS.value)).findFirst();

            if (getClientOptional.isPresent()) {
                BLangType returnTypeNode = getClientOptional.get().returnTypeNode;
                if (returnTypeNode instanceof BLangUserDefinedType) {
                    Optional<BLangObject> objectDefOptional = balPackage.getObjects().stream().filter(objDef -> {
                        if (objDef.getName().getValue().equals(((BLangUserDefinedType) returnTypeNode).getTypeName()
                                .getValue())) {
                            return true;
                        }
                        return false;
                    }).findFirst();
                    if (objectDefOptional.isPresent()) {
                        BLangObject objectDefinition = objectDefOptional.get();
                        objectDefinition.setName(epName);
                        String objDesc = description(obj);
                        return createDocForNode(objectDefinition, obj.functions, objDesc, true);
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        endpoints.forEach(endpointDoc -> {
            if (endpointDoc != null) {
                visitedObjects.add(endpointDoc.getObject());
            }
        });
        documentables.addAll(endpoints);

        // Check for objects in the package
        balPackage.getObjects().removeAll(visitedObjects);
        for (BLangObject connector : balPackage.getObjects()) {
            if (connector.getFlags().contains(Flag.PUBLIC)) {
                documentables.add(createDocForNode(connector, false));
            }
        }
        // Check for type definitions in the package
        for (BLangTypeDefinition typeDefinition : balPackage.getTypeDefinitions()) {
            if (typeDefinition.getFlags().contains(Flag.PUBLIC)) {
                documentables.add(createDocForNode(typeDefinition));
            }
        }
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
     * @param balPackage The ballerina.builtin package.
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
            primitiveTypes.add(new PrimitiveTypeDoc(type, desc != null && !desc.isEmpty() ? BallerinaDocUtils
                    .mdToHtml(desc) : desc, new ArrayList<>()));
        }

        // Check for functions in the package
        if (balPackage.getFunctions().size() > 0) {
            for (BLangFunction function : balPackage.getFunctions()) {
                if (function.getFlags().contains(Flag.PUBLIC) && function.getReceiver() != null) {
                    TypeNode langType = function.getReceiver().getTypeNode();
                    if (!(langType instanceof BLangUserDefinedType)) {
                        // Check for primitives in ballerina.builtin
                        Optional<PrimitiveTypeDoc> existingPrimitiveType = primitiveTypes.stream().filter((doc) ->
                                doc instanceof PrimitiveTypeDoc && (((PrimitiveTypeDoc) doc)).name.equals(langType
                                        .toString())).map(doc -> (PrimitiveTypeDoc) doc).findFirst();

                        PrimitiveTypeDoc primitiveTypeDoc;
                        if (existingPrimitiveType.isPresent()) {
                            primitiveTypeDoc = existingPrimitiveType.get();
                        } else {
                            String desc = BallerinaDocUtils.getPrimitiveDescription(descriptions, langType.toString());
                            primitiveTypeDoc = new PrimitiveTypeDoc(langType.toString(), desc != null && !desc
                                    .isEmpty() ? BallerinaDocUtils.mdToHtml(desc) : desc, new ArrayList<>());
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
     * @return documentation for type definition.
     */
    public static TypeDefinitionDoc createDocForNode(BLangTypeDefinition typeDefinition) {
        String enumName = typeDefinition.getName().getValue();
        String values = typeDefinition.getValueSet().stream().map(value -> value.toString()).sorted(Collections
                .reverseOrder()).collect(Collectors.joining(" | "));

        return new TypeDefinitionDoc(enumName, description(typeDefinition), new ArrayList<>(), values);
    }

    /**
     * Create documentation for annotations.
     *
     * @param annotationNode ballerina annotation node.
     * @return documentation for annotation.
     */
    public static AnnotationDoc createDocForNode(BLangAnnotation annotationNode) {
        String annotationName = annotationNode.getName().getValue();
        String dataType = "-", href = "";
        if (annotationNode.typeNode != null) {
            dataType = getTypeName(annotationNode.typeNode);
            href = extractLink(annotationNode.typeNode);
        }
        String attachments = annotationNode.attachmentPoints.stream().map(attachmentPoint -> attachmentPoint
                .attachmentPoint.getValue()).collect(Collectors.joining(", "));

        return new AnnotationDoc(annotationName, description(annotationNode), dataType, href, attachments);
    }

    //TODO
    private static String extractLink(BLangType typeNode) {
        if (typeNode instanceof BLangUserDefinedType) {
            BLangUserDefinedType type = (BLangUserDefinedType) typeNode;
            String pkg = type.pkgAlias.getValue();
            BTypeSymbol tsymbol = type.type.tsymbol;
            if (tsymbol instanceof BStructSymbol) {
                pkg = ((BStructSymbol) tsymbol).pkgID.getName().getValue();
            }
            return pkg != null && !pkg.isEmpty() ? pkg + ".html#" + type.typeName.getValue() : "#" + type.typeName
                    .getValue();
        } else if (typeNode instanceof BLangValueType) {
            if (((BLangValueType) typeNode).type != null && ((BLangValueType) typeNode).type.tsymbol != null) {
                return BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_HREF + ".html#" + typeNode.type.tsymbol.getName()
                        .value;
            }
        } else if (typeNode instanceof BLangUnionTypeNode) {
            BLangUnionTypeNode union = (BLangUnionTypeNode) typeNode;
            return union.memberTypeNodes.stream().map(member -> extractLink(member)).collect(Collectors.joining(","));
        } else if (typeNode instanceof BLangTupleTypeNode) {
            BLangTupleTypeNode tuple = (BLangTupleTypeNode) typeNode;
            return tuple.memberTypeNodes.stream().map(member -> extractLink(member)).collect(Collectors.joining(","));
        } else if (typeNode instanceof BLangBuiltInRefTypeNode) {
            BLangBuiltInRefTypeNode builtInRefTypeNode = (BLangBuiltInRefTypeNode) typeNode;
            return BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_HREF + ".html#" + builtInRefTypeNode.type.tsymbol
                    .getName().value;
        } else if (typeNode instanceof BLangArrayType) {
            return extractLink(((BLangArrayType) typeNode).elemtype);
        } else {
            // TODO
            return "";
        }
        return "";
    }

    private static String extractLink(BType type) {
        if (type == null || type.tsymbol == null || type.tsymbol.pkgID == null) {
            return "";
        }
        String pkg = type.tsymbol.pkgID.getName().getValue();
        return pkg != null && !pkg.isEmpty() ? pkg + ".html#" + type.tsymbol.getName().getValue() : "#" + type
                .tsymbol.getName().getValue();
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
                Variable variable = new Variable("", dataType, desc, href);
                returnParams.add(variable);
            }

        }

        return new FunctionDoc(functionName, description(functionNode), new ArrayList<>(), parameters, returnParams);
    }

    private static Field getVariable(BLangFunction functionNode, BLangVariable param) {
        String dataType = type(param);
        String desc = paramAnnotation(functionNode, param);
        String href = param.typeNode != null ? extractLink(param.typeNode) : extractLink(param.type);
        String defaultValue = "";
        if (null != param.getInitialExpression()) {
            defaultValue = param.getInitialExpression().toString();
        }
        return new Field(param.getName().value, dataType, desc, defaultValue, href);
    }

    /**
     * Create documentation for records.
     *
     * @param recordNode ballerina record node.
     * @return documentation of the record.
     */
    public static RecordDoc createDocForNode(BLangRecord recordNode) {
        String structName = recordNode.getName().getValue();
        // Check if its an anonymous struct
        if (structName.contains(ANONYMOUS_STRUCT)) {
            structName = "Anonymous Record" + structName.substring(structName.lastIndexOf('$') + 1);
        }
        List<Field> fields = new ArrayList<>();

        // Iterate through the record fields
        if (recordNode.getFields().size() > 0) {
            getFields(recordNode, recordNode.fields, fields);
        }

        return new RecordDoc(structName, description(recordNode), new ArrayList<>(), fields);
    }

    private static void getFields(BLangNode node, List<BLangVariable> allFields, List<Field> fields) {
        for (BLangVariable param : allFields) {
            if (param.getFlags().contains(Flag.PUBLIC)) {
                String dataType = type(param);
                String desc = fieldAnnotation(node, param);
                String defaultValue = "";
                if (null != param.getInitialExpression()) {
                    defaultValue = param.getInitialExpression().toString();
                }
                String href = extractLink(param.getTypeNode());
                Field variable = new Field(param.getName().value, dataType, desc, defaultValue, href);
                fields.add(variable);
            }
        }
    }

    public static EndpointDoc createDocForNode(BLangObject connectorNode, List<BLangFunction> utilitiyFunctions,
                                               String description, boolean isConnector) {
        String connectorName = connectorNode.getName().value;
        List<Field> parameters = new ArrayList<>();
        List<Documentable> actions = new ArrayList<>();
        List<Documentable> functions = new ArrayList<>();

        // Iterate through the connector parameters
        if (connectorNode.fields.size() > 0) {
            getFields(connectorNode, connectorNode.fields, parameters);
        }

        if (connectorNode.initFunction != null) {
            BLangFunction constructor = connectorNode.initFunction;
            if (constructor.flagSet.contains(Flag.PUBLIC)) {
                FunctionDoc initFunction = createDocForNode(constructor);
                // if it's the default constructor, we don't need to document
                if (initFunction.parameters.size() > 0) {
                    actions.add(initFunction);
                }
            }
        }

        //Iterate through the actions of the connectors
        if (connectorNode.getFunctions().size() > 0) {
            for (BLangFunction action : connectorNode.getFunctions()) {
                if (action.flagSet.contains(Flag.PUBLIC)) {
                    actions.add(createDocForNode(action));
                }
            }
        }

        for (BLangFunction func : utilitiyFunctions) {
            if (func.flagSet.contains(Flag.PUBLIC)) {
                functions.add(createDocForNode(func));
            }
        }
        EndpointDoc endpointDoc = new EndpointDoc(connectorName, description == null ? description(connectorNode)
                : description, actions, parameters, functions, isConnector, false);
        endpointDoc.setObject(connectorNode);
        return endpointDoc;
    }

    /**
     * Create documentation for connectors.
     *
     * @param connectorNode ballerina connector node.
     * @return documentation for connectors.
     */
    public static EndpointDoc createDocForNode(BLangObject connectorNode, boolean isConnector) {
        return createDocForNode(connectorNode, new ArrayList<>(), null, isConnector);
    }

    /**
     * Get the type of the variable.
     *
     * @param bLangVariable
     * @return data type of the variable.
     */
    private static String type(final BLangVariable bLangVariable) {
        return bLangVariable.type != null ? bLangVariable.type.toString() : "null";
    }

    /**
     * Get the type name of the type node.
     *
     * @param bLangType
     * @return type name.
     */
    private static String getTypeName(BLangType bLangType) {
        return (bLangType instanceof BLangUserDefinedType ? ((BLangUserDefinedType) bLangType).typeName.value :
                bLangType.toString());
    }

    /**
     * Get the annotation attachments for the node.
     *
     * @param node
     * @return list of annotation attachments.
     */
    private static List<? extends AnnotationAttachmentNode> getAnnotationAttachments(BLangNode node) {
        return ((AnnotatableNode) node).getAnnotationAttachments();
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

        if (node instanceof DocumentableNode && !((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
            // new syntax
            if (!((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
                DocumentationNode bLangDocumentation = ((DocumentableNode) node).getDocumentationAttachments().get(0);
                for (DocumentationAttributeNode attribute : bLangDocumentation.getAttributes()) {
                    if (attribute instanceof BLangDocumentationAttribute) {
                        BLangDocumentationAttribute docAttribute = (BLangDocumentationAttribute) attribute;
                        if (docAttribute.docTag == DocTag.PARAM && docAttribute.getDocumentationField().toString()
                                .equals(subName)) {
                            return BallerinaDocUtils.mdToHtml(attribute.getDocumentationText());
                        }
                    }
                }
            }
        } else {
            for (AnnotationAttachmentNode annotation : getAnnotationAttachments(node)) {
                BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) annotation.getExpression();
                if (bLangRecordLiteral == null || bLangRecordLiteral.getKeyValuePairs().size() != 1) {
                    continue;
                }
                BLangExpression bLangLiteral = bLangRecordLiteral.getKeyValuePairs().get(0).getValue();
                String attribVal = bLangLiteral.toString();
                if ((annotation.getAnnotationName().getValue().equals("Param")) && attribVal.startsWith(subName +
                        ":")) {

                    return attribVal.split(subName + ":")[1].trim();
                }
            }
        }
        return "";
    }

    /**
     * Get description annotation of the return parameter.
     *
     * @param node parent node.
     * @return description of the return parameter.
     */
    public static String returnParamAnnotation(BLangNode node) {
        if (node instanceof DocumentableNode && !((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
            // new syntax
            if (!((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
                DocumentationNode bLangDocumentation = ((DocumentableNode) node).getDocumentationAttachments().get(0);
                for (DocumentationAttributeNode attribute : bLangDocumentation.getAttributes()) {
                    if (attribute instanceof BLangDocumentationAttribute) {
                        BLangDocumentationAttribute docAttribute = (BLangDocumentationAttribute) attribute;
                        if (docAttribute.docTag == DocTag.RETURN) {
                            // should have only one return variable
                            return BallerinaDocUtils.mdToHtml(attribute.getDocumentationText());
                        }
                    }
                }
            }
        } else {
            for (AnnotationAttachmentNode annotation : getAnnotationAttachments(node)) {
                BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) annotation.getExpression();
                if (bLangRecordLiteral == null || bLangRecordLiteral.getKeyValuePairs().size() != 1) {
                    continue;
                }
                if (annotation.getAnnotationName().getValue().equals("Return")) {
                    BLangExpression bLangLiteral = bLangRecordLiteral.getKeyValuePairs().get(0).getValue();
                    return bLangLiteral.toString();
                }
            }
        }

        return "";
    }

    /**
     * Get description annotation of the field.
     *
     * @param node  parent node.
     * @param param field.
     * @return description of the field.
     */
    private static String fieldAnnotation(BLangNode node, BLangNode param) {
        String subName = "";
        if (param instanceof BLangVariable) {
            BLangVariable paramVariable = (BLangVariable) param;
            subName = (paramVariable.getName() == null) ? paramVariable.type.tsymbol.name.value : paramVariable
                    .getName().getValue();
        }

        if (node instanceof DocumentableNode && !((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
            // new syntax
            if (!((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
                DocumentationNode bLangDocumentation = ((DocumentableNode) node).getDocumentationAttachments().get(0);
                for (DocumentationAttributeNode attribute : bLangDocumentation.getAttributes()) {
                    if (attribute instanceof BLangDocumentationAttribute) {
                        BLangDocumentationAttribute docAttribute = (BLangDocumentationAttribute) attribute;
                        if (docAttribute.docTag == DocTag.FIELD && docAttribute.getDocumentationField().toString()
                                .equals(subName)) {
                            return BallerinaDocUtils.mdToHtml(attribute.getDocumentationText());
                        }
                    }
                }
            }
        } else {
            for (AnnotationAttachmentNode annotation : getAnnotationAttachments(node)) {
                BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) annotation.getExpression();
                if (bLangRecordLiteral.getKeyValuePairs().size() != 1) {
                    continue;
                }
                BLangExpression bLangLiteral = bLangRecordLiteral.getKeyValuePairs().get(0).getValue();
                String attribVal = bLangLiteral.toString();
                if (annotation.getAnnotationName().getValue().equals("Field") && attribVal.startsWith(subName + ":")) {
                    return attribVal.split(subName + ":")[1].trim();
                }
            }
        }
        return "";
    }

    /**
     * Get the description annotation of the node.
     *
     * @param node top level node.
     * @return description of the node.
     */
    private static String description(BLangNode node) {
        if (node instanceof DocumentableNode && !((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
            // new syntax
            if (!((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
                DocumentationNode bLangDocumentation = ((DocumentableNode) node).getDocumentationAttachments().get(0);
                return BallerinaDocUtils.mdToHtml(bLangDocumentation.getDocumentationText());
            }
        } else {
            if (getAnnotationAttachments(node).size() == 0) {
                return null;
            }
            for (AnnotationAttachmentNode annotation : getAnnotationAttachments(node)) {
                BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) annotation.getExpression();
                if (bLangRecordLiteral != null && bLangRecordLiteral.getKeyValuePairs() != null && bLangRecordLiteral
                        .getKeyValuePairs().size() == 1) {
                    if (annotation.getAnnotationName().getValue().equals("Description")) {
                        BLangExpression bLangLiteral = bLangRecordLiteral.getKeyValuePairs().get(0).getValue();
                        return bLangLiteral.toString();
                    }
                }
            }
        }
        return "";
    }

}

