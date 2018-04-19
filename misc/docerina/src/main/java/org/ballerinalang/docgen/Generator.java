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
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.ballerinalang.docgen.model.ActionDoc;
import org.ballerinalang.docgen.model.AnnotationDoc;
import org.ballerinalang.docgen.model.ConnectorDoc;
import org.ballerinalang.docgen.model.Documentable;
import org.ballerinalang.docgen.model.EnumDoc;
import org.ballerinalang.docgen.model.Field;
import org.ballerinalang.docgen.model.FunctionDoc;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.Link;
import org.ballerinalang.docgen.model.PackageName;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.PrimitiveTypeDoc;
import org.ballerinalang.docgen.model.StaticCaption;
import org.ballerinalang.docgen.model.StructDoc;
import org.ballerinalang.docgen.model.Variable;
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.DocumentationAttributeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Generates the Page objects for bal packages.
 */
public class Generator {
    private static final String ANONYMOUS_STRUCT = "$anonStruct$";

    /**
     * Generate the page when the bal package is passed.
     *
     * @param balPackage  The current package that is being viewed.
     * @param packages    List of available packages.
     * @param description package description
     * @param primitives  list of primitives
     * @return A page model for the current package.
     */
    public static Page generatePage(BLangPackage balPackage, List<Link> packages, String description, List<Link>
            primitives) {
        ArrayList<Documentable> documentables = new ArrayList<>();
        //TODO till orgName gets fixed
        String currentPackageName = "ballerina/" + (balPackage.symbol).pkgID.name.value;

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
        // Check for connectors in the package
        for (BLangObject connector : balPackage.getObjects()) {
            if (connector.getFlags().contains(Flag.PUBLIC)) {
                documentables.add(createDocForNode(connector));
            }
        }
        // Check for connectors in the package
        for (BLangTypeDefinition enumNode : balPackage.getTypeDefinitions()) {
            if (enumNode.getFlags().contains(Flag.PUBLIC)) {
                documentables.add(createDocForNode(enumNode));
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
        Properties descriptions = BallerinaDocUtils.loadPrimitivesDescriptions();

        for (Link primitiveType : primitives) {
            String type = primitiveType.caption.value;
            String desc = descriptions.getProperty(type);
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
                            String desc = descriptions.getProperty(langType.toString());
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
     * Create documentation for enums.
     *
     * @param enumNode ballerina enum node.
     * @return documentation for enum.
     * TODO
     */
    public static EnumDoc createDocForNode(BLangTypeDefinition enumNode) {
        String enumName = enumNode.getName().getValue();
        List<Variable> enumerators = new ArrayList<>();

        // Iterate through the enumerators
        if (enumNode.getValueSet().size() > 0) {
            for (BLangExpression enumerator : enumNode.getValueSet()) {
//                String desc = fieldAnnotation((BLangNode) enumNode, (BLangNode) enumerator);
//                Variable variable = new Variable(enumerator., "", desc);
//                enumerators.add(variable);
            }
        }
        return new EnumDoc(enumName, description((BLangNode) enumNode), new ArrayList<>(), enumerators);
    }

    /**
     * Create documentation for annotations.
     *
     * @param annotationNode ballerina annotation node.
     * @return documentation for annotation.
     */
    public static AnnotationDoc createDocForNode(BLangAnnotation annotationNode) {
        String annotationName = annotationNode.getName().getValue();
        List<Variable> attributes = new ArrayList<>();

        if (!annotationNode.getDocumentationAttachments().isEmpty()) {
            // new syntax


        } else {
            // older syntax
            // Iterate through the attributes of the annotation
            if (annotationNode.getAttributes().size() > 0) {
                for (BLangAnnotAttribute annotAttribute : annotationNode.getAttributes()) {
                    String dataType = getTypeName(annotAttribute.getTypeNode());
                    String desc = annotFieldAnnotation(annotationNode, annotAttribute);
                    String href = extractLink(annotAttribute.getTypeNode());
                    Variable variable = new Variable(annotAttribute.getName().value, dataType, desc, href);
                    attributes.add(variable);
                }
            }
        }

        return new AnnotationDoc(annotationName, description(annotationNode), new ArrayList<>(), attributes);
    }

    //TODO
    private static String extractLink(BLangType typeNode) {
        if (typeNode instanceof BLangUserDefinedType) {
            BLangUserDefinedType type = (BLangUserDefinedType) typeNode;
            String pkg = type.pkgAlias.getValue();
            BTypeSymbol tsymbol = ((BLangUserDefinedType) type).type.tsymbol;
            if (tsymbol instanceof BStructSymbol) {
                pkg = ((BStructSymbol) tsymbol).pkgID.getName().getValue();
            }
            return pkg + ".html#" + type.typeName.getValue();
        } else if (typeNode instanceof BLangValueType) {
            if (((BLangValueType) typeNode).type != null && ((BLangValueType) typeNode).type.tsymbol != null) {
                return BallerinaDocConstants.PRIMITIVE_TYPES_PAGE_HREF + ".html#" + typeNode.type.tsymbol.getName()
                        .value;
            }
        } else {
            // TODO
            return "";
        }
        return "";
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
        return new GlobalVariableDoc(globalVarName, desc, new ArrayList<>(), dataType);
    }

    /**
     * Create documentation for functions.
     *
     * @param functionNode ballerina function node.
     * @return documentation for functions.
     */
    public static FunctionDoc createDocForNode(BLangFunction functionNode) {
        String functionName = functionNode.getName().value;
        List<Variable> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        // Iterate through the parameters
        if (functionNode.getParameters().size() > 0) {
            for (BLangVariable param : functionNode.getParameters()) {
                String dataType = type(param);
                String desc = paramAnnotation(functionNode, param);
                String href = extractLink(param.getTypeNode());
                Variable variable = new Variable(param.getName().value, dataType, desc, href);
                parameters.add(variable);
            }
        }

        if (functionNode.getReturnTypeNode() != null) {
            BLangVariable returnParam = new BLangVariable();
            returnParam.typeNode = functionNode.getReturnTypeNode();
            String dataType = type(returnParam);
            if (!dataType.equals("null")) {
                String desc = returnParamAnnotation(functionNode);
                String href = extractLink(returnParam.getTypeNode());
                Variable variable = new Variable("", dataType, desc, href);
                returnParams.add(variable);
            }

        }

        // Iterate through the return types
//        if (functionNode.getReturnParameters().size() > 0) {
//            for (int i = 0; i < functionNode.getReturnParameters().size(); i++) {
//                BLangVariable returnParam = functionNode.getReturnParameters().get(i);
//                String dataType = type(returnParam);
//                String desc = returnParamAnnotation(functionNode, i);
//                Variable variable = new Variable(returnParam.getName().value, dataType, desc);
//                returnParams.add(variable);
//            }
//        }
        return new FunctionDoc(functionName, description(functionNode), new ArrayList<>(), parameters, returnParams);
    }

    /**
     * Create documentation for actions.
     *
     * @param actionNode ballerina action node.
     * @return documentation for actions.
     */
    public static ActionDoc createDocForNode(BLangAction actionNode) {
        String actionName = actionNode.getName().value;
        List<Variable> parameters = new ArrayList<>();
        List<Variable> returnParams = new ArrayList<>();
        // Iterate through the parameters
        if (actionNode.getParameters().size() > 0) {
            for (BLangVariable param : actionNode.getParameters()) {
                String dataType = type(param);
                String desc = paramAnnotation(actionNode, param);
                String href = extractLink(param.getTypeNode());
                Variable variable = new Variable(param.getName().value, dataType, desc, href);
                parameters.add(variable);
            }
        }

//        // Iterate through the return types
//        if (actionNode.getReturnParameters().size() > 0) {
//            for (int i = 0; i < actionNode.getReturnParameters().size(); i++) {
//                BLangVariable returnParam = actionNode.getReturnParameters().get(i);
//                String dataType = type(returnParam);
//                String desc = returnParamAnnotation(actionNode, i);
//                Variable variable = new Variable(returnParam.getName().value, dataType, desc);
//                returnParams.add(variable);
//            }
//        }
        return new ActionDoc(actionName, description(actionNode), new ArrayList<>(), parameters, returnParams);
    }

    /**
     * Create documentation for structs.
     *
     * @param structNode ballerina struct node.
     * @return documentation for structs.
     */
    public static StructDoc createDocForNode(BLangRecord structNode) {
        String structName = structNode.getName().getValue();
        // Check if its an anonymous struct
        if (structName.contains(ANONYMOUS_STRUCT)) {
            structName = "Anonymous Struct";
        }
        List<Field> fields = new ArrayList<>();

        // Iterate through the struct fields
        if (structNode.getFields().size() > 0) {
            getFields(structNode, structNode.fields, fields);
        }

        return new StructDoc(structName, description(structNode), new ArrayList<>(), fields);
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

    /**
     * Create documentation for connectors.
     *
     * @param connectorNode ballerina connector node.
     * @return documentation for connectors.
     */
    public static ConnectorDoc createDocForNode(BLangObject connectorNode) {
        String connectorName = connectorNode.getName().value;
        List<Field> parameters = new ArrayList<>();
        List<Documentable> actions = new ArrayList<>();

        // Iterate through the connector parameters
        if (connectorNode.fields.size() > 0) {
            getFields(connectorNode, connectorNode.fields, parameters);
        }

        //Iterate through the actions of the connectors
        if (connectorNode.getFunctions().size() > 0) {
            for (BLangFunction action : connectorNode.getFunctions()) {
                if (action.flagSet.contains(Flag.PUBLIC)) {
                    actions.add(createDocForNode(action));
                }
            }
        }
        return new ConnectorDoc(connectorName, description(connectorNode), actions, parameters, isConnector
                (connectorNode));
    }

    /**
     * Determine whether a given node is a Connector endpoint node.
     */
    private static boolean isConnector(BLangObject node) {
        if (!((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
            // new syntax
            if (!((DocumentableNode) node).getDocumentationAttachments().isEmpty()) {
                DocumentationNode bLangDocumentation = ((DocumentableNode) node).getDocumentationAttachments().get(0);
                for (DocumentationAttributeNode attribute : bLangDocumentation.getAttributes()) {
                    if (attribute instanceof BLangDocumentationAttribute) {
                        BLangDocumentationAttribute docAttr = (BLangDocumentationAttribute) attribute;
                        if (docAttr.docTag == DocTag.ENDPOINT) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

//    private static boolean hasConnectorAnnotation(BLangObject node) {
//        for (AnnotationAttachmentNode annotation : getAnnotationAttachments(node)) {
//            if (annotation.getAnnotationName().getValue().equals("Connector")) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Get the type of the variable.
     *
     * @param bLangVariable
     * @return data type of the variable.
     */
    private static String type(final BLangVariable bLangVariable) {
        if (bLangVariable.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
            if (((BLangUserDefinedType) bLangVariable.typeNode).typeName.value.contains(ANONYMOUS_STRUCT)) {
                return getAnonStructString((BStructType) bLangVariable.type);
            }
        }
        return getTypeName(bLangVariable.typeNode);
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
//        // if the annotation values cannot be found still, return the first matching
//        // annotation's value
//        for (AnnotationAttachmentNode annotation : getAnnotationAttachments(node)) {
//            BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) annotation.getExpression();
//            if (bLangRecordLiteral.getKeyValuePairs().size() != 1) {
//                continue;
//            }
//            if (annotation.getAnnotationName().getValue().equals("Field")) {
//                BLangExpression bLangLiteral = bLangRecordLiteral.getKeyValuePairs().get(0).getValue();
//                return bLangLiteral.toString();
//            }
//        }
        return "";
    }

    /**
     * Get description annotation of the annotation attribute.
     *
     * @param annotationNode parent node.
     * @param annotAttribute annotation attribute.
     * @return description of the annotation attribute.
     */
    private static String annotFieldAnnotation(BLangAnnotation annotationNode, BLangAnnotAttribute annotAttribute) {
        List<? extends AnnotationAttachmentNode> annotationAttachments = getAnnotationAttachments(annotationNode);

        for (AnnotationAttachmentNode annotation : annotationAttachments) {
            if ("Field".equals(annotation.getAnnotationName().getValue())) {
                BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) annotation.getExpression();
                BLangExpression bLangLiteral = bLangRecordLiteral.getKeyValuePairs().get(0).getValue();
                String value = bLangLiteral.toString();
                if (value.startsWith(annotAttribute.getName().getValue())) {
                    String[] valueParts = value.split(":");
                    return valueParts.length == 2 ? valueParts[1] : valueParts[0];
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

    /**
     * Get the anonymous struct string.
     *
     * @param type struct type.
     * @return anonymous struct string.
     */
    private static String getAnonStructString(BStructType type) {
        StringBuilder builder = new StringBuilder();
        builder.append("struct {");

        BStructType.BStructField field;
        int nFields = type.fields.size();
        for (int i = 0; i < nFields; i++) {
            field = type.fields.get(i);
            builder.append(field.type.toString()).append(" ").append(field.name.value);
            if (i == nFields - 1) {
                return builder.append("}").toString();
            }
            builder.append(", ");
        }

        return builder.append("}").toString();
    }
}
