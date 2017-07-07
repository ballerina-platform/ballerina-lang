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

package org.ballerinalang.plugins.idea.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiLeafNode;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.antlr.jetbrains.adaptor.psi.Trees;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.ActionInvocationReference;
import org.ballerinalang.plugins.idea.psi.references.AnnotationAttributeReference;
import org.ballerinalang.plugins.idea.psi.references.AnnotationReference;
import org.ballerinalang.plugins.idea.psi.references.ConnectorReference;
import org.ballerinalang.plugins.idea.psi.references.FieldReference;
import org.ballerinalang.plugins.idea.psi.references.FunctionReference;
import org.ballerinalang.plugins.idea.psi.references.PackageNameReference;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.ballerinalang.plugins.idea.psi.references.StatementReference;
import org.ballerinalang.plugins.idea.psi.references.StructKeyReference;
import org.ballerinalang.plugins.idea.psi.references.StructValueReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.*;

public class IdentifierPSINode extends ANTLRPsiLeafNode implements PsiNamedElement, PsiNameIdentifierOwner {

    public IdentifierPSINode(IElementType type, CharSequence text) {
        super(type, text);
    }

    @Override
    public String getName() {
        return getText();
    }

    /**
     * Alter this node to have text specified by the argument. Do this by
     * creating a new node through parsing of an ID and then doing a
     * replace.
     */
    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if (getParent() == null) {
            return this;
        }
        PsiElement newID = Trees.createLeafFromText(getProject(), BallerinaLanguage.INSTANCE, getContext(), name,
                BallerinaTypes.IDENTIFIER);
        if (newID != null) {
            // use replace on leaves but replaceChild on ID nodes that are part of defs/decls.
            return this.replace(newID);
        }
        return this;
    }

    /**
     * Create and return a PsiReference object associated with this ID
     * node. The reference object will be asked to resolve this ref
     * by using the text of this node to identify the appropriate definition
     * site. The definition site is typically a subtree for a function
     * or variable definition whereas this reference is just to this ID
     * leaf node.
     * <p>
     * As the AST factory has no context and cannot create different kinds
     * of PsiNamedElement nodes according to context, every ID node
     * in the tree will be of this type. So, we distinguish references
     * from definitions or other uses by looking at context in this method
     * as we have parent (context) information.
     */
    @Override
    public PsiReference getReference() {
        PsiElement parent = getParent();
        IElementType elType = parent.getNode().getElementType();

        PsiElement prevVisibleLeaf;

        // do not return a reference for the ID nodes in a definition
        if (elType instanceof RuleIElementType) {
            switch (((RuleIElementType) elType).getRuleIndex()) {
                case RULE_packageName:
                    return new PackageNameReference(this);
                case RULE_actionInvocation:
                    return new ActionInvocationReference(this);
                case RULE_nameReference:


//                    prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
//                    if (prevVisibleLeaf != null && ":".equals(prevVisibleLeaf.getText())) {
//                        PsiElement packageName = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
//                        if (packageName != null) {
//                            PsiFile containingFile = parent.getContainingFile();
//                            // Todo - use util?
//                            List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages
//                                    (containingFile);
//                            for (PsiElement importedPackage : importedPackages) {
//                                PsiReference reference = importedPackage.findReferenceAt(0);
//                                if (reference == null) {
//                                    continue;
//                                }
//                                PsiElement resolvedElement = reference.resolve();
//                                if (resolvedElement == null) {
//                                    continue;
//                                }
//                                PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
//                                if (packageName.getText().equals(resolvedPackage.getName())) {
//                                    return new NameReference(this);
//                                }
//                            }
//                        }
//                    }

                    MapStructKeyNode mapStructKeyNode = PsiTreeUtil.getParentOfType(parent, MapStructKeyNode.class);
                    if (mapStructKeyNode != null) {

                        // Eg: {test:'firstName':firstName}
                        PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(mapStructKeyNode,
                                PackageNameNode.class);
                        if (packageNameNode != null) {
                            return new NameReference(this);
                        }

                        PsiFile containingFile = parent.getContainingFile();
                        // Todo - use util?
                        List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages
                                (containingFile);
                        for (PsiElement importedPackage : importedPackages) {
                            PsiReference reference = importedPackage.findReferenceAt(0);
                            if (reference == null) {
                                continue;
                            }
                            PsiElement resolvedElement = reference.resolve();
                            if (resolvedElement == null) {
                                continue;
                            }
                            PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
                            if (parent.getText().equals(resolvedPackage.getName())) {
                                return new PackageNameReference(this);
                            }

                        }

                        return new StructKeyReference(this);
                    }
                    MapStructValueNode mapStructValueNode = PsiTreeUtil.getParentOfType(parent,
                            MapStructValueNode.class);
                    if (mapStructValueNode != null) {
                        return new StructValueReference(this);
                    }
                    return new NameReference(this);
                case RULE_field:
                    return new FieldReference(this);
                case RULE_functionReference:
                    return new FunctionReference(this);
                case RULE_annotationReference:
                    return new AnnotationReference(this);
                case RULE_connectorReference:
                    return new ConnectorReference(this);
                //                case RULE_variableReference:
                //                case RULE_parameter:
                //                    // If "package:" is typed as an argument, it will be identified as a
                // variableReference. So we
                //                    // need to match it with a regex and return a PackageNameReference.
                //                    if (parent.getText().matches(".+:")) {
                //                        return new PackageNameReference(this);
                //                    }
                //                    PsiElement prevSibling = getPrevSibling();
                //                    if (prevSibling != null && prevSibling.getText().matches(".+:")) {
                //                        return new PackageNameReference(this);
                //                    }
                //                    return new VariableReference(this);
                case RULE_annotationAttribute:
                    return new AnnotationAttributeReference(this);
                case RULE_statement:
                    prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(getParent());
                    if (prevVisibleLeaf != null)
                        if (".".equals(prevVisibleLeaf.getText())) {
                            return new FieldReference(this);
                        }
                    return new StatementReference(this);
                default:
                    return null;
            }
        }
        if (parent instanceof PsiErrorElement) {
            return suggestReferenceType();
        }
        return null;
    }

    private PsiReference suggestReferenceType() {
        PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(getParent());
        if (nextVisibleLeaf != null) {
            if (":".equals(nextVisibleLeaf.getText())) {
                return new PackageNameReference(this);
            } else if (".".equals(nextVisibleLeaf.getText())) {
                return new NameReference(this);
            }
        }

        return new NameReference(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        PsiElement parent = getParent();
        if (parent instanceof IdentifierDefSubtree) {
            return ((IdentifierDefSubtree) parent).getPresentation();
        }
        return super.getPresentation();
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        // If the parent is a ParameterNode, we return this identifier object. Otherwise when we get parents, it
        // might return excluded nodes below and can cause issues.
        if (getParent() instanceof ParameterNode || getParent() instanceof VariableDefinitionNode) {
            return this;
        }

        // We should return the name identifier node (which is "this" object) for every identifier which can be used
        // to find usage. But for some nodes like TypeMapperNode, ServiceDefinitionNode, etc, we don't need to find
        // usages because they will not be used in other places.
        PsiElement parent = PsiTreeUtil.getParentOfType(this, TypeMapperNode.class);
        if (parent != null) {
            return null;
        }
        parent = PsiTreeUtil.getParentOfType(this, ResourceDefinitionNode.class);
        if (parent != null) {
            return null;
        }
        parent = PsiTreeUtil.getParentOfType(this, ServiceDefinitionNode.class);
        if (parent != null) {
            return null;
        }
        return this;
    }
}
