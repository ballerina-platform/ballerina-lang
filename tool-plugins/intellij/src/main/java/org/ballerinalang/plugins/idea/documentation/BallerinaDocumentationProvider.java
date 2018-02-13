/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.documentation;

import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.codeInsight.javadoc.JavaDocUtil;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.editor.BallerinaParameterInfoHandler;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.InvocationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ReturnParametersNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeListNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides quick documentation support.
 */
public class BallerinaDocumentationProvider extends AbstractDocumentationProvider {

    private static final String DOC_SEPARATOR = ":";
    private static final String DOC_DESCRIPTION = "Description";
    private static final String DOC_PARAM = "Param";
    private static final String DOC_RETURN = "Return";
    private static final String DOC_FIELD = "Field";

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        // This method will be called when we do "Ctrl + Mouse Over" an element.
        if (element == null) {
            return null;
        }
        // We change the default "directory" with "Package".
        if (element instanceof PsiDirectory) {
            return "Package \"" + ((PsiDirectory) element).getName() + "\"";
        }
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return element;
    }

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        // This method will be called to generate the doc.
        // First we get the signature.
        String signature = getSignature(element);
        signature = StringUtil.isNotEmpty(signature) ? "<b>" + signature + "</b><br>" : signature;
        // Then we get the text from docs.
        String generatedDoc = StringUtil.nullize(signature + getDocText(element, originalElement));
        return generatedDoc == null || generatedDoc.isEmpty() ? null : generatedDoc;
    }

    @NotNull
    private static String getSignature(PsiElement element) {
        if (element == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        // element will be an identifier. So we need to get the parent of the element to identify the type to
        // generate the signature.
        PsiElement parent = element.getParent();
        // Generate the signature according to the parent type.
        if (parent instanceof FunctionDefinitionNode) {
            // Add the function signature.
            stringBuilder.append("function ");
            stringBuilder.append(element.getText());

            // Get parameters.
            String params = getParameterString(parent, false);
            // Add parameters.
            stringBuilder.append("(");
            stringBuilder.append(params);
            stringBuilder.append(")");

            // Get return types. These can be either return types or return parameters.
            List<String> returnParamsList = getReturnTypes(parent);
            String returnParams = StringUtil.join(returnParamsList, ", ");
            if (!returnParams.isEmpty()) {
                // Add return types/parameters.
                stringBuilder.append("(");
                stringBuilder.append(returnParams);
                stringBuilder.append(")");
            }
        } else if (parent instanceof ActionDefinitionNode || parent instanceof InvocationNode) {
            // Add the action signature.
            stringBuilder.append("action ");
            stringBuilder.append(element.getText());

            // Get parameters.
            String params = getParameterString(parent, false);
            // Add parameters.
            stringBuilder.append("(");
            stringBuilder.append(params);
            stringBuilder.append(")");

            // Get return types/parameters.
            List<String> returnParamsList = getReturnTypes(parent);
            String returnParams = StringUtil.join(returnParamsList, ", ");
            if (!returnParams.isEmpty()) {
                // Add return types/parameters.
                stringBuilder.append("(");
                stringBuilder.append(returnParams);
                stringBuilder.append(")");
            }
        } else if (parent instanceof ConnectorDefinitionNode) {
            // Add the connector signature.
            stringBuilder.append("connector ");
            stringBuilder.append(element.getText());

            // Get parameters.
            String params = getParameterString(parent, false);
            // Add parameters.
            stringBuilder.append("(");
            stringBuilder.append(params);
            stringBuilder.append(")");
        } else if (parent instanceof StructDefinitionNode) {
            // Add the function signature.
            stringBuilder.append("struct ");
            stringBuilder.append(element.getText());
            stringBuilder.append(" { }");
        } else if (parent instanceof ConstantDefinitionNode) {
            // Add the function signature.
            stringBuilder.append(parent.getText());
        }

        // If the doc is available, add package to the top.
        if (!stringBuilder.toString().isEmpty()) {
            // Add the containing package to the quick doc if there is any.
            PsiElement packagePathNode = getContainingPackage(element);
            if (packagePathNode != null) {
                stringBuilder.insert(0, packagePathNode.getText() + "<br><br>");
            }
        }
        // Return the signature.
        return stringBuilder.toString();
    }

    /**
     * Returns parameters as a single string concatenated with ",". Leading and Trailering parenthesis will be added
     * if the {@literal withParenthesis} is true.
     *
     * @param definitionNode  parent element (definition element)
     * @param withParenthesis indicate whether we want to enclose the parameters with parenthesis
     * @return a string containing the parameters.
     */
    @NotNull
    public static String getParameterString(PsiElement definitionNode, boolean withParenthesis) {
        ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(definitionNode, ParameterListNode.class);
        List<String> presentations = BallerinaParameterInfoHandler.getParameterPresentations(parameterListNode);
        StringBuilder stringBuilder = new StringBuilder();
        if (withParenthesis) {
            stringBuilder.append("(");
        }
        stringBuilder.append(StringUtil.join(presentations, ", "));
        if (withParenthesis) {
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    @NotNull
    private static String getDocText(PsiElement element, PsiElement originalElement) {
        if (element == null) {
            return "";
        }
        if (originalElement == null) {
            return "";
        }
        // Get all doc annotations.
        List<PsiElement> annotations = getDocAnnotations(element);

        StringBuilder stringBuilder = new StringBuilder();
        // Get the function description.
        String description = getDescriptionFromDoc(annotations);
        // Add the description to the string builder.
        if (!description.isEmpty()) {
            stringBuilder.append("<DL><DT>").append(description).append("</DT></DL>");
        }
        // Get the parameter descriptions from docs.
        List<String> params = getParamDescriptions(annotations);
        if (!params.isEmpty()) {
            // Generate parameters section.
            generateSection(stringBuilder, "Parameters:", params);
        }
        // Get return value descriptions from docs.
        List<String> returnTypes = getReturnValueDescriptions(annotations);
        if (!returnTypes.isEmpty()) {
            // Generate return values section.
            generateSection(stringBuilder, "Returns:", returnTypes);
        }
        if (element.getParent() instanceof StructDefinitionNode) {
            // Get return value descriptions from docs.
            List<String> fields = getFieldDescriptions(annotations);
            if (!fields.isEmpty()) {
                // Generate return values section.
                generateSection(stringBuilder, "Fields:", fields);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Returns the return types of the given definition node. This return types are later used to generate the
     * signature.
     *
     * @param definitionNode definition node which we want to get the return types
     * @return list of return type strings.
     */
    private static List<String> getReturnTypes(PsiElement definitionNode) {
        List<String> results = new LinkedList<>();
        // Parameters are in the ReturnParametersNode. So we first get the ReturnParametersNode from the definition
        // node.
        ReturnParametersNode node = PsiTreeUtil.findChildOfType(definitionNode, ReturnParametersNode.class);
        if (node == null) {
            return results;
        }
        // But there can be two possible scenarios. The actual return types can be in either TypeListNode or
        // ParameterListNode. This is because return types can be named parameters. In that case, ParameterListNode is
        // available.

        // First we check for TypeListNode.
        TypeListNode typeListNode = PsiTreeUtil.findChildOfType(node, TypeListNode.class);
        // If it is available, that means the return types are not named parameters.
        if (typeListNode != null) {
            // Each parameter will be of type TypeNameNode. So we get all return types.
            Collection<TypeNameNode> typeNameNodes =
                    PsiTreeUtil.getChildrenOfTypeAsList(typeListNode, TypeNameNode.class);
            // Add each TypeNameNode to the result list.
            typeNameNodes.forEach(typeNameNode ->
                    results.add(BallerinaParameterInfoHandler.formatParameter(typeNameNode.getText())));
            // Return the results.
            return results;
        }

        // If there is no return type node, we check for ParameterListNode.
        ParameterListNode parameterListNode = PsiTreeUtil.findChildOfType(node, ParameterListNode.class);
        if (parameterListNode != null) {
            // Actual parameters are in ParameterNodes.
            Collection<ParameterNode> parameterNodes =
                    PsiTreeUtil.findChildrenOfType(parameterListNode, ParameterNode.class);
            // Add each ParameterNode to the result list.
            parameterNodes.forEach(parameterNode ->
                    results.add(BallerinaParameterInfoHandler.formatParameter(parameterNode.getText())));
            // Return the results.
            return results;
        }
        // Return empty list.
        return results;
    }

    /**
     * Returns parameters and return types of the given definition.
     *
     * @param definitionNode definition node which we want to use when getting the parameter and return type
     * @return parameters and return types. Eg: (param1, param2)(rt1, rt2)
     */
    public static String getParametersAndReturnTypes(PsiElement definitionNode) {
        String parameterString = BallerinaDocumentationProvider.getParameterString(definitionNode, true);
        String returnTypeString = "";
        List<String> returnTypes = getReturnTypes(definitionNode);
        if (!returnTypes.isEmpty()) {
            returnTypeString = "(" + StringUtil.join(returnTypes, ", ") + ")";
        }
        return parameterString + returnTypeString;
    }

    /**
     * Returns the package which contains the given element.
     *
     * @param element element which needs to be used to get the package
     * @return the package which contains the given element.
     */
    @Nullable
    private static FullyQualifiedPackageNameNode getContainingPackage(PsiElement element) {
        // Get the containing file.
        PsiFile containingFile = element.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        // Get the PackageDeclarationNode from the file.
        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(containingFile,
                PackageDeclarationNode.class);
        if (packageDeclarationNode == null) {
            return null;
        }
        // Return the FullyQualifiedPackageNameNode since it contains the package.
        return PsiTreeUtil.findChildOfType(packageDeclarationNode, FullyQualifiedPackageNameNode.class);
    }

    /**
     * Returns all doc annotations which precedes the given element.
     *
     * @param element element which is used to get the annotations
     * @return all annotation nodes which precedes the given element.
     */
    @NotNull
    private static List<PsiElement> getDocAnnotations(PsiElement element) {
        List<PsiElement> annotations = new LinkedList<>();
        if (element.getParent() instanceof ActionDefinitionNode) {
            // For action definition nodes, annotations precedes the parent node.
            findAnnotations(element.getParent(), annotations);
        } else {
            // For other types of nodes, annotations precedes the super parent node.
            findAnnotations(element.getParent().getParent(), annotations);
        }
        // Return the annotations.
        return annotations;
    }

    /**
     * Finds annotations for the given definition node and adds to the provided list.
     *
     * @param definitionNode node which we use to find annotations.
     * @param annotations    list which is used to add result
     */
    private static void findAnnotations(PsiElement definitionNode, List<PsiElement> annotations) {
        PsiElement prevSibling = definitionNode.getPrevSibling();
        // We iterate through all nodes ignoring PsiWhiteSpace nodes.
        while (prevSibling != null && (prevSibling instanceof AnnotationAttachmentNode ||
                prevSibling instanceof PsiWhiteSpace)) {
            // If the prevSibling is not a PsiWhiteSpace, that means it is an AnnotationAttachmentNode.
            if (!(prevSibling instanceof PsiWhiteSpace)) {
                annotations.add(0, prevSibling);
            }
            prevSibling = prevSibling.getPrevSibling();
        }
    }

    /**
     * Returns the description from the {@code Description} doc annotation.
     *
     * @param annotations list of all annotations
     * @return the description as a string
     */
    @NotNull
    private static String getDescriptionFromDoc(List<PsiElement> annotations) {
        for (PsiElement annotation : annotations) {
            AnnotationAttributeValueNode valueNode = getAnnotationAttributeNode(annotation, DOC_DESCRIPTION);
            if (valueNode == null) {
                continue;
            }
            String text = valueNode.getText();
            // We ignore the " in the beginning and end.
            return text.substring(1, text.length() - 1);
        }
        return "";
    }

    /**
     * Returns the parameters from the {@code Param}  doc annotations.
     *
     * @param annotations list of all annotations
     * @return list of parameter strings
     */
    @NotNull
    private static List<String> getParamDescriptions(List<PsiElement> annotations) {
        List<String> params = new LinkedList<>();
        for (PsiElement annotation : annotations) {
            AnnotationAttributeValueNode valueNode = getAnnotationAttributeNode(annotation, DOC_PARAM);
            if (valueNode == null) {
                continue;
            }
            String text = valueNode.getText();
            // We ignore the " in the beginning and end. We also replace the ":" with " -" to increase the
            // readability of the docs.
            params.add(text.substring(1, text.length() - 1).replaceFirst(DOC_SEPARATOR, " -"));
        }
        return params;
    }

    /**
     * Returns the return value descriptions from the {@code Return} doc annotations.
     *
     * @param annotations list of all annotations
     * @return list of return value strings
     */
    @NotNull
    private static List<String> getReturnValueDescriptions(List<PsiElement> annotations) {
        List<String> returnTypes = new LinkedList<>();
        for (PsiElement annotation : annotations) {
            AnnotationAttributeValueNode valueNode = getAnnotationAttributeNode(annotation, DOC_RETURN);
            if (valueNode == null) {
                continue;
            }
            String text = valueNode.getText();
            // We ignore the " in the beginning and end. We also replace the ":" with " -" to increase the
            // readability of the docs.
            returnTypes.add(text.substring(1, text.length() - 1).replaceFirst(DOC_SEPARATOR, " -"));
        }
        return returnTypes;
    }

    /**
     * Returns the field descriptions from the {@code Field} doc annotations.
     *
     * @param annotations list of all annotations
     * @return list of field description strings
     */
    @NotNull
    private static List<String> getFieldDescriptions(List<PsiElement> annotations) {
        List<String> returnTypes = new LinkedList<>();
        for (PsiElement annotation : annotations) {
            AnnotationAttributeValueNode valueNode = getAnnotationAttributeNode(annotation, DOC_FIELD);
            if (valueNode == null) {
                continue;
            }
            String text = valueNode.getText();
            // We ignore the " in the beginning and end. We also replace the ":" with " -" to increase the
            // readability of the docs.
            returnTypes.add(text.substring(1, text.length() - 1).replaceFirst(DOC_SEPARATOR, " -"));
        }
        return returnTypes;
    }

    /**
     * Extract and return the {@link AnnotationAttributeValueNode} if the provided annotation matches the provided type.
     *
     * @param annotation     annotation element
     * @param annotationType annotation type
     * @return {@link AnnotationAttributeValueNode} of the given element if the annotation matches the given type.
     * {@code null}
     * otherwise.
     */
    private static AnnotationAttributeValueNode getAnnotationAttributeNode(PsiElement annotation,
                                                                           String annotationType) {
        AnnotationReferenceNode annotationReferenceNode = PsiTreeUtil.findChildOfType(annotation,
                AnnotationReferenceNode.class);
        if (annotationReferenceNode == null) {
            return null;
        }
        PsiElement nameReferenceNodeIdentifier = annotationReferenceNode.getNameIdentifier();
        if (nameReferenceNodeIdentifier == null) {
            return null;
        }
        if (!annotationType.equals(nameReferenceNodeIdentifier.getText())) {
            return null;
        }
        return PsiTreeUtil.findChildOfType(annotation, AnnotationAttributeValueNode.class);
    }

    /**
     * Generates sections in doc like parameters, return values, etc.
     *
     * @param stringBuilder stringBuilder object which is used to generate the doc
     * @param title         title of the section
     * @param tags          list of values(parameter descriptions, return type descriptions, etc) which needs to be
     *                      added to the section
     */
    private static void generateSection(StringBuilder stringBuilder, String title, List<String> tags) {
        stringBuilder.append("<DL>");
        stringBuilder.append("<DT><b>").append(title).append("</b></DT>");
        for (String tag : tags) {
            generateOneTag(stringBuilder, tag);
        }
        stringBuilder.append("</DL>");
    }

    /**
     * generate a single tag in the doc.
     *
     * @param stringBuilder stringBuilder object which is used to generate the doc
     * @param tag           value(parameter description, return type description, etc) which needs to be added to
     *                      the section
     */
    private static void generateOneTag(StringBuilder stringBuilder, String tag) {
        stringBuilder.append("<DD>").append(tag).append("</DD>");
    }

    /**
     * Creates element link in the doc.
     *
     * @param stringBuilder stringBuilder object which is used to generate the doc
     * @param element       element which needs to be linked
     * @param displayName   display text of the link
     */
    private static void createElementLink(StringBuilder stringBuilder, PsiElement element, String displayName) {
        stringBuilder.append("&nbsp;&nbsp;<a href=\"" + DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL);
        stringBuilder.append(JavaDocUtil.getReferenceText(element.getProject(), element));
        stringBuilder.append("\">");
        stringBuilder.append(displayName);
        stringBuilder.append("</a>");
        stringBuilder.append("<br>");
    }
}
