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
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ReturnParametersNode;
import org.ballerinalang.plugins.idea.psi.ReturnTypeListNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BallerinaDocumentationProvider extends AbstractDocumentationProvider {

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
        String signature = getSignature(element, originalElement);
        signature = StringUtil.isNotEmpty(signature) ? "<b>" + signature + "</b><br>" : signature;
        // Then we get the text from docs.
        return StringUtil.nullize(signature + getDocText(element, originalElement));
    }

    @NotNull
    public static String getSignature(PsiElement element, PsiElement originalElement) {
        if (element == null) {
            return "";
        }
        PsiElement parent = element.getParent();
        if (parent instanceof FunctionNode) {
            StringBuilder stringBuilder = new StringBuilder();
            // Add the containing package if there is any.
            PsiElement packagePathNode = getContainingPackage(element);
            if (packagePathNode != null) {
                stringBuilder.append(packagePathNode.getText()).append("<br><br>");
            }
            // Add the function signature.
            stringBuilder.append("func ");
            stringBuilder.append(element.getText());
            stringBuilder.append("(");

            // Get parameters.
            ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(parent, ParameterListNode.class);
            List<String> presentations = BallerinaParameterInfoHandler.getParameterPresentations(parameterListNode);
            String params = StringUtil.join(presentations, ", ");
            stringBuilder.append(params);
            stringBuilder.append(")");

            // Get return parameters.
            List<String> returnParamsList = getReturnParams(parent);
            String returnParams = StringUtil.join(returnParamsList, ", ");
            if (!returnParams.isEmpty()) {
                stringBuilder.append("(");
                stringBuilder.append(returnParams);
                stringBuilder.append(")");
            }
            // Return the function signature.
            return stringBuilder.toString();
        } else if (parent instanceof ActionDefinitionNode) {
            // Todo
        } else if (parent instanceof ConnectorNode) {
            // Todo
        } else if (parent instanceof StructDefinitionNode) {
            // Todo
        }
        return "";
    }

    @NotNull
    public static String getDocText(PsiElement element, PsiElement originalElement) {
        if (element == null) {
            return "";
        }
        if (originalElement == null) {
            return "";
        }
        PsiElement parent = element.getParent();
        // Get all doc annotations.
        List<PsiElement> annotations = getDocAnnotations(element);
        if (parent instanceof FunctionNode) {
            StringBuilder stringBuilder = new StringBuilder();
            // Get the function description.
            String description = getDescriptionFromDoc(annotations);
            stringBuilder.append("<DL><DT>").append(description).append("</DT></DL>");
            // Get the parameter descriptions from docs.
            List<String> params = getParamDescriptions(annotations);
            if (!params.isEmpty()) {
                stringBuilder.append("<DL>");
                stringBuilder.append("<DT><b>").append("Parameters:").append("</b></DT>");
                for (String param : params) {
                    stringBuilder.append("<DD>").append(param).append("</DD>");
                }
                stringBuilder.append("</DL>");
            }
            // Get return value descriptions from docs.
            List<String> returnTypes = getReturnValueDescriptions(annotations);
            if (!returnTypes.isEmpty()) {
                stringBuilder.append("<DL>");
                stringBuilder.append("<DT><b>").append("Returns:").append("</b></DT>");
                for (String returnType : returnTypes) {
                    stringBuilder.append("<DD>").append(returnType).append("</DD>");
                }
                stringBuilder.append("</DL>");
            }
            return stringBuilder.toString();
        } else if (parent instanceof ActionDefinitionNode) {
            //Todo
        } else if (parent instanceof ConnectorNode) {
            //Todo
        } else if (parent instanceof StructDefinitionNode) {
            //Todo
        }
        return "";
    }

    private static List<String> getReturnParams(PsiElement functionNode) {
        List<String> results = new LinkedList<>();
        ReturnParametersNode node = PsiTreeUtil.findChildOfType(functionNode, ReturnParametersNode.class);
        if (node == null) {
            return results;
        }
        ReturnTypeListNode returnTypeListNode = PsiTreeUtil.findChildOfType(node, ReturnTypeListNode.class);
        if (returnTypeListNode != null) {
            Collection<TypeNameNode> typeNameNodes =
                    PsiTreeUtil.findChildrenOfType(returnTypeListNode, TypeNameNode.class);
            for (TypeNameNode typeNameNode : typeNameNodes) {
                results.add(typeNameNode.getText());
            }
            return results;
        }

        ParameterListNode parameterListNode = PsiTreeUtil.findChildOfType(node, ParameterListNode.class);
        if (parameterListNode != null) {
            Collection<ParameterNode> parameterNodes =
                    PsiTreeUtil.findChildrenOfType(parameterListNode, ParameterNode.class);
            for (ParameterNode parameterNode : parameterNodes) {
                results.add(parameterNode.getText());
            }
            return results;
        }
        return results;
    }

    @Nullable
    private static PsiElement getContainingPackage(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(containingFile,
                PackageDeclarationNode.class);
        if (packageDeclarationNode == null) {
            return null;
        }
        return PsiTreeUtil.findChildOfType(packageDeclarationNode, PackagePathNode.class);
    }

    @NotNull
    private static List<PsiElement> getDocAnnotations(PsiElement element) {
        List<PsiElement> annotations = new LinkedList<>();
        PsiElement superParent = element.getParent().getParent();
        PsiElement prevSibling = superParent.getPrevSibling();

        while (prevSibling != null && (prevSibling instanceof AnnotationAttachmentNode ||
                prevSibling instanceof PsiWhiteSpace)) {
            if (!(prevSibling instanceof PsiWhiteSpace)) {
                annotations.add(0, prevSibling);
            }
            prevSibling = prevSibling.getPrevSibling();
        }
        return annotations;
    }

    @NotNull
    private static String getDescriptionFromDoc(List<PsiElement> annotations) {
        for (PsiElement annotation : annotations) {
            NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(annotation, NameReferenceNode.class);
            if (nameReferenceNode == null) {
                continue;
            }
            PsiElement nameReferenceNodeIdentifier = nameReferenceNode.getNameIdentifier();
            if (nameReferenceNodeIdentifier == null) {
                continue;
            }
            if (!"Description".equals(nameReferenceNodeIdentifier.getText())) {
                continue;
            }
            PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(nameReferenceNode, PackageNameNode.class);
            if (packageNameNode == null) {
                continue;
            }
            PsiElement packageNameNodeIdentifier = packageNameNode.getNameIdentifier();
            if (packageNameNodeIdentifier == null) {
                continue;
            }
            if (!"doc".equals(packageNameNodeIdentifier.getText())) {
                continue;
            }
            AnnotationAttributeValueNode valueNode = PsiTreeUtil.findChildOfType(annotation,
                    AnnotationAttributeValueNode.class);
            if (valueNode == null) {
                continue;
            }
            return valueNode.getText().substring(1, valueNode.getText().length() - 1);
        }
        return "";
    }

    @NotNull
    private static List<String> getParamDescriptions(List<PsiElement> annotations) {
        List<String> params = new LinkedList<>();
        for (PsiElement annotation : annotations) {
            NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(annotation, NameReferenceNode.class);
            if (nameReferenceNode == null) {
                continue;
            }
            PsiElement nameReferenceNodeIdentifier = nameReferenceNode.getNameIdentifier();
            if (nameReferenceNodeIdentifier == null) {
                continue;
            }
            if (!"Param".equals(nameReferenceNodeIdentifier.getText())) {
                continue;
            }
            PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(nameReferenceNode, PackageNameNode.class);
            if (packageNameNode == null) {
                continue;
            }
            PsiElement packageNameNodeIdentifier = packageNameNode.getNameIdentifier();
            if (packageNameNodeIdentifier == null) {
                continue;
            }
            if (!"doc".equals(packageNameNodeIdentifier.getText())) {
                continue;
            }
            AnnotationAttributeValueNode valueNode = PsiTreeUtil.findChildOfType(annotation,
                    AnnotationAttributeValueNode.class);
            if (valueNode == null) {
                continue;
            }
            params.add(valueNode.getText().substring(1, valueNode.getText().length() - 1).replaceFirst(":", " -"));
        }
        return params;
    }

    @NotNull
    private static List<String> getReturnValueDescriptions(List<PsiElement> annotations) {
        List<String> returnTypes = new LinkedList<>();
        for (PsiElement annotation : annotations) {
            NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(annotation, NameReferenceNode.class);
            if (nameReferenceNode == null) {
                continue;
            }
            PsiElement nameReferenceNodeIdentifier = nameReferenceNode.getNameIdentifier();
            if (nameReferenceNodeIdentifier == null) {
                continue;
            }
            if (!"Return".equals(nameReferenceNodeIdentifier.getText())) {
                continue;
            }
            PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(nameReferenceNode, PackageNameNode.class);
            if (packageNameNode == null) {
                continue;
            }
            PsiElement packageNameNodeIdentifier = packageNameNode.getNameIdentifier();
            if (packageNameNodeIdentifier == null) {
                continue;
            }
            if (!"doc".equals(packageNameNodeIdentifier.getText())) {
                continue;
            }
            AnnotationAttributeValueNode valueNode = PsiTreeUtil.findChildOfType(annotation,
                    AnnotationAttributeValueNode.class);
            if (valueNode == null) {
                continue;
            }
            returnTypes.add(valueNode.getText().substring(1, valueNode.getText().length() - 1).replaceFirst(":",
                    " -"));
        }
        return returnTypes;
    }

    //    @NotNull
    //    private static String getReturnValueDescriptions(List<PsiElement> annotations) {
    //        for (PsiElement annotation : annotations) {
    //            NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(annotation, NameReferenceNode
    // .class);
    //            if (nameReferenceNode == null) {
    //                continue;
    //            }
    //            PsiElement nameReferenceNodeIdentifier = nameReferenceNode.getNameIdentifier();
    //            if (nameReferenceNodeIdentifier == null) {
    //                continue;
    //            }
    //            if (!"Return".equals(nameReferenceNodeIdentifier.getText())) {
    //                continue;
    //            }
    //            PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(nameReferenceNode, PackageNameNode
    // .class);
    //            if (packageNameNode == null) {
    //                continue;
    //            }
    //            PsiElement packageNameNodeIdentifier = packageNameNode.getNameIdentifier();
    //            if (packageNameNodeIdentifier == null) {
    //                continue;
    //            }
    //            if (!"doc".equals(packageNameNodeIdentifier.getText())) {
    //                continue;
    //            }
    //            AnnotationAttributeValueNode valueNode = PsiTreeUtil.findChildOfType(annotation,
    //                    AnnotationAttributeValueNode.class);
    //            if (valueNode == null) {
    //                continue;
    //            }
    //            return valueNode.getText().substring(1, valueNode.getText().length() - 1);
    //        }
    //        return "";
    //    }

    private static void createElementLink(StringBuilder sb, PsiElement element, String str) {
        sb.append("&nbsp;&nbsp;<a href=\"" + DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL);
        sb.append(JavaDocUtil.getReferenceText(element.getProject(), element));
        sb.append("\">");
        sb.append(str);
        sb.append("</a>");
        sb.append("<br>");
    }
}
