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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.ParenthesisInsertHandler;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class ConnectorReference extends BallerinaElementReference {

    public ConnectorReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return resolveLocalConnector(identifier);
        } else {
            return resolveConnectorInPackage(packageNameNode, identifier);
        }
    }

    @Nullable
    private PsiElement resolveLocalConnector(@NotNull IdentifierPSINode identifier) {
        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiDirectory psiDirectory = containingFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(psiDirectory);
        for (PsiElement connector : connectors) {
            if (connector.getText().equals(identifier.getText())) {
                return connector;
            }
        }
        return null;
    }

    @Nullable
    private PsiElement resolveConnectorInPackage(@NotNull PackageNameNode packageNameNode,
                                                 @NotNull IdentifierPSINode identifier) {
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return null;
        }

        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return null;
        }

        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(psiDirectory);
        for (PsiElement connector : connectors) {
            String connectorName = connector.getText();
            if (connectorName == null || connectorName.isEmpty()) {
                continue;
            }
            if (connectorName.equals(identifier.getText())) {
                return connector;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage != null) {
            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                    ParenthesisInsertHandler.INSTANCE));
        } else {
            // Todo - suggest all packages
        }
        return results.toArray(new LookupElement[results.size()]);
    }
}
