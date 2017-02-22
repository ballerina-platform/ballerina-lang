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

package org.ballerinalang.plugins.idea.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionBodyNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BallerinaElementReference extends PsiReferenceBase<IdentifierPSINode>
        implements PsiPolyVariantReference {

    public BallerinaElementReference(@NotNull IdentifierPSINode element) {
        /** WARNING: You must send up the text range or you get this error:
         * "Cannot find manipulator for PsiElement(ID) in org.antlr.jetbrains.sample.SampleElementRef"...
         *  when you click on an identifier.  During rename you get this
         *  error too if you don't impl handleElementRename().
         *
         *  The range is relative to start of the token; I guess for
         *  qualified references we might want to use just a part of the name.
         *  Or we might look inside string literals for stuff.
         */
        super(element, new TextRange(0, element.getText().length()));
    }

    /**
     * Change the REFERENCE's ID node (not the targeted def's ID node)
     * to reflect a rename.
     * <p>
     * Without this method, we get an error ("Cannot find manipulator...").
     * <p>
     * getElement() refers to the identifier node that references the definition.
     */
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        //		System.out.println(getClass().getSimpleName()+".handleElementRename("+myElement.getName()
        // +"->"+newElementName+
        //			                   ") on "+myElement+" at "+Integer.toHexString(myElement.hashCode()));

        return myElement.setName(newElementName);
    }

    /**
     * Resolve a reference to the definition subtree (subclass of
     * IdentifierDefSubtree), do not resolve to the ID child of that
     * definition subtree root.
     */
    @Nullable
    @Override
    public PsiElement resolve() {
        //		System.out.println(getClass().getSimpleName()+
        //		                   ".resolve("+myElement.getName()+
        //		                   " at "+Integer.toHexString(myElement.hashCode())+")");
        ScopeNode scope = (ScopeNode) myElement.getContext();
        if (scope == null) return null;

        return scope.resolve(myElement);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        ScopeNode scope = (ScopeNode) myElement.getContext();
        if (scope == null) {
            return new ResolveResult[0];
        }

        return scope.multiResolve(myElement);
        //        return new ResolveResult[0];
    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        String refName = myElement.getName();
        //		System.out.println(getClass().getSimpleName()+".isReferenceTo("+refName+"->"+definitionElement.getText
        // ()+")");
        // sometimes definitionElement comes in pointing to ID node itself. depends on what you click on
        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent())) {
            definitionElement = definitionElement.getParent();
        }
        if (isDefinitionNode(definitionElement)) {

            // Todo - check offset as well to identify the locations of the variable declaration and usages
            // Check the scope of the variable
            if (definitionElement instanceof ParameterNode) {
                // If the common context is file, that means the myElement is not in the scope where the
                // definitionElement is defined in.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof FunctionDefinitionNode
                        || commonContext instanceof FunctionNode
                        || commonContext instanceof ResourceDefinitionNode
                        || commonContext instanceof ConnectorDefinitionNode
                        || commonContext instanceof ActionDefinitionNode)) {
                    return false;
                }
            } else if (definitionElement instanceof VariableDefinitionNode) {
                // If the common context is file, that means the myElement is not in the scope where the
                // definitionElement is defined in.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof FunctionBodyNode || commonContext instanceof ConnectorBodyNode)) {
                    return false;
                }
            }else if (definitionElement instanceof PsiErrorElement) {
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement.getParent(), myElement);
                if (!(commonContext instanceof FunctionDefinitionNode
                        || commonContext instanceof FunctionBodyNode
                        || commonContext instanceof FunctionNode
                        || commonContext instanceof ResourceDefinitionNode
                        || commonContext instanceof ConnectorDefinitionNode
                        || commonContext instanceof ActionDefinitionNode)) {
                    return false;
                }

                String defName = definitionElement.getText();

                return refName != null && defName != null && refName.equals(defName);
            }
            PsiElement id = ((PsiNameIdentifierOwner) definitionElement).getNameIdentifier();
            String defName = id != null ? id.getText() : null;

            return refName != null && defName != null && refName.equals(defName);
        }
        return false;
    }

    /**
     * Is the targeted def a subtree associated with this ref's kind of node?
     * E.g., for a variable def, this should return true for VardefSubtree.
     */
    public abstract boolean isDefinitionNode(PsiElement def);
}
